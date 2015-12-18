package robustnessEvaluators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.RobustnessRelated;
import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.inferredData;
import data.parameters;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class EventBasedR6 extends RobustnessEvaluatorBase {
	// R6.a.
	// This is our another heuristic robustness measure. Maximization.
	// For soft constraints,  optimality impact matrix is incorporated
	// Only move operation is considered
	// For each event, alternative positions are multiplied with optimality impact index of that position, 
	// and the best one is returned.
	// Maximizes the alternative positions with high desirability
	
	ArrayList<Integer> feasTimes; 
	ArrayList<Integer> feasRooms;
	
	HashSet<Integer> forbiddenTimes; // Current timeslots are forbidden for each event
	ArrayList<Integer> eventsToBeDisrupted;
	Individual currentIndividual;
	List<ConstraintBase> optimalityConstraints;
//	List<HardConstraint> originalFeasConstraints;
	public float[][] ROpt; // related to optimality
	
	float avgEventPenalty;
	
	public EventBasedR6(RobustnessManager manager) {
		super(manager);
		forbiddenTimes= new HashSet<Integer>();
//		this.originalFeasConstraints= new ArrayList<HardConstraint>();
//		for (ConstraintBase con: manager.constraints)
//			if (con instanceof HardConstraint){
//				this.originalFeasConstraints.add((HardConstraint) con);
//			}
		
		this.feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: manager.constraints)
			if (con instanceof HardConstraint){
				if (con instanceof RobustnessRelated )
					this.feasConstraints.add((HardConstraint) con);
			}
		this.optimalityConstraints= new ArrayList<ConstraintBase>();
		for (ConstraintBase con: manager.constraints)
			if (!(con instanceof HardConstraint))
				this.optimalityConstraints.add(con);
		fillEventsSet();
		createResourceOptimalityImpact();
	}

	private void fillEventsSet() {
		this.eventsToBeDisrupted= new ArrayList<Integer>();
		// whose given available timeslots total is at least as much as three times of its hours	
		// events of the same course are not considered!!!
//		int hours= 0; 		
		for (int ev= 0; ev< parameters.numEvents; ev++){
//			hours= dataHolder.eventHours[ev];
//			feasTimes= ((ArrayList<Integer>) inferredData.eventFeasStartTimes[ev]);
//			HashSet<Integer> tmp= new HashSet<Integer>();
//			for (int i: feasTimes){
//				for (int h=0; h< hours; h++)
//					tmp.add(i+h);
//			}		
//			if (tmp.size()>=3*hours)
				this.eventsToBeDisrupted.add(ev);
		} // end ev for
		System.out.println("Number of events to be disrupted: "+ this.eventsToBeDisrupted.size()+ " among " + parameters.numEvents +" events.");
	}

	public void evaluateIndividual(Individual indiv) {
		this.currentIndividual= indiv;
		this.avgEventPenalty= (float)this.currentIndividual.totalPenalty / parameters.numEvents;
		indiv.createMatrix();
		float total= 0; 
		for (int e=0; e< parameters.numEvents; e++){
			if (this.eventsToBeDisrupted.contains(e)){
				total+= evaluateEvent(indiv, e);
			}
		} // end e for
		indiv.robustnessValue= total;
	}

	@SuppressWarnings("unchecked")
	private float evaluateEvent(Individual indiv, int ev) {
		// Count the number of other timeslots that the current event can be assigned to:
		// Add a number of 1 for each timeslot where there is at least one available room:

		Event event= convertionManager.shortToEvent(ev, indiv.shortData[ev]);
		int timeOriginal= event.timeSlotStart;
		int roomOriginal= event.room;
		int hours= dataHolder.eventHours[ev];
		int origVal= indiv.shortData[ev];
		float newP= 0;
		int ev2= parameters.UNUSED_EVENT;
		int origVal2 = 0;
		
		forbiddenTimes.clear();
		for (int t= timeOriginal; t< timeOriginal+ hours; t++)
			forbiddenTimes.add(t);

		feasTimes= ((ArrayList<Integer>) inferredData.eventFeasStartTimes[ev]);
		feasRooms= (ArrayList<Integer>) inferredData.eventFeasRooms[ev];
		
		HashSet<Integer> currentlyConflictingEvents= new HashSet<Integer>();
		ArrayList<Float> tOptValues= new ArrayList<Float>();
		for (int t: feasTimes){
			tOptValues.clear();
			if (forbiddenTimes.contains(t))
				continue;
			for (int p=t; p< t+hours; p++){
				currentlyConflictingEvents= getCurrentlyConflictingEvents(ev, p);
				if (currentlyConflictingEvents.size()> 1)
					break;
			} // end for
			if (currentlyConflictingEvents.size()> 1)
				continue; // not appropriate for other hours of the event
			for (int r: feasRooms){
				ev2= indiv.dataMatrix[r][t]; // it may be empty= unused event
				if (ev2== parameters.UNUSED_EVENT){
					indiv.shortData[ev] = convertionManager.eventToShort(event.hours, t, r);
					if (moveAvailable(ev, event.hours, t, r)){  // new position is available!
//						newP= evaluateNew(ev, parameters.UNUSED_EVENT);
						newP= ROpt[r][t];
						tOptValues.add(newP);
					} // end if
					indiv.shortData[ev] = (short) origVal; // To original values
				} // end if
//				else if (ev2!= parameters.UNUSED_EVENT){
//					if (dataHolder.eventCourseID[ev2]== dataHolder.eventCourseID[ev])
//						continue; // events of the same course
//					indiv.shortData[ev] = convertionManager.eventToShort(event.hours, t, r);
//					origVal2= indiv.shortData[ev2];
//					indiv.shortData[ev2] = convertionManager.eventToShort(dataHolder.eventHours[ev2],event.timeSlotStart, event.room);
//					if (swapAvailable(ev, timeOriginal, roomOriginal, ev2, t, r)){
//						newP= evaluateNew(ev, ev2);
//						tOptValues.add(newP);			
//					} // end if	
//					currentIndividual.shortData[ev] = (short) origVal; // To original values
//					currentIndividual.shortData[ev2] = (short) origVal2;	// To original values
//				} // end else if
			} // end r for
		} // end t for

		if (tOptValues.size()== 0)
			return 0;
		float bestP= chooseBest(tOptValues);
		return bestP;
	}

	private float chooseBest(ArrayList<Float> tOptValues) {
		float max= tOptValues.get(0);
		
		for( int i=1; i< tOptValues.size(); i++){
			if (tOptValues.get(i) > max)
				max= tOptValues.get(i);
		} // end i for
		return max;
	}

	private int evaluateNew(int ev, int ev2) {
		int penalty= 0;
		if (ev != parameters.UNUSED_EVENT){
			for(ConstraintBase constr: this.optimalityConstraints )
				penalty+= constr.computeSingleEvent(currentIndividual.shortData, ev);
		}
		if (ev2 != parameters.UNUSED_EVENT){
			for(ConstraintBase constr: this.optimalityConstraints )
				penalty+= constr.computeSingleEvent(currentIndividual.shortData, ev2);
			penalty+= this.avgEventPenalty;
		}		
		return penalty;
	}

	private HashSet<Integer> getCurrentlyConflictingEvents(int ev, int t) {
		@SuppressWarnings("unchecked")
		HashSet<Integer> conflictingEvents= (HashSet<Integer>) inferredData.eventConflictingEvents[ev];
		// set of events conflicting with event ev, at timeslot t:
		HashSet<Integer> tempEvents= new HashSet<Integer>();
		int ev2;
		for (int r= 0; r< parameters.numRooms; r++){
			ev2= this.currentIndividual.dataMatrix[r][t];
			if (ev2!= parameters.UNUSED_EVENT){
				if (conflictingEvents.contains(ev2)){
					tempEvents.add(ev2);
				} // end if
			} // end if
		} // end r for
		return tempEvents;
	}

	private boolean swapAvailable(int ev, int time, int room, int ev2, int t2, int r2) {
		int hours1= dataHolder.eventHours[ev];
		int hours2= dataHolder.eventHours[ev2];
	
		if (t2+hours1 > parameters.numTimeSlots)
			return false;
		if (time+hours2 > parameters.numTimeSlots)
			return false;
		if ( dataHolder.timeslotDay[t2]!= dataHolder.timeslotDay[t2+hours1-1])
			return false; // All the hours should be scheduled on that day.
		if ( dataHolder.timeslotDay[time]!= dataHolder.timeslotDay[time+hours2-1])
			return false; // All the hours should be scheduled on that day.
		
		// For event 1:
		boolean allEmpty= true;
		for (int l=0; l< hours1; l++){
			if (this.currentIndividual.dataMatrix[r2][t2+l] != parameters.UNUSED_EVENT ||
					this.currentIndividual.dataMatrix[r2][t2+l]!= ev2){
				allEmpty= false;
				break;
			} // end if
		} // end l for
		if (!allEmpty)
			return false;
		HashSet<Integer> temp;
		for (int tev1=t2; tev1< t2+hours1; tev1++){
			temp= getCurrentlyConflictingEvents(ev, tev1);
			if (temp.size()> 0)
				return false;
		} // end time for
		// RobustnessRelated Hard Constraints:
		for(HardConstraint constr: this.feasConstraints ){
			if (!constr.checkSingleEventFeasibility(currentIndividual.shortData, ev))
				return false;
		}	
//		for(HardConstraint constr: this.originalFeasConstraints ){
//			if (!constr.checkSingleEventFeasibility(currentIndividual.shortData, ev)){
//				System.out.println("Error infeasibility during move!!! Constraint: "+ constr.getClass());
//				try {
//					throw new Exception();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		} // end for
		
		// For event 2:
		allEmpty= true;
		for (int l=0; l< hours2; l++){
			if (this.currentIndividual.dataMatrix[room][time+l] != parameters.UNUSED_EVENT ||
					this.currentIndividual.dataMatrix[room][time+l]!= ev){
				allEmpty= false;
				break;
			} // end if
		} // end l for
		if (!allEmpty)
			return false;
		
		for (int p=time; p< time+hours2; p++){
			temp= getCurrentlyConflictingEvents(ev2, p);
			if (temp.size()> 0)
				return false;
		} // end for
		// RobustnessRelated Hard Constraints:
		for(HardConstraint constr: this.feasConstraints ){
			if (!constr.checkSingleEventFeasibility(currentIndividual.shortData, ev2))
				return false;
		}	
		
//		for(HardConstraint constr: this.originalFeasConstraints ){
//			if (!constr.checkSingleEventFeasibility(currentIndividual.shortData, ev2)){
//				System.out.println("Error infeasibility during move!!! Constraint: "+ constr.getClass());
//				try {
//					throw new Exception();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		} // end for
		return true;
	}

	private boolean moveAvailable(int ev, int hours, int t, int r) {
		// current position and the consecutive timeslots should be empty to accomodate the event. 
		// emptiness is not enough. Consecutive timeslots should also not contain conflicting events
		if (t+hours > parameters.numTimeSlots)
			return false;
		if ( dataHolder.timeslotDay[t]!= dataHolder.timeslotDay[t+hours-1])
			return false; // All the hours should be scheduled on that day.

		boolean allEmpty= true;
		for (int l=0; l< hours; l++){
			if (this.currentIndividual.dataMatrix[r][t+l] != parameters.UNUSED_EVENT){
				allEmpty= false;
				break;
			} // end if
		} // end l for
		if (!allEmpty)
			return false;
		
		HashSet<Integer> temp;
		for (int time=t; time< t+hours; time++){
			temp= getCurrentlyConflictingEvents(ev, time);
			if (temp.size()> 0)
				return false;
		} // end time for
		
		// RobustnessRelated Hard Constraints:
		for(HardConstraint constr: this.feasConstraints ){
			if (!constr.checkSingleEventFeasibility(currentIndividual.shortData, ev))
				return false;
		}
		
//		for(HardConstraint constr: this.originalFeasConstraints ){
//			if (!constr.checkSingleEventFeasibility(currentIndividual.shortData, ev)){
//				System.out.println("Error infeasibility during move!!! Constraint: "+ constr.getClass());
//				try {
//					throw new Exception();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		} // end for

		return true;
	}

	public void evaluatePop(Population pop) {
		for (int ind=0; ind< PopulationParameters.populationSize; ind++)
			evaluateIndividual(pop.individuals[ind]);
	}

	private void createResourceOptimalityImpact() {
		// Count for each resource the events that prefer those resources. 
		// The following arrays keeps the number of events schedulable:

		int NEP; // events with time preferences
		int NER; // events with room preferences
		int[] Ntime; // number of events schedulable at given timeslot
		int[] Nroom; // number of events schedulable at given timeslot
		Ntime= new int[parameters.numTimeSlots];
		Nroom= new int[parameters.numRooms];

		for (int t= 0; t< parameters.numTimeSlots; t++)
			Ntime[t]= 0;
		for (int r= 0; r< parameters.numRooms; r++)
			Nroom[r]= 0;	
		
		NEP= parameters.numEvents; // All events have time preferences: none at lunch
		NER= 0; // # of events that have room preferences
		for (int event= 0; event< parameters.numEvents; event++){
			if (dataHolder.courseHasRoomPref[dataHolder.eventCourseID[event]])
				NER++;
			for (int r= 0; r< parameters.numRooms; r++)
				if (dataHolder.courseRoomPref[dataHolder.eventCourseID[event]][r])
					Nroom[r]++; // preferred
			for (int t= 0; t< parameters.numTimeSlots; t++){
				if ( t % parameters.numDailyTimeSlots!=4) 
					Ntime[t]++; // preferred
			} // end t for
		} // end event for
		int NbrENoPerPref= parameters.numEvents - NEP;
		int NbrENoRoomPref= parameters.numEvents - NER;
		
		// Now fill the Optimality Impact Matrix:
		float a= 0; float b= 0;
		ROpt= new float[parameters.numRooms][parameters.numTimeSlots];
		for (int t= 0; t< parameters.numTimeSlots; t++){
			a= (float)(Ntime[t]+NbrENoPerPref) / parameters.numEvents;
			for (int r= 0; r< parameters.numRooms; r++){
				b= (float)(Nroom[r]+NbrENoRoomPref) / parameters.numEvents;
				ROpt[r][t]= a + b;
			} // end r for
		} // end t for
	}
}

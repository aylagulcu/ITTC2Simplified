package robustnessEvaluators;

import java.util.ArrayList;
import java.util.HashSet;

import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.Unary;
import data.dataHolder;
import data.parameters;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class TimeRoomBasedR4 extends RobustnessEvaluatorBase {

	// Heuristic measure for Disrupt event SC
	
	public ArrayList<Unary> unaryConstraints;
	public Object[] eventFeasTimes;
	public Object[] eventFeasRooms;
	
	public float[][] RFeas; // related to feasibility
	public float[][] ROpt; // related to optimality
	public float[][] Rfeasopt; // robustness related to both feasibility and optimality
	public float Rfeasopttotal; // sum of each element in R
	
	public TimeRoomBasedR4(RobustnessManager manager) {
		super(manager);
		this.feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: manager.constraints)
			if (con instanceof HardConstraint){
				this.feasConstraints.add((HardConstraint) con);
			}

		initializeFeasResources();
		createResourceMatrix();
	}


	private void createResourceMatrix() {
		createResourceFeasibilityImpact();
		createResourceOptimalityImpact();	
		// now keep the feasibility and optimality values in one matrix:
		Rfeasopt= new float[parameters.numRooms][parameters.numTimeSlots];
		for (int t= 0; t< parameters.numTimeSlots; t++){
			for (int r= 0; r< parameters.numRooms; r++){
				Rfeasopt[r][t]= 0;
			} // end r for
		} // end t for
		Rfeasopttotal= 0;
		for (int time= 0; time< parameters.numTimeSlots; time++){
			for (int room= 0; room< parameters.numRooms; room++){
				Rfeasopt[room][time]= (float) (RFeas[room][time]+  0.25 * ROpt[room][time]);
				Rfeasopttotal+= Rfeasopt[room][time];
			} // end room for
		} // end t for
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

	@SuppressWarnings("unchecked")
	private void createResourceFeasibilityImpact() {
		// Count feasible events for each resource. Then fill resource feasibility impact matrix.
		// The following arrays keeps the number of events schedulable:
		int[][] Nroomtime; // number of events schedulable at given room-timeslot
		int[] Ntime; // number of events schedulable at given timeslot
		int[] Nroom; // number of events schedulable at given timeslot
		Nroomtime= new int[parameters.numRooms][parameters.numTimeSlots];
		Ntime= new int[parameters.numTimeSlots];
		Nroom= new int[parameters.numRooms];

		for (int t= 0; t< parameters.numTimeSlots; t++){
			Ntime[t]= 0;
			for (int r= 0; r< parameters.numRooms; r++){
				Nroomtime[r][t]= 0;
			} // end r for
		} // end t for
		for (int r= 0; r< parameters.numRooms; r++)
			Nroom[r]= 0;		
		
		HashSet<Integer> times;
		HashSet<Integer> rooms;
		for (int event= 0; event< parameters.numEvents; event++){
			times= (HashSet<Integer>) this.eventFeasTimes[event];
			rooms= (HashSet<Integer>) this.eventFeasRooms[event];
			for (int r: rooms)
				Nroom[r]+= 1;
			for (int t: times){
				Ntime[t]+= 1;
				for (int r: rooms)
					Nroomtime[r][t]+= 1;
			} // end t for
		} // end event for		
		
		// Now fill the Feasibility Impact Matrix:
		RFeas= new float[parameters.numRooms][parameters.numTimeSlots];
		for (int t= 0; t< parameters.numTimeSlots; t++){
			for (int r= 0; r< parameters.numRooms; r++){
				RFeas[r][t]= 0;
				RFeas[r][t]= (float)Nroomtime[r][t] / (float)parameters.numEvents;
			} // end r for
		} // end t for
		
	}

	private void initializeFeasResources() {
		eventFeasTimes= new Object[parameters.numEvents]; // set of feasible time slots for each event
		eventFeasRooms= new Object[parameters.numEvents]; // set of feasible rooms for each event

		// According to the unary constraints given above, 
		// finds the feasible time slots and rooms for each event
		unaryConstraints = new ArrayList<Unary>();	
		for (HardConstraint con: this.feasConstraints)
			if (con instanceof Unary)
				this.unaryConstraints.add((Unary) con);
		// Below feasible timeslots lists are filled:
		// Only starting time slots that an event can be scheduled at is included in this list.
		HashSet<Integer> tempStartTimes;
		boolean result;
		for (int event=0; event <parameters.numEvents; event++){ 
			tempStartTimes= new HashSet<Integer>(); 
			for (int time=0; time< parameters.numTimeSlots; time++){ 
				result= true;
				for (Unary uc: this.unaryConstraints){
					result= uc.checkEventTime(event, time); // according to the constraints in event initializer 
					if (!result)
						break;
				} // end uc for
				if (result)
					tempStartTimes.add(time); // True if all the hours can be placed in the consecutive timeslots.
			} // end time for
			this.eventFeasTimes[event]= tempStartTimes; 
		} // end event for 

		// Now, feasible room lists are filled:
		HashSet<Integer> rooms;
		for (int event=0; event <parameters.numEvents; event++){ 
			rooms= new HashSet<Integer>(); 
			for (int r=0; r< parameters.numRooms; r++){ 
				result= true;
				for (Unary uc: this.unaryConstraints){
					result= uc.checkEventRoom(event, r); // according to the constraints in event initializer 
					if (!result)
						break;
				} // end uc for
				if (result)
					rooms.add(r); // True if all the hours can be placed in the consecutive timeslots.
			} // end time for
			this.eventFeasRooms[event]= rooms; 
		} // end event for 
	}
	
	public void evaluatePop(Population pop) {
		for (int ind=0; ind< PopulationParameters.populationSize; ind++)
			evaluateIndividual(pop.individuals[ind]);
	}

	public void evaluateIndividual(Individual indiv) {
		
		float[][] Uroomtime=new float[parameters.numRooms][parameters.numTimeSlots]; // Utilization
		for (int t= 0; t< parameters.numTimeSlots; t++)
			for (int r= 0; r< parameters.numRooms; r++)
				Uroomtime[r][t]= 0;
		
		// Compute utilization acc.to current assignment:
		indiv.createMatrix();
		for (int time= 0; time< parameters.numTimeSlots; time++){
			for (int room= 0; room< parameters.numRooms; room++){
				if (indiv.dataMatrix[room][time]!= parameters.UNUSED_EVENT)
					Uroomtime[room][time]= 1;
			} // end room for
		} // end t for
		
		float usedFeasOptTotal= 0;
		for (int time= 0; time< parameters.numTimeSlots; time++)
			for (int room= 0; room< parameters.numRooms; room++)
				usedFeasOptTotal+= Uroomtime[room][time] * Rfeasopt[room][time];
		
		indiv.robustnessValue= usedFeasOptTotal;
	}

}

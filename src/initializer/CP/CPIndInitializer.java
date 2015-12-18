
package initializer.CP;

import ga.Individual;
import initializer.tournamentManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import orderingMethods.OrderingBase;
import orderingMethods.orderingManager;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.SoftConstraint;
import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class CPIndInitializer{
	private List<ConstraintBase> initializationconstraints;
	
	public Individual myIndiv;
	public Random myRandom;
	
	public Object[] eventCurrentlyAvailableTimes; // all available times for an event 
	public int[] eventAvailableTimesCount; // number of available/hours 
	
	private Object[] tabuPositionWithEvent; 
	private Object[] permanentTabuPosition; 
	
	public List<Integer> NAEvents; // HashSet is not used because the inner ordering of events is not known.
	private tournamentManager tManager;
	private int curEvent;
	private eventInitializer eventInit;
	OrderingBase ordering;
	
	private int[] eventEmptyTimes; 
	private int[] eventAssignAttempts; 
	
	
	public CPIndInitializer(List<ConstraintBase> initConstraints) {
		this.initializationconstraints= initConstraints;
		eventInit= new eventInitializer(this.initializationconstraints);
		tManager= new tournamentManager();
		
		NAEvents= new ArrayList<Integer>();
		for (int e=0; e< parameters.dataArraySize; e++)
			NAEvents.add(e);
		
		permanentTabuPosition= new Object[parameters.numEvents]; 
		for (int e=0; e< parameters.numEvents; e++) { 
			permanentTabuPosition[e]= new ArrayList<int[]>(); 
		} 
		 
		tabuPositionWithEvent= new Object[parameters.numEvents]; 
		for (int e=0; e< parameters.numEvents; e++) { 
			tabuPositionWithEvent[e]= new ArrayList<int[]>(); 
		} 
		 
		eventEmptyTimes= new int[parameters.numEvents]; 
		for (int e=0; e< parameters.numEvents; e++) 
			eventEmptyTimes[e]= 0; 
		 
		eventAssignAttempts= new int[parameters.numEvents]; 
		for (int e=0; e< parameters.numEvents; e++) 
			eventAssignAttempts[e]= 0;
	}


	private void fillEventTimesOriginal() { 
		// public Object[] eventCurrentlyAvailableTimes; // all available times for an event 
		// public float[] eventAvailableTimesCount; // number of available/hours
		eventCurrentlyAvailableTimes= new Object[parameters.numEvents]; 
		eventAvailableTimesCount= new int[parameters.numEvents];  
		for (int event=0; event <parameters.numEvents; event++){ 
			fillSingleEventTimes(event);
		} // end event for  
	}
	
	public void fillSingleEventTimes(int event){
		HashSet<Integer> tempTimes= new HashSet<Integer>();
		for (int time=0; time< parameters.numTimeSlots; time++){ 
			if (eventInit.checkFeasibility(event, time)){ // according to the constraints in event initializer 
				tempTimes.add(time); // feasible
			} // end if checkFeasibility 
		} // end time for 
		eventCurrentlyAvailableTimes[event]= tempTimes; 
		eventAvailableTimesCount[event]= tempTimes.size();
	}

	
	@SuppressWarnings("unchecked")
	private void updateFilterEventTimes(int time, int assignedEvent) { 
		// remove time from the list of other events if it is not consistent
		HashSet<Integer> tempTimes;
		for (int event=0; event <parameters.numEvents; event++){ 
			if (event== assignedEvent) continue;
			tempTimes= (HashSet<Integer>) eventCurrentlyAvailableTimes[event];
			if (tempTimes.contains(time)){
				if (!eventInit.checkFeasibility(event, time)) // according to the constraints in event initializer 
					((HashSet<Integer>) eventCurrentlyAvailableTimes[event]).remove(time);
			} // end if
		} // end e for
	}
	
	@SuppressWarnings("unchecked")
	private void updateUnFilterEventTimes(int time) { 
		// add time from the list of other events if it is consistent
		HashSet<Integer> tempTimes;
		for (int event=0; event <parameters.numEvents; event++){ 
			tempTimes= (HashSet<Integer>) eventCurrentlyAvailableTimes[event];
			if (!tempTimes.contains(time)){
				if (eventInit.checkFeasibility(event, time)) // according to the constraints in event initializer 
					((HashSet<Integer>) eventCurrentlyAvailableTimes[event]).add(time);
			} // end if
		} // end e for
	}

	
	@SuppressWarnings("unchecked")
	public void initializeIndividual(Individual ind, Random threadRandom, orderingManager oManager) throws IOException{			
		// clear eventAssignAttempts Counts:
		for (int e=0; e< parameters.numEvents; e++) 
			eventAssignAttempts[e]= 0; 
		
		ordering= oManager.selectOrdering(); 
		myIndiv= ind; 
		myRandom= threadRandom; 
	 
		List<Move> assignments= new ArrayList<Move>(); 
		List<Move> assignmentsSecondary= new ArrayList<Move>(); 
		AssignmentResult result; 
		boolean assigned= false; 
		 
		fillEventTimesOriginal();
		while (!NAEvents.isEmpty()) {
			curEvent= ordering.selectEvent(tManager.createTournament(NAEvents, myRandom), this); // a set of events sent, one is returned		 
			
			assert NAEvents.contains(curEvent); 
			assigned= false; 
			result= eventInit.assign(curEvent);	
			
			eventAssignAttempts[curEvent]++;

			if (result!= null) { // assignable 
				assignments.add(new Move(curEvent, result.time, result.room)); 
				NAEvents.remove((Integer)curEvent); 
				assigned= true; 
				updateFilterEventTimes(result.time, curEvent);
			} // end if 
			else { // not assignable 
//				System.out.println("Event "+ curEvent+ " could not be assigned"); 
				int index= assignments.size(); 
				assignmentsSecondary.clear(); 
				while (index>0) { 
					Move lastMove= assignments.get(--index); 
					if (lastMoveisTabu(curEvent, lastMove)) 
						continue; 					
					assignments.remove(index); 
					eventInit.unDoAssignment(lastMove); 
					NAEvents.add(lastMove.event); 
					// This undone may increase time availability of the current event
					updateUnFilterEventTimes(lastMove.time);
					result= eventInit.assign(curEvent);  
					if (result!=null) { 
//						System.out.println("\t Event "+ curEvent+ " assigned to time: "+ result.time +" and room: "+ result.room+" attempts: "+eventAssignAttempts[curEvent]); 
						int[] position= new int[3]; // time + room+ current event 
						position[0]= lastMove.time; position[1]= lastMove.room; position[2]= curEvent; 
						((List<int[]>)tabuPositionWithEvent[lastMove.event]).add(position); 
						if (assignments.size()==0){ // last was the fist assignment.
							// put to permanent tabu:
							position= new int[2]; 
							position[0]= lastMove.time; position[1]= lastMove.room; 
							((List<int[]>)permanentTabuPosition[lastMove.event]).add(position);
						}
						assignments.add(new Move(curEvent, result.time, result.room)); 
						NAEvents.remove((Integer)curEvent); 
						assigned= true; 
						updateFilterEventTimes(result.time, curEvent);
						
						if (assignmentsSecondary.size()> 0) { 
							for (int c=assignmentsSecondary.size()-1; c>=0 ;c--) {						 
								Move m= assignmentsSecondary.get(c); 
								if (eventInit.moveFeasible(m)) { 
									eventInit.reDoAssignment(m); 
									assignments.add(new Move(m.event, m.time, m.room)); 
									NAEvents.remove((Integer)m.event); 
									updateFilterEventTimes(m.time, m.event);
								} 
							} 
						} // end if 
						break; //while (index>0) { 
					} // end if 
					else if (result== null) {
						assignmentsSecondary.add(lastMove); 
					}
				} // while	 
				if (!assigned){ 
					//eventEmptyTimes[curEvent]++; // All Assignments cleared. Event cannot be assigned! Clear tabu list for that event! 
//					System.out.println("All moves undone. Current event: "+ curEvent+ " number of times of these undone:  "+ eventEmptyTimes[curEvent]); 
					for (int event=0; event< parameters.numEvents; event++)
						((List<int[]>)tabuPositionWithEvent[event]).clear(); 
				} // end if 
			} // end else // not assignable  
		} // end while (!NAEvents.isEmpty()) 

//		for (int e=0; e< parameters.numEvents; e++){ 
//			System.out.println("Event "+ e+ " number of assignment attempts: "+ eventAssignAttempts[e]); 
//		} // end e for 
		 
//		int penalty= 0; int partial= 0; 
//		for (ConstraintBase con: eventInit.initConstraints){ 
//			partial= con.Compute(myIndiv.Data); 
//			System.out.println(con.getClass().getSimpleName()+ " violation: " + partial); 
//			penalty+= partial; 
//		} 
//		System.out.println("Penalty of the individual: "+ penalty); 
//		System.out.println(); 
	}
	
	@SuppressWarnings("unchecked") 
	private boolean lastMoveisTabu(int curEvent2, Move lastAssignment) { //lastMoveisTabu(curEvent, lastMove) 
		List<int[]> temp= (List<int[]>)tabuPositionWithEvent[lastAssignment.event]; 
		for (int[] arr: temp){ 
			if (arr[0]== lastAssignment.time)// && arr[2]== curEvent2) 
				return true; 
		} 
		return false; // not tabu 
	} 
	
		
	private class eventInitializer{
		
		private List<ConstraintBase> initConstraints;
		private List<HardConstraint> feasConstraints; 
		private List<SoftConstraint> optConstraints; 
		List<int[]> posValList;

		eventInitializer(List<ConstraintBase> initializationconstraints) {
			this.initConstraints= initializationconstraints;
		
			this.feasConstraints= new ArrayList<HardConstraint>(); 
			for (ConstraintBase con: this.initConstraints) 
				if (con instanceof HardConstraint){ 
					feasConstraints.add((HardConstraint) con); 
				}
			
			this.optConstraints= new ArrayList<SoftConstraint>(); 
			for (ConstraintBase con: this.initConstraints) 
				if (con instanceof SoftConstraint){ 
					optConstraints.add((SoftConstraint) con); 
				}
		}
		
		public boolean checkFeasibility(int event, int time) { 
			int originalData= myIndiv.Data[event]; 
			Event eventObject= new Event(event, 1, parameters.UNUSED_TSS, parameters.UNUSED_ROOM); // event with 1 hour is created 
			
			boolean partialCheck= true; 
			for (int room=0; room< parameters.numRooms; room++) { 
				if (!isAvailable(event, time, room)) // not place for all hours of the event 
						continue; 
				eventObject.time= time;
				eventObject.room= room;
				myIndiv.Data[event]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 
						eventObject.hours, eventObject.time, eventObject.room);
				myIndiv.dataMatrix[room][time]= event;
				
				partialCheck= true; 
				for(HardConstraint constr: this.feasConstraints ){
					partialCheck&= constr.checkEventFeasibility(myIndiv, event); 
					if (!partialCheck) 
						break; 
				}
				if (partialCheck){ 
					myIndiv.Data[event]= originalData; 
					myIndiv.dataMatrix[room][time]= parameters.UNUSED_EVENT;
					return true; 
				}
				myIndiv.dataMatrix[room][time]= parameters.UNUSED_EVENT;
			} // end room for
			myIndiv.Data[event]= originalData; 
			return false;
		} // end method checkFeasibility 
		
		private boolean isAvailable(int event, int time, int room ){ 
			if (myIndiv.dataMatrix[room][time] != parameters.UNUSED_EVENT) // Room uniqueness constraint is ensured!!!
				return false;
			return true; 
		} 
		
		public void unDoAssignment(Move last) {
			myIndiv.dataMatrix[last.room][last.time]= parameters.UNUSED_EVENT;
			Event eventObject= new Event(last.event, 1, parameters.UNUSED_TSS, parameters.UNUSED_ROOM); 
			myIndiv.Data[last.event]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[last.event], 
					eventObject.hours, eventObject.time, eventObject.room);
		}

		public void reDoAssignment(Move last) {
			myIndiv.dataMatrix[last.room][last.time]= last.event;
			Event eventObject= new Event(last.event, 1, last.time, last.room); 
			myIndiv.Data[last.event]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[last.event], 
					eventObject.hours, eventObject.time, eventObject.room);
		}
		
		@SuppressWarnings("unchecked")
		public boolean moveFeasible(Move last) {
			if (myIndiv.dataMatrix[last.room][last.time] != parameters.UNUSED_EVENT)  // Room uniqueness constraint is ensured!!!
				return false;
			HashSet<Integer> availTimes= (HashSet<Integer>) eventCurrentlyAvailableTimes[last.event];
			if (!availTimes.contains(last.time))
				return false;
			return true;
		}
		
		// Assign it least-cost position: Select random in case of tie
		@SuppressWarnings("unchecked")
		public AssignmentResult assign(int event) {
			HashSet<Integer> availTimes= (HashSet<Integer>) eventCurrentlyAvailableTimes[event];
			
			int totalValue=0;
			int[] positionValues;
			posValList= new ArrayList<int[]>();
			Event eventObject= new Event(event, 1, parameters.UNUSED_TSS, parameters.UNUSED_ROOM); // event with 1 hour is created
			posValList.clear();	
			
			for (int room=0; room< parameters.numRooms; room++) {
				for (int time=0; time< parameters.numTimeSlots; time++) {
					if (!availTimes.contains(time))
						continue;
					if (myIndiv.dataMatrix[room][time] != parameters.UNUSED_EVENT) // Room uniqueness constraint is ensured!!!
						continue;
					if (isPermanentTabuPosition(event, time, room)) 
						continue;
					eventObject.time= time;
					eventObject.room= room;
					myIndiv.Data[event]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 
							eventObject.hours, eventObject.time, eventObject.room);
		
					myIndiv.dataMatrix[room][time]= event;
					
					totalValue=0;
					for(SoftConstraint constr: this.optConstraints ){
						totalValue = totalValue + constr.computeEventForInitializer(myIndiv, event, time, room);
					}
					
					myIndiv.dataMatrix[room][time]= parameters.UNUSED_EVENT;
					positionValues= new int[] {time, room, totalValue };
					posValList.add(positionValues);
				} // end room for
			} // end time for
			int[] min;
			if (posValList.size()== 0) {
				return null;
			}
			else{
				min = getBestRandomly(); // t, r, totalValue 
			} // end else
			
			eventObject.time= min[0];
			eventObject.room= min[1];
			myIndiv.Data[event]=convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 
					eventObject.hours, eventObject.time, eventObject.room);
			myIndiv.dataMatrix[eventObject.room][eventObject.time]= event;

			return new AssignmentResult(eventObject.time, eventObject.room);
		}
		
	
		private int[] getBestRandomly(){
			// Assign the event to the least cost position:
			int[] tempArray= new int[3]; // t, r, totalValue
			int[] minArray = new int[3]; // t, r, totalValue 
			int minVal;
			minArray= posValList.get(0);
			minVal= minArray[2];
			for( int i=1; i< posValList.size(); i++){
				tempArray= posValList.get(i);
				if (tempArray[2]< minVal){
					minVal= tempArray[2];
				}
			} // end i for
			// Now put list elements whose values are equal to minValue to another list:
			List<int[]> posValListMins= new ArrayList<int[]>();
			for( int i=0; i< posValList.size(); i++){
				tempArray= posValList.get(i);
				if (tempArray[2] == minVal){
					posValListMins.add(new int[] {tempArray[0], tempArray[1], minVal });
				}
			} // end i for
			
			if(posValListMins.size()== 1)
				return posValListMins.get(0);
			else{
				int rndIndex= myRandom.nextInt(posValListMins.size());
				return posValListMins.get(rndIndex);
			}
		} // end getBestRandomly
		
		private boolean isPermanentTabuPosition(int event, int time, int room) { 
			@SuppressWarnings("unchecked") 
			List<int[]> tabuPositions= (List<int[]>) permanentTabuPosition[event]; 
			for(int[] arr: tabuPositions) 
				if (arr[0]== time) // && arr[1]== room) 
					return true; 
			return false; // not tabu 
		} 

		
	} // end class
	
	private class Move { 
		private int event; 
		private int time; 
		private int room; 
		private Move(int e, int t, int r) { 
			this.event= e;  
			this.time= t; 
			this.room= r;
		} 
	} 
	
	private class AssignmentResult{
		private int time;
		private int room;
		private AssignmentResult(int t, int r) {
			this.time= t;
			this.room= r;
		}
	}

}
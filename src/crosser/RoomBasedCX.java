package crosser;

import java.util.ArrayList;

import constraints.ClashConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;

public class RoomBasedCX extends crosserBase {
	
	int e; int currentTime; int currentRoom;
	int t1; int t2;
	
	int minRoomP= 10000; int minRoom= 0;
	int roomP; 
	int eventParent; int eventChild;
	
	ArrayList<ConstraintBase> myConstraints= new ArrayList<ConstraintBase>();
	ArrayList<HardConstraint> myHardConstraints= new ArrayList<HardConstraint>();
	Individual child1= new Individual();
	Individual child2= new Individual();
	
	public RoomBasedCX(crossoverManager mngr) {
		super(mngr);
		myOffSprings= new Individual[2];
		
		this.myConstraints.add(new InstructorTimeAvailabilityConstraint(100));
		this.myConstraints.add(new RoomCapacityConstraint());
		this.myConstraints.add(new RoomStabilityConstraint());
		
		this.myHardConstraints.add(new ClashConstraint(100));
		this.myHardConstraints.add(new InstructorTimeAvailabilityConstraint(100));
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {
		// Assumes that all matrices all up to date!!! 
		this.myOffSprings= new Individual[2];
		child1= ind1.clone(); 
		child2= ind2.clone();
		
		// Step1 : Select the best rooms in each of the parents.
		// For each room, find a total penalty of all the events scheduled on that day for the constraints:
		// TimeAvailability, RoomCapacity, RoomStability ---> Constraints that may be improved by room change in the child
		// Why not others: Room is not changed; 
		
		// Step 2:
		// Create Chil A: Copy Parent A's best room to Parent B. 
		// Move only to empty and feasible positions. 
		// Remove duplicates
		// Create Child B the same way.
		
		// Find the best room in ind1
		minRoomP= 10000;
	
		for (int room=0; room< parameters.numRooms; room++){
			roomP= 0;
			for (int time= 0; time< parameters.numTimeSlots; time++){
				eventParent= ind1.dataMatrix[room][time];
				if (eventParent== parameters.UNUSED_EVENT) continue;
				for (ConstraintBase constr: myConstraints){
					roomP+= constr.computeEvent(ind1, eventParent, time, room);
				} // end constr for each
			} // end time for
			if (roomP< minRoomP){
				minRoomP= roomP;
				minRoom= room;	
			}
//			System.out.println("room"+ room+ " penalty: "+ roomP);
		} // end room for
//		System.out.println("Min Room: "+ minRoom+ " with penalty: "+ minRoomP);	
		
		// Now copy from ind1 to child 2:
		for (int time=0; time< parameters.numTimeSlots; time++){
			eventParent= ind1.dataMatrix[minRoom][time];
			if (eventParent== parameters.UNUSED_EVENT) continue;
			if (child2.dataMatrix[minRoom][time]== parameters.UNUSED_EVENT){
				currentTime= convertionManager.intToTime(child2.Data[eventParent]);
				currentRoom= convertionManager.intToRoom(child2.Data[eventParent]);
				assign(child2, eventParent, time, minRoom, currentTime, currentRoom); // Returns true if new position assignment is positive.
			} // end if
		} // end time for
		
		// Find the best room in ind2:
		minRoomP= 10000;
	
		for (int room=0; room< parameters.numRooms; room++){
			roomP= 0;
			for (int time= 0; time< parameters.numTimeSlots; time++){
				eventParent= ind2.dataMatrix[room][time];
				if (eventParent== parameters.UNUSED_EVENT) continue;
				for (ConstraintBase constr: myConstraints){
					roomP+= constr.computeEvent(ind2, eventParent, time, room);
				} // end constr for each
			} // end time for
			if (roomP< minRoomP){
				minRoomP= roomP;
				minRoom= room;	
			}
//			System.out.println("room"+ room+ " penalty: "+ roomP);
		} // end room for
//		System.out.println("Min Room: "+ minRoom+ " with penalty: "+ minRoomP);	
		
		// Now copy from ind2 to child 1:
		for (int time=0; time< parameters.numTimeSlots; time++){
			eventParent= ind2.dataMatrix[minRoom][time];
			if (eventParent== parameters.UNUSED_EVENT) continue;
			if (child1.dataMatrix[minRoom][time]== parameters.UNUSED_EVENT){
				currentTime= convertionManager.intToTime(child1.Data[eventParent]);
				currentRoom= convertionManager.intToRoom(child1.Data[eventParent]);
				assign(child1, eventParent, time, minRoom, currentTime, currentRoom); // Returns true if new position assignment is positive.
			} // end if
		} // end time for
		
		this.myCXManager.mySimpleEvaluator.evaluateIndividual(child1);
		this.myCXManager.mySimpleEvaluator.evaluateIndividual(child2);

		myOffSprings[0]= child1;
		myOffSprings[1]= child2;
		return myOffSprings;
	}

	private boolean assign(Individual child, int event, int newTime, int newRoom, int currTime, int currRoom) {
		// assigns if new position is feasible
		// it not, takes back all the changes.
		
		// update data matrix:
		child.dataMatrix[newRoom][newTime]= event;
		child.dataMatrix[currRoom][currTime]= parameters.UNUSED_EVENT;

		// update curriculum compactness matrix:
		curList= dataHolder.eventCurriculums.get(event);
		for (int cur: curList){
			assert child.timeCurriculum[currTime][cur]> 0;
			child.timeCurriculum[currTime][cur]--; // old position
			child.timeCurriculum[newTime][cur]++; // new position
		} // end for each
		
		int evOrigVal= child.Data[event];
		child.Data[event] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 1, newTime, newRoom);
		if (checkFeasForMyConstr(child, event, newTime, newRoom)){
			return true; // return the current updated matrices
		} // end if
		
//		System.out.println("event cannot be assigned due to feasibility: "+ event);
		// if not returned true:
		child.Data[event] = evOrigVal; // To original values
		// matrix to original values:
		child.dataMatrix[currRoom][currTime]= event;
		child.dataMatrix[newRoom][newTime]= parameters.UNUSED_EVENT;

		// curriculum compactness matrix to original:
		curList= dataHolder.eventCurriculums.get(event);
		for (int cur: curList){
			child.timeCurriculum[currTime][cur]++; // original position
			child.timeCurriculum[newTime][cur]--; // new position
		} // end for each

		return false;
	}

	private boolean checkFeasForMyConstr(Individual child, int event, int newTime, int newRoom) {
		assert event!= parameters.UNUSED_EVENT;
		for (HardConstraint hc: this.myHardConstraints){
			if (!hc.checkEventFeasibilityInSA(child, event, newTime, newRoom))
				return false;
		}
		return true;
	}

}

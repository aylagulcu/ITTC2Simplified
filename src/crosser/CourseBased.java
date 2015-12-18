package crosser;

import java.util.ArrayList;
import java.util.HashSet;

import constraints.ClashConstraint;
import constraints.ClashSoftConstraint;
import constraints.CompletenessConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import constraints.RoomUniquenessConstraint;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;

public class CourseBased extends crosserBase {
	
	int e; int currentTime; int currentRoom;
	int t1; int t2;
	
	int eventParent; int eventChild;

	ArrayList<ConstraintBase> myConstraints= new ArrayList<ConstraintBase>();
	ArrayList<ConstraintBase> mySoftConstraints= new ArrayList<ConstraintBase>();
	ArrayList<HardConstraint> myHardConstraints= new ArrayList<HardConstraint>();
	
	Individual child1= new Individual();
	Individual child2= new Individual();
	
	int[] courseP1= new int[parameters.numCourses];
	int[] courseP2= new int[parameters.numCourses];
	
	int[] courseOrder1= new int[parameters.numCourses];
	int[] courseOrder2= new int[parameters.numCourses];
	
	HashSet<Integer> eventsToMove= new HashSet<Integer>();
	
	public CourseBased(crossoverManager mngr) {
		super(mngr);
		myOffSprings= new Individual[2];
		
		// Only the constraints that are related to absolute positioning are considered:
		this.myConstraints.add(new InstructorTimeAvailabilityConstraint(100));
		this.myConstraints.add(new CurriculumCompactnessConstraint());
		this.myConstraints.add(new MinimumWorkingDaysConstraint());
		this.myConstraints.add(new RoomCapacityConstraint());	
		this.myConstraints.add(new RoomStabilityConstraint());
		
		this.myHardConstraints.add(new InstructorTimeAvailabilityConstraint(100));
				
		this.mySoftConstraints.add(new CurriculumCompactnessConstraint()); // ok: course computation
		this.mySoftConstraints.add(new MinimumWorkingDaysConstraint()); // ok
		this.mySoftConstraints.add(new RoomCapacityConstraint()); // ok
		this.mySoftConstraints.add(new RoomStabilityConstraint()); // ok
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {
		eventsToMove.clear();
		// Compute course penalties for each individual:
		for (ConstraintBase con: this.myConstraints){
			con.ComputeCoursePenalties(ind1);
			con.ComputeCoursePenalties(ind2);
		} // end con for each
		for (int i=0; i< parameters.numCourses; i++){
			this.courseP1[i] = ind1.curCompP[i] + ind1.minWorkDaysP[i] + ind1.roomCapP[i] + ind1.roomStabP[i];
			this.courseP2[i] = ind2.curCompP[i] + ind2.minWorkDaysP[i] + ind2.roomCapP[i] + ind2.roomStabP[i];
			
			this.courseOrder1[i]= i;
			this.courseOrder2[i]= i;
		}
		
		// now order the courses acc to decreasing order of the penalties:
		int temp;
		for (int i=0; i< parameters.numCourses; i++)
			for (int j=i+1; j< parameters.numCourses; j++){
				if (courseP1[courseOrder1[j]]< courseP1[courseOrder1[i]]){
					temp= courseOrder1[i];
					courseOrder1[i]= courseOrder1[j];
					courseOrder1[j]= temp;
				} // end if
				
				if (courseP2[courseOrder2[j]]< courseP2[courseOrder2[i]]){
					temp= courseOrder2[i];
					courseOrder2[i]= courseOrder2[j];
					courseOrder2[j]= temp;
				} // end if
			} // end j for

		// Assumes that all matrices all up to date!!! 
		this.myOffSprings= new Individual[2];
		// create child1:
		
		child1= ind1.clone();
		// copy the best course in ind2 to child1:
		int crsBest= courseOrder2[0];
		if (courseP2[crsBest] < courseP1[crsBest]){
			for (int event= dataHolder.courseStartIndex[crsBest]; event< dataHolder.courseStartIndex[crsBest]+dataHolder.numLectures[crsBest]; event++){
				// learn the event's time and room in the other parent. event in child will be assigned to these:
				int timeNew= convertionManager.intToTime(ind2.Data[event]);
				int roomNew= convertionManager.intToRoom(ind2.Data[event]);
				
				int ev2= child1.dataMatrix[roomNew][timeNew];
				if (ev2!= parameters.UNUSED_EVENT) eventsToMove.add(ev2);

				int timeCurrent= convertionManager.intToTime(child1.Data[event]);
				int roomCurrent= convertionManager.intToRoom(child1.Data[event]);
				
				AssignNewPosition(child1, event, timeNew, roomNew, ev2, timeCurrent, roomCurrent); // move related data is recorded if it updates the current best move			
			}
		} // end if
		myCXManager.mySimpleEvaluator.evaluateIndividual(child1);
		myCXManager.applyMicroSA(child1, eventsToMove);
		
		child2= ind2.clone();
		eventsToMove.clear();
		// copy the best course in ind1 to child2:
		crsBest= courseOrder1[0];
		if (courseP1[crsBest] < courseP2[crsBest]){
			for (int event= dataHolder.courseStartIndex[crsBest]; event< dataHolder.courseStartIndex[crsBest]+dataHolder.numLectures[crsBest]; event++){
				// learn the event's time and room in the other parent. event in child will be assigned to these:
				int timeNew= convertionManager.intToTime(ind1.Data[event]);
				int roomNew= convertionManager.intToRoom(ind1.Data[event]);
				
				int ev2= child2.dataMatrix[roomNew][timeNew];
				if (ev2!= parameters.UNUSED_EVENT) eventsToMove.add(ev2);

				int timeCurrent= convertionManager.intToTime(child2.Data[event]);
				int roomCurrent= convertionManager.intToRoom(child2.Data[event]);
				
				AssignNewPosition(child2, event, timeNew, roomNew, ev2, timeCurrent, roomCurrent); // move related data is recorded if it updates the current best move			
			}
		} // end if
		;
		myCXManager.mySimpleEvaluator.evaluateIndividual(child2);
		myCXManager.applyMicroSA(child2, eventsToMove);
		
		myOffSprings[0]= child1;
		myOffSprings[1]= child2;
		return myOffSprings;
	}
								
	// no check is performed...
	private void AssignNewPosition(Individual child, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		// ev1 should be evaluated for: time2, room2
		// ev2 should be evaluated for: time1, room1
		if (ev1== ev2)
			return;
		
		// update matrix:
		child.dataMatrix[room2][time2]= ev1;
		child.dataMatrix[room1][time1]= ev2;
		// update curriculum compactness matrix:
		if (ev1!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				if (child.timeCurriculum[time1][cur]> 0)
					child.timeCurriculum[time1][cur]--; // old position
				child.timeCurriculum[time2][cur]++; // new position
			} // end for each
		}
		if (ev2!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				if (child.timeCurriculum[time2][cur]> 0)
					child.timeCurriculum[time2][cur]--; // old position
				child.timeCurriculum[time1][cur]++; // new position
			} // end for each
		}
		
		// Now try the current move:
		if (ev1== parameters.UNUSED_EVENT){
			child.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
		} // end if
		
		if (ev2== parameters.UNUSED_EVENT){
			child.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
		} // end if
		
		if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
			child.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			child.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
		} // end else if

	}




}

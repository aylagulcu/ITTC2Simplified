package initializer;

import ga.Individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import constraints.ClashConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import data.Course;

public class CourseInitializer {
	// Has its own set of constraints:
	private List<ConstraintBase> initConstraints;
	
	Individual indiv;
	Course myCourse;
	Random tRandom;
	
	public CourseInitializer() {
		this.initConstraints= new ArrayList<ConstraintBase>();
		this.initConstraints.add(new ClashConstraint(100));
		this.initConstraints.add(new InstructorTimeAvailabilityConstraint(100));
//		this.initConstraints.add(new RoomUniquenessConstraint(50)); // This will already be ensured!!!
		this.initConstraints.add(new RoomCapacityConstraint());
//		this.initConstraints.add(new CompletenessConstraint(100)); // This is ensured by removing the assigned time slot
//
		this.initConstraints.add(new RoomStabilityConstraint()); // 
		this.initConstraints.add(new CurriculumCompactnessConstraint());
		this.initConstraints.add(new MinimumWorkingDaysConstraint());
		
	}

	public boolean initializeCourse(Individual individual, int courseId, Random tRand) {
		this.tRandom= tRand; 
		this.indiv= individual;
		createCourse(courseId); 
		assignTimeRoomToCourse();
		return true;
	}
	
	public void createCourse(int courseId) {
//		myCourse= new Course(courseId);
//		for (int e=0; e< dataHolder.numLectures[courseId]; e++){
//			Event evt = new Event(1, parameters.UNUSED_TSS, parameters.UNUSED_ROOM); // event with 1 hour is created
//			myCourse.myEvents.add(evt);	
//		} // end e for
//		
//		convertionManager.putCourseToArray(myCourse, indiv.Data);
	}

	private void assignTimeRoomToCourse() {
//
//		List<Integer> allTimes= new ArrayList<Integer>();
//		for (int t=0; t< parameters.numTimeSlots;t++)
//			allTimes.add(t);
//		
//		int timeLenght= parameters.numTimeSlots;
//		Set<Integer> timesToTry = new HashSet<Integer>();
//		int rndTimeIndex;
//		int rndTime;
//		do {
//			rndTimeIndex= RandomNumberGenerator.getRandomInt(allTimes.size());
//			rndTime= allTimes.get(rndTimeIndex);
//			timesToTry.add(rndTime);
//			allTimes.remove((Integer)rndTime);
//		}while (timesToTry.size() < timeLenght);
//		
//
//		List<Integer> allRooms= new ArrayList<Integer>();
//		for (int r=0; r< parameters.numRooms;r++)
//			allRooms.add(r);
//		
//		int roomLenght= parameters.numRooms;
//		Set<Integer> roomsToTry = new HashSet<Integer>();
//		int rndRoomIndex;
//		int rndRoom;
//		do {
//			rndRoomIndex= RandomNumberGenerator.getRandomInt(allRooms.size());
//			rndRoom= allRooms.get(rndRoomIndex);
//			roomsToTry.add(rndRoom);
//			allRooms.remove((Integer)rndRoom);
//		}while (roomsToTry.size() < roomLenght);
//		
//		int totalValue=0;
//		int[] positionValues;
//		List<int[]> posValList= new ArrayList<int[]>();
//		
//		int eventCounter=0;
//		int eventIndex;
//		
//		while (eventCounter < myCourse.myEvents.size()) {
//			eventIndex= dataHolder.courseStartIndex[myCourse.ID]+ eventCounter;
//			Event e= this.myCourse.myEvents.get(eventCounter);	
//			posValList.clear();	
//			
//			for (int room: roomsToTry) {
//				for (int time: timesToTry) {
//					if (indiv.dataMatrix[room][time] != parameters.UNUSED_EVENT) // Room uniqueness constraint is ensured!!!
//						continue;
//					e.time= time;
//					e.room= room;
//					indiv.Data[eventIndex]= convertionManager.eventValuesToInt(myCourse.ID, e.hours, e.time, e.room);
//					totalValue=0;
//					for(ConstraintBase constr: this.initConstraints ){
//						totalValue = totalValue + constr.computeSingleEvent(indiv.Data, eventIndex);
//					}
//					positionValues= new int[] {time, room, totalValue };
//					posValList.add(positionValues);
//				} // end room for
//			} // end time for
//			if (posValList.size()== 0) {
//				System.out.println("Course Initialization PosVal List size is zero!!!");
//			}
//			
//			// Assign the event to the least cost position:
//			int[] temp= new int[3]; // t, r, totalValue
//			int[] min = new int[3]; // t, r, totalValue 
//			min= posValList.get(0);
//			for( int i=0; i< posValList.size(); i++){
//				temp= posValList.get(i);
//				if (temp[2]<= min[2]){
//					min[0]= temp[0];
//					min[1]= temp[1];
//					min[2]= temp[2];
//				}
//			} // end i for
//			e.time= min[0];
//			e.room= min[1];
//			indiv.Data[eventIndex]=convertionManager.eventValuesToInt(myCourse.ID, e.hours, e.time, e.room); // Assignment!
//			indiv.dataMatrix[e.room][e.time]= eventIndex;
//			
//			timesToTry.remove((Integer)e.time);
//		
//			eventCounter++;
//		} // end while
	}
}
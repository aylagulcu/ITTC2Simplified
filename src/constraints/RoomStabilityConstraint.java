package constraints;
import ga.Individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import data.Course;
import data.Event;
import data.TemporaryData;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class RoomStabilityConstraint extends ConstraintBase implements SoftConstraint{
	// Events of the same course should be assigned to the same room. 
	// More than 1 different room is returned as penalty
	HashSet<Integer> roomsUsed= new HashSet<Integer>();
	
	public RoomStabilityConstraint() {
		super();	
	}
	
	public RoomStabilityConstraint(int weight) {
		super(weight);	
	}

	@Override
	public int Compute(Individual indiv) {
		int violationCount=0; 
		for (int courseId1 = 0; courseId1 < parameters.numCourses; courseId1++) {
			violationCount+= computeSingleCourse(indiv, courseId1);
		} // end courseID for
		indiv.RoomStabP= violationCount;
		return violationCount;
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1) {
		int room;
		roomsUsed.clear();
		int startEv= dataHolder.courseStartIndex[courseId1];
		for (int ev= startEv; ev< startEv+ dataHolder.numLectures[courseId1]; ev++){
			room= convertionManager.intToRoom(indiv.Data[ev]);
			roomsUsed.add(room);
		}
//		assert roomsUsed.size()>0;
		return (roomsUsed.size()-1)* this.weight;
	}
	
	@Override
	public int computeEvent(Individual indiv, int eventId, int time, int room) {
		// Assign a penalty if the event is assigned to a different room other than the curren ones:
		int penalty= 0;
		int rm;
		Course crs= convertionManager.getCourseFromArray(dataHolder.eventCourseId[eventId], indiv.Data);
		for (int ev=0; ev< crs.myEvents.size(); ev++){
			rm= convertionManager.intToRoom(indiv.Data[dataHolder.courseStartIndex[crs.ID]+ev]);	
			if (rm!= room)
				penalty+= 1;
		} // end ev for
		return penalty * this.weight;
	}

	

	
	public RoomStabilityConstraint Clone() {
		RoomStabilityConstraint con = new RoomStabilityConstraint(this.weight);
		return con;
	}
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		List<String> results= new ArrayList<String>();
		int violationCount=0; 
		boolean[] roomUsed= new boolean[parameters.numRooms];
		int totalRoom=0;
		Event evt1;

		for (int c=0; c< parameters.numCourses; c++){
			totalRoom=0;
			for (short r=0; r< parameters.numRooms; r++)
				roomUsed[r]= false;
			for (int e1=dataHolder.courseStartIndex[c]; e1< (dataHolder.courseStartIndex[c]+dataHolder.numLectures[c]); e1++){	
				evt1= convertionManager.intToEvent(e1, dataArray[e1]);	
				roomUsed[evt1.room]=true; 
			} // end e1 for
			for (int r=0; r< roomUsed.length; r++){
				if (roomUsed[r])
					totalRoom++;
			}
			if (totalRoom > 1){
				results.add("Room Stability Constraint violation: Course "+ TemporaryData.courseCode[c]+ " and number of rooms is: "+ totalRoom);
				violationCount=violationCount+totalRoom-1;
			}
		} // end c for
		results.add("Penalty for room stability constraint "+ violationCount);
		return results;
	}

	
	@Override
	public int computeEventForInitializer(Individual indiv, int eventId, int time, int room) {
		// Assign a penalty if the event is assigned to a different room other than the curren ones:
		int penalty= 0;
		int rm;
		Course crs= convertionManager.getCourseFromArray(dataHolder.eventCourseId[eventId], indiv.Data);
		for (int ev=0; ev< crs.myEvents.size(); ev++){
			rm= convertionManager.intToRoom(indiv.Data[dataHolder.courseStartIndex[crs.ID]+ev]);	
			if (rm== parameters.UNUSED_ROOM)
				continue;
			if (rm!= room)
				penalty+= 1;
		} // end ev for
		return penalty * this.weight;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		for (int c=0; c< parameters.numCourses; c++){
			indiv.roomStabP[c]= computeSingleCourse(indiv, c);
		} // end c for
		
	}





}

package constraints;
import ga.Individual;

import java.util.ArrayList;
import java.util.List;

import data.Course;
import data.Event;
import data.TemporaryData;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class RoomCapacityConstraint extends ConstraintBase implements SoftConstraint{

	// The capacity of the room should be greater than or equal to the The number of students that take a course
	// 1 violation is counted for each student without a seat
	public RoomCapacityConstraint() {
		super();	
	}
	
	public RoomCapacityConstraint(int weight) {
		super(weight);	
	}

	@Override
	public int Compute(Individual indiv) {
		int violationCount=0;

		for (int crs=0; crs< parameters.numCourses; crs++){
			violationCount+= computeSingleCourse(indiv, crs); // Single course contains weight!
		} // end crs for
		indiv.RoomCapP= (violationCount* this.weight);
		return violationCount * this.weight;
	}

	public int computeSingleCourse(Individual indiv, int courseId1) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);
		
		for (Event evt1: crs.myEvents) {
			if (evt1.room== parameters.UNUSED_ROOM)
				continue;
			int difference = dataHolder.numStudents[crs.ID] - dataHolder.roomCapacity[evt1.room];
			if( difference > 0) 
				penalty+= (difference * evt1.hours);
		} // end evt1 for each
		return penalty * this.weight;
	}
	
	@Override
	public int computeEvent(Individual indiv, int eventId, int time, int room) {
		int diff= dataHolder.numStudents[dataHolder.eventCourseId[eventId]] - dataHolder.roomCapacity[room];	
		if( diff > 0) 
			return (diff * this.weight);
		else 
			return 0; 
	}

	
	
	
	public int[] computeSingleCourseEvents(int[] dataArray, Course course){
		int[] eventsPenalties= new int[course.myEvents.size()];
		for (int d=0; d< course.myEvents.size(); d++)
			eventsPenalties[d]= 0;
		
		Event evt1;
		int difference= 0;
		for (int ev=0; ev< course.myEvents.size(); ev++){
			evt1= convertionManager.intToEvent(dataHolder.courseStartIndex[course.ID]+ev, dataArray[dataHolder.courseStartIndex[course.ID]+ev]);
			difference = dataHolder.numStudents[course.ID] - dataHolder.roomCapacity[evt1.room];
			if( difference > 0) 
				eventsPenalties[ev]+= (difference * evt1.hours) * this.weight;
		} // end ev for

		return eventsPenalties;
	} // end computeEvents
	
	public int computeSingleEvent(int[] dataArray, int eventIndex){
		Event evt= convertionManager.intToEvent(eventIndex, dataArray[eventIndex]);
	
		int eventP= 0;
		int courseId1= convertionManager.intToCourseId(dataArray[eventIndex]);
		
		int difference = dataHolder.numStudents[courseId1] - dataHolder.roomCapacity[evt.room];
		if( difference > 0) 
			eventP+= difference;

		return eventP* this.weight;
	} // end computeSingleEvent
	
	public RoomCapacityConstraint Clone() {
		RoomCapacityConstraint con = new RoomCapacityConstraint(this.weight);
		return con;
	}
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		int violationCount=0; 
		List<String> results= new ArrayList<String>();
		Event evt1; int difference;
		for (int c=0; c< parameters.numCourses; c++){
			for (int e1=dataHolder.courseStartIndex[c]; e1< (dataHolder.courseStartIndex[c]+dataHolder.numLectures[c]); e1++){
				evt1= convertionManager.intToEvent(e1, dataArray[e1]);
				difference= dataHolder.numStudents[c] - dataHolder.roomCapacity[evt1.room];
				if( difference > 0) {
					for (int h=0; h< evt1.hours; h++)
						results.add("Room Capacity Constraint violation: Course "+ TemporaryData.courseCode[c]+ " with " +dataHolder.numStudents[c]+ " students is assigned to room"+ TemporaryData.roomCode[evt1.room]+ " with size of "+ dataHolder.roomCapacity[evt1.room]);
					violationCount= violationCount + (difference * evt1.hours);
				}
			} // end e1 for
		} // end c for
		results.add("Penalty for room capacity constraint "+ violationCount);
		return results;
	}

	
	
	@Override
	public int computeEventForInitializer(Individual indiv, int eventId, int time, int room) {
		int penalty= 0;
		if (room== parameters.UNUSED_ROOM)
			return 0;
		int diff= dataHolder.numStudents[dataHolder.eventCourseId[eventId]] - dataHolder.roomCapacity[room];	
		if( diff > 0) 
			penalty= diff;
		
		return penalty * this.weight;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		for (int c=0; c< parameters.numCourses; c++){
			indiv.roomCapP[c]= computeSingleCourse(indiv, c);
		} // end c for
		
	}




}

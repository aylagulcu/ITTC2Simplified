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

public class InstructorTimeAvailabilityConstraint extends ConstraintBase implements HardConstraint, Unary{

	// 1 violation is counted for the hour of each event.
	int weightInitialization;
	
	public InstructorTimeAvailabilityConstraint() {
		super();	
	}

	public InstructorTimeAvailabilityConstraint(int weight) {
		super(weight);	
	}

	@Override
	public boolean checkEventFeasibility(Individual indiv, int eventIndex) {
		int time= convertionManager.intToTime(indiv.Data[eventIndex]);
		if( dataHolder.courseTimeSlotUnavailability[dataHolder.eventCourseId[eventIndex]][time])
			return false;

		return true;
	}
	
	@Override
	public boolean checkEventFeasibilityInSA(Individual indiv, int eventIndex, int time, int room) {
		if( dataHolder.courseTimeSlotUnavailability[dataHolder.eventCourseId[eventIndex]][time])
			return false;
		return true;
	}
	
	@Override
	public int Compute(Individual indiv) {
		int violationCount=0;
		int time;
		
		for(int i=0; i< indiv.Data.length; i++){
			time= convertionManager.intToTime(indiv.Data[i]);
			if( dataHolder.courseTimeSlotUnavailability[dataHolder.eventCourseId[i]][time])
				violationCount+= 1;
		} // end i for
		indiv.TimeAvailP= (violationCount* this.weight);
		return (violationCount* this.weight);
	}

	public int computeSingleCourse(Individual indiv, int courseId1) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);
		for (Event evt1: crs.myEvents) {
			if (evt1.time== parameters.UNUSED_TSS)
				continue;
			if( dataHolder.courseTimeSlotUnavailability[crs.ID][evt1.time])
				penalty++;
		} // end evt1 for each

		return penalty * this.weight;
	}
	
	
	@Override
	public int computeEvent(Individual currentInd, int i, int time1, int room1) {
		if( dataHolder.courseTimeSlotUnavailability[dataHolder.eventCourseId[i]][time1])
			return this.weight;
		return 0;
	}


	
	
	
	
	
	
	
	
	
	public int[] computeSingleCourseEvents(int[] dataArray, Course course){
		int[] eventsPenalties= new int[course.myEvents.size()];
		for (int d=0; d< course.myEvents.size(); d++)
			eventsPenalties[d]= 0;

		Event evt1;
		for (int e1=0; e1<course.myEvents.size(); e1++) {
			evt1= course.myEvents.get(e1);
			if (evt1.time== parameters.UNUSED_TSS)
				continue;
			if( dataHolder.courseTimeSlotUnavailability[course.ID][evt1.time])
				eventsPenalties[e1]+= this.weight;
		} // end e1 for
		return eventsPenalties;
	} // end computeEvents
	
	public int computeSingleEvent(int[] dataArray, int eventIndex){
		int time= convertionManager.intToTime(dataArray[eventIndex]);
		int courseId1= convertionManager.intToCourseId(dataArray[eventIndex]);
		
		if( dataHolder.courseTimeSlotUnavailability[courseId1][time])
			return this.weight;

		return 0;
	} // end computeSingleEvent
	

	public InstructorTimeAvailabilityConstraint Clone() {
		InstructorTimeAvailabilityConstraint con = new InstructorTimeAvailabilityConstraint(this.weight);
		return con;
	}
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		List<String> results= new ArrayList<String>();
		Event evt1;
		for (int c=0; c< parameters.numCourses; c++){
			for (int e1=dataHolder.courseStartIndex[c]; e1< (dataHolder.courseStartIndex[c]+dataHolder.numLectures[c]); e1++){
				evt1= convertionManager.intToEvent(e1, dataArray[e1]);
				if (evt1.time== parameters.UNUSED_TSS) 
					continue;
				if( dataHolder.courseTimeSlotUnavailability[c][evt1.time])
					results.add("InstructorTimeAvailabilityConstraint violation of course  "+ TemporaryData.courseCode[c]+ " at time: "+ evt1.time);
			} // end e1 for
		} // end c for
		return results;
	}

	
	@Override
	public boolean checkEventTime(int event, int time) {
		int courseId1= dataHolder.eventCourseId[event];
		
		if( dataHolder.courseTimeSlotUnavailability[courseId1][time])
			return false;
		return true;
	}

	@Override
	public boolean checkEventRoom(int event, int room) {
		return true;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		// TODO Auto-generated method stub
		
	}







	


}

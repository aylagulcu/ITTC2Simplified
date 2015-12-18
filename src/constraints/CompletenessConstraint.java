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

public class CompletenessConstraint extends ConstraintBase implements HardConstraint {
	// Each event belonging to the same course should be assigned to a different room time slot
	// Do not check this constraint during initialization!
	
	public CompletenessConstraint() {
		super();	
	}
	
	public CompletenessConstraint(int weight) {
		super(weight);	
	}

	@Override
	public boolean checkEventFeasibility(Individual indiv, int eventIndex) {
		if (convertionManager.intToTime(indiv.Data[eventIndex]) == parameters.UNUSED_TSS) // Completeness ensures this!
			return false;
		if (convertionManager.intToRoom(indiv.Data[eventIndex]) == parameters.UNUSED_ROOM) // Completeness ensures this!
			return false;
		return true;
	}
	
	@Override
	public boolean checkEventFeasibilityInSA(Individual indiv, int eventIndex, int time, int room) {
		if (time == parameters.UNUSED_TSS) // Completeness ensures this!
			return false;
		if (room == parameters.UNUSED_ROOM) // Completeness ensures this!
			return false;
		return true;
	}

	public int Compute(Individual indiv) {
		int violationCount=0;

		for(int i=0; i< indiv.Data.length; i++){
			if (convertionManager.intToTime(indiv.Data[i]) == parameters.UNUSED_TSS)
				violationCount+= 1;
			if (convertionManager.intToRoom(indiv.Data[i]) == parameters.UNUSED_ROOM)
				violationCount+= 1;
		} // end i for
		
		indiv.ComletenessP= violationCount* this.weight;
		return (violationCount* this.weight);
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);

		for (Event event: crs.myEvents) {
			if (event.time== parameters.UNUSED_TSS){
				penalty++;
				continue;
			}	
			if (event.room== parameters.UNUSED_ROOM){
				penalty++;
			}
		} // end evt1 for each
		return penalty * this.weight;
	}

	public int[] computeSingleCourseEvents(int[] dataArray, Course course){
		int[] eventsPenalties= new int[course.myEvents.size()];
		for (int d=0; d< course.myEvents.size(); d++)
			eventsPenalties[d]= 0;

		Event evt1; Event evt2;
		for (int e1=0; e1<course.myEvents.size(); e1++) {
			evt1= course.myEvents.get(e1);
			if (evt1.time== parameters.UNUSED_TSS){
				eventsPenalties[e1]+= this.weight;
				continue;
			}	
			if (evt1.room== parameters.UNUSED_ROOM){
				eventsPenalties[e1]+= this.weight;
			}
			for (int e2=0; e2< course.myEvents.size(); e2++){
				if (e2== e1) continue;
				evt2= course.myEvents.get(e2);
				if (evt2.time== parameters.UNUSED_TSS)
					continue;
				if( evt1.time == evt2.time)
					eventsPenalties[e1]+= this.weight;
			} // end e2 for
		} // end evt1 for each
		return eventsPenalties;
	} // end computeEvents
	

	public List<String> AnayzeFinalSol(int[] dataArray) {
		List<String> results= new ArrayList<String>();
		Event evt1; Event evt2;
		
		for (int c=0; c< parameters.numCourses; c++){
			for (int e1=dataHolder.courseStartIndex[c]; e1< (dataHolder.courseStartIndex[c]+dataHolder.numLectures[c]); e1++){
				evt1= convertionManager.intToEvent(e1, dataArray[e1]);
				if (evt1.time== parameters.UNUSED_TSS) 
					results.add("Completeness Constraint Violation: A "+ evt1.hours + " hour event of course: "+ TemporaryData.courseCode[c]+ " does not have a timeslot assigned" );
				for (int e2=e1+1; e2< (dataHolder.courseStartIndex[c]+dataHolder.numLectures[c]); e2++){
					evt2= convertionManager.intToEvent(e2, dataArray[e2]);
					if (evt2.time== parameters.UNUSED_TSS) 
						results.add("Completeness Constraint Violation: A "+ evt2.hours + " hour event of course: "+ TemporaryData.courseCode[c] + " does not have a timeslot assigned");
					if( evt1.time == evt2.time){
						results.add("Completeness Constraint Violation: Events of course: "+ TemporaryData.courseCode[c] + " has been assigned to the same timeslot");
					}
				} // end e2 for
			} // end e1 for
		} // end c for
		return results;
	}
	
	public CompletenessConstraint Clone() {
		CompletenessConstraint con = new CompletenessConstraint(this.weight);
		return con;
	}

	
	
	@Override
	public int computeEvent(Individual currentInd, int i, int time1, int room1) {
		int penalty= 0;
		if (time1 == parameters.UNUSED_TSS)
			penalty++;
		if (room1 == parameters.UNUSED_ROOM)
			penalty++;
		
		return penalty * this.weight;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		// TODO Auto-generated method stub
		
	}



}

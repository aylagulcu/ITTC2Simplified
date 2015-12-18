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

public class MinimumWorkingDaysConstraint extends ConstraintBase implements SoftConstraint{

	// Each course should be spread into a minimum number of days
	// 5 penalty is counted for each day below the minimum
	
	HashSet<Integer> daysUsed= new HashSet<Integer>();
	
	public MinimumWorkingDaysConstraint() {
		super();	
	}
	
	public MinimumWorkingDaysConstraint(int weight) {
		super(weight);	
	}

	@Override
	public int Compute(Individual indiv) {
		int violationCount=0;
		
		for (int courseId1 = 0; courseId1 < parameters.numCourses; courseId1++) {
			violationCount+= computeSingleCourse(indiv, courseId1);
		} // end courseID for
		indiv.MinWorkDaysP= violationCount;
		return violationCount;
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1) {
		int penalty= 0;
		this.daysUsed.clear();
		int startEv= dataHolder.courseStartIndex[courseId1];
		int time;
		for (int ev= startEv; ev< startEv+ dataHolder.numLectures[courseId1]; ev++){
			time= convertionManager.intToTime(indiv.Data[ev]);
			daysUsed.add(dataHolder.timeslotDays[time]);
		}
//		assert daysUsed.size() > 0;
		int diff= dataHolder.numMinDays[courseId1] - daysUsed.size();
		if (diff > 0)
			penalty= 5 * diff;

		return (penalty * this.weight);
		
//		int penalty= 0;
//		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);
//		boolean[] days= new boolean[parameters.numDays];
//		for (short t=0; t< days.length; t++)
//			days[t]=false;
//		for (Event evt1: crs.myEvents){
//			days[dataHolder.timeslotDays[evt1.time]]=true;
//		}
//		int spreadCount=0;
//		for (short t=0; t< days.length; t++)
//			if (days[t])
//				spreadCount++; // Number of days is found
//		int diff= dataHolder.numMinDays[crs.ID] - spreadCount;
//		if (diff > 0)
//			penalty= 5 * diff;
//
//		return (penalty * this.weight);
	}
	
	
	@Override
	public int computeEvent(Individual indiv, int eventId, int time, int room) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(dataHolder.eventCourseId[eventId], indiv.Data);
		Event evt= convertionManager.intToEvent(eventId, indiv.Data[eventId]);
		
		int spreadCount=0;
		int[] dayAssignment= new int[parameters.numDays];

		for (short t=0; t< dayAssignment.length; t++)
			dayAssignment[t]=0;
		
		spreadCount=0; int eventsCount= 0;
		for (Event evt1: crs.myEvents) {
			if (evt1.time== parameters.UNUSED_TSS) continue;
			eventsCount++;
			dayAssignment[dataHolder.timeslotDays[evt1.time]]++;
		}
		for (short t=0; t< parameters.numDays; t++)
			if (dayAssignment[t] > 0)
				spreadCount++; // number of days is found
		if (spreadCount+(crs.myEvents.size()-eventsCount) < dataHolder.numMinDays[crs.ID]){
			// There is a possibility that the unassigned event may be assigned to another day!
			int day= dataHolder.timeslotDays[evt.time];
			if (dayAssignment[day] > 0)
				penalty+= 5;
		} // end if
		return penalty * this.weight;
	}
	
	
	
	
	
	
	
	
	public int[] computeSingleCourseEvents(int[] dataArray, Course course){ 
		int[] eventsPenalties= new int[course.myEvents.size()];
		for (int d=0; d< course.myEvents.size(); d++)
			eventsPenalties[d]= 0;

		Course crs= convertionManager.getCourseFromArray(course.ID, dataArray);
		int[] dayAssignment= new int[parameters.numDays];
		for (short t=0; t< dayAssignment.length; t++)
			dayAssignment[t]=0;
		
		int spreadCount=0; int eventsCount= 0;
		for (Event evt1: crs.myEvents) {
			if (evt1.time== parameters.UNUSED_TSS) continue;
			eventsCount++;
			dayAssignment[dataHolder.timeslotDays[evt1.time]]++;
		}
		for (short t=0; t< parameters.numDays; t++)
			if (dayAssignment[t] > 0)
				spreadCount++; // number of days is found
		if (spreadCount+(crs.myEvents.size()-eventsCount) < dataHolder.numMinDays[crs.ID]){
			// There is a possibility that the unassigned event may be assigned to another day!
			for (int ev=0; ev< course.myEvents.size(); ev++){
				int time= convertionManager.intToTime(dataArray[dataHolder.courseStartIndex[course.ID]+ev]);
				int day= dataHolder.timeslotDays[time];
				if (dayAssignment[day] > 0)
					eventsPenalties[ev]+= 5 * this.weight;
			} // end ev for

		} // end if
		return eventsPenalties;
	} // end computeEvents
	
	public int computeSingleEvent(int[] dataArray, int eventIndex){ 
		
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(dataHolder.eventCourseId[eventIndex], dataArray);
		Event evt= convertionManager.intToEvent(eventIndex, dataArray[eventIndex]);
		
		int spreadCount=0;
		int[] dayAssignment= new int[parameters.numDays];

		for (short t=0; t< dayAssignment.length; t++)
			dayAssignment[t]=0;
		
		spreadCount=0; int eventsCount= 0;
		for (Event evt1: crs.myEvents) {
			if (evt1.time== parameters.UNUSED_TSS) continue;
			eventsCount++;
			dayAssignment[dataHolder.timeslotDays[evt1.time]]++;
		}
		for (short t=0; t< parameters.numDays; t++)
			if (dayAssignment[t] > 0)
				spreadCount++; // number of days is found
		if (spreadCount+(crs.myEvents.size()-eventsCount) < dataHolder.numMinDays[crs.ID]){
			// There is a possibility that the unassigned event may be assigned to another day!
			int day= dataHolder.timeslotDays[evt.time];
			if (dayAssignment[day] > 0)
				penalty+= 5;
		} // end if
		return penalty * this.weight;
	} // end computeSingleEvent
	
	public MinimumWorkingDaysConstraint Clone() {
		MinimumWorkingDaysConstraint con = new MinimumWorkingDaysConstraint(this.weight);
		return con;
	}
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		List<String> results= new ArrayList<String>();
		int violationCount=0;
		Event evt1; 
		int spreadCount=0;
		boolean[] days= new boolean[parameters.numDays];
	
		for (int c=0; c< parameters.numCourses; c++){
			for (short t=0; t< days.length; t++)
				days[t]=false;
			spreadCount=0;
			for (int e=dataHolder.courseStartIndex[c]; e< dataHolder.courseStartIndex[c]+dataHolder.numLectures[c]; e++){
				evt1= convertionManager.intToEvent(e, dataArray[e]);
				days[dataHolder.timeslotDays[evt1.time]]=true;
			} // end e for
			for (short t=0; t< days.length; t++)
				if (days[t])
					spreadCount++;
			int diff= dataHolder.numMinDays[c] - spreadCount;
			if (diff > 0){
				results.add("Minimum Working Days Constraint violation: course "+TemporaryData.courseCode[c]+ " spreads over "+ spreadCount+ " days instead of "+ dataHolder.numMinDays[c]+ " days" );
				violationCount= violationCount+ 5 * diff;
			}
		} // end c for
		results.add("Penalty for minimum working days constraint "+ violationCount);
		return results;
	}

	
	@Override
	public int computeEventForInitializer(Individual indiv, int eventId, int time, int room) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(dataHolder.eventCourseId[eventId], indiv.Data);
		Event evt= convertionManager.intToEvent(eventId, indiv.Data[eventId]);
		
		int spreadCount=0;
		int[] dayAssignment= new int[parameters.numDays];

		for (short t=0; t< dayAssignment.length; t++)
			dayAssignment[t]=0;
		
		spreadCount=0; int eventsCount= 0;
		for (Event evt1: crs.myEvents) {
			if (evt1.time== parameters.UNUSED_TSS) continue;
			eventsCount++;
			dayAssignment[dataHolder.timeslotDays[evt1.time]]++;
		}
		for (short t=0; t< parameters.numDays; t++)
			if (dayAssignment[t] > 0)
				spreadCount++; // number of days is found
		if (spreadCount+(crs.myEvents.size()-eventsCount) < dataHolder.numMinDays[crs.ID]){
			// There is a possibility that the unassigned event may be assigned to another day!
			int day= dataHolder.timeslotDays[evt.time];
			if (dayAssignment[day] > 0)
				penalty+= 5;
		} // end if
		return penalty * this.weight;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		for (int c=0; c< parameters.numCourses; c++){
			indiv.minWorkDaysP[c]= computeSingleCourse(indiv, c);
		} // end c for
		
	}







}

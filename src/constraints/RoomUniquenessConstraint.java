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

public class RoomUniquenessConstraint extends ConstraintBase implements HardConstraint{

	// Checks if more than one course is assigned to a room at the same time
	// 1 violation is counted for each clash, not hour!
	public RoomUniquenessConstraint() {
		super();	
	}
	
	public RoomUniquenessConstraint(int weight) {
		super(weight);	
	}

	@Override
	public boolean checkEventFeasibility(Individual indiv, int eventIndex) {
		int time1= convertionManager.intToTime(indiv.Data[eventIndex]);
		if (time1== parameters.UNUSED_TSS) // Completeness ensures this!
			return false;
		int room1= convertionManager.intToRoom(indiv.Data[eventIndex]);
		if (room1== parameters.UNUSED_ROOM) // Completeness ensures this!
			return false;
		
		int tempEv= indiv.dataMatrix[room1][time1];
		if (tempEv!= eventIndex)
			return false;
	
		return true;
	}

	@Override
	public boolean checkEventFeasibilityInSA(Individual indiv, int eventIndex, int time, int room) {
		return true;
	}

	
	@Override
	public int Compute(Individual indiv) {
		int violationCount=0;
		indiv.RoomUniquenessP= (violationCount* this.weight);
		return (violationCount* this.weight);
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);

		Event evt1; Event evt2;
		int evt1Index;

		for (int ev=0; ev< crs.myEvents.size(); ev++){
			evt1Index= dataHolder.courseStartIndex[courseId1]+ev;
			evt1= convertionManager.intToEvent(evt1Index, indiv.Data[evt1Index]);
			if (evt1.time== parameters.UNUSED_TSS)
				continue;
			if (evt1.room== parameters.UNUSED_ROOM)
				continue;
			for(int j=0; j< indiv.Data.length; j++){
				if (j== evt1Index) continue;
				evt2= convertionManager.intToEvent(j, indiv.Data[j]);
				if (evt2.time== parameters.UNUSED_TSS) 
					continue;	
				if (evt2.room== parameters.UNUSED_ROOM) 
					continue;	
				if( evt2.room == evt1.room){
					if (evt1.time == evt2.time)
						penalty++;	
				} // end if
			} // end j for
		} // end evt1 for each
		return  penalty * this.weight;
	}

	@Override
	public int computeEvent(Individual currentInd, int i, int time1,
			int room1) {
		// TODO Auto-generated method stub
		return 0;
	}


	
	
	
	public int[] computeSingleCourseEvents(int[] dataArray, Course course){
		int[] eventsPenalties= new int[course.myEvents.size()];
		for (int d=0; d< course.myEvents.size(); d++)
			eventsPenalties[d]= 0;
		Event evt1; Event evt2;
		int evt1Index;
		
		for (int ev=0; ev< course.myEvents.size(); ev++){
			evt1Index= dataArray[dataHolder.courseStartIndex[course.ID]]+ev;
			evt1= convertionManager.intToEvent(evt1Index, dataArray[evt1Index]);
			for(int j=0; j< dataArray.length; j++){
				if (j== evt1Index)
					continue;
				evt2= convertionManager.intToEvent(j, dataArray[j]);	
				if (evt2.time== parameters.UNUSED_TSS) 
					continue;	
				if (evt2.room== parameters.UNUSED_ROOM) 
					continue;	
				if( evt2.room == evt1.room){
					if (evt1.time == evt2.time)
						eventsPenalties[ev] += this.weight;
				} // end if
			} // end j for		
		} // end ev for
		return eventsPenalties;
	} // end computeEvents
	
	public int computeSingleEvent(int[] dataArray, int eventIndex){
		Event evt= convertionManager.intToEvent(eventIndex, dataArray[eventIndex]);
		int eventP= 0;
		Event evt2;

		for(int j=0; j< dataArray.length; j++){
			if (j== eventIndex) continue;
			evt2= convertionManager.intToEvent(j, dataArray[j]);			
			if( evt2.room == evt.room){
				if (evt2.time == evt.time)
					eventP++;
			} // end if
		} // end j for		
		return eventP * this.weight;
	} // end computeSingleEvent
	
	public RoomUniquenessConstraint Clone() {
		RoomUniquenessConstraint con = new RoomUniquenessConstraint(this.weight);
		return con;
	}
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		List<String> results= new ArrayList<String>();
		Event evt1; Event evt2;
		int courseId1; int courseId2;
		
		for(int i=0; i< dataArray.length-1; i++){
			courseId1= convertionManager.intToCourseId(dataArray[i]);
			evt1= convertionManager.intToEvent(i, dataArray[i]);
			if (evt1.time== parameters.UNUSED_TSS){
				results.add("Room Uniqueness Constraint: An event of course "+ TemporaryData.courseCode[courseId1]+ " has no timeslot assigned!");
				continue;
			}
			if (evt1.room== parameters.UNUSED_ROOM){
				results.add("Room Uniqueness Constraint: An event of course "+ TemporaryData.courseCode[courseId1]+ " has no room assigned!");
				continue;
			}
			for(int j=i+1; j< dataArray.length; j++){
				courseId2= convertionManager.intToCourseId(dataArray[j]);
				evt2= convertionManager.intToEvent(j, dataArray[j]);
				if (evt2.time== parameters.UNUSED_TSS){
					results.add("Room Uniqueness Constraint: An event of course "+ TemporaryData.courseCode[courseId2]+ " has no timeslot assigned!");
					continue;
				}
				if (evt2.room== parameters.UNUSED_ROOM){
					results.add("Room Uniqueness Constraint: An event of course "+ TemporaryData.courseCode[courseId2]+ " has no room assigned!");
					continue;
				}		
				if( evt2.room == evt1.room){
					if (evt1.time == evt2.time){
						results.add("Room Uniqueness Constraint violation between "+ TemporaryData.courseCode[courseId1]+ " and "+ TemporaryData.courseCode[courseId2]);
					}	
				} // end if
			}
		}
		return results;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		// TODO Auto-generated method stub
		
	}







}

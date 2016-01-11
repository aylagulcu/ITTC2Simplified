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

public class ClashSoftConstraint extends ConstraintBase implements SoftConstraint{
	// One violation is counted for each pair of conflicting lectures not courses!

	public ClashSoftConstraint(){
		super();	
	}
	
	public ClashSoftConstraint(int weight){
		super(weight);	
	}
	
	public boolean checkEventFeasibility(Individual indiv, int eventIndex) {
		int time1= convertionManager.intToTime(indiv.Data[eventIndex]);
		if (time1== parameters.UNUSED_TSS) // Completeness ensures this!
			return true;
		int tempEv;
		
		for (int r=0; r< parameters.numRooms; r++){
			tempEv= indiv.dataMatrix[r][time1];
			if (tempEv != parameters.UNUSED_EVENT && tempEv!= eventIndex){
				if (dataHolder.eventCourseId[eventIndex]== dataHolder.eventCourseId[tempEv])
					return false;				
				if(dataHolder.courseCourseClash[dataHolder.eventCourseId[eventIndex]][dataHolder.eventCourseId[tempEv]]){ // those courses should not be assigned to the same time slot!
					return false;
				} // end if
			}
		} // end r for
		return true;
	}
	
	//@Override
	public int Compute(Individual indiv) {
		
		int violationCount=0;
		int courseId1; int courseId2;
		int time1; int time2;
		
		for(int i=0; i< indiv.Data.length-1; i++){
			courseId1= dataHolder.eventCourseId[i];
			time1= convertionManager.intToTime(indiv.Data[i]);
			if (time1== parameters.UNUSED_TSS)
				continue;
			for(int j=i+1; j< indiv.Data.length; j++){
				time2= convertionManager.intToTime(indiv.Data[j]);
				if (time2== parameters.UNUSED_TSS)
					continue;
				courseId2= dataHolder.eventCourseId[j];
				if( time1 == time2){
					if (courseId1== courseId2){
						violationCount++;
//						System.out.println("Events clashes: "+ i+ "  "+ j+ " time: "+ time1);
					}
					else if(dataHolder.courseCourseClash[courseId1][courseId2]){// those courses should not be assigned to the same time slot!
						violationCount++;
//						System.out.println("Events clashes: "+ i+ "  "+ j+ " time: "+ time2);
					}
				}
			} // end j for
		}
		indiv.ClashP= violationCount* this.weight;
		return violationCount* this.weight;
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1) {
		int penalty= 0;
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);
		int eventIndex; int time; int tempEv;
		
		for (int ev=0; ev< crs.myEvents.size(); ev++) {
			eventIndex= dataHolder.courseStartIndex[courseId1]+ ev;
			time= convertionManager.intToTime(indiv.Data[eventIndex]);
			if (time== parameters.UNUSED_TSS) continue;
			for (int r=0; r< parameters.numRooms; r++){
				tempEv= indiv.dataMatrix[r][time];
				if (tempEv != parameters.UNUSED_EVENT && tempEv!= eventIndex){
					if (dataHolder.eventCourseId[tempEv]== dataHolder.eventCourseId[eventIndex])
						penalty++;
					else if(dataHolder.courseCourseClash[dataHolder.eventCourseId[eventIndex]][dataHolder.eventCourseId[tempEv]]){ // those courses should not be assigned to the same time slot!
						penalty++;
					} // end if
				}
			} // end r for
		} // end evt1 for each
		return  penalty * this.weight;
	}


	
	
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		List<String> results= new ArrayList<String>();
		Event evt1; Event evt2;
		int courseId1; int courseId2;
		
		for(int i=0; i< dataArray.length-1; i++){
			if (dataArray[i]==0)
				continue;
			courseId1= convertionManager.intToCourseId(dataArray[i]);
			evt1= convertionManager.intToEvent(i, dataArray[i]);
			if (evt1.time== parameters.UNUSED_TSS)
				continue;
			for(int j=i+1; j< dataArray.length; j++){
				courseId2= convertionManager.intToCourseId(dataArray[j]);
				if (courseId1!= courseId2){					
					if(dataHolder.courseCourseClash[courseId1][courseId2]){ // those courses should not be assigned to the same time slot!
						evt2= convertionManager.intToEvent(j, dataArray[j]);
						if (evt2.time== parameters.UNUSED_TSS) 
							continue;
						if( evt1.time == evt2.time){
							results.add("ClashConstraint violation between"+ "\t"+ TemporaryData.courseCode[courseId1]+" AND "+ TemporaryData.courseCode[courseId2]);
						}
					} // end if
				}
			}
		}
		return results;
	}

	public ClashSoftConstraint Clone() {
		ClashSoftConstraint con = new ClashSoftConstraint(this.weight);
		return con;
	}

	
	
	@Override
	public int computeEventForInitializer(Individual indiv, int eventId, int time, int room) {
		int penalty= 0;
		int tempEv;
		
		for (int r=0; r< parameters.numRooms; r++){
			tempEv= indiv.dataMatrix[r][time];
			if (tempEv != parameters.UNUSED_EVENT && tempEv!= eventId){
				if (dataHolder.eventCourseId[tempEv]== dataHolder.eventCourseId[eventId])
					penalty++;
				else if(dataHolder.courseCourseClash[dataHolder.eventCourseId[eventId]][dataHolder.eventCourseId[tempEv]]){ // those courses should not be assigned to the same time slot!
					penalty++;
				} // end if
			}
		} // end r for

		return penalty* this.weight;
	}

	public int computeEvent(Individual indiv, int eventIndex, int time, int room){
		int penalty= 0;
		int tempEv;
		if (eventIndex== parameters.UNUSED_EVENT) return 0;
		
		for (int r=0; r< parameters.numRooms; r++){
			if (r== room) continue;
			tempEv= indiv.dataMatrix[r][time];
			if (tempEv != parameters.UNUSED_EVENT){
				if (dataHolder.eventCourseId[tempEv]== dataHolder.eventCourseId[eventIndex]){
					penalty++;
				}
				else if(dataHolder.courseCourseClash[dataHolder.eventCourseId[eventIndex]][dataHolder.eventCourseId[tempEv]]){ // those courses should not be assigned to the same time slot!
					penalty++;
				} // end if
			} // end if
		} // end r for

		return penalty* this.weight;
	} // end computeEvent

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		// TODO Auto-generated method stub
		
	}







	

}
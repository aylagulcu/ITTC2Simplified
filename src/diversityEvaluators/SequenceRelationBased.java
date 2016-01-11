package diversityEvaluators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.Course;
import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Population;
import ga.PopulationParameters;

public class SequenceRelationBased extends DiversityEvaluatorBase {
	// sets are used!
	// BASED ON COURSES!
	Object[][] indCoursePrecedessor;
	
	public void evaluate(Population pop) {
		indCoursePrecedessor= new Object[pop.individuals.length][parameters.numCourses];
		fillCoursePrecedessor(pop);
		int tot= 0;
		int absDistanceOfTwo=0;
		
		for (int ind1=0; ind1< pop.individuals.length-1; ind1++){ 
			for (int ind2= ind1+1; ind2< pop.individuals.length; ind2++){
				absDistanceOfTwo= sequentalMeasure(ind1, ind2);
				
				pop.indIndSeqRelationalDiff[ind1][ind2]= absDistanceOfTwo;
				pop.indIndSeqRelationalDiff[ind2][ind1]= absDistanceOfTwo;
				
				tot+= pop.indIndSeqRelationalDiff[ind1][ind2];
			}
		}
		pop.seqRelationalDiversity= Math.round((float)tot / (Combination(pop.individuals.length,2)));	
	}
	
	@SuppressWarnings("unchecked")
	private int sequentalMeasure(int ind1, int ind2) {
		int totalDiff= 0;
		
		Set<Integer> precedings1;
		Set<Integer> precedings2;

		List<Integer> temp1= new ArrayList<Integer>();
		List<Integer> temp2= new ArrayList<Integer>();
	
		for (int crs=0; crs < parameters.numCourses; crs++){
			precedings1= (HashSet<Integer>) indCoursePrecedessor[ind1][crs];
			temp1.clear();
			for (int i: precedings1) // deep copy array
				temp1.add(i);
			precedings2= (HashSet<Integer>) indCoursePrecedessor[ind2][crs];
			temp2.clear();
			for (int i: precedings2) // deep copy array
				temp2.add(i);
			for(int i1=0; i1< temp1.size(); i1++){
				for(int i2=0; i2< temp2.size(); i2++){
					if (temp1.get(i1)== temp2.get(i2)){
						temp1.remove(i1);
						temp2.remove(i2);
						i1--;
						break;
					} // end if
				} // end i2 for
			} // end i1 for
			int max= Math.max(temp1.size(), temp2.size());
			totalDiff+= max;
//			totalDiff+= Math.min( max, dataHolder.numLectures[crs]); // un-matched times are remained.
		} // end crs for
		assert totalDiff>=0;
		return totalDiff;
	}
	
	private void fillCoursePrecedessor(Population pop) {
		Set<Integer> precedings;
		for (int ind=0; ind< pop.individuals.length; ind++){
			
			Course course1; Course course2;
			for (int crs=0; crs < parameters.numCourses; crs++){
				precedings= new HashSet<Integer>();	
				course1= convertionManager.getCourseFromArray(crs, pop.individuals[ind].Data);
				for (int crs2=0; crs2 < parameters.numCourses; crs2++){
					if (crs2== crs) 
						continue;
					course2= convertionManager.getCourseFromArray(crs2, pop.individuals[ind].Data);
						for (Event e: course1.myEvents){
							if (e.time % parameters.numDailyPeriods ==0)
								continue;	
							for (Event e2: course2.myEvents){
								if (e.id== e2.id) 
									continue;
								if(e2.time+1 == e.time){
									precedings.add(crs2);
								}
							} // end e2 for each
						} // end e for each
				} // end crs2 for
				indCoursePrecedessor[ind][crs]= precedings;
			} // end crs for
		} // end ind for

	} // end method
	
	// Only same-curriculum coures are put into the list
	private void fillCoursePrecedessorOld(Population pop) {
		List<Integer> precedings;
		boolean sameCur= false;
		for (int ind=0; ind< pop.individuals.length; ind++){
			
			Course course1; Course course2;
			for (int crs=0; crs < parameters.numCourses; crs++){
				precedings= new ArrayList<Integer>();	
				course1= convertionManager.getCourseFromArray(crs, pop.individuals[ind].Data);
				for (int crs2=0; crs2 < parameters.numCourses; crs2++){
					sameCur= false;
					for (int cur=0; cur< parameters.numCurriculums; cur++){
						if (dataHolder.course_Curriculum[crs][cur] && dataHolder.course_Curriculum[crs2][cur]){
							sameCur= true;
							break;
						}
					}
					if (!sameCur)
						continue;
					course2= convertionManager.getCourseFromArray(crs2, pop.individuals[ind].Data);
						for (Event e: course1.myEvents){
							if (e.time % parameters.numDailyPeriods ==0)
								continue;	
							for (Event e2: course2.myEvents){
								if (e.id== e2.id) 
									continue;
								if(e2.time+1 == e.time){
									precedings.add(crs2);
								}
							} // end e2 for each
						} // end e for each
				} // end crs2 for
				indCoursePrecedessor[ind][crs]= precedings;
			} // end crs for
		} // end ind for

	} // end method
}

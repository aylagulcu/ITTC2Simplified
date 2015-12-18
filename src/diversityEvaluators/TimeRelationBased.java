package diversityEvaluators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.Course;
import data.Event;
import data.convertionManager;
import data.parameters;
import ga.Population;

public class TimeRelationBased extends DiversityEvaluatorBase {
	// sets are used!
	
	// A list should be filled that holds the same-time courses for each course
	Object[][] indCourseSameTime;
	
	public void evaluateNoPercentage(Population pop) {
		indCourseSameTime= new Object[pop.individuals.length][parameters.numCourses];
		
		fillCourseSameTime(pop);
		int tot= 0;
		int timeDistanceOfTwo=0;
		for (int ind1=0; ind1< pop.individuals.length-1; ind1++){ 	
			for (int ind2= ind1+1; ind2< pop.individuals.length; ind2++){
				timeDistanceOfTwo= timelyMeasure(ind1, ind2);
				pop.indIndTimeRelationalDiff[ind1][ind2]= timeDistanceOfTwo;
				pop.indIndTimeRelationalDiff[ind2][ind1]= timeDistanceOfTwo;
				tot+= pop.indIndTimeRelationalDiff[ind1][ind2];
			}
		}
		pop.timeRelationalDiversity=  (int) ((double)tot / (double)Combination(pop.individuals.length,2));
	}
	
	public void evaluate(Population pop) {
		double maxAmongTwo= parameters.numEvents* (parameters.numRooms-1);
		indCourseSameTime= new Object[pop.individuals.length][parameters.numCourses];
		
		fillCourseSameTime(pop);
		int tot= 0;
		int timeDistanceOfTwo=0;
		for (int ind1=0; ind1< pop.individuals.length-1; ind1++){ 	
			for (int ind2= ind1+1; ind2< pop.individuals.length; ind2++){
				timeDistanceOfTwo= timelyMeasure(ind1, ind2);
				pop.indIndTimeRelationalDiff[ind1][ind2]= (int) ((double)(timeDistanceOfTwo*100) / maxAmongTwo);
				pop.indIndTimeRelationalDiff[ind2][ind1]= (int) ((double)(timeDistanceOfTwo*100) / maxAmongTwo);
				tot+= pop.indIndTimeRelationalDiff[ind1][ind2];
			}
		}
		pop.timeRelationalDiversity=  (int) ((double)tot / (double)Combination(pop.individuals.length,2));
	}
	
	@SuppressWarnings("unchecked")
	private int timelyMeasure(int ind1, int ind2) {
		int totalDiff= 0;
		
		HashSet<Integer> sameTime1;
		HashSet<Integer> sameTime2;

		List<Integer> temp1= new ArrayList<Integer>();
		List<Integer> temp2= new ArrayList<Integer>();
	
		for (int crs=0; crs < parameters.numCourses; crs++){
			sameTime1= (HashSet<Integer>) indCourseSameTime[ind1][crs];
			temp1.clear();
			for (int i: sameTime1) // deep copy array
				temp1.add(i);
			sameTime2= (HashSet<Integer>) indCourseSameTime[ind2][crs];
			temp2.clear();
			for (int i: sameTime2) // deep copy array
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
		assert totalDiff >=0;
		return totalDiff;
	}

	private void fillCourseSameTime(Population pop) {
		// each course is included only once in the set
		Set<Integer> sameTimeCourses;
		for (int ind=0; ind< pop.individuals.length; ind++){
			Course course1; Course course2;
			for (int crs=0; crs < parameters.numCourses; crs++){
				sameTimeCourses= new HashSet<Integer>();	
				course1= convertionManager.getCourseFromArray(crs, pop.individuals[ind].Data);
				for (int crs2=0; crs2 < parameters.numCourses; crs2++){
					if (crs2== crs) 
						continue;
					course2= convertionManager.getCourseFromArray(crs2, pop.individuals[ind].Data);
					for (Event e: course1.myEvents){	
						for (Event e2: course2.myEvents){
							if(e2.time == e.time)
								sameTimeCourses.add(crs2);
						} // end e2 for each
					} // end e for each
				} // end crs2 for
				indCourseSameTime[ind][crs]= sameTimeCourses;
			} // end crs for
		} // end ind for
	} // end method
}

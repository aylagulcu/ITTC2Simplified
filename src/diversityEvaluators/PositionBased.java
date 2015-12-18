package diversityEvaluators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.Course;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;
import ga.Population;

public class PositionBased extends DiversityEvaluatorBase {

	private float[] courseDiversityContribution= new float[parameters.numCourses];
	
	public void evaluate(Population pop) { 
		float tot= 0;
		float distanceOfTwo=0;
		for (int ind1=0; ind1< pop.individuals.length-1; ind1++){ 
			for (int ind2= ind1+1; ind2< pop.individuals.length; ind2++){
				distanceOfTwo= compute(pop.individuals[ind1], pop.individuals[ind2]);
				pop.indIndDiff[ind1][ind2]= distanceOfTwo;
				pop.indIndDiff[ind2][ind1]= distanceOfTwo;
				tot+= pop.indIndDiff[ind1][ind2];
			} // end ind2 for
		} // end ind1 for
		pop.avgDiff= (tot / (float)Combination(pop.individuals.length,2));	
	} // end method
	
	/// Returns the absolute difference between two individuals:
	private float compute(Individual ind1, Individual ind2) { 
		float totalDistance= 0;
		float distance= 0;

		for (int i=0; i< parameters.numCourses; i++)
			courseDiversityContribution[i]= 0;
		// contribution of a course is between zero and one!
		// 1, A and B has nothing in common; 0, if A is identical to B.

		Course course1; Course course2;
		List<Integer> temp1= new ArrayList<Integer>();
		List<Integer> temp2= new ArrayList<Integer>();
		Set<Integer> intersection= new HashSet<Integer>(); 
		
		for (int crs=0; crs < parameters.numCourses; crs++){
			intersection.clear();
			course1= convertionManager.getCourseFromArray(crs, ind1.Data);
			course2= convertionManager.getCourseFromArray(crs, ind2.Data);
			temp1.clear();
			temp2.clear();
			for (int e=0; e< dataHolder.numLectures[crs]; e++){
				temp1.add(course1.myEvents.get(e).time);
				temp2.add(course2.myEvents.get(e).time);
			} // end e for
			intersection.addAll(temp1); 
			intersection.retainAll(temp2);
			distance= 1- ((float)intersection.size() / course1.myEvents.size());
			assert distance<=1;
			totalDistance+= distance;	
			courseDiversityContribution[crs]= distance;
		} // end crs for
		assert totalDistance >=0;
		return totalDistance;
	} // end method
	
}

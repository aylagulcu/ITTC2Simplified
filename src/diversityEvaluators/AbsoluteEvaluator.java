package diversityEvaluators;

import java.util.Arrays;
import data.Course;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class AbsoluteEvaluator extends DiversityEvaluatorBase {

	public void evaluate(Population pop) { // Evaluates and writes it back to the population
	
		int absDistanceOfTwo=0;
		for (int ind1=0; ind1< PopulationParameters.populationSize-1; ind1++){ 
			for (int ind2= ind1+1; ind2< PopulationParameters.populationSize; ind2++){
				absDistanceOfTwo= absoluteMeasure(pop.individuals[ind1], pop.individuals[ind2]);
				
				pop.indIndPositionalDiff[ind1][ind2]= absDistanceOfTwo;
				pop.indIndPositionalDiff[ind2][ind1]= absDistanceOfTwo;		
			} // end ind2 for
			
		} // end ind1 for
		
		int total=0;
		for (int ind1=0; ind1< PopulationParameters.populationSize-1; ind1++)
			for (int ind2= ind1+1; ind2< PopulationParameters.populationSize; ind2++)
				total+= pop.indIndPositionalDiff[ind1][ind2];
		pop.positionalDiversity= Math.round((float)total / (Combination(PopulationParameters.populationSize,2)));
		
		// For single individual's distance:
		for (int i1=0; i1< PopulationParameters.populationSize; i1++){
			pop.individuals[i1].positionalDiff= 0;
			for (int i2= 0; i2< PopulationParameters.populationSize; i2++)
				pop.individuals[i1].positionalDiff+= pop.indIndPositionalDiff[i1][i2];
		} // end i1 for
	
	} // end method

	
	/// Returns the absolute difference between two individuals:
	private int absoluteMeasure(Individual ind1, Individual ind2) { 
		int totalDistance=0;
		Course course1; Course course2;
	
		for (int crs=0; crs < parameters.numCourses; crs++){
			course1= convertionManager.getCourseFromArray(crs, ind1.Data);
			course2= convertionManager.getCourseFromArray(crs, ind2.Data);
			int[] arr1= new int[dataHolder.numLectures[crs]];
			int[] arr2= new int[dataHolder.numLectures[crs]];
			for (int e=0; e< dataHolder.numLectures[crs]; e++){
				arr1[e]= course1.myEvents.get(e).time;
				arr2[e]= course2.myEvents.get(e).time;
			} // end e for
			Arrays.sort(arr1); Arrays.sort(arr2);
			for (int e=0; e< dataHolder.numLectures[crs]; e++){
				totalDistance+= Math.abs(arr1[e]- arr2[e]);
			} // end e for
		} // end crs for
		
		return totalDistance;
	} // end method
	
	private int Combination(int populationSize, int i) {
		// Combination (n,2)= n(n-1)/2
		return populationSize*(populationSize-1)/2 ;
	}
}

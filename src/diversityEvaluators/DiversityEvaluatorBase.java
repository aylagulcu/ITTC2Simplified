package diversityEvaluators;

import ga.Population;

public abstract class DiversityEvaluatorBase {
	
	public abstract void evaluate( Population pop);
	
	public int Combination(int populationSize, int i) {
		// Combination (n,2)= n(n-1)/2
		return populationSize*(populationSize-1)/2 ;
	}
}

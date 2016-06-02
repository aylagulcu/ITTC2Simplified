package diversityEvaluators;

import ga.Individual;
import ga.Population;

public class DiversityManager {

	DiversityEvaluatorBase evaluator;

	public DiversityManager() {
		evaluator= new PositionBased();
	}
	
	public void evaluatePopDiversity(Population pop){
		evaluator.evaluate(pop);		
	}
	
	public double diffTwoIndividuals(Individual ind1, Individual ind2){
		return evaluator.computeBetweenTwo(ind1, ind2);
	}

}

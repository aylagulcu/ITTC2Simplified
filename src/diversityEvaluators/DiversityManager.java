package diversityEvaluators;

import ga.Population;

public class DiversityManager {

	DiversityEvaluatorBase evaluator;

	public DiversityManager() {
		evaluator= new PositionBased();
	}
	
	public void evaluatePopDiversity(Population pop){
		evaluator.evaluate(pop);		
	}

}

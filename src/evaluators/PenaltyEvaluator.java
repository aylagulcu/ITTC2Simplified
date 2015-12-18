package evaluators;

import java.util.List;

import constraints.ConstraintBase;
import ga.Individual;
import ga.Population;

public class PenaltyEvaluator extends EvaluatorBase {
	
	public PenaltyEvaluator(List<ConstraintBase> constr) {
		super(constr);
	}

	public void Evaluate(Population pop) {
		for(Individual ind: pop.individuals)
			evaluateIndividual(ind);
	}
	
	public void evaluateIndividual(Individual ind) {
		ind.isFeasible= true;

		ind.totalPenalty= 0;
		for (ConstraintBase constr: this.hardConstraints){
			ind.totalPenalty+= constr.Compute(ind);
		} // end for each
		if (ind.totalPenalty>0) 
			ind.isFeasible= false;
		for (ConstraintBase constr: this.softConstraints){
			ind.totalPenalty+= constr.Compute(ind);
		} // end for each	

	}

}

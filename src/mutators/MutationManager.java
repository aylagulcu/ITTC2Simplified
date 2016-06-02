package mutators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import constraints.ClashConstraint;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import data.parameters;
import util.RandomNumberGenerator;
import evaluators.EvaluatorBase;
import evaluators.PenaltyEvaluator;
import ga.Individual;
import ga.PopulationParameters;
import robustnessEvaluators.RobustnessManager;

public class MutationManager {
	
	public List<ConstraintBase> constraints;
	public List<HardConstraint> feasConstraints;
	public mutatorBase myMutator;
	public Individual indToMutate;
	
	protected RobustnessManager rm;
	protected EvaluatorBase pEvaluator;
	
	public Random myRandGen;
	float prob;
	int numEvents= (int)(parameters.numEvents * PopulationParameters.eventMutRate);
	int counter;
	
	public MutationManager(List<ConstraintBase> constr) {
		this.constraints=constr;
		feasConstraints= new ArrayList<HardConstraint>();
		feasConstraints.add(new InstructorTimeAvailabilityConstraint(100));
		feasConstraints.add(new ClashConstraint(100));

		this.pEvaluator= new PenaltyEvaluator(this.constraints);
		this.rm= new RobustnessManager(this.constraints);
		
		myRandGen= new Random(RandomNumberGenerator.getNewSeed());
		
		myMutator= new MoveSwapMutator(this);
	}


	public void mutateIndividual(Individual indiv) {
		
		prob= RandomNumberGenerator.getRandomFloat(); // [0,1)
		if (prob > PopulationParameters.mutationRate){
			return; // No mutation will be applied
		}
		else{
			this.indToMutate= indiv;
			myMutator.mutate();
			
			pEvaluator.evaluateIndividual(indToMutate);
			rm.evalIndivRobustness(indToMutate);
		} // end else		
	}
	
}

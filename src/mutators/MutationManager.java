package mutators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import constraints.ClashConstraint;
import constraints.CompletenessConstraint;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.RoomUniquenessConstraint;
import data.parameters;
import util.RandomNumberGenerator;
import evaluators.EvaluatorBase;
import evaluators.PenaltyEvaluator;
import ga.GlobalVars;
import ga.Individual;
import ga.PopulationParameters;

public class MutationManager {
	
	public List<ConstraintBase> constraints;
	public List<HardConstraint> feasConstraints;
	public mutatorBase myMutator;
	public Individual indToMutate;
	public EvaluatorBase mySimpleEvaluator;
	public Random myRandGen;
	float prob;
	int numEvents= (int)(parameters.numEvents * PopulationParameters.eventMutRate);
	int counter;
	
	public MutationManager(List<ConstraintBase> constr) {
		this.constraints=constr;
		feasConstraints= new ArrayList<HardConstraint>();
		feasConstraints.add(new InstructorTimeAvailabilityConstraint(100));

		this.mySimpleEvaluator= new PenaltyEvaluator(this.constraints);
		myRandGen= new Random(RandomNumberGenerator.getNewSeed());
		
		myMutator= new MoveSwapMutator(this);
	}


	public void mutateIndividual(Individual indiv) throws IOException{
		
		prob= RandomNumberGenerator.getRandomFloat(); // [0,1)
		if (prob > PopulationParameters.mutationRate){
			return; // No mutation will be applied
		}
		else{
			this.indToMutate= indiv;
			myMutator.mutate();
			mySimpleEvaluator.evaluateIndividual(indToMutate);
		} // end else
		
//		if (GlobalVars.iterCounterWithNoPenaltyImprovement > 50){
//			this.indToMutate= indiv;
//			myMutator.mutate();
//			mySimpleEvaluator.evaluateIndividual(indToMutate);
//		}

		
	}
	
}

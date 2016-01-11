package crosser;

import ga.Individual;
import ga.PopulationParameters;
import robustnessEvaluators.RobustnessManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.RandomNumberGenerator;
import vnSearchers.MicroSAforP;
import vnSearchers.MicroSAforRobustness;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import evaluators.EvaluatorBase;
import evaluators.PenaltyEvaluator;

public class crossoverManager {
	boolean sap;
	boolean sar;
	public Random myRandom;
	
	public crosserBase myCrosser;
	
	public MicroSAforP MicroSAP;
	public MicroSAforRobustness MicroSAR;
	
	// 2: after only time X, 2: time then room, 2: after only room, 2: after room then time
	public Individual[] offSprings= new Individual[2]; 

	public List<ConstraintBase> constraints;
	public List<HardConstraint> feasConstraints;
	
	protected RobustnessManager rm;
	protected EvaluatorBase pEvaluator;
	
	double popAvgPenalty;
	
	public crossoverManager(List<ConstraintBase> constr) {
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
		
		this.constraints=constr;
		feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: this.constraints)
			if (con instanceof HardConstraint)
				feasConstraints.add((HardConstraint) con);	
		
		myCrosser=  new TimeBasedCX(this); 
		MicroSAP= new MicroSAforP(this.constraints);
		MicroSAR= new MicroSAforRobustness(this.constraints); 
		
		this.pEvaluator= new PenaltyEvaluator(this.constraints);
		this.rm= new RobustnessManager(this.constraints);
		
	}

	public Individual[] crossIndividuals(Individual ind1, Individual ind2, double popAverageP){

		sap= true;
		sar= true;
		
		popAvgPenalty= popAverageP;
		
		float rnd= RandomNumberGenerator.getRandomFloat(); // [0,1)
		if (rnd < PopulationParameters.crossoverRate){			
			offSprings= myCrosser.cross(ind1, ind2);
			
//			this.mySimpleEvaluator.evaluateIndividual(temp[0]);
//			this.mySimpleEvaluator.evaluateIndividual(temp[1]);
			
			return offSprings;
		}
		else{
			offSprings[0]= ind1;
			offSprings[1]= ind2;
		}
		return offSprings;
	}

	
	public void applyMicroSA(Individual child) {

		// After CX, evaluation is required:
		this.pEvaluator.evaluateIndividual(child);
		this.rm.evalIndivRobustness(child);
		
		// if rand < 0.5 then apply penalty improvement
		if (myRandom.nextDouble()<0.5){	
			this.MicroSAP.applySA(child);
			sap= false;
		}
		else{
			this.MicroSAR.applySA(child);
			sar= false;
		}
	}
	
	public void applyMicroSAForSecond(Individual child) {
		// After CX, evaluation is required:
		this.pEvaluator.evaluateIndividual(child);
		this.rm.evalIndivRobustness(child);
		
		if (sap){
			this.MicroSAP.applySA(child);
		}
		else{
			this.MicroSAR.applySA(child);
		}
	}
	
}
package crosser;

import ga.Individual;
import ga.PopulationParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import util.RandomNumberGenerator;
import vnSearchers.MicroSAforP;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import evaluators.EvaluatorBase;
import evaluators.PenaltyEvaluator;

public class crossoverManager {
	// Individuals should be evaluated after returning from CX operation
	public EvaluatorBase mySimpleEvaluator;
	public crosserBase myDayCrosser;
	public MicroSAforP MicroSA;
	
	// 2: after only time X, 2: time then room, 2: after only room, 2: after room then time
	public Individual[] offSprings= new Individual[8]; // after time exchange
	public Individual[] temp= new Individual[2]; // after time exchange

	public List<ConstraintBase> constraints;
	public List<HardConstraint> feasConstraints;
	
	double popAvgPenalty;
	
	public crossoverManager(List<ConstraintBase> constr) {
		this.constraints=constr;
		feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: this.constraints)
			if (con instanceof HardConstraint)
				feasConstraints.add((HardConstraint) con);	
		
		myDayCrosser=  new DayBasedCX(this); 
		MicroSA= new MicroSAforP(this.constraints);
		mySimpleEvaluator= new PenaltyEvaluator(this.constraints);
	}

	public synchronized Individual[] crossIndividuals(Individual ind1, Individual ind2, double popAverageP){

		popAvgPenalty= popAverageP;
		
		float rnd= RandomNumberGenerator.getRandomFloat(); // [0,1)
		if (rnd < PopulationParameters.crossoverRate){			
			temp= myDayCrosser.cross(ind1, ind2);
			
//			this.mySimpleEvaluator.evaluateIndividual(temp[0]);
//			this.mySimpleEvaluator.evaluateIndividual(temp[1]);
			
			return temp;
		}
		else{
			temp[0]= ind1;
			temp[1]= ind2;
		}
		return temp;
	}

	public void applyMicroSA(Individual child, int eventToMove) {
		this.MicroSA.applyVNS(child, eventToMove);
	}
	
	public void applyMicroSA(Individual child) {
		this.MicroSA.applyVNS(child);

	}
	
//	public void applyMicroSA(Individual child, 	HashSet<Integer> eventsToMove) {
//		this.mySimpleEvaluator.evaluateIndividual(child);
////		System.out.println("After CX but before micro SA penalty:\t"+ child.totalPenalty);
//
//		for (int event: eventsToMove)
//			this.MicroSA.applyVNS(child, event);
//	}

}
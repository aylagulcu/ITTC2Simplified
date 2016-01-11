package evaluators;

import ga.Individual;
import ga.Population;

import java.util.ArrayList;
import java.util.List;

import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.SoftConstraint;

public abstract class EvaluatorBase {

	public List<ConstraintBase> constraints;
	public List<ConstraintBase> hardConstraints;
	public List<ConstraintBase> softConstraints;
	
	public EvaluatorBase(List<ConstraintBase> constr) {
		this.constraints=constr;
		
		this.hardConstraints = new ArrayList<ConstraintBase>();
		for (ConstraintBase con: this.constraints)
			if (con instanceof HardConstraint)
				hardConstraints.add((ConstraintBase) con);	
		
		this.softConstraints = new ArrayList<ConstraintBase>();
		for (ConstraintBase con: this.constraints)
			if (con instanceof SoftConstraint)
				softConstraints.add((ConstraintBase) con);			
	}
	
	public abstract void Evaluate(Population pop);	

	public abstract void evaluateIndividual(Individual ind);
	

}

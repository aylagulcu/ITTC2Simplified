package robustnessEvaluators;

import java.util.List;

import constraints.ConstraintBase;
import ga.Individual;
import ga.Population;

public class RobustnessManager {
	
	public List<ConstraintBase> constraints;
	private RobustnessEvaluatorBase robMeasure;
	
	public RobustnessManager(List<ConstraintBase> constr) {
		this.constraints=constr;	
//		this.robMeasure= new DisruptEventSC(this); // Our Real measure
		this.robMeasure= new EventBasedR5Move(this);
	}
	
	public void setRobustnessMeasure(RobustnessEvaluatorBase rMeasure){
		this.robMeasure= rMeasure;
	}
	
	public void evalPopRobustness(Population pop){
		this.robMeasure.evaluatePop(pop);
	}
	
	public void evalIndivRobustness(Individual ind){
		this.robMeasure.evaluateIndividual(ind);
	}
	
	public void evalIndivRobustnessForCurrentOp(Individual ind, int ev1,
			int time2, int room2, int ev2, int time1, int room1) {
		this.robMeasure.evaluateIndividualPartial(ind, ev1,
				time2, room2, ev2, time1, room1);
	}


	
}

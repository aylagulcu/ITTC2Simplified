package vnSearchers;

import ga.Individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import robustnessEvaluators.RobustnessManager;
import util.RandomNumberGenerator;
import constraints.ClashSoftConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import evaluators.EvaluatorBase;

public abstract class VNS {
	public List<ConstraintBase> constraints;
	public List<HardConstraint> feasConstraints;
	public List<ConstraintBase> optConstraints;
	
	protected RobustnessManager rm;
	protected EvaluatorBase pEvaluator;
	
	public Random myRandom;
	
	public List<VNSearcherBase> VNSList;
	
	protected Individual currentInd;
	protected VNSearcherBase searcher;
	
	float[] tempArray;
	
	public VNS(List<ConstraintBase> constrList){
		
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
		this.VNSList= new ArrayList<VNSearcherBase>();
	}
	
	public abstract void applyVNS(int iterCounter, Individual indiv);
	
	protected abstract boolean acceptCurrentMove();

	public abstract void updateOriginalValue();
	public abstract void computeOriginalPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1);
	public abstract void computeNewPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1);
	
	public VNSearcherBase selectSearcher(){	
		// Select one randomly, independent of the previous one:
		double moverate=0.57;
		double r= this.myRandom.nextDouble();
		if (r<= moverate){
			return this.VNSList.get(0); // return move
		}
		else{
			return this.VNSList.get(1); // return swap
		} // end else
	}

}

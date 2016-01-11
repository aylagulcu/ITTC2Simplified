package vnSearchers;

import ga.Individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import robustnessEvaluators.RobustnessManager;
import util.RandomNumberGenerator;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import evaluators.EvaluatorBase;

public abstract class SABase {
	
	public List<ConstraintBase> originalConstraints;
	public List<ConstraintBase> constraints;
	public List<HardConstraint> feasConstraints;
	public List<ConstraintBase> optConstraints;
	
	protected RobustnessManager rm;
	protected EvaluatorBase pEvaluator;
	
	public Random myRandom;
	public List<SearcherBase> VNSList;
	
	protected Individual currentInd;
	protected SearcherBase searcher;
	
	public SABase(List<ConstraintBase> constrList){
		this.constraints= constrList;
		
		this.originalConstraints= constrList;
		
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
		this.VNSList= new ArrayList<SearcherBase>();
	}
	
	public abstract void applySA(Individual indiv);
	
	public abstract boolean acceptCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1);
	public abstract void updateOriginalValue();
	public abstract void computeOriginalPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1);
	public abstract void computeNewPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1);
	
	public SearcherBase selectSearcher(){	
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

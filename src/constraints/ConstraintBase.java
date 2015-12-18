package constraints;

import ga.Individual;

import java.util.List;

public abstract class ConstraintBase {

	public int weight; // used for evaluation and selection of best curriculum during reproduction
	
	public ConstraintBase() {
		this.weight=1; // Default weight for each constraint
	}
	
	public ConstraintBase(int wt) {
		this.weight= wt;
	}
	
	public abstract int Compute(Individual indiv); // Updates individual's relevant cost component field and also returns violation count!

	public abstract int computeSingleCourse(Individual indiv, int crs);

	public abstract int computeEvent(Individual currentInd, int i, int time1, int room1);

	public abstract ConstraintBase Clone(); 
	
	public abstract List<String> AnayzeFinalSol(int[] dataArray);


	public abstract void ComputeCoursePenalties(Individual indiv);
	

}

package constraints;

import ga.Individual;

public interface HardConstraint {
	
	public boolean checkEventFeasibility(Individual indiv, int eventIndex);
	
	public boolean checkEventFeasibilityInSA(Individual indiv, int eventIndex, int time, int room);
	
	
}

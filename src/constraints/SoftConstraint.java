package constraints;

import ga.Individual;

public interface SoftConstraint {

	// not applicable for all the soft constraints, but this is needed for the Initializer
	public abstract int computeEventForInitializer(Individual indiv, int eventId, int time, int room);
	
	
}

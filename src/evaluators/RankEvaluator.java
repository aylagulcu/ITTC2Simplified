package evaluators;

import ga.Population;

public class RankEvaluator {
	// Assigns each individual a rank.
	// Rank 1 belongs to the individuals in the 1st non-dominated front
		
	// Weak domination implemented: compare x and y:
	// if for some dimensions y is better than x while for all the other dimensions they are the same then y dominates x.
	// if y is better than x for all the dimensions then y strongly dominates x.
	public void Evaluate(Population pop) {
		// Assign each individual a rank.
		// Each individual has a penalty and a robustness value. Compare these values...
		int dCount; // number of solutions dominating the current solution
		double p1; double r1;
		double p2; double r2;
		for (int i= 0; i< pop.individuals.length; i++){
			dCount= 0; // number of solutions dominating i
			p1= pop.individuals[i].totalPenalty;
			r1= pop.individuals[i].robustValueMin;
			// find the number of individuals dominating this individual:
			for (int j= 0; j< pop.individuals.length; j++){
				if (i==j) continue;
				p2= pop.individuals[j].totalPenalty;
				r2= pop.individuals[j].robustValueMin;
				if (p2<=p1 && r2<=r1){
					if (p2==p1 && r2==r1)
						continue; // Both values are equal. Probably, they are the same individuals
					// at least one objective should be smaller
					else if (p2<p1 || r2<r1)
						dCount+= 1; // jth solution dominates ith solution
				} // end if
			} // end j for
			pop.individuals[i].rank= dCount + 1;
		} // end i for
		
	}
}

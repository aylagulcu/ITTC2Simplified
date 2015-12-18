package evaluators;

import java.util.ArrayList;

import ga.Individual;
import ga.Population;

public class CrowdingDistanceEvaluator {
	private float M= 1000;
	
	// Evaluate for each front seperately!
	// Ranks should have been evaluated beforehand. Rank values should be up to date!!!
	public void Evaluate(Population pop) {
		// Make each individual's crowd dist equal to zero and also find the biggest rank in the pop:
		int biggestRNK= 0;
		for (int i= 0; i< pop.individuals.length; i++){
			pop.individuals[i].crowdDistance= 0;
			if (pop.individuals[i].rank> biggestRNK)
				biggestRNK= pop.individuals[i].rank;
		} // end for

		// Step1: Distance for the 1st objective: Penalty
		// Step2: Distance for the 2nd objective: Robustness
		// Step3: Compute total distance for each individual and assign it to the individual's crowding distance
		ArrayList<Individual> frontIndivs= new ArrayList<Individual>();
		int[] sortedIndividuals;
		int[] sortedPenalties;
		float[] sortedRobustness;
		int tempI; int tempP; float tempR;

		int RNK= 0;
		while(RNK<= biggestRNK){	
			RNK+= 1;
			frontIndivs.clear();
			for (Individual ind: pop.individuals)
				if (ind.rank== RNK)
					frontIndivs.add(ind);
			if (frontIndivs.size()==0) continue;
			
			// Step1: Objective 1 Penalty
			// Individual indices sorted in asc order of their penalty values:
			// 0th index refers to the individual with the smallest penalty
			sortedIndividuals= new int[frontIndivs.size()];
			for (int i= 0; i< sortedIndividuals.length; i++)
				sortedIndividuals[i]= i;
			// Corresponding individual penalties are kept in below:
			sortedPenalties= new int[frontIndivs.size()];
			for (int i= 0; i< sortedPenalties.length; i++)
				sortedPenalties[i]= frontIndivs.get(i).totalPenalty;
			
			for (int i= 0; i< frontIndivs.size()-1; i++){
				for (int j= i+1; j< frontIndivs.size(); j++){
					if (sortedPenalties[j] < sortedPenalties[i]){
						tempI= sortedIndividuals[i];
						sortedIndividuals[i]= sortedIndividuals[j];
						sortedIndividuals[j]= tempI;
						
						tempP= sortedPenalties[i];
						sortedPenalties[i]= sortedPenalties[j];
						sortedPenalties[j]= tempP;
					} // end if
					else if (sortedPenalties[j] == sortedPenalties[i]){
						if (frontIndivs.get(sortedIndividuals[j]).robustValueMin < frontIndivs.get(sortedIndividuals[i]).robustValueMin){
							tempI= sortedIndividuals[i];
							sortedIndividuals[i]= sortedIndividuals[j];
							sortedIndividuals[j]= tempI;
							
							tempP= sortedPenalties[i];
							sortedPenalties[i]= sortedPenalties[j];
							sortedPenalties[j]= tempP;
						} // end if
					} // end else if	
				} // end j for
			} // end i for
			
			// Crowding distance is computed using the above sorted arrays:
			// Individuals in the same order as in above arrays
			// 0th index refers to the individual with the smallest penalty
			float range = sortedPenalties[frontIndivs.size()-1] - sortedPenalties[0];
			frontIndivs.get(sortedIndividuals[0]).crowdDistance += this.M;	
			if (frontIndivs.size()> 1){
				frontIndivs.get(sortedIndividuals[sortedIndividuals.length-1]).crowdDistance += this.M;
				for (int i= 1; i< sortedIndividuals.length-1; i++){
					frontIndivs.get(sortedIndividuals[i]).crowdDistance += ( sortedPenalties[i+1] - sortedPenalties[i-1]) / range;
				}
			} // end if
			// Step2: Objective 2 Robustness
			// Individual indices sorted according to their robustness values:
			// 0th index refers to the individual with the smallest penalty
			sortedIndividuals= new int[frontIndivs.size()];
			for (int i= 0; i< sortedIndividuals.length; i++)
				sortedIndividuals[i]= i;
			// Corresponding individual robustness values are kept in below:
			sortedRobustness= new float[frontIndivs.size()];
			for (int i= 0; i< sortedRobustness.length; i++)
				sortedRobustness[i]= frontIndivs.get(i).robustValueMin;
			
			for (int i= 0; i< frontIndivs.size()-1; i++){
				for (int j= i+1; j< frontIndivs.size(); j++){
					if (sortedRobustness[j] < sortedRobustness[i]){
						tempI= sortedIndividuals[i];
						sortedIndividuals[i]= sortedIndividuals[j];
						sortedIndividuals[j]= tempI;
						
						tempR= sortedRobustness[i];
						sortedRobustness[i]= sortedRobustness[j];
						sortedRobustness[j]= tempR;
					} // end if
					else if (sortedRobustness[j] == sortedRobustness[i]){
						if (frontIndivs.get(sortedIndividuals[j]).totalPenalty < frontIndivs.get(sortedIndividuals[i]).totalPenalty){
							tempI= sortedIndividuals[i];
							sortedIndividuals[i]= sortedIndividuals[j];
							sortedIndividuals[j]= tempI;
							
							tempR= sortedRobustness[i];
							sortedRobustness[i]= sortedRobustness[j];
							sortedRobustness[j]= tempR;
						} // end if
					} // end if	
					
				} // end j for
			} // end i for
			
			// Crowding distance is computed using the above sorted arrays:
			// Individuals in the same order as in above arrays
			// 0th index refers to the individual with the smallest robustness
			range = sortedRobustness[frontIndivs.size()-1] - sortedRobustness[0];		
			frontIndivs.get(sortedIndividuals[0]).crowdDistance += this.M;
			if (frontIndivs.size()> 1){
				frontIndivs.get(sortedIndividuals[sortedIndividuals.length-1]).crowdDistance += this.M;
				for (int i= 1; i< sortedIndividuals.length-1; i++)
					frontIndivs.get(sortedIndividuals[i]).crowdDistance += ( sortedRobustness[i+1] - sortedRobustness[i-1]) / range;
			} // end if
		}; // end while
		
//		System.out.println("Counter: "+ counter);
//		System.out.println("Individual"+ "\t"+ "Rank"+ "\t"+"Penalty"+ "\t"+ "Robustness"+ "\t" + "CrowdDistance");
//		for (int i=0; i< pop.individuals.length; i++){
//			System.out.println(i + "\t"+ pop.individuals[i].rank+ "\t"+pop.individuals[i].totalPenalty+ "\t" + pop.individuals[i].robustValueMin+ "\t"+ pop.individuals[i].crowdDistance );
//		}
//		System.out.println();
	}
	
}

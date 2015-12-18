package selectors;

import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class RouletteWheelSelector extends SelectorBase {
	// Same individuals are not allowed!
	
	// Our function is a minimization function. The following steps are performed 
	// in order to be able to apply RouletteWheelSelector:
//	1- Take the sum of the penalty all of the individuals
//	2- Assign each individual a fitness value as shown below:
//	fitness i= sum/penalty i
//	3- No need to compute probabilies for each individual as:
//	p i= fitness i / total fitness
//	Instead, sort the individuals based on the fitness value and then use the partial fitnes values
	Individual[] indivs= new Individual[2];
	
	float[] fitnesses= new float[PopulationParameters.populationSize];
	int[] individualsSorted= new int[PopulationParameters.populationSize];
	float totalFitness;
	float[] probabilities= new float[PopulationParameters.populationSize];
	
	public Individual[] selectIndividual(Population population) {
		computeFitness(population); 

		float randomFitness= (myRandom.nextFloat() * totalFitness);
		float partialTotal=0;
		for(int i=0; i< population.individuals.length; i++){
			partialTotal+= fitnesses[individualsSorted[i]];
			if (partialTotal >= randomFitness){
				indivs[0]= population.individuals[i].clone();
				break; // i for
			}
		} // end i for

		randomFitness= (myRandom.nextFloat() * totalFitness);
		partialTotal=0;
		for(int i=0; i< population.individuals.length; i++){
			partialTotal+= fitnesses[individualsSorted[i]];
			if (partialTotal >= randomFitness){
				indivs[1]= population.individuals[i].clone();
				break; // i for
			}
		} // end i for
		
		return indivs;
	}

	private void computeFitness(Population population) {
		int totalPenalty;
		
		totalPenalty=0; 
		for(int i=0; i< population.individuals.length; i++)
			totalPenalty= totalPenalty +  population.individuals[i].totalPenalty;
		
		for(int i=0; i< population.individuals.length; i++){
			if (population.individuals[i].totalPenalty == 0 )
				fitnesses[i]= totalPenalty; 
			else
				fitnesses[i]= (totalPenalty /  population.individuals[i].totalPenalty); 
		} // end i for

		// the higher the fitness, better the individual:
		totalFitness=0; 
		for(int i=0; i< population.individuals.length; i++)
			totalFitness+= fitnesses[i];
		
		// now sort individuals acc to fitness values:
		for (int i=0; i< individualsSorted.length; i++)
			individualsSorted[i]= i;
		int temp;
		for (int i=0; i< individualsSorted.length; i++)
			for (int j=i+1; j< individualsSorted.length; j++){
				if (fitnesses[individualsSorted[j]]> fitnesses[individualsSorted[i]]){
					temp= individualsSorted[i];
					individualsSorted[i]= individualsSorted[j];
					individualsSorted[j]= temp;
				} // end if
			} // end j for
		
	}
}

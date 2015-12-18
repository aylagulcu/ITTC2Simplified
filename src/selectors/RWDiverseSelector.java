package selectors;

import java.util.Random;

import util.RandomNumberGenerator;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class RWDiverseSelector extends SelectorBase {
	// first is selected acc to RW
	// second is the most diverse to the first one
	
	// Our function is a minimization function. The following steps are performed 
	// in order to be able to apply RouletteWheelSelector:
//	1- Take the sum of all of the individuals
//	2- Assign each individual a fitness value as shown below:
//	fitness i= sum/penalty i
//	3- Then compute probabilies for each individual as follows:
//	p i= fitness i / total fitness

	Random myRandom;
	
	float[] fitnesses= new float[PopulationParameters.populationSize];
	float totalFitness;
	
	float[] probabilities= new float[PopulationParameters.populationSize];
	
	public RWDiverseSelector(){
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
	}
	
	public Individual[] selectIndividual(Population population) {
		computeProbabilities(population);

		Individual[] indivs= new Individual[2];
		float randomFitness;
		int index1= 0;
		randomFitness= (myRandom.nextFloat() * totalFitness);
		float partialTotal=0;
		for(int i=0; i< population.individuals.length; i++){
			partialTotal+= fitnesses[i];
			if (partialTotal >= randomFitness){
				indivs[0]= population.individuals[i].clone();
				index1= i;
				break; // i for
			}
		} // end i for
		
		int index2= selectLargestDistance(index1, population);
		indivs[1]= population.individuals[index2].clone();
		
		for (int i=0; i< indivs.length; i++)
			assert indivs[i]!= null;
		assert index1!= index2;
		return indivs;
	}

	private void computeProbabilities(Population population) {
		int[] penalties= new int[PopulationParameters.populationSize];
		int totalPenalty;
		for(int i=0; i< population.individuals.length; i++)
			penalties[i]= population.individuals[i].totalPenalty;
				
		totalPenalty=0; 
		for(int i=0; i< population.individuals.length; i++)
			totalPenalty= totalPenalty +  population.individuals[i].totalPenalty;
		
		for(int i=0; i< population.individuals.length; i++){
			if (population.individuals[i].totalPenalty == 0 )
				fitnesses[i]= (float) (totalPenalty /  0.000001); 
			else
				fitnesses[i]= (totalPenalty /  population.individuals[i].totalPenalty); 
		}
		totalFitness=0; 
		for(int i=0; i< population.individuals.length; i++){
			totalFitness= totalFitness +  fitnesses[i];
		}
		
	}
	
	private int selectLargestDistance(int ind1, Population pop) {
		// Select the event which is the most diverse from the currently selected individual:
		int maxInd;
		if (ind1== 0)
			maxInd= 1;
		else
			maxInd= 0;
		
		for(int i=0; i< pop.individuals.length; i++){
			if(i== ind1)
				continue;
			if (pop.indIndTotalDifferences[ind1][i] > pop.indIndTotalDifferences[ind1][maxInd])
				maxInd= i;			
		} // end i for
		
		return maxInd;
	}

}

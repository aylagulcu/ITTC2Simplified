package selectors;

import java.util.Random;

import util.RandomNumberGenerator;

import ga.Individual;
import ga.Population;

public class RandomDiverseSelector extends SelectorBase{
	// first is selected randomly
	// second is the most diverse to the first one
	Random myRandom;
	
	public RandomDiverseSelector(){
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
	}
	
	public Individual[] selectIndividual(Population population) {
		
		Individual[] indivs= new Individual[2];
		int index1; 
		index1= myRandom.nextInt(population.individuals.length);
		indivs[0]= population.individuals[index1].clone();
		
		int index2= selectLargestDistance(index1, population);
		indivs[1]= population.individuals[index2].clone();
		
		for (int i=0; i< indivs.length; i++)
			assert indivs[i]!= null;
		assert index1!= index2;
		return indivs;
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
			if (pop.indIndDiff[ind1][i] > pop.indIndDiff[ind1][maxInd])
				maxInd= i;			
		} // end i for
		
		return maxInd;
	}

	@Override

	
	public int[] selectIndividualIndices(Population population) {
		// TODO Auto-generated method stub
		return null;
	}

}

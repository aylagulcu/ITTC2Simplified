package selectors;

import ga.Individual;
import ga.Population;

public class RandomSelector extends SelectorBase {
	

	public Individual[] selectIndividual(Population population) {
		Individual[] indivs= new Individual[2];
		int index1; int index2;
		index1= myRandom.nextInt(population.individuals.length);
		do{
			index2= myRandom.nextInt(population.individuals.length);
		} while(index1== index2);
		
		indivs[0]= population.individuals[index1].clone();
		indivs[1]= population.individuals[index2].clone();
		
		return indivs;
	}

	@Override
	public int[] selectIndividualIndices(Population population) {
		int[] indivs= new int[2];
		int index1; int index2;
		index1= myRandom.nextInt(population.individuals.length);
		do{
			index2= myRandom.nextInt(population.individuals.length);
		} while(index1== index2);
		indivs[0]= index1;
		indivs[1]= index2;
		return indivs;
	}

}

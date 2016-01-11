package selectors;

import ga.Individual;
import ga.Population;

public class TournamentBinary extends SelectorBase {

	public Individual[] selectIndividual(Population population) {
		
		Individual[] indivs= new Individual[2];
		int index1= myRandom.nextInt(population.individuals.length);
		
		int index2; int selected;
		do
			index2= myRandom.nextInt(population.individuals.length);
		while(index2==index1);

		if(population.individuals[index1].totalPenalty < population.individuals[index2].totalPenalty){
			indivs[0]= population.individuals[index1].clone();
			selected= index1;
		}
		else{
			indivs[0]= population.individuals[index2].clone();
			selected= index2;
		}
		
		do
			index1= myRandom.nextInt(population.individuals.length);
		while (index1== selected);
		
		do{
			index2= myRandom.nextInt(population.individuals.length);
		}while(index2==index1);

		if(population.individuals[index1].totalPenalty < population.individuals[index2].totalPenalty)
			indivs[1]= population.individuals[index1].clone();
		else
			indivs[1]= population.individuals[index2].clone();
		
		return indivs;
	}

	@Override
	public int[] selectIndividualIndices(Population population) {
		
		int[] indivs= new int[2];
		int index1= myRandom.nextInt(population.individuals.length);
		
		int index2; int selected;
		do
			index2= myRandom.nextInt(population.individuals.length);
		while(index2==index1);

		if(population.individuals[index1].totalPenalty < population.individuals[index2].totalPenalty){
			indivs[0]=index1;
			selected= index1;
		}
		else{
			indivs[0]=index2;
			selected= index2;
		}
		
		do
			index1= myRandom.nextInt(population.individuals.length);
		while (index1== selected);
		
		do{
			index2= myRandom.nextInt(population.individuals.length);
		}while(index2==index1);

		if(population.individuals[index1].totalPenalty < population.individuals[index2].totalPenalty)
			indivs[1]= index1;
		else
			indivs[1]= index2;
		
		return indivs;
	}

}

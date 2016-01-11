package selectors;

import util.RandomNumberGenerator;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class TournamentSelector extends SelectorBase {

	// This selector uses the totalPenalty Value of each individual
	public Individual selectIndividual(Population population) {

		int[] tournament= new int[PopulationParameters.selectionTournamentSize];
		int indIndex;
		for (int c=0; c< PopulationParameters.selectionTournamentSize; c++){
			indIndex= RandomNumberGenerator.getRandomInt(population.individuals.length);
			tournament[c]= indIndex;
		} // end c for
		
		// Now find the best and return it.
		int bestIndividual=tournament[0];
		int bestValue=population.individuals[bestIndividual].totalPenalty;
		
		for (int c=1; c< PopulationParameters.selectionTournamentSize; c++){
			if (population.individuals[tournament[c]].totalPenalty < bestValue){
				bestIndividual= tournament[c];
				bestValue= population.individuals[tournament[c]].totalPenalty;
			}
		} // end c for
		
//		System.out.println("Selected Individual No: " + bestIndividual);
		return population.individuals[bestIndividual].clone();
	}

}

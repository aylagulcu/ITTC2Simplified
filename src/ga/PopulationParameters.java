package ga;

public class PopulationParameters {
	
	public static int populationSize= 40;
	
	public static int tournamentSize= (int)data.parameters.numEvents* 100/100;
	// initialization tournament size: 20% of all the events is used.
	// size of 10% to 20% is recommended

	public static int maxIteration= 30;
	public static int maxIterWithOutImprovement= 50;
	public static int currentIteration= 0;
	
//	public static int selectionTournamentSize= 5; // Selection of the individuals to reproduce
	
	public static double crossoverRate= 0.6;
	
	public static double mutationRate= 0.01; 
	
	public static double eventMutRate= 0.4; // Mutation probability of an event in the selected individual
	
}

package ga;

public class Population {

	public Individual[] individuals; // Clonned data.
	
	public Individual bestPIndividual;
	public int bestPIndIndex;
	public int worstPIndex;
	
	public int worstPValue;
	public double avgPValue;
	
	public Individual bestRIndividual;
	public int bestRIndIndex;
	public int worstRIndex;
	public Individual worstRIndividual;

	public float[][] indIndDiff; // Clonned data.
	public float avgDiff;

	public Population() {
		individuals= new Individual[PopulationParameters.populationSize];
		// population initializers will initialize the individual
		int i=0;
		while(i < individuals.length){
			individuals[i]= new Individual();
			i++;
		}
		
		indIndDiff= new float[PopulationParameters.populationSize][PopulationParameters.populationSize];
		for (int j= 0; j< PopulationParameters.populationSize; j++)
			for (int k= 0; k< PopulationParameters.populationSize; k++)
				indIndDiff[j][k]= 0;
	}
	
	public Population(int numIndivs) {
		individuals= new Individual[numIndivs];
		// population initializers will initialize the individual
		int i=0;
		while(i < individuals.length){
			individuals[i]= new Individual();
			i++;
		}
		
		indIndDiff= new float[numIndivs][numIndivs];
		for (int j= 0; j< numIndivs; j++)
			for (int k= 0; k< numIndivs; k++)
				indIndDiff[j][k]= 0;
	}
	
	public Population Clone(){
		Population pop= new Population();
		for (int i=0; i< pop.individuals.length; i++){
			pop.individuals[i]= this.individuals[i].clone();
		}
		
		pop.indIndDiff= new float[pop.individuals.length][pop.individuals.length];
		for (int j= 0; j< pop.individuals.length; j++)
			for (int k= 0; k< pop.individuals.length; k++)
				pop.indIndDiff[j][k]= this.indIndDiff[j][k];
		
		return pop;
	}
	
}

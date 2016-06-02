package vnSearchers;

import ga.GlobalVars;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

import java.util.List;
import util.RandomNumberGenerator;
import constraints.ConstraintBase;

public class SAManager {
	
	public List<ConstraintBase> constraints;
	
	public SABase penaltyImprover;
	public SABase robustnessImprover;
	public SABase penaltyRobustnessImprover;
	
	public MicroSAforP MicroSAP;
	public MicroSAforRobustness MicroSAR;
	
	public int SAPrunCount, SARrunCount, SAPRrunCount;
	
	public int iterPApplied= 0; // iteration at which penalty improver is applied
	public int iterRApplied= 0;
	
	public int nbrIndividuals= 3;
	 
	int[] individualsSorted= new int[PopulationParameters.populationSize];
	double[] fitnessValues= new double[PopulationParameters.populationSize];
	double totalFitness=0; 
	
	public SAManager(List<ConstraintBase> constr) {
		this.constraints=constr;
			
		penaltyImprover= new SAforP(constraints);
		robustnessImprover= new SAforRobustness(constraints);
		penaltyRobustnessImprover= new SAforPR(constraints);
		
		SAPrunCount= 0; SARrunCount= 0; SAPRrunCount= 0;
		
		MicroSAP= new MicroSAforP(this.constraints);
		MicroSAR= new MicroSAforRobustness(this.constraints); 
	}

	public Individual applySA(Population pop){
	
		// Select P or R or PR:
		Individual ind;
		double rnd= RandomNumberGenerator.getRandomDouble(); // [0,1)
		int rndIndIndex= RandomNumberGenerator.myRandom.nextInt(pop.individuals.length);
		if (rnd <= 0.5){		
			ind= penaltyImprover.applySA(pop.individuals[rndIndIndex]);
			SAPrunCount++;
		} // end if
		else{
			ind= robustnessImprover.applySA(pop.individuals[rndIndIndex]);
			SARrunCount++;
		}

		GlobalVars.runDetails.add("SAP Count: "+ SAPrunCount+ "\tSAR Count: "+ SARrunCount+ "\tSAPR Count: "+ SAPRrunCount);
		System.out.println("SAP Count: "+ SAPrunCount+ "\tSAR Count: "+ SARrunCount+ "\tSAPR Count: "+ SAPRrunCount);
		return ind;
		
	}

}


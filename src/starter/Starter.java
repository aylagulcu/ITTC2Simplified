package starter;

import java.util.Random;

import util.FileOperations;
import util.RandomNumberGenerator;
import ga.GAManager;
import data.dataLoader;
import data.instanceDetails;

public class Starter {

	public static void main(String[] args) throws Exception {
		
		dataLoader.loadData();
		instanceDetails.computeInstanceDetails();
		FileOperations.clearStatsFiles();
		
		Random r= new Random(123456); // the same random object at each run
		// Run genetic algorithm
		int nbrOfRuns= 1;
		for (int i=0; i<nbrOfRuns; i++){	
			int seed= r.nextInt();
			RandomNumberGenerator.myRandom= new Random(seed);
			System.out.println("Run: "+ i+ " and Seed of the Random Object: "+ seed); 
			new GAManager().runGA();
		
		} // end i for
		System.out.println(nbrOfRuns+ " Runs have been completed");
		
		
	}

}

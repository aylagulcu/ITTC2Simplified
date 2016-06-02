package starter;

import java.util.ArrayList;
import java.util.Random;

import util.FileOperations;
import util.RandomNumberGenerator;
import ga.GAManager;
import ga.GlobalVars;
import data.dataLoader;
import data.instanceDetails;

public class Starter {

	
	public static void main(String[] args) throws Exception {
		
		dataLoader.loadData();
		instanceDetails.computeInstanceDetails();
		FileOperations.clearAllFiles(); // except FinalPopulation.txt
		
		
//		// Random seeds: 
//		int[] seedArray= { 1306062569,	-149496183,	-1931382557,	343731325,	-740358605,
//				-1297485126,	1514741374,	-907110007,	1762039,	763759587,	96446163,
//				1470184035,	810388213,	995042188,	-1322033668,	-1642006699,	-1863972325,
//				-716427447,	1330185310,	-1193031224,	-633815730,	1020137003,	1098193166,
//				-1152948011,	1536820585,	840128889,	-496144785,	-307696868,	1094887045,
//				-1362359136 	};
// 		
		
		Random r= new Random(); // the same random object at each run
		// Run genetic algorithm
		int nbrOfRuns= 30;
		
		for (int i=1; i<=nbrOfRuns; i++){
			
			GlobalVars.runCount= i;
			GlobalVars.runDetails= new ArrayList<String>();
			
			int seed= r.nextInt();
			RandomNumberGenerator.myRandom= new Random(seed);
			
			System.out.println("Run: "+ i+ " and Seed of the Random Object: "+ seed); 
			
			GlobalVars.runDetails.add("Run: "+ i+ " and Seed of the Random Object: "+ seed);
			
			new GAManager().runGA();
		} // end i for
		System.out.println(nbrOfRuns+ " Runs have been completed");
		
		
		
	}
	
}

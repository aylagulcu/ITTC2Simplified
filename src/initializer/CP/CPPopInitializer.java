
package initializer.CP;

import ga.Population;
import ga.PopulationParameters;
import initializer.iPopulationInitializer;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import constraints.ConstraintBase;
import orderingMethods.orderingManager;
import util.RandomNumberGenerator;

public class CPPopInitializer implements iPopulationInitializer{
	private Population population;
	private orderingManager oManager;
	private List<ConstraintBase> initializationconstraints;
			
	public CPPopInitializer(List<ConstraintBase> constraints) {
		this.initializationconstraints= constraints;
	}

	public void initialize(Population popObject) throws IOException{
		population= popObject; // now, the two pops point to the same object
		
		oManager= new orderingManager();
		CPIndInitializer indInitializer;
		int indCounter= 0;
		while (indCounter < PopulationParameters.populationSize){
			indInitializer= new CPIndInitializer(this.initializationconstraints);
			indInitializer.initializeIndividual(population.individuals[indCounter], new Random(RandomNumberGenerator.getNewSeed()), oManager);
			
			indCounter++;
			if (PopulationParameters.tournamentSize> (float)data.parameters.numEvents* 20/100)
				PopulationParameters.tournamentSize-= PopulationParameters.tournamentSize*0.1; // (decrement by 10 percent)
		} // end while
	} // end method
	

	
	
	
	
//	public class MyThread implements Runnable {		
//		int individualIndex;
//		public Random randomNumber;
//		
//		public MyThread(int index, int seed) {
//			this.individualIndex= index;
//			this.randomNumber= new Random(seed);
//		}
//		public void run() {
//			initializeIndividual(population.individuals[this.individualIndex], this.randomNumber, oManager);
//		}
//	}
//
//	// Multi-threaded:
//	public void initialize(Population popObject) {
//		population= popObject; // now, the two pops point to the same object
//		fillSetOfEventTimesOriginal();		
//		
//		oManager= new orderingManager();
//		population= popObject; // now, the two pops point to the same object
//		int randSeed;
//		List<Thread> myThreads= new ArrayList<Thread>();
//		for (int i=0; i< PopulationParameters.populationSize; i++){
//			randSeed= RandomNumberGenerator.getNewSeed();
//			Thread t= new Thread(new MyThread(i, randSeed));
//			myThreads.add(t);
//		}
//		for (int t=0; t< myThreads.size(); t++)
//			myThreads.get(t).start();
//		// Now, wait for all threads to finish!
//		for( int i=0; i< myThreads.size(); i++){
//			myThreads.get(i).join();
//		}
//	} // end method
	


	
	
}
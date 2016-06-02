
package initializer;

import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

import java.util.Random;

public class PopulationInitializer implements iPopulationInitializer {

	private Population population;

	// Single Thread Version!
	public void initialize(Population popObject){
		// Important: CREATES COMPLETE SOLUTIONS! - but may be infeasible
		population= popObject; // now, the two pops point to the same object
		for (int indiv=0; indiv< PopulationParameters.populationSize; indiv++){
			Random randomNumber= new Random(indiv);
			initializeIndividual(population.individuals[indiv], randomNumber);
		}	
	} // end method
	
	private void initializeIndividual(Individual ind, Random threadRandom){	
//		
//		CourseInitializer courseInitializer= new CourseInitializer(); // FOR MULTIPLE THREADS!
//		tournamentManager tManager= new tournamentManager();
//		orderingManager oManager= new orderingManager();
//		
//		List<Integer> crsToInitialize= new ArrayList<Integer>();
//		for (int c=0; c< parameters.numCourses; c++)
//			crsToInitialize.add(c, c);
//		List<Integer> tournamentCourses= new ArrayList<Integer>();
//		int courseId;
//		while (crsToInitialize.size()>0){ // While there are still courses to initialize
//			tournamentCourses.clear();
//			tournamentCourses= tManager.createTournamentCourses(crsToInitialize, threadRandom); // creates a tournament of courses
//			courseId= oManager.selectOrdering(ind, tournamentCourses, threadRandom).selectCourse();
//			crsToInitialize.remove((Object)courseId);
//			boolean result= courseInitializer.initializeCourse(ind, courseId, threadRandom);
//			if (result==false)
//				System.out.println("There is a serious error during the initialization of a new course!!!");
//		} // end while
	}
		
	
	public class MyThread implements Runnable {		
		int individualIndex;
		public Random randomNumber;
		
		public MyThread(int index, int seed) {
			this.individualIndex= index;
			this.randomNumber= new Random(seed);
		}
		public void run() {
			initializeIndividual(population.individuals[this.individualIndex], this.randomNumber);
		}
	}
	
	// Multi-threaded:
//	public void initialize(Population popObject) throws InterruptedException{
//		population= popObject; // now, the two pops point to the same object
//		Random randomNumber= new Random(12345);
//		int randSeed;
//		List<Thread> myThreads= new ArrayList<Thread>();
//		for (int i=0; i< PopulationParameters.populationSize; i++){
//			randSeed= randomNumber.nextInt();
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
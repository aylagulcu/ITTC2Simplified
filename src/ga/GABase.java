package ga;

import initializer.iPopulationInitializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import diversityEvaluators.DiversityManager;
import evaluators.CrowdingDistanceEvaluator;
import evaluators.PenaltyEvaluator;
import evaluators.RankEvaluator;
import constraints.*;

import java.io.IOException;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import mutators.MutationManager;
import robustnessEvaluators.RobustnessManager;
import selectors.SelectorBase;
import vnSearchers.SAManager;
import crosser.crossoverManager;

public abstract class GABase {
	public List<ConstraintBase> constraints;

	protected iPopulationInitializer popInitializer; 
	protected PenaltyEvaluator penaltyEvaluator;
	protected SelectorBase selector;
	protected crossoverManager cxManager;
	protected SAManager mySAManager;
	protected MutationManager mutManager;
	
	public RobustnessManager rManager;
	public RobustnessManager secondRManager;
	public RankEvaluator rankEvaluator;
	public CrowdingDistanceEvaluator crowdEvaluator;
	public DiversityManager divManager;
	
	protected Population pop;
	protected Population oldPop;
	protected Population mergedPop;
	
	protected Individual bestPindSoFar;
	protected Individual bestRindSoFar;
		
//	public boolean bestUpdated= false;

	public abstract void run() throws IOException, InterruptedException, RowsExceededException, WriteException;
	
	protected void updatePopulationStats(Population pop) {
		// Attention: It is assumed that the Penalty and the Robustness values are up to date
		updatePopulationFitnessStats(pop); // population best - avg - worst is found
		updatePopulationRobustnessStats(pop); // population best - worst is found
		
		// Population Fitness and Robustness values should be up to date
		// to correctly compute the following:
		this.rankEvaluator.Evaluate(pop);
		// Crowding distance should be evaluated after rank evaluation! 
		this.crowdEvaluator.Evaluate(pop);
		
		this.divManager.evaluatePopDiversity(pop);	
	}

	protected void evaluatePopulation(Population pop) {
		// Each individual in the population is re-evaluated:
		this.penaltyEvaluator.Evaluate(pop);  // Simple Penalty Evaluator
		this.rManager.evalPopRobustness(pop);
	}
	
	protected Population mergePopulations(Population oldPop, Population newPop) {
		// Assumption: individuals' Penalty & Robustness values should be up to date (both in oldPop and newPop)
		int size= oldPop.individuals.length + newPop.individuals.length;
		Population mergedPop= new Population(size);
		for (int i=0; i< oldPop.individuals.length; i++)
			mergedPop.individuals[i]= oldPop.individuals[i].clone();
		for (int i=newPop.individuals.length; i< size; i++)
			mergedPop.individuals[i]= newPop.individuals[i-newPop.individuals.length].clone();
		
		rankEvaluator.Evaluate(mergedPop); // assigns each individual a rank.
		crowdEvaluator.Evaluate(mergedPop); // assigns each individual a crowding distance.
		
		// sort individuals first acc to their ranks, and then acc to their crowd distance values:
		// sort individuals in the first pareto front acc to distance
		int improvedPopSize= newPop.individuals.length;
		Population improvedPop= new Population(improvedPopSize);
		
		ArrayList<Individual> frontIndividuals= new ArrayList<Individual>();
		int rankIndiv= 1;
		int index= 0;
		while (index< improvedPopSize){
			frontIndividuals.clear();
			for (int i= 0; i< size; i++){
				if (mergedPop.individuals[i].rank== rankIndiv)
					frontIndividuals.add(mergedPop.individuals[i]);
			} // end i for
			// sort the individuals in this rank acc to their distance values only if
			// there is not enough room to accomodate all of them
			if (index + frontIndividuals.size()<= improvedPopSize){ 
				for (Individual ind: frontIndividuals){
					improvedPop.individuals[index]= ind;
					index+= 1;
				} // end for
			} // end if
			else{
				int bestInd= 0;
				while (index< improvedPopSize){
					bestInd= 0;
					for (int i= 1; i< frontIndividuals.size(); i++){
						if (frontIndividuals.get(i).crowdDistance > frontIndividuals.get(bestInd).crowdDistance)
							bestInd= i;
					} // end i for
					improvedPop.individuals[index]= frontIndividuals.get(bestInd);
					index+= 1;
					frontIndividuals.remove(bestInd);
				}; // end while
			} // end else

			rankIndiv +=1;
		}; // end while
		
		
//		System.out.println();
//		System.out.println("********************************************");
//		
//		System.out.println("improvedPop:");
//		System.out.println("Individual"+"\t"+"Rank"+"\t"+"CrowdDistance"+"\t"+"Penalty"+"\t"+"Robustness");
//		for (int i=0; i< improvedPop.individuals.length; i++)
//			System.out.println(i+"\t"+improvedPop.individuals[i].rank+"\t"+improvedPop.individuals[i].crowdDistance+"\t"+improvedPop.individuals[i].totalPenalty+"\t"+improvedPop.individuals[i].robustValueMin);
		
		return improvedPop;
	}


	private void updatePopulationFitnessStats(Population pop2) {
		pop2.bestPIndividual= getBestPIndividual(pop2);	
		pop2.worstPValue= getWorstPValue(pop2);
		pop2.avgPValue= getAvgPenalty(pop2);
	}
	
	private Individual getBestPIndividual(Population pop2) {
		int bestIndex= 0;
		for (int i=1; i < pop2.individuals.length; i++){
			if(pop2.individuals[i].totalPenalty < pop2.individuals[bestIndex].totalPenalty)		
				bestIndex= i;
		}
		pop2.bestPIndIndex= bestIndex;
		return pop2.individuals[bestIndex].clone();
	}
	
	private int getWorstPValue(Population pop2) {
		int worstIndex= 0;
		int worstP= pop2.individuals[0].totalPenalty;
		for (int i=1; i < pop2.individuals.length; i++){
			if(pop2.individuals[i].totalPenalty > worstP){		
				worstP= pop2.individuals[i].totalPenalty;
				worstIndex= i;
			}
		}
		pop2.worstPIndex= worstIndex;
		return worstP;	
	}

	private double getAvgPenalty(Population pop2) {
		double totalPenalty=0;
		for (int i=0; i < pop2.individuals.length; i++)
				totalPenalty+= pop2.individuals[i].totalPenalty;
		return (totalPenalty/pop2.individuals.length);
	}

	private void updatePopulationRobustnessStats(Population pop2) {
		this.pop.bestRIndividual= getBestRIndividual(pop2);
		this.pop.worstRIndividual= getWorstRIndividual(pop2);
	}

	// Minimization
	private Individual getBestRIndividual(Population pop2) {
		int bestIndex= 0;
		for (int i= 1; i< pop2.individuals.length; i++){
			if(pop2.individuals[i].robustValueMin < pop2.individuals[bestIndex].robustValueMin){
				bestIndex= i;
			}
		} // end i 
		pop2.bestRIndIndex= bestIndex;
		return pop2.individuals[bestIndex];
	}
	
	private Individual getWorstRIndividual(Population pop2) {
		int worstIndex= 0;
		for (int i= 1; i< pop2.individuals.length; i++){
			if(pop2.individuals[i].robustValueMin > pop2.individuals[worstIndex].robustValueMin){
				worstIndex= i;
			}
		} // end i 
		pop2.worstRIndex= worstIndex;
		return pop2.individuals[worstIndex];
	}
	
	protected void writeIterationStats(Population pop2) {
		int[] stats= {pop2.bestPIndividual.totalPenalty, (int) pop2.avgPValue, pop2.worstPValue};
		GlobalVars.popPenaltyStats.add(stats);
		
		int[] penaltyValues= new int[pop2.individuals.length];
		for (int i=0; i< pop2.individuals.length; i++)
			penaltyValues[i]= pop2.individuals[i].totalPenalty;
		GlobalVars.popPenaltyValues.add(penaltyValues);
		
		GlobalVars.popRobustnessStats.add(pop2.bestRIndividual.robustValueMin);
		
		double[] statsR= new double[pop2.individuals.length];
		for (int i=0; i< pop2.individuals.length; i++)
			statsR[i]= pop2.individuals[i].robustValueMin;
		GlobalVars.popRobustnessValues.add(statsR);
		
//		computeSecondRobustnessAndRecord(pop2);

		GlobalVars.popAvgDiversity.add(pop2.avgDiff);
	}
	
	private void computeSecondRobustnessAndRecord(Population pop2) {
//		float[] statsRSecond= new float[pop2.individuals.length];
//		float originalVal;
//		for (int i=0; i< pop2.individuals.length; i++){
//			originalVal= pop2.individuals[i].robustValueMin;
//			secondRManager.evalIndivRobustness(pop2.individuals[i]);
//			statsRSecond[i]= pop2.individuals[i].robustValueMin;
//			pop2.individuals[i].robustValueMin= originalVal;
//		} // end i for
//
//		GlobalVars.popSecondRobustnessValues.add(statsRSecond);
	}
	
	// Start time is sent as parameter; elapsed time is computed and sent back as the return value
	protected int getElapsedTime(Date start) {
		return (int) ((new Date().getTime()- start.getTime())/1000); 
	}
	
	protected boolean isTerminate() {
		if (PopulationParameters.currentIteration>= PopulationParameters.maxIteration)
			return true;
		return false;
	}

	public Population generate(Population population) throws IOException, InterruptedException {
		// Evaluate individuals after this method
		Population newPopulation = new Population(); // Individuals are automatically initialized!
		int counter= 0; // Important!: Start this counter from 0 when elitizm is not applied

		Individual[] selectedIndivs;
		Individual[] offSprings;
		
		while(counter < PopulationParameters.populationSize){
			selectedIndivs= this.selector.selectIndividual(population); // returns copies!
			offSprings= this.cxManager.crossIndividuals(selectedIndivs[0], selectedIndivs[1], population.avgPValue);
	
			for (Individual child: offSprings){
//				this.mutManager.mutateIndividual(child);
				newPopulation.individuals[counter]= child;
				counter++;
				if (counter>= PopulationParameters.populationSize)
					break;
			} // end for each
		} // end while
	
		return newPopulation;
	}
	
	protected void writeToFile() throws IOException, RowsExceededException, WriteException {
		
		util.FileOperations.writePopPenaltyStats(GlobalVars.popPenaltyStats); // At each iteration: best + avg + worst
		util.FileOperations.writePopPenaltyValues(GlobalVars.popPenaltyValues);
		
		util.FileOperations.writePopAvgDiversity(GlobalVars.popAvgDiversity);
	
		util.FileOperations.writePopRobustnessStats(GlobalVars.popRobustnessStats);
		util.FileOperations.writePopRobustnessValues(GlobalVars.popRobustnessValues);
		
		util.FileOperations.writePopSecondRobustnessValues(GlobalVars.popSecondRobustnessValues);
		
		util.FileOperations.writeFinalPopToTxt(this.pop.individuals);
		util.FileOperations.writeBestIndividualFoundToTxt(this.pop.bestPIndividual);
		util.FileOperations.writeIndividualConstraintValuesToFile(this.pop.individuals);
		
		util.FileOperations.writeDiversityRelationToFile(this.pop.indIndDiff);
		
		util.FileOperations.printFinalSolutionToText(this.pop.bestPIndividual); // Best solution for online check
		util.FileOperations.printFinalSolutionToSheet(this.pop.bestPIndividual); // Best solution for online check
		
		util.FileOperations.printParetoToFile(this.pop);
		
		//util.FileOperations.printVNSInfoToFile(GlobalVars.LSStats);
	}
	


	
	
	
	
	
	
	
	
	
	
	
	


	
//	public List<Individual> selectIndividualsToVNS(Population currentPop) {
//		List<Individual> indivs= new ArrayList<Individual>();
//		
//		List<Integer> temp= new ArrayList<Integer>();
//		for (int i=0; i< currentPop.individuals.length; i++)
//			temp.add(i);
//		
//		int bestIndiv; int bestIndivP;
//		
//		for (int constrCounter=0; constrCounter< constraints.size(); constrCounter++) {
//			bestIndiv= temp.get(0); bestIndivP= currentPop.individuals[bestIndiv].constraintPenalties[constrCounter];
//			// Find the best individual in terms of this constraint:
//			for ( int index=1; index< temp.size(); index++) {
//				if (currentPop.individuals[index].constraintPenalties[constrCounter] < bestIndivP){
//					bestIndiv= index;
//					bestIndivP= currentPop.individuals[bestIndiv].constraintPenalties[constrCounter];
//				} // end if
//			} // end ind for
//			
//			indivs.add(currentPop.individuals[bestIndiv]);
//			temp.remove((Integer)bestIndiv); // should not be selected again!
//		} // end constraints for
//		
//		indivs.add(this.pop.individuals[this.pop.bestIndIndex]);
//		return indivs;
//	}
	
//	public List<Individual> selectRandomIndividualsToVNS(Population currentPop) {
//		Random rand= new Random(RandomNumberGenerator.getNewSeed());
//		
//		List<Individual> indivs= new ArrayList<Individual>();
//		
//		List<Integer> temp= new ArrayList<Integer>();
//		for (int i=0; i< currentPop.individuals.length; i++)
//			temp.add(i);
//		int selected;
//		for (int constrCounter=0; constrCounter< constraints.size(); constrCounter++) {
//			selected= temp.get(rand.nextInt(temp.size()));
//			temp.remove((Integer)selected); // should not be selected again!	
//			indivs.add(currentPop.individuals[selected]);
//		} // end constraints for
//		
//		indivs.add(this.pop.bestIndividual);
//		return indivs;
//	}
	

	
	
	
////	Multi-thread:
//	public Population generateMulti(Population population) throws IOException, InterruptedException {
//		generatedPopulation = new Population(); // Individuals are automatically initialized!
//		// repeat the following steps until pop size new individuals are created:
//		generatedPopulation.individuals[0]= bestIndividualSoFar; // Elitism is applied.
//		int counter= 1; // Important!: Start this counter from 0 when elitism is not applied
//		
//		Random randomNumber= new Random(12345);
//		int randSeed;
//		List<Thread> myGenThreads= new ArrayList<Thread>();
//		while(counter<PopulationParameters.populationSize){
//			Individual selectedInd1= this.selector.selectIndividual(population);
//			Individual selectedInd2= this.selector.selectIndividual(population);
//			randSeed= randomNumber.nextInt();
//			Thread t= new Thread(new MyGeneratorThread(counter, randSeed, selectedInd1, selectedInd2));
//			myGenThreads.add(t);
//			counter++;
//		} // end while
//		
//		for (int t=0; t< myGenThreads.size(); t++)
//			myGenThreads.get(t).start();
//		// Now, wait for all threads to finish!
//		for( int i=0; i< myGenThreads.size(); i++)
//			myGenThreads.get(i).join();
//		
//		// new population may be of different size!
////		newPopulation= removePoorsAndShrink(newPopulation);
//		return generatedPopulation;
//}
	
//	// Each thread is going to select two individuals and cross them and apply vns on them and return them
//	public class MyGeneratorThread implements Runnable {		
//		VNSManager myVNSManager= new VNSManager(constraints);
//		int indIndex;
//		Random randomNumber;
//		Individual ind1;
//		Individual ind2;
//		
//		public MyGeneratorThread(int index, int seed, Individual i1, Individual i2) {
//			this.indIndex= index;
//			this.randomNumber= new Random(seed);
//			this.ind1= i1;
//			this.ind2= i2;
//		}
//		public void run() {
//			Individual[] offSprings = null;
//			
//			try {
//				offSprings = cxManager.crossIndividuals(ind1, ind2);
//				
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//			// Apply VNS for both of the individuals
//			try {
//				myVNSManager.applyVNS(iterationCount, offSprings[0]);
//				myVNSManager.applyVNS(iterationCount, offSprings[1]);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
////			util.FileOperations.printVNStrajectory(myVNSManager.vnsDetails);
//
//			if (indIndex < PopulationParameters.populationSize)
//				generatedPopulation.individuals[indIndex]= offSprings[0];
//			if (indIndex+1 <  PopulationParameters.populationSize)
//				generatedPopulation.individuals[indIndex+1]= offSprings[1];
//		}
//	}

//	private void updateSAParameters(int iter) {
//	if (GlobalVars.iterCounterWithoutImprovement == 0){
//		GlobalVars.saAccept= false;
//		GlobalVars.currentTemp= GlobalVars.initialTemp;
//	}
//	else {
//		GlobalVars.saAccept= true;
//	} // end else
//	GlobalVars.currentTemp= GlobalVars.currentTemp*(1+GlobalVars.iterCounterWithoutImprovement);
//}
	

	
}
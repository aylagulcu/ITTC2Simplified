package ga;

import initializer.CP.CPPopInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import mutators.MutationManager;
import robustnessEvaluators.DisruptEventSC;
import robustnessEvaluators.RobustnessEvaluatorBase;
import robustnessEvaluators.RobustnessManager;
import selectors.RandomSelector;
import selectors.RouletteWheelSelector;
import vnSearchers.VNSManager;
import constraints.ConstraintBase;
import crosser.crossoverManager;
import data.parameters;
import diversityEvaluators.DiversityManager;
import evaluators.CrowdingDistanceEvaluator;
import evaluators.PenaltyEvaluator;
import evaluators.RankEvaluator;

public class Deme extends GABase {

	public Deme(ArrayList<ConstraintBase> listOfConst) {
		this.constraints= listOfConst;
		this.pop= new Population();
		this.popInitializer= new CPPopInitializer(this.constraints);
		this.penaltyEvaluator= new PenaltyEvaluator(this.constraints);
		this.selector= new RandomSelector();
		
		this.cxManager = new crossoverManager(this.constraints);
		this.vnsManager= new VNSManager(constraints);
		this.mutManager= new MutationManager(this.constraints);
		this.rManager= new RobustnessManager(this.constraints);
		this.rankEvaluator= new RankEvaluator();
		this.crowdEvaluator= new CrowdingDistanceEvaluator();
		this.divManager= new DiversityManager();
		
		this.secondRManager= new RobustnessManager(this.constraints);
		RobustnessEvaluatorBase R= new DisruptEventSC(this.secondRManager);
		this.secondRManager.setRobustnessMeasure(R);
	}

	Date start;
	
	public void run() throws IOException, InterruptedException, RowsExceededException, WriteException  {				
		GlobalVars.initialize();

		start = new Date();
//		popInitializer.initialize(this.pop); 
//		util.FileOperations.writeInitialPopToTxt(this.pop.individuals);
		util.FileOperations.getInitialSolutionFromTxt(this.pop.individuals);
//		util.FileOperations.getInitialSolutionFromFinalPopTxt(this.pop.individuals);
		System.out.println("Initialization took "+ getElapsedTime(start) + " seconds");	
			
		// The following matrices should always be updated!!!
		for (int i=0; i< this.pop.individuals.length; i++){
			this.pop.individuals[i].createMatrix(); 
			this.pop.individuals[i].createTimeCurMatrix();
		} // end i for

		evaluatePopulation(this.pop); // penalty and robustness values are re-evaluated
		updatePopulationStats(this.pop); // Population-related computations: Find best; compute rank and crowding distance, and diversity
		bestPindSoFar= this.pop.bestPIndividual.clone();
		bestRindSoFar= this.pop.bestRIndividual.clone();
		
		writeIterationStats(this.pop); // write pop statistics to file
		System.out.println("Initial bestP: "+ this.pop.bestPIndividual.totalPenalty);

		PopulationParameters.currentIteration=0;

		while (true){
			if (!isTerminate()){ 
				Date dateGenStart = new Date();	
				this.oldPop= this.pop.Clone();

				this.pop= this.generate(this.pop); // penalty values should already be up to date
				updatePopulationStats(this.pop); // Population-related computations: Find best; compute rank and crowding distance, and diversity

//				if (PopulationParameters.currentIteration % 10 ==0){
//					this.vnsManager.applyVNS(PopulationParameters.currentIteration, this.pop); // VNS manager will decide which individuals to apply VNS and which LocalSearch to apply
//					updatePopulationStats(this.pop); // Population-related computations: Find best; compute rank and crowding distance, and diversity
//				}
				
//				int p= this.pop.individuals[0].totalPenalty;
//				this.penaltyEvaluator.evaluateIndividual(pop.individuals[0]);
//				assert p== pop.individuals[0].totalPenalty;
				
				// now it is time to merge two populations both of which have the individuals' values up to date
				this.pop= mergePopulations(this.oldPop, this.pop);
			
				updatePopulationStats(this.pop); // Population-related computations: Find best; compute rank and crowding distance, and diversity
				
//				if (GlobalVars.iterCounterWithNoPenaltyImprovement >= 5){
//					this.vnsManager.applyVNS(PopulationParameters.currentIteration, this.pop); // VNS manager will decide which individuals to apply VNS and which LocalSearch to apply
//					updatePopulationStats(this.pop); // Population-related computations: Find best; compute rank and crowding distance, and diversity
//				} // end if
				
				writeIterationStats(this.pop); // write the pop statistics to file	
				Date dateGenEnd = new Date(); 
				float diff= (dateGenEnd.getTime()- dateGenStart.getTime())/1000; 
				System.out.println("Iteration "+ PopulationParameters.currentIteration+ " took "+ diff + 
						" seconds. Best P Value: "+ this.pop.bestPIndividual.totalPenalty+ "\t"+ "Average penalty: "+ this.pop.avgPValue);

				updateBestIndividualSoFar(this.pop);
			} // end if isTerminate
			else{ // Termination criteria occurs		
				System.out.println();
				System.out.println("********************************************");
				System.out.println("Index"+"\t"+"Rank"+"\t"+"CrowdDist"+"\t\t"+"Penalty"+"\t"+"Robustness");
				for (int i=0; i< this.pop.individuals.length; i++)
					System.out.println(i+"\t"+this.pop.individuals[i].rank+"\t"+this.pop.individuals[i].crowdDistance+"\t\t"+this.pop.individuals[i].totalPenalty+"\t"+this.pop.individuals[i].robustValueMin);

				System.out.println("Best Penalty Value: "+ this.pop.bestPIndividual.totalPenalty);
				System.out.println("Best Robustness Value: " + this.pop.bestRIndividual.robustValueMin);
				FinalSolAnalyzer.Analyze(constraints, this.pop.bestPIndividual);
				writeToFile();
				System.out.println();
				System.out.println("********************************************");
				System.out.println("Constraint violations of the best P individual:");
				for (ConstraintBase c: this.constraints)
					System.out.println(c.getClass().getSimpleName()+" violation: "+ c.Compute(this.pop.bestPIndividual));
				System.out.println("Best Individual So Far penalty: "+ bestPindSoFar.totalPenalty);

				break;
			} // end else
			PopulationParameters.currentIteration++;	
		} // end while iteration loop	

		System.out.println("Total running time is "+ getElapsedTime(start) + " seconds");
	} // end run	

	private void reInitializePopulation(Population pop) throws IOException {
		if (GlobalVars.iterCounterWithNoPenaltyImprovement> 10){
			System.out.println("Reinitializating...");
			
			Population newPop= new Population();
			popInitializer.initialize(newPop); 
			System.out.println("Initialized");
			// The following matrices should always be updated!!!
			for (int i=0; i< this.pop.individuals.length; i++){
				newPop.individuals[i].createMatrix(); 
				newPop.individuals[i].createTimeCurMatrix();
			} // end i for

			evaluatePopulation(newPop); // penalty and robustness values are re-evaluated
			updatePopulationStats(newPop); // Population-related computations: Find best; compute rank and crowding distance, and diversity
			System.out.println("Best in newpop: "+ newPop.bestPIndividual.totalPenalty);
			
			newPop.individuals[pop.worstPIndex]= bestPindSoFar.clone();
			pop= newPop;
		}
		
	}

	private void updateBestIndividualSoFar(Population p) {

		if (p.bestPIndividual.totalPenalty < bestPindSoFar.totalPenalty){
			bestPindSoFar= p.bestPIndividual.clone();
			GlobalVars.iterCounterWithNoPenaltyImprovement= 0;
		}
		else{
			GlobalVars.iterCounterWithNoPenaltyImprovement+= 1;
		}
		
		if (p.bestRIndividual.robustValueMin < bestRindSoFar.robustValueMin)
			bestRindSoFar= p.bestRIndividual.clone();
		
	}


}


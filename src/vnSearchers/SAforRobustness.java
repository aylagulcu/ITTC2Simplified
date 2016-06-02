package vnSearchers;

import ga.Individual;

import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import robustnessEvaluators.RobustnessManager;
import constraints.ClashConstraint;
import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import evaluators.PenaltyEvaluator;

public class SAforRobustness extends SABase {
	Individual bestIndiv= new Individual();
	
	public double origTotalRobustness;
	public double newTotalRobustness;
	
	double Tinit= 15.25;
	double Tcurrent;
	double Tfinal= 0.1567;
	double coolratio= 0.99;
	double acceptRatio= 0.0364;
	
	double outerLimit= Math.log(Tfinal / Tinit )/ Math.log(coolratio); 
	// total number of iterations= number of outer * number of inner
	double totalAllowedIterations= 1000; // benchmarking result for HP is 216 seconds. 
	// In 1 seconds, 327879 iterations are performed.
	// inner iteration count 
	// steps in the inner loop (loop for each T level)
	double innerLimit= (totalAllowedIterations/ outerLimit);
	
	ArrayList<Integer> penaltyAtEachTemperature= new ArrayList<Integer>();
	
	public SAforRobustness(List<ConstraintBase> constr) {
		super(constr);
		
		this.feasConstraints= new ArrayList<HardConstraint>();
		this.feasConstraints.add((HardConstraint) new InstructorTimeAvailabilityConstraint(100));		
		this.feasConstraints.add((HardConstraint) new ClashConstraint(100));
		
		this.pEvaluator= new PenaltyEvaluator(this.originalConstraints);
		this.rm= new RobustnessManager(this.constraints);
		
		VNSList.clear();
		VNSList.add(new MoveNew(this, 0)); // put Move in the first order!
		VNSList.add(new SwapNew(this, 1));
		VNSList.add(new MoveSwapNew(this, 2));		
	}

	public Individual applySA(Individual indiv){	
//		if (!indiv.isFeasible)
//			return;
//		System.out.println("Outer loop count: +" +outerLimit+ " Inner loop count: "+ innerLimit);
//		System.out.println("Before SAR,  Current penalty: " + indiv.totalPenalty);
		
		// Attention: VNS searcher should return up to date values of penalty and robustness!!!
		this.currentInd= indiv.clone(); // with the same reference. This reference should not be changed!!!
		copyCurrentToBest(); // copy the fields of to current individual to the best individual

		penaltyAtEachTemperature.clear();
	
		boolean result= false;

		int innerCounter= 0;
		int countAccepted= 0;
		Tcurrent= Tinit;
		penaltyAtEachTemperature.add(this.currentInd.totalPenalty);
		int counter= 0;
//		Date startLS = new Date();
		do{
			innerCounter= 0; countAccepted= 0;
			while(continueSearch(innerCounter, (int)innerLimit, countAccepted)){
				this.searcher= selectSearcher();
				result= this.searcher.search();
				counter++;
				if (result){ 
					countAccepted+= 1; 
					updateBestIndiv();
				}
				innerCounter+= 1;
			} // end while
			penaltyAtEachTemperature.add(this.currentInd.totalPenalty);
//			System.out.println("For the current temperature, "+ Tcurrent+",  inner counter and count accepted are: "+ innerCounter+"  and "+ countAccepted);
			Tcurrent*= coolratio;
		}while (Tcurrent >= Tfinal);

		// Complete the above total number of iterations (use the iteration budget):
//		System.out.println("Iterations remaining: "+ (totalAllowedIterations- counter));
		while (counter< totalAllowedIterations){
			this.searcher= selectSearcher();
			result= this.searcher.search();
			counter++;
		}
			
//		Date endLS= new Date();
//		float diff= (endLS.getTime()- startLS.getTime())/1000; // to get time in seconds
//		System.out.println(counter+ "iterations in SA took "+ diff + " seconds.");
//		try {
//			FileOperations.writeToFile(penaltyAtEachTemperature);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	
		updateBestIndiv();
		
//		System.out.println("SA Best Individual penalty:  "+ this.bestIndiv.totalPenalty);
		if (this.bestIndiv.totalPenalty < this.currentInd.totalPenalty){
			bestIndiv.createMatrix();
			bestIndiv.createTimeCurMatrix();
			this.bestIndiv.clone(this.currentInd); // Copy the fields of best to the current
		} // end if
//		System.out.println("After SA Current Individual penalty:  "+ this.currentInd.totalPenalty);
		this.pEvaluator.evaluateIndividual(currentInd);
		
		return this.currentInd;
	}

	private void copyCurrentToBest() {
		for(int i=0;i< this.currentInd.Data.length;i++)
			this.bestIndiv.Data[i]= this.currentInd.Data[i];
		
		this.bestIndiv.isFeasible= this.currentInd.isFeasible;
		this.bestIndiv.totalPenalty= this.currentInd.totalPenalty;
		this.bestIndiv.ClashP= this.currentInd.ClashP;
		this.bestIndiv.ComletenessP= this.currentInd.ComletenessP;
		this.bestIndiv.CurCompP= this.currentInd.CurCompP;
		this.bestIndiv.MinWorkDaysP= this.currentInd.MinWorkDaysP;
		this.bestIndiv.RoomCapP= this.currentInd.RoomCapP;
		this.bestIndiv.RoomStabP= this.currentInd.RoomStabP;
		this.bestIndiv.TimeAvailP= this.currentInd.TimeAvailP;
		this.bestIndiv.RoomUniquenessP= this.currentInd.RoomUniquenessP;
	}

	private void updateBestIndiv() {
		if (this.currentInd.totalPenalty< this.bestIndiv.totalPenalty){
			for(int i=0;i< this.bestIndiv.Data.length;i++)
				this.bestIndiv.Data[i]= this.currentInd.Data[i];
			
			this.bestIndiv.isFeasible= this.currentInd.isFeasible;
			this.bestIndiv.totalPenalty= this.currentInd.totalPenalty;
			
			this.bestIndiv.ClashP= this.currentInd.ClashP;
			this.bestIndiv.ComletenessP= this.currentInd.ComletenessP;
			this.bestIndiv.CurCompP= this.currentInd.CurCompP;
			this.bestIndiv.MinWorkDaysP= this.currentInd.MinWorkDaysP;
			this.bestIndiv.RoomCapP= this.currentInd.RoomCapP;
			this.bestIndiv.RoomStabP= this.currentInd.RoomStabP;
			this.bestIndiv.TimeAvailP= this.currentInd.TimeAvailP;
			this.bestIndiv.RoomUniquenessP= this.currentInd.RoomUniquenessP;
		} // end if			
	}

	private boolean continueSearch(int innerCounter, int limit, int countAccepted) {
		if (innerCounter>= limit)
			return false;
		else if (((double)countAccepted/ limit) >= acceptRatio)
			return false; // do not continue in the same Temperature level
		return true;
	}


	@Override
	public void updateOriginalValue() {
		this.origTotalRobustness= this.currentInd.robustValueMin;
	}
	
	@Override
	public void computeOriginalPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1){
		// no partial computation is available for robustness
	}
	
	@Override
	public void computeNewPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1){
		// Important: The following operation does not change the individual's robustness arrays.
		// It modifies only a single field: robustValueMin		
		this.rm.evalIndivRobustnessForCurrentOp(currentInd, ev1, time2, room2, ev2, time1, room1);
		this.newTotalRobustness= this.currentInd.robustValueMin;
	}

	
	@Override
	public boolean acceptCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		
		if (this.newTotalRobustness <= this.origTotalRobustness){ 
			this.rm.evalIndivRobustnessForCurrentOpUpdateMatrix(currentInd, ev1, time2, room2, ev2, time1, room1);			
			return true;
		}
		else {
			double acceptProbability= Math.exp(- (this.newTotalRobustness - this.origTotalRobustness)/ Tcurrent);
			double rnd= this.myRandom.nextDouble(); // [0,1)
			if (rnd < acceptProbability){
				this.rm.evalIndivRobustnessForCurrentOpUpdateMatrix(currentInd, ev1, time2, room2, ev2, time1, room1);
				return true;
			}
			else{
				this.currentInd.robustValueMin= this.origTotalRobustness;				
				return false; // With false return, vns searcher takes back the changes!
			}
		} // end else	
	}
	
}

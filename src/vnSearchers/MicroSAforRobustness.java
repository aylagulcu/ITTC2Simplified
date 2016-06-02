package vnSearchers;

import ga.Individual;

import java.math.BigDecimal;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import robustnessEvaluators.RobustnessManager;
import constraints.ClashConstraint;
import constraints.ClashSoftConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import data.dataHolder;
import data.parameters;
import evaluators.PenaltyEvaluator;

@SuppressWarnings("unused")
public class MicroSAforRobustness extends SABase {
	Individual bestIndiv= new Individual();
	
	public double origTotalRobustness;
	public double newTotalRobustness;
	
	double Tinit= 0.25;
	double Tcurrent;
	double Tfinal= 0.1567;
	double coolratio= 0.99;
	
	double outerLimit= Math.log(Tfinal / Tinit )/ Math.log(coolratio); 
	// total number of iterations= number of outer * number of inner
//	double totalAllowedIterations= 70821864 * 0.00001; // benchmarking result for HP is 216 seconds. 
	double totalAllowedIterations= 100;
	// In 1 seconds, 327879 iterations are performed.
	// inner iteration count 
	// steps in the inner loop (loop for each T level)
	double innerLimit= (totalAllowedIterations/ outerLimit);
	
	public MicroSAforRobustness(List<ConstraintBase> constr) {

		super(constr);
			
		this.feasConstraints= new ArrayList<HardConstraint>();
		this.feasConstraints.add((HardConstraint) new InstructorTimeAvailabilityConstraint(100));	
		this.feasConstraints.add((HardConstraint) new ClashConstraint(100));
		
		this.pEvaluator= new PenaltyEvaluator(this.originalConstraints);
		this.rm= new RobustnessManager(this.constraints); // here, constraints are originalConstraints are the same
		
		this.searcher= new MoveSwapForMicroR(this);
	}

	public void applySAMicro(Individual indiv){	
		this.rm.evalIndivRobustness(indiv);
		
		// Attention: VNS searcher should return up to date values of penalty and robustness!!!
		this.currentInd= indiv; // with the same reference. This reference should not be changed!!!

		int innerCounter= 0;
		Tcurrent= Tinit;
//		Date startLS = new Date();
		do{
			innerCounter= 0;
			while(continueSearch(innerCounter, (int)innerLimit)){
				this.searcher.search();
				innerCounter+= 1;
			} // end while
			Tcurrent*= coolratio;
		}while (Tcurrent >= Tfinal);

//		Date endLS= new Date();
//		float diff= (endLS.getTime()- startLS.getTime())/1000; // to get time in seconds
//		System.out.printf("Before SA Penalty: %d and After SA Penalty: %d \n", origP , this.currentInd.totalPenalty);
	
		// Individual's penalty values should also be up to date!!!
		this.pEvaluator.evaluateIndividual(currentInd);
//		System.out.println("HC Robustness Current individual feasibility: "+ currentInd.isFeasible);
	}


	private boolean continueSearch(int innerCounter, int limit) {
		if (innerCounter>= limit)
			return false;
		return true;
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

	
	@Override
	public void updateOriginalValue() {
		// Robustness:
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

	

}

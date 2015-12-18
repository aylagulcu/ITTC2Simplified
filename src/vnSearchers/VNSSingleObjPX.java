package vnSearchers;

import ga.GlobalVars;
import ga.Individual;
import ga.PopulationParameters;

import java.util.ArrayList;
import java.util.List;

import constraints.ConstraintBase;
import data.parameters;

public class VNSSingleObjPX extends VNS {
	// Feasibility will always be maintained.
	protected float originalP; // Value of the individual before a VNS move is applied.
	protected float newPValue;
	protected int idleIterForSAstart= 10;
	double Tinit;
	double Tfinal= (double) 0.1567;
	float coolratio= (float) 0.99;
	boolean SAPhase= false;
	boolean HCPhase= false;
	
	public VNSSingleObjPX(List<ConstraintBase> constr) {
		super(constr);

		myEventSelectorRW= new EventSelectorPenaltyRW();
		myCourseSelector= new CourseSelectorPenaltyRW();
		
		VNSList.add(new Move(this, 0)); // All 
		VNSList.add(new Swap(this, 1)); // All
		VNSList.add(new MoveAnotherRoom(this, 2)); // Room Capacity, Room Stability
		VNSList.add(new MoveNeighbour(this, 3)); 
		VNSList.add(new SwapConflicting(this, 4)); // All
		VNSList.add(new MoveForRoomStabilityNew(this, 5)); // Room Stability
		VNSList.add(new MoveForMinWorkDays(this, 6));
		VNSList.add(new SwapRoomsEachTime(this, 7));
		VNSList.add(new SwapTimesEachRoom(this, 8));
		VNSList.add(new MoveForCurriculumCompactness(this, 9));
		VNSList.add(new SwapRandomTwo(this, 10)); // All
		
		
//		VNSList.add(new ImproveCurComp(this, 11)); // What to do here???
				
		GlobalVars.LSStats= new ArrayList<float[]>(); // new float[3+ parameters.numSoftConstTypes]; Operator index + GA iteration + total diff +....
	}

	public void applyVNS(int iterCounter, Individual indiv){
		// Attention: VNS searcher should return up to date values of penalty and robustness!!!
		this.currentInd= indiv; // with the same reference. This reference should not be changed!!!
		assert this.currentInd.isFeasible;
		boolean result= false;
		SAPhase= false; HCPhase= false;
		
//		if ((!GlobalVars.improvedPrevious) && GlobalVars.iterCounterWithNoPenaltyImprovement >= idleIterForSAstart){
			SAPhase= true; HCPhase= false;
			// SA phase:
			int origP; int newP;
			int[] origConstraintValues= new int[parameters.numSoftConstTypes];
			int[] newConstraintValues= new int[parameters.numSoftConstTypes];
			Tinit= computeTinit(this.currentInd.totalPenalty, parameters.numCourses);
			
			// One iteration: each searcher is applied once (selection order: random)
			do{
//				for (int i=0; i< this.VNSList.size(); i++){
					this.searcher= selectSearcher();

					clearFields();
					origP= indiv.totalPenalty;

					result= this.searcher.search();

					newP= indiv.totalPenalty;
//					for (int c=0; c< parameters.numSoftConstTypes; c++)
//						newConstraintValues[c]= indiv.softConstrP[c];
//
//					tempArray= new float[3+ parameters.numSoftConstTypes]; // GA iteration+Operator Index+ Total decrease inP + decrease of each soft constraint
//					tempArray[0]= PopulationParameters.currentIteration;
//					tempArray[1]= searcher.myIndex;
//					tempArray[2]= (origP-newP); // if positive: percentage of decrease in penalty.
//					for (int c=0; c< parameters.numSoftConstTypes; c++)
//						tempArray[3+c]= (origConstraintValues[c]-newConstraintValues[c]);
//					GlobalVars.LSStats.add(tempArray);
//				} // end i for
				Tinit*= coolratio;
				
			}while (Tinit >= Tfinal);
//		} // end if
//		else{
//			// Hill Climbing Phase:
//			SAPhase= false; HCPhase= true;
//			int origP; int newP;
//			int[] origConstraintValues= new int[parameters.numSoftConstTypes];
//			int[] newConstraintValues= new int[parameters.numSoftConstTypes];
//			
//			// a random searcher is selected and is applied as long as it yielded improvement
//			// each VNS searcher is applied in a different order
//			for (int i=0; i< this.VNSList.size(); i++){
//				this.searcher= selectSearcher();
//				int idleLimit= 5; // each searcher is allowed to run for at most this limit number of iterations if it yields no improvement
//				int idleCounter= 0;
//				do{
//					clearFields();
//					origP= indiv.totalPenalty;
//					for (int c=0; c< parameters.numSoftConstTypes; c++)
//						origConstraintValues[c]= indiv.softConstrP[c];
//								
//					result= this.searcher.search();
//					if (result) {
//						idleCounter= 0;
//						GlobalVars.iterCounterWithNoPenaltyImprovement= 0;
//					}
//					else idleCounter+= 1;
//					
//					newP= indiv.totalPenalty;
//					for (int c=0; c< parameters.numSoftConstTypes; c++)
//						newConstraintValues[c]= indiv.softConstrP[c];
//								
//					tempArray= new float[3+ parameters.numSoftConstTypes]; // GA iteration+Operator Index+ Total decrease inP + decrease of each soft constraint
//					tempArray[0]= PopulationParameters.currentIteration;
//					tempArray[1]= searcher.myIndex;
//					tempArray[2]= (origP-newP); // if positive: percentage of decrease in penalty.
//					for (int c=0; c< parameters.numSoftConstTypes; c++)
//						tempArray[3+c]= (origConstraintValues[c]-newConstraintValues[c]);
//					GlobalVars.LSStats.add(tempArray);
//				} while (idleCounter < idleLimit); // also implies: while (result);
//			} // end i for
//		} // end else
		

	}
	
	private double computeTinit(int totalPenalty, int numCourses) {
		double T;
		float courseAvgP= (float)totalPenalty / numCourses;
		double probability= (double) 0.9;
		T= -courseAvgP / Math.log(probability);

		System.out.println("Initial Temperature: "+ T);
		return T;
	}
	

	@Override
	protected void clearFields() {
		this.bestMove= new newMove(); 
		// Assumed that the individual's penalty value is already up to date!
		storeOriginalValues(); // current indiv values are stored to be used for acceptance check mechanism
	}
	
	protected void storeOriginalValues() {
		this.originalP= this.currentInd.totalPenalty;
	}
	
	@Override
	public void restoreOriginalValues() {
		this.currentInd.totalPenalty= (int) this.originalP;		
	}

	@Override
	public void evaluateOptimality(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		// assigns the new penalty value (only acc to opt constr) to the field: newPValue

		float val= 0;
		float p= 0;
		for (ConstraintBase c: this.optConstraints){
			val= c.Compute(this.currentInd.Data);
			p+= val;
		}
		this.newPValue= p;

//		System.out.print("New optimality Event: "+ ev1+" cur penalty: "+ new CurriculumCompactnessConstraint().computeSingleEvent(this.currentInd.Data, ev1));
//		if (ev2!= parameters.UNUSED_EVENT)
//			System.out.println(" The other Event: "+ ev2+" cur penalty: "+ new CurriculumCompactnessConstraint().computeSingleEvent(this.currentInd.Data, ev2));
//
//		System.out.println();
	}

	@Override
	public boolean checkUpdateBest(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.evaluateOptimality(ev1, time2, room2, ev2, time1, room1); // refreshes this.newPValue
		if (this.newPValue <= this.bestMove.P)
			return true;
		return false;
	}

	@Override
	public void updateBest(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.bestMove= new newMove(this.newPValue, 99999, ev1, time2, room2, ev2, time1, room1 );
	}

	@Override
	public void updateBest(int ev1, int time2, int room2) {
		this.bestMove= new newMove(this.newPValue, 99999, ev1, time2, room2);
	}

	@Override
	protected boolean acceptBest() {
		if (!this.bestMove.updated)
			return false;		
		if (this.bestMove.P <= this.originalP){ // do not add equality here! because each vns quits at the first improvement
			commitBestMove();
			clearFields();
			
			this.pEvaluator.evaluateIndividual(this.currentInd);
			this.rm.evalIndivRobustness(this.currentInd);  // Robustness values should also be up to date!!!
			
			if (this.bestMove.P < this.originalP)
				return true;
			return false;
		}
		else if (SAPhase){
			float delta= this.bestMove.P - this.originalP;
			double acceptProbability= Math.exp(-delta/ Tinit);
			double rnd= this.myRandom.nextDouble(); // [0,1)
//			System.out.println("Random probability: " + rnd + "  Acceptance probability: "+ acceptProbability);

			if (rnd < acceptProbability){
				System.out.println("Random probability: " + rnd + "  Acceptance probability: "+ acceptProbability+"  RESULT: ACCEPTED!!!");
				commitBestMove();
				clearFields();
				
				this.pEvaluator.evaluateIndividual(this.currentInd);
				this.rm.evalIndivRobustness(this.currentInd);  // Robustness values should also be up to date!!!
				
				if (this.bestMove.P < this.originalP)
					return true;
				return false;
			}
		} // end else if
		
		
		restoreOriginalValues();
		return false;
	}
	
}

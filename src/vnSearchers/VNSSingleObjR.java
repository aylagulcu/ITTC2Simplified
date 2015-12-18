package vnSearchers;

import ga.GlobalVars;
import ga.Individual;
import ga.PopulationParameters;

import java.util.List;

import constraints.ConstraintBase;
import data.parameters;

public class VNSSingleObjR extends VNS{
	// Feasibility will always be maintained.
	protected float originalR;
	protected float newRValue;
	
	public VNSSingleObjR(List<ConstraintBase> constr) {
		super(constr);

	}
	
	public void applyVNS(int iterCounter, Individual indiv){
		
		
//		// Attention: VNS searcher should return up to date values of penalty and robustness!!!
//		int origP; int newP;
//		int[] origConstraintValues= new int[parameters.numSoftConstTypes];
//		int[] newConstraintValues= new int[parameters.numSoftConstTypes];
//		
//		this.currentInd= indiv; // with the same reference. This reference should not be changed!!!
//		assert this.currentInd.isFeasible;
//		// a random searcher is selected while the search improved the current value
//		boolean result= false;
//
//		// each VNS searcher is applied in a different order.
//		for (int i=0; i< this.VNSList.size(); i++){
//			this.searcher= selectSearcher();
//			int idleLimit= 5;
//			int idleCounter= 0;
//			do{
//				clearFields();
//				origP= indiv.totalPenalty;
//				for (int c=0; c< parameters.numSoftConstTypes; c++)
//					origConstraintValues[c]= indiv.softConstrP[c];
//				
////				this.pEvaluator.evaluateIndividual(indiv);
////				assert indiv.totalPenalty== origP;
//				
//				result= this.searcher.search();
//				if (result) idleCounter= 0;
//				else idleCounter+= 1;
//				
//				newP= indiv.totalPenalty;
//				for (int c=0; c< parameters.numSoftConstTypes; c++)
//					newConstraintValues[c]= indiv.softConstrP[c];
//				
////				this.pEvaluator.evaluateIndividual(indiv);
////				assert indiv.totalPenalty== newP;
//				
//				tempArray= new float[3+ parameters.numSoftConstTypes]; // GA iteration+Operator Index+ Total decrease inP + decrease of each soft constraint
//				tempArray[0]= PopulationParameters.currentIteration;
//				tempArray[1]= searcher.myIndex;
//				tempArray[2]= (origP-newP); // if positive: percentage of decrease in penalty.
//				for (int c=0; c< parameters.numSoftConstTypes; c++)
//					tempArray[3+c]= (origConstraintValues[c]-newConstraintValues[c]);
//				
//				GlobalVars.LSStats.add(tempArray);
//	
////			} while (result);
//			} while (idleCounter < idleLimit);
//		} // end i for
	}
	
	
	
	
	@Override
	protected void clearFields() {
		this.bestMove= new newMove(); 
		// Assumed that the individual's penalty value is already up to date!
		storeOriginalValues(); // current indiv values are stored to be used for acceptance check mechanism
	}

	protected void storeOriginalValues() {
		this.originalR= this.currentInd.robustValueMin;
	}
	
	@Override
	public void restoreOriginalValues() {
		this.currentInd.robustValueMin= this.originalR;		
	}
	
	@Override
	public void evaluateOptimality(int ev1, int time2, int room2, int ev2,int time1, int room1) {
		this.rm.evalIndivRobustnessForCurrentOp(this.currentInd, ev1, time2, room2, ev2, time1, room1);
		this.newRValue= this.currentInd.robustValueMin;
		this.currentInd.robustValueMin= this.originalR;
	}
	
	@Override
	public boolean checkUpdateBest(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.evaluateOptimality(ev1, time2, room2, ev2, time1, room1); // refreshes this.newPValue
	
		if (!this.bestMove.updated)
			return true;
		else if (this.newRValue < this.bestMove.R)
			return true;
		return false;
	}

	@Override
	public void updateBest(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.bestMove= new newMove(99999, this.newRValue, ev1, time2, room2, ev2, time1, room1 );
	}

	@Override
	public void updateBest(int ev1, int time2, int room2) {
		this.bestMove= new newMove(99999, this.newRValue, ev1, time2, room2);
	}

	@Override
	protected boolean acceptBest() {
		if (!this.bestMove.updated)
			return false;
		if (this.bestMove.R < this.originalR){
			commitBestMove();
			this.rm.evalIndivRobustness(this.currentInd);
			this.pEvaluator.evaluateIndividual(this.currentInd);

			assert this.currentInd.robustValueMin<= this.originalR;
			return true;
		}
		restoreOriginalValues();
		return false;
	}

	@Override
	protected boolean acceptCurrentMove() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateOriginalValue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeOriginalCourseValues(int ev1, int time2, int room2,
			int ev2, int time1, int room1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeNewCourseValues(int ev1, int time2, int room2, int ev2,
			int time1, int room1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeOriginalPartialValues(int ev1, int time2, int room2,
			int ev2, int time1, int room1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeNewPartialValues(int ev1, int time2, int room2, int ev2,
			int time1, int room1) {
		// TODO Auto-generated method stub
		
	}

}

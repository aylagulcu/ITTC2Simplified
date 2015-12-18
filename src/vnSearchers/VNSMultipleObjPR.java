package vnSearchers;

import ga.Individual;

import java.util.List;

import constraints.ConstraintBase;

public class VNSMultipleObjPR extends VNS{
	// Feasibility will always be maintained.
	protected float originalP;
	protected float originalR;
	protected float newPValue;
	protected float newRValue;
	
	public VNSMultipleObjPR(List<ConstraintBase> constr) {
		super(constr);
	}
	
	@Override
	protected void clearFields() {
		this.bestMove= new newMove(); 
		// Assumed that the individual's penalty value is already up to date!
		storeOriginalValues(); // current indiv values are stored to be used for acceptance check mechanism
	}

	protected void storeOriginalValues() {
		this.originalP= this.currentInd.totalPenalty;
		this.originalR= this.currentInd.robustValueMin;
	}
	
	@Override
	public void restoreOriginalValues() {
		this.currentInd.totalPenalty= (int) this.originalP;
		this.currentInd.robustValueMin= this.originalR;		
	}
	
	@Override
	public void evaluateOptimality(int ev1, int time2, int room2, int ev2,
			int time1, int room1) {
		float p= 0;
		for (ConstraintBase c: this.optConstraints)
			p+= c.Compute(this.currentInd.Data);
		this.newPValue= p;		
		
		this.rm.evalIndivRobustnessForCurrentOp(this.currentInd, ev1, time2, room2, ev2, time1, room1);
		this.newRValue= this.currentInd.robustValueMin;	
		this.currentInd.robustValueMin= this.originalR;
	}
	
	@Override
	public boolean checkUpdateBest(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.evaluateOptimality(ev1, time2, room2, ev2, time1, room1); // refreshes this.newPValue
	
		if (!this.bestMove.updated)
			return true;
		else if (this.newPValue < this.bestMove.P && this.newRValue< this.bestMove.R) // OR??? AND???
			return true;
		return false;
	}

	@Override
	public void updateBest(int ev1, int time2, int room2, int ev2, int time1,
			int room1) {
		this.bestMove= new newMove(this.newPValue, this.newRValue, ev1, time2, room2, ev2, time1, room1 );
	}

	@Override
	public void updateBest(int ev1, int time2, int room2) {
		this.bestMove= new newMove(this.newPValue, this.newRValue, ev1, time2, room2);
	}

	@Override
	protected boolean acceptBest() {
		if (!this.bestMove.updated)
			return false;
		if (this.bestMove.P < this.originalP && this.bestMove.R < this.originalR){
			commitBestMove();
			this.pEvaluator.evaluateIndividual(this.currentInd);
			this.rm.evalIndivRobustness(this.currentInd);
			assert this.currentInd.totalPenalty<= this.originalP;
			assert this.currentInd.robustValueMin<= this.originalR;
			return true;
		}
		restoreOriginalValues();
		return false;
	}

	@Override
	public void applyVNS(int iterCounter, Individual indiv) {
		// TODO Auto-generated method stub
		
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

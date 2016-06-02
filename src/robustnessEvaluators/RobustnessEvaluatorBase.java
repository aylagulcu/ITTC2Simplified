package robustnessEvaluators;

import java.util.ArrayList;
import java.util.List;

import constraints.ConstraintBase;
import constraints.HardConstraint;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public abstract class RobustnessEvaluatorBase {
	
	RobustnessManager robManager;
	public List<HardConstraint> feasConstraints;
	public List<ConstraintBase> optimalityConstraints;
	public Individual currentIndividual;
	
	
	public RobustnessEvaluatorBase(RobustnessManager manager){
		this.robManager= manager;
 
		this.feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: manager.constraints)
			if (con instanceof HardConstraint){
				this.feasConstraints.add((HardConstraint) con);
			}
		this.optimalityConstraints= new ArrayList<ConstraintBase>();
		for (ConstraintBase con: manager.constraints)
			if (!(con instanceof HardConstraint)){
				this.optimalityConstraints.add(con);
			}
	}
	
	public void evaluatePop( Population pop){
		for (int ind=0; ind< PopulationParameters.populationSize; ind++)
			evaluateIndividual(pop.individuals[ind]);
	}
	
	
	public abstract void evaluateIndividual(Individual indiv);
	
	public abstract void evaluateIndividualPartial(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1);
	
	public abstract void evaluateIndividualPartialUpdateMatrix(Individual ind, int ev1, int time2, int room2, int ev2, int time1,
			int room1) ;

	public abstract double tryCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	
//	
//	public void tryCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) { // ev1 should be evaluated for: time2, room2!
//		if (ev1== parameters.UNUSED_EVENT && ev2== parameters.UNUSED_EVENT)
//			return;
//		if (ev1== parameters.UNUSED_EVENT){
//			int ev2OrigVal= this.currentIndividual.Data[ev2];
//			this.currentIndividual.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
//			if (checkFeas(this.currentIndividual.Data, ev2)){ 
//				int newP= evaluateOptimality(ev2);
//				if(updatesBest(newP)){
//					this.bestMove= new newMove(newP, ev2, time1, room1);
//				}
//			} // end if
//			this.currentIndividual.Data[ev2] = ev2OrigVal; // To original values
//		} // end if
//		
//		if (ev2== parameters.UNUSED_EVENT){
//			int ev1OrigVal= this.currentIndividual.Data[ev1];
//			this.currentIndividual.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
//			if (checkFeas(this.currentIndividual.Data, ev1)){ 
//				int newP= evaluateOptimality(ev1);
//				if(updatesBest(newP)){
//					this.bestMove= new newMove(newP, ev1, time2, room2);
//				}
//			} // end if
//			this.currentIndividual.Data[ev1] = ev1OrigVal; // To original values
//		} // end if
//		else if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
//			if (dataHolder.eventCourseId[ev2]== dataHolder.eventCourseId[ev1]) // events of the same course
//				return;
//			int ev2OrigVal= this.currentIndividual.Data[ev2];
//			int ev1OrigVal= this.currentIndividual.Data[ev1];
//			this.currentIndividual.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
//			this.currentIndividual.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
//			if (checkFeas(this.currentIndividual.Data, ev1, ev2)){ 
//				int newP= evaluateOptimality(ev1, ev2);
//				newP+= this.avgEventPenalty; // swap cost
//				if(updatesBest(newP)){
//					this.bestMove= new newMove(newP, ev1, time2, room2, ev2, time1, room1);
//				}
//			} // end if
//			this.currentIndividual.Data[ev1] = ev1OrigVal; // To original values
//			this.currentIndividual.Data[ev2] = ev2OrigVal;	// To original values
//		} // end else if
//	} // end method tryCurrentMove
//	
//	

	
//	public float tryCurrentMoveSwapFeasibility(int ev1, int time2, int room2, int ev2, int time1, int room1) {
//		float counter= 0;
//		if (ev1== parameters.UNUSED_EVENT && ev2== parameters.UNUSED_EVENT)
//			return counter;
//		if (ev1== parameters.UNUSED_EVENT){
//			int ev2OrigVal= this.currentIndividual.Data[ev2];
//			this.currentIndividual.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
//			if (checkFeas(this.currentIndividual.Data, ev2)){ 
//				counter+= 1;
//			} // end if
//			this.currentIndividual.Data[ev2] = ev2OrigVal; // To original values
//		} // end if
//		
//		if (ev2== parameters.UNUSED_EVENT){
//			int ev1OrigVal= this.currentIndividual.Data[ev1];
//			this.currentIndividual.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
//			if (checkFeas(this.currentIndividual.Data, ev1)){ 
//				counter+= 1;
//			} // end if
//			this.currentIndividual.Data[ev1] = ev1OrigVal; // To original values
//		} // end if
//		else if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
//			if (dataHolder.eventCourseId[ev2]== dataHolder.eventCourseId[ev1]) // events of the same course
//				return counter;
//			int ev2OrigVal= this.currentIndividual.Data[ev2];
//			int ev1OrigVal= this.currentIndividual.Data[ev1];
//			this.currentIndividual.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
//			this.currentIndividual.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
//			if (checkFeas(this.currentIndividual.Data, ev1, ev2)){ 
//				counter+= 0.5; // not 1 because of the cost of swap
//			} // end if
//			this.currentIndividual.Data[ev1] = ev1OrigVal; // To original values
//			this.currentIndividual.Data[ev2] = ev2OrigVal;	// To original values
//		} // end else if
//		return counter;
//	}
//	
//
//
	

//	
//	public int evaluateOptimality(int ev1, int ev2) {
//		int penalty= 0;
//		if (ev1 != parameters.UNUSED_EVENT){
//			for(ConstraintBase constr: this.optimalityConstraints )
//				penalty+= constr.computeSingleEvent(currentIndividual.Data, ev1);
//		}
//		if (ev2 != parameters.UNUSED_EVENT){
//			for(ConstraintBase constr: this.optimalityConstraints )
//				penalty+= constr.computeSingleEvent(currentIndividual.Data, ev2);
//		}		
//		return penalty;	
//	}
//	
//	public int evaluateOptimality(int ev1) {
//		return evaluateOptimality(ev1, parameters.UNUSED_EVENT);
//	}
//		
//	public boolean updatesBest(int newP) {
//		if (!this.bestMove.updated)
//			return true;
//		else if (newP < this.bestMove.penalty)
//			return true;
//		return false;
//	}
//	
//
//	public class newMove{
//		boolean updated;
//		int penalty;
//		
//		int event1; int time1; int room1;
//		int event2; int time2; int room2;
//		
//		public newMove(){
//			this.penalty= 999999;
//			this.event1= parameters.UNUSED_EVENT;
//			this.time1= parameters.UNUSED_TSS;
//			this.room1= parameters.UNUSED_ROOM;
//			this.event2= parameters.UNUSED_EVENT;
//			this.time2= parameters.UNUSED_TSS;
//			this.room2= parameters.UNUSED_ROOM;
//			updated= false;
//		}
//		
//		public newMove(int p, int e1, int t1, int r1){
//			this.penalty= p;
//			this.event1= e1;
//			this.time1= t1;
//			this.room1= r1;
//			this.event2= parameters.UNUSED_EVENT;
//			this.time2= parameters.UNUSED_TSS;
//			this.room2= parameters.UNUSED_ROOM;
//			updated= true;
//		}
//		
//		public newMove(int p, int e1, int t1, int r1, int e2, int t2, int r2){
//			this.penalty= p;
//			this.event1= e1;
//			this.time1= t1;
//			this.room1= r1;
//			this.event2= e2;
//			this.time2= t2;
//			this.room2= r2;
//			updated= true;
//		}
//		
//	} // end class newMove
//
//
//	
//	
//	
	
}

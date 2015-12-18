package crosser;

import java.util.ArrayList;
import java.util.List;

import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.SoftConstraint;
import data.parameters;
import ga.Individual;

public abstract class PMX extends crosserBase{

	protected int[] matchings;
	protected List<HardConstraint> myFeasConstraints;
	protected List<ConstraintBase> mySoftConstraints;
	
	public PMX(crossoverManager mngr) {
		super(mngr);
		matchings= new int[parameters.numEvents];
		
		myFeasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: myCXManager.constraints)
			if (con instanceof HardConstraint)
				myFeasConstraints.add((HardConstraint) con);
		
		mySoftConstraints= new ArrayList<ConstraintBase>();
		for (ConstraintBase con: myCXManager.constraints)
			if (con instanceof SoftConstraint)
				mySoftConstraints.add(con);	
	}
	
	public abstract void createMatchings();
	public abstract void exchangeMatchingEvents();
	public abstract void superimposeParents(Individual ind1, Individual ind2);


//	public void repairOLD(Individual indiv){
//		// Checking feasibility during CX is not effective. Best way is to repair offsprings after CX:
//		boolean feasible= true;
//		for (int event= 0; event< parameters.numEvents; event++){
//			feasible= true;
//			for(HardConstraint constr: this.myFeasConstraints ){
//				if (! constr.checkEventFeasibility(indiv.Data, event)){
//					feasible= false;
//					break; // for loop
//				}
//			} // end constr for	
//			if (!feasible){
//				// try to assign the infeasible event to a feasible position
//				assignFeasiblePosition(event, indiv);
////				if (!assignFeasiblePosition(event, indiv)) // Nothing to do. Event remains in the same position
////					System.out.println("Not able to assign it to a feasible position");
//			}
//		} // end for
//	}
	

//	private boolean assignFeasiblePosition(int event, Individual offSpring) {
//		// false is returned if no feasible event is found for the current position
//		// event will stay in its previous position
//		int totalValue=0;
//		boolean feasible= false;
//		int[] positionValues;
//		List<int[]> posValList= new ArrayList<int[]>();
//	
//		int origVal= offSpring.Data[event];
//		
//		for (int t=0; t< parameters.numTimeSlots;t++){
//			for (int r=0; r< parameters.numRooms; r++){
//				if (offSpring.dataMatrix[r][t] != parameters.UNUSED_EVENT) continue;
//				offSpring.Data[event]=convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 1, t, r);
//				for(HardConstraint constr: this.myFeasConstraints ){
//					feasible= constr.checkEventFeasibility(offSpring.Data, event);
//					if(!feasible){
//						break;	
//					}	
//				} // end constr for		
//				if (feasible){ // assign it to the least cost room:
//					totalValue=0;
//					for(ConstraintBase constr: this.mySoftConstraints ){
//						totalValue+= constr.computeSingleEvent(offSpring.Data, event);
//					}
//					positionValues= new int[] {t, r, totalValue };
//					posValList.add(positionValues);
//				} // end if
//			} // end r for
//		} // end t for
//
//		if (posValList.size()>0){
//			// Assign the event to the least cost position:
//			int[] tempArray= new int[3]; // t, r, totalValue
//			int[] minArray = new int[3]; // t, r, totalValue 
//			int minVal;
//			minArray= posValList.get(0);
//			minVal= minArray[2];
//			for(int i=1; i< posValList.size(); i++){
//				tempArray= posValList.get(i);
//				if (tempArray[2]< minVal){
//					minVal= tempArray[2];
//					minArray[0]= tempArray[0];
//					minArray[1]= tempArray[1];
//					minArray[2]= tempArray[2];
//				}
//			} // end i for
//			offSpring.Data[event]=convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 1, minArray[0], minArray[1]);
//			offSpring.dataMatrix[minArray[1]][minArray[0]]= event;	
//		
//			return true;
//		} // end if
//		
//		offSpring.Data[event]= origVal;
//		return false;
//	}
	

	protected Individual[] selectBestTwo(Individual ind1, Individual ind2,
			Individual child1, Individual child2) {
		Individual[] myResult= new Individual[2];
		ArrayList<Individual> inds= new ArrayList<Individual>();
		inds.add(ind1); inds.add(ind2); inds.add(child1); inds.add(child2); 
		int bestP= inds.get(0).totalPenalty;
		int bestIndex= 0;
		for (int i= 1; i< inds.size(); i++){
			if  (inds.get(i).totalPenalty<bestP){
				bestP= inds.get(i).totalPenalty;
				bestIndex= i;
			}
		}
		myResult[0]= inds.get(bestIndex);
		inds.remove(bestIndex);
		
		bestP= inds.get(0).totalPenalty;
		bestIndex= 0;
		for (int i= 1; i< inds.size(); i++){
			if  (inds.get(i).totalPenalty<bestP){
				bestP= inds.get(i).totalPenalty;
				bestIndex= i;
			}
		}
		myResult[1]= inds.get(bestIndex);
		inds.remove(bestIndex);
		
		return myResult;
	}
	
	
	
//	public void printConstraintPenalties(String s, Individual indiv) {
//		myCXManager.mySimpleEvaluator.evaluateIndividual(indiv);
//		System.out.println(s + indiv.totalPenalty);
//		for (int c=0; c< myCXManager.constraints.size(); c++) {
//			ConstraintBase con= myCXManager.constraints.get(c);
//			System.out.println("\t"+con.getClass().getSimpleName()+ " violation: "+ indiv.constraintPenalties[c]);
//		}
//	}
//	
//	public void printToScreen(String id, Individual indiv) {
//		System.out.println(id);
//		for( int r= 0; r< parameters.numRooms; r++){ // room2 included
//			System.out.println();
//			for ( int t= 0; t< parameters.numTimeSlots; t++){ // time2 included
//				System.out.print(indiv.dataMatrix[r][t]+"\t");
//			} // end t for
//		} // end r for
//		System.out.println();
//	}
	
}
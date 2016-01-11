package mutators;

import ga.Individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import constraints.HardConstraint;
import util.RandomNumberGenerator;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public abstract class mutatorBase {
	float mutProb;
	public Random myRandGen;
	public MutationManager myMutationManager;

	List<Integer> curList= new ArrayList<Integer>();
	
	public mutatorBase(MutationManager mngr) {
		this.myMutationManager= mngr;
		myRandGen= new Random(RandomNumberGenerator.getNewSeed());
		
	}

	public abstract void mutate();

	public void tryCurrentMove(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) { 
		// ev1 should be evaluated for: time2, room2
		// ev2 should be evaluated for: time1, room1
		if (ev1== ev2)
			return;	
		
		// Make changes in the matrices acc to the new situation, because constraits use them!
		prepareMatricesForNew(ind, ev1, time2, room2, ev2, time1, room1);
		
		if (ev1== parameters.UNUSED_EVENT){
			int ev2OrigVal= ind.Data[ev2];
			ind.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ind, ev2, time1, room1)){
				return; // with the new matrix 
			} // end if checkFeas
			// if not returned true:
			ind.Data[ev2] = ev2OrigVal; // To original values
			// matrix to original values:
			ind.dataMatrix[room2][time2]= ev2;
			ind.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				ind.timeCurriculum[time2][cur]++; // original position
				ind.timeCurriculum[time1][cur]--; // new position
			} // end for each
		} // end if
		
		if (ev2== parameters.UNUSED_EVENT){
			int ev1OrigVal= ind.Data[ev1];
			ind.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			if (checkFeas(ind, ev1, time2, room2)){
				return; // with the new matrix 
			} // end if
			// if not returned true:
			ind.Data[ev1] = ev1OrigVal; // To original values
			// matrix to original values:
			ind.dataMatrix[room2][time2]= ev2;
			ind.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				ind.timeCurriculum[time1][cur]++; // original position
				ind.timeCurriculum[time2][cur]--; // new position
			} // end for each
		} // end if
		
		if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
			int ev2OrigVal= ind.Data[ev2];
			int ev1OrigVal= ind.Data[ev1];
			ind.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			ind.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			
			if (checkFeas(ind, ev1, time2, room2, ev2, time1, room1)){
				return; // with the new matrix 
			}
			// if not returned true:
			ind.Data[ev1] = ev1OrigVal; // To original values
			ind.Data[ev2] = ev2OrigVal;	// To original values		
			// matrix to original values:
			ind.dataMatrix[room2][time2]= ev2;
			ind.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				ind.timeCurriculum[time1][cur]++; // original position
				ind.timeCurriculum[time2][cur]--; // new position
			} // end for each
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				ind.timeCurriculum[time2][cur]++; // original position
				ind.timeCurriculum[time1][cur]--; // new position
			} // end for each
		} // end else if

	} // end method evaluateTempMove
	
	public boolean checkFeas(Individual ind, int ev1, int t1, int r1, int ev2, int t2, int r2) {
		return checkFeas(ind, ev1, t1, r1) && checkFeas(ind, ev2, t2, r2);
	}
	
	public boolean checkFeas(Individual ind, int ev, int t, int r) {
		if (ev== parameters.UNUSED_EVENT)
			return true;
		for (HardConstraint hc: myMutationManager.feasConstraints){
			if (!hc.checkEventFeasibilityInSA(ind, ev, t, r))
				return false;
		}
		return true;
		
	}
	
	public void prepareMatricesForNew(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1){
		// update matrix:
		ind.dataMatrix[room2][time2]= ev1;
		ind.dataMatrix[room1][time1]= ev2;
		// update curriculum compactness matrix:
		if (ev1!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				if (ind.timeCurriculum[time1][cur]> 0)
					ind.timeCurriculum[time1][cur]--; // old position
				ind.timeCurriculum[time2][cur]++; // new position
			} // end for each
		}
		if (ev2!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				if (ind.timeCurriculum[time2][cur]> 0)
					ind.timeCurriculum[time2][cur]--; // old position
				ind.timeCurriculum[time1][cur]++; // new position
			} // end for each
		}
	} // end updateMatrices
	
	
}

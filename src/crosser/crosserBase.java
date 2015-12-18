package crosser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import constraints.HardConstraint;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import util.RandomNumberGenerator;
import ga.Individual;

public abstract class crosserBase{

	public Individual[] myOffSprings; // The number of individuals returned changes from one crosser to another
	public Random myRandom;
	public crossoverManager myCXManager;	
	
	int ev2OrigVal; int ev1OrigVal;

	List<Integer> curList= new ArrayList<Integer>();
	
	public crosserBase(crossoverManager mngr) {
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
		myCXManager= mngr;
	}
	
	public abstract Individual[] cross(Individual ind1, Individual ind2);
	
	// true if current move has been accepted; false o.w.
	public boolean tryCurrentMove(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) { 
		// ev1 should be evaluated for: time2, room2
		// ev2 should be evaluated for: time1, room1
		if (ev1== ev2)
			return false;	
		
		this.updateOriginalValues(ind);
		this.computeOriginalPartialValues(ind, ev1, time2, room2, ev2, time1, room1);
		
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
		
		// Now try the current move:
		if (ev1== parameters.UNUSED_EVENT){
			ev2OrigVal= ind.Data[ev2];
			ind.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (this.checkFeas(ind, ev2, time1, room1)){
				this.computeNewPartialValues(ind, ev1, time2, room2, ev2, time1, room1);
				this.acceptCurrentMove(ind);
				return true;
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
			return false;
		} // end if
		
		if (ev2== parameters.UNUSED_EVENT){
			ev1OrigVal= ind.Data[ev1];
			ind.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			if (this.checkFeas(ind, ev1, time2, room2)){
				this.computeNewPartialValues(ind, ev1, time2, room2, ev2, time1, room1);
				this.acceptCurrentMove(ind);
				return true;
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
			return false;
		} // end if
		
		if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
			ev2OrigVal= ind.Data[ev2];
			ev1OrigVal= ind.Data[ev1];
			ind.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			ind.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (this.checkFeas(ind, ev1, time2, room2, ev2, time1, room1)){
				this.computeNewPartialValues(ind, ev1, time2, room2, ev2, time1, room1);
				this.acceptCurrentMove(ind);
				return true;
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
			return false;
		} // end else if

		return false;
	} // end method evaluateTempMove

	public abstract boolean checkFeas(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1);

	public abstract boolean checkFeas(Individual ind, int ev2, int time1, int room1);

	public abstract boolean acceptCurrentMove(Individual ind);

	public abstract void computeNewPartialValues(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1);

	public abstract void computeOriginalPartialValues(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1);
	
	public abstract void updateOriginalValues(Individual ind);
	
	
	
}
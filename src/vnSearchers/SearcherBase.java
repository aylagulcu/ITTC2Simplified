package vnSearchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.RandomNumberGenerator;
import constraints.HardConstraint;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public abstract class SearcherBase {
	int myIndex;
	public Random myRandGen;
	public SABase mySA;
	
	public boolean result;
	int ev2OrigVal; int ev1OrigVal;
	
	List<Integer> curList= new ArrayList<Integer>();

	public SearcherBase(SABase mngr, int index) {
		this.mySA= mngr;
		
		myRandGen= new Random(RandomNumberGenerator.getNewSeed());
		myIndex= index;		
	}
		
	public abstract boolean search();

	// true if current move has been accepted; false o.w.
	public boolean tryCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) { 
		// ev1 should be evaluated for: time2, room2
		// ev2 should be evaluated for: time1, room1
		if (ev1== ev2)
			return false;	
		
		mySA.updateOriginalValue();
		mySA.computeOriginalPartialValues(ev1, time2, room2, ev2, time1, room1);
		
		// update matrix:
		mySA.currentInd.dataMatrix[room2][time2]= ev1;
		mySA.currentInd.dataMatrix[room1][time1]= ev2;
		// update curriculum compactness matrix:
		if (ev1!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				if (mySA.currentInd.timeCurriculum[time1][cur]> 0)
					mySA.currentInd.timeCurriculum[time1][cur]--; // old position
				mySA.currentInd.timeCurriculum[time2][cur]++; // new position
			} // end for each
		}
		if (ev2!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				if (mySA.currentInd.timeCurriculum[time2][cur]> 0)
					mySA.currentInd.timeCurriculum[time2][cur]--; // old position
				mySA.currentInd.timeCurriculum[time1][cur]++; // new position
			} // end for each
		}
		
		// Now try the current move:
		if (ev1== parameters.UNUSED_EVENT){
			ev2OrigVal= mySA.currentInd.Data[ev2];
			mySA.currentInd.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ev2, time1, room1)){
				mySA.computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				if (mySA.acceptCurrentMove(ev1, time2, room2, ev2, time1, room1)){
					return true;
				}
			} // end if checkFeas
			// if not returned true:
			mySA.currentInd.Data[ev2] = ev2OrigVal; // To original values
			// matrix to original values:
			mySA.currentInd.dataMatrix[room2][time2]= ev2;
			mySA.currentInd.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				mySA.currentInd.timeCurriculum[time2][cur]++; // original position
				mySA.currentInd.timeCurriculum[time1][cur]--; // new position
			} // end for each
			return false;
		} // end if
		
		if (ev2== parameters.UNUSED_EVENT){
			ev1OrigVal= mySA.currentInd.Data[ev1];
			mySA.currentInd.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			if (checkFeas(ev1, time2, room2)){
				mySA.computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				if (mySA.acceptCurrentMove(ev1, time2, room2, ev2, time1, room1)){
					return true;
				}
			} // end if
			// if not returned true:
			mySA.currentInd.Data[ev1] = ev1OrigVal; // To original values
			// matrix to original values:
			mySA.currentInd.dataMatrix[room2][time2]= ev2;
			mySA.currentInd.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				mySA.currentInd.timeCurriculum[time1][cur]++; // original position
				mySA.currentInd.timeCurriculum[time2][cur]--; // new position
			} // end for each
			return false;
		} // end if
		
		if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
			ev2OrigVal= mySA.currentInd.Data[ev2];
			ev1OrigVal= mySA.currentInd.Data[ev1];
			mySA.currentInd.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			mySA.currentInd.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ev1, time2, room2, ev2, time1, room1)){
				mySA.computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				if (mySA.acceptCurrentMove(ev1, time2, room2, ev2, time1, room1)){
					return true;
				} // end if
			}
			// if not returned true:
			mySA.currentInd.Data[ev1] = ev1OrigVal; // To original values
			mySA.currentInd.Data[ev2] = ev2OrigVal;	// To original values		
			// matrix to original values:
			mySA.currentInd.dataMatrix[room2][time2]= ev2;
			mySA.currentInd.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				mySA.currentInd.timeCurriculum[time1][cur]++; // original position
				mySA.currentInd.timeCurriculum[time2][cur]--; // new position
			} // end for each
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				mySA.currentInd.timeCurriculum[time2][cur]++; // original position
				mySA.currentInd.timeCurriculum[time1][cur]--; // new position
			} // end for each
			return false;
		} // end else if

		return false;
	} // end method evaluateTempMove

	public boolean checkFeas(int ev1, int t1, int r1, int ev2, int t2, int r2) {
		return checkFeas(ev1, t1, r1) && checkFeas(ev2, t2, r2);
	}
	
	public boolean checkFeas(int ev, int t, int r) {
		for (HardConstraint hc: mySA.feasConstraints){
			if (!hc.checkEventFeasibilityInSA(mySA.currentInd, ev, t, r))
				return false;
		}
		return true;
		
	}
	
	
	
	
	
}

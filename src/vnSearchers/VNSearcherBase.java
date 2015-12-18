package vnSearchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.RandomNumberGenerator;
import constraints.HardConstraint;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public abstract class VNSearcherBase {
	int myIndex;
	public Random myRandGen;
	public VNS myVNS;
	
	public boolean result;
	int ev2OrigVal; int ev1OrigVal;
	
	List<Integer> curList= new ArrayList<Integer>();

	public VNSearcherBase(VNS mngr, int index) {
		this.myVNS= mngr;
		myRandGen= new Random(RandomNumberGenerator.getNewSeed());
		myIndex= index;		
	}
		
	public abstract boolean search();
	public abstract boolean search(int eventID);

	// true if current move has been accepted; false o.w.
	public boolean tryCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) { 
		// ev1 should be evaluated for: time2, room2
		// ev2 should be evaluated for: time1, room1
		if (ev1== ev2)
			return false;	
		
		myVNS.updateOriginalValue();
		myVNS.computeOriginalPartialValues(ev1, time2, room2, ev2, time1, room1);
		
		// update matrix:
		myVNS.currentInd.dataMatrix[room2][time2]= ev1;
		myVNS.currentInd.dataMatrix[room1][time1]= ev2;
		// update curriculum compactness matrix:
		if (ev1!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				if (myVNS.currentInd.timeCurriculum[time1][cur]> 0)
					myVNS.currentInd.timeCurriculum[time1][cur]--; // old position
				myVNS.currentInd.timeCurriculum[time2][cur]++; // new position
			} // end for each
		}
		if (ev2!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				if (myVNS.currentInd.timeCurriculum[time2][cur]> 0)
					myVNS.currentInd.timeCurriculum[time2][cur]--; // old position
				myVNS.currentInd.timeCurriculum[time1][cur]++; // new position
			} // end for each
		}
		
		// Now try the current move:
		if (ev1== parameters.UNUSED_EVENT){
			ev2OrigVal= myVNS.currentInd.Data[ev2];
			myVNS.currentInd.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ev2, time1, room1)){
				myVNS.computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				if (myVNS.acceptCurrentMove()){
					return true;
				}
			} // end if checkFeas
			// if not returned true:
			myVNS.currentInd.Data[ev2] = ev2OrigVal; // To original values
			// matrix to original values:
			myVNS.currentInd.dataMatrix[room2][time2]= ev2;
			myVNS.currentInd.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				myVNS.currentInd.timeCurriculum[time2][cur]++; // original position
				myVNS.currentInd.timeCurriculum[time1][cur]--; // new position
			} // end for each
			return false;
		} // end if
		
		if (ev2== parameters.UNUSED_EVENT){
			ev1OrigVal= myVNS.currentInd.Data[ev1];
			myVNS.currentInd.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			if (checkFeas(ev1, time2, room2)){
				myVNS.computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				if (myVNS.acceptCurrentMove()){
					return true;
				}
			} // end if
			// if not returned true:
			myVNS.currentInd.Data[ev1] = ev1OrigVal; // To original values
			// matrix to original values:
			myVNS.currentInd.dataMatrix[room2][time2]= ev2;
			myVNS.currentInd.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				myVNS.currentInd.timeCurriculum[time1][cur]++; // original position
				myVNS.currentInd.timeCurriculum[time2][cur]--; // new position
			} // end for each
			return false;
		} // end if
		
		if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
			ev2OrigVal= myVNS.currentInd.Data[ev2];
			ev1OrigVal= myVNS.currentInd.Data[ev1];
			myVNS.currentInd.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			myVNS.currentInd.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ev1, time2, room2, ev2, time1, room1)){
				myVNS.computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				if (myVNS.acceptCurrentMove()){
					return true;
				} // end if
			}
			// if not returned true:
			myVNS.currentInd.Data[ev1] = ev1OrigVal; // To original values
			myVNS.currentInd.Data[ev2] = ev2OrigVal;	// To original values		
			// matrix to original values:
			myVNS.currentInd.dataMatrix[room2][time2]= ev2;
			myVNS.currentInd.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				myVNS.currentInd.timeCurriculum[time1][cur]++; // original position
				myVNS.currentInd.timeCurriculum[time2][cur]--; // new position
			} // end for each
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				myVNS.currentInd.timeCurriculum[time2][cur]++; // original position
				myVNS.currentInd.timeCurriculum[time1][cur]--; // new position
			} // end for each
			return false;
		} // end else if

		return false;
	} // end method evaluateTempMove

	public boolean checkFeas(int ev1, int t1, int r1, int ev2, int t2, int r2) {
		return checkFeas(ev1, t1, r1) && checkFeas(ev2, t2, r2);
	}
	
	public boolean checkFeas(int ev, int t, int r) {
		for (HardConstraint hc: myVNS.feasConstraints){
			if (!hc.checkEventFeasibilityInSA(myVNS.currentInd, ev, t, r))
				return false;
		}
		return true;
		
	}
	
	
	
	
	
}

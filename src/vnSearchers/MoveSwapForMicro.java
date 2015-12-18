package vnSearchers;

import java.util.List;

import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class MoveSwapForMicro extends VNSearcherBase {
	
	// Select a random event, Select a position randomly, move it is an empty position! 
	int ev, time, room, ev2, time2, room2;
	List<Integer> Times; 

	public MoveSwapForMicro(VNS mngr) {
		super(mngr, 0); // remove this index afterwards
	}

	public boolean search() {		
		ev= myRandGen.nextInt(parameters.numEvents);
		
		time= convertionManager.intToTime(myVNS.currentInd.Data[ev]);
		room= convertionManager.intToRoom(myVNS.currentInd.Data[ev]);
		
		Times= dataHolder.cTFeasList.get(dataHolder.eventCourseId[ev]);
		time2 =Times.get(myRandGen.nextInt(Times.size()));
		room2 =myRandGen.nextInt(parameters.numRooms);
		ev2= myVNS.currentInd.dataMatrix[room2][time2]; // it may be empty= unused event
	
		return tryCurrentMove(ev, time2, room2, ev2, time, room); // move related data is recorded if it updates the current best move
	}

	@Override
	public boolean search(int eventID) {
		ev= eventID;
		
		time= convertionManager.intToTime(myVNS.currentInd.Data[ev]);
		room= convertionManager.intToRoom(myVNS.currentInd.Data[ev]);
		
		Times= dataHolder.cTFeasList.get(dataHolder.eventCourseId[ev]);
		time2 =Times.get(myRandGen.nextInt(Times.size()));
		room2 =myRandGen.nextInt(parameters.numRooms);
		ev2= myVNS.currentInd.dataMatrix[room2][time2]; // it may be empty= unused event
		
		if (ev2!= parameters.UNUSED_EVENT && dataHolder.eventCourseId[ev]== dataHolder.eventCourseId[ev2])
			return false;
		return tryCurrentMove(ev, time2, room2, ev2, time, room); // move related data is recorded if it updates the current best move
	}

}
	

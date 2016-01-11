package vnSearchers;

import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class SwapNew extends SearcherBase{
	// Select two events of two distinct courses
	// Perform swap

	int ev1, time1, room1;
	int ev2, time2, room2;
	
	public SwapNew(SABase mngr, int index) {
		super(mngr, index);
	}

	public boolean search() {
		ev1= myRandGen.nextInt(parameters.numEvents);		
		ev2= this.myRandGen.nextInt(parameters.numEvents);
		
		if (dataHolder.eventCourseId[ev1]== dataHolder.eventCourseId[ev2])
			return false;
		time1= convertionManager.intToTime(mySA.currentInd.Data[ev1]);
		room1= convertionManager.intToRoom(mySA.currentInd.Data[ev1]);

		time2= convertionManager.intToTime(mySA.currentInd.Data[ev2]);
		room2= convertionManager.intToRoom(mySA.currentInd.Data[ev2]);
		
		return tryCurrentMove(ev1, time2, room2, ev2, time1, room1); // move related data is recorded if it updates the current best move
	}

}
	

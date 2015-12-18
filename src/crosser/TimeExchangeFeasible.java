package crosser;

import data.convertionManager;
import data.parameters;
import ga.Individual;

public class TimeExchangeFeasible extends crosserBase {
	
	int e; int oldTime; int oldRoom;
	int t1; int t2;

	public TimeExchangeFeasible(crossoverManager mngr) {
		super(mngr);
		myOffSprings= new Individual[2];
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {

		t1= myRandom.nextInt(parameters.numTimeSlots);
		do {
			t2 = myRandom.nextInt(parameters.numTimeSlots);
		} while(t2 == t1);
		
		for (int r=0; r< parameters.numRooms; r++) {
			if (ind1.dataMatrix[r][t1] == parameters.UNUSED_EVENT){
				e= ind2.dataMatrix[r][t2]; // Accept from other parent
				if (e!= parameters.UNUSED_EVENT) {
					oldTime= convertionManager.intToTime(ind1.Data[e]);
					oldRoom= convertionManager.intToRoom(ind1.Data[e]);
					// e's new position in ind 1 will be the current position:
					tryCurrentMove(ind1, e, t1, r, parameters.UNUSED_EVENT, oldTime, oldRoom );
				} // end if
			} // end if

			if (ind2.dataMatrix[r][t2] == parameters.UNUSED_EVENT){
				e= ind1.dataMatrix[r][t1]; // Accept from other parent
				if (e!= parameters.UNUSED_EVENT) {
					oldTime= convertionManager.intToTime(ind2.Data[e]);
					oldRoom= convertionManager.intToRoom(ind2.Data[e]);
					// e's new position in ind 1 will be the current position:
					tryCurrentMove(ind2, e, t2, r, parameters.UNUSED_EVENT, oldTime, oldRoom );
				} // end if
			} // end if

		} // end r for
		myOffSprings[0]= ind1;
		myOffSprings[1]= ind2;
		return myOffSprings;
	}

}

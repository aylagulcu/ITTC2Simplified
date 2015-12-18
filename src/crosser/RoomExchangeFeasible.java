package crosser;

import data.convertionManager;
import data.parameters;
import ga.Individual;

public class RoomExchangeFeasible extends crosserBase {

	int r1; int r2;
	int e; int oldTime; int oldRoom;
	
	public RoomExchangeFeasible(crossoverManager mngr) {
		super(mngr);
		
		myOffSprings= new Individual[2];
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {
		
		r1=myRandom.nextInt(parameters.numRooms);
		do {
			r2 = myRandom.nextInt(parameters.numRooms);
		} while(r2 == r1);
		
		for (int t=0; t< parameters.numTimeSlots; t++) {
			if (ind1.dataMatrix[r1][t] == parameters.UNUSED_EVENT) { // Available to accept from other parent
				e= ind2.dataMatrix[r2][t]; // other parent
				if (e!= parameters.UNUSED_EVENT) {
					oldTime= convertionManager.intToTime(ind1.Data[e]);
					oldRoom= convertionManager.intToRoom(ind1.Data[e]);
					// e's new position in ind 1 will be the current position:
					tryCurrentMove(ind1, e, t, r1, parameters.UNUSED_EVENT, oldTime, oldRoom );
				} // end if
			} // end if
			
			if (ind2.dataMatrix[r2][t] == parameters.UNUSED_EVENT) { // Accept from other parent
				e= ind1.dataMatrix[r1][t]; // other parent
				if (e!= parameters.UNUSED_EVENT) {
					oldTime= convertionManager.intToTime(ind2.Data[e]);
					oldRoom= convertionManager.intToRoom(ind2.Data[e]);
					// e's new position in ind 1 will be the current position:
					tryCurrentMove(ind2, e, t, r2, parameters.UNUSED_EVENT, oldTime, oldRoom );
				} // end if
			} // end if
		} // end t for
		
		myOffSprings[0]= ind1;
		myOffSprings[1]= ind2;
		
		return myOffSprings;
	}

}

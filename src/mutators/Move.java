package mutators;

import ga.PopulationParameters;

import java.util.HashSet;

import data.convertionManager;
import data.parameters;

public class Move extends mutatorBase {

	HashSet<Integer> EventsToBeMutated= new HashSet<Integer>();
	int numEvents= (int)(parameters.numEvents * PopulationParameters.eventMutRate);
	int time1; int room1;
	int ev2; int time2; int room2;
	
	public Move(MutationManager mngr) {
		super(mngr);
	}


	public void mutate() {
		EventsToBeMutated.clear();
		while (EventsToBeMutated.size()<= numEvents){
			EventsToBeMutated.add(myRandGen.nextInt(parameters.numEvents));
		} // end while

		for (int ev1: EventsToBeMutated){
			time1= convertionManager.intToTime(myMutationManager.indToMutate.Data[ev1]);
			room1= convertionManager.intToRoom(myMutationManager.indToMutate.Data[ev1]);
			
			time2 =myRandGen.nextInt(parameters.numTimeSlots);
			room2 =myRandGen.nextInt(parameters.numRooms);
			ev2= myMutationManager.indToMutate.dataMatrix[room2][time2];
			if (ev2!= parameters.UNUSED_EVENT)
				continue;
			tryCurrentMove(myMutationManager.indToMutate, ev1, time2, room2, ev2, time1, room1); 
		} // end for

	}

}
	

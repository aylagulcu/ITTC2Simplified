package mutators;

import ga.PopulationParameters;

import java.util.HashSet;
import data.convertionManager;
import data.parameters;

public class SwapSimple extends mutatorBase {
	// 2 randomly chosen events are Swapped.
	// Matrix is kept up to date.
	
	HashSet<Integer> EventsToBeMutated= new HashSet<Integer>();
	int numEvents= (int)(parameters.numEvents * PopulationParameters.eventMutRate);
	int time1; int room1;
	int ev2; int time2; int room2;
	
	public SwapSimple(MutationManager mngr) {
		super(mngr);
	}

	public void mutate() {
		EventsToBeMutated.clear();
		while (EventsToBeMutated.size()<= numEvents){
			EventsToBeMutated.add(myRandGen.nextInt(parameters.numEvents));
		} // end while

		for (int ev1: EventsToBeMutated){
			ev2= myRandGen.nextInt(parameters.numEvents);
			time1= convertionManager.intToTime(myMutationManager.indToMutate.Data[ev1]);
			room1= convertionManager.intToRoom(myMutationManager.indToMutate.Data[ev1]);
			
			time2= convertionManager.intToTime(myMutationManager.indToMutate.Data[ev2]);
			room2= convertionManager.intToRoom(myMutationManager.indToMutate.Data[ev2]);
			
			tryCurrentMove(myMutationManager.indToMutate, ev1, time2, room2, ev2, time1, room1); 
		} // end for
	}
	
}
	

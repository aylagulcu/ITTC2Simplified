package mutators;

import java.util.List;

import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class MoveSwapMutator extends mutatorBase {
	// Respects only feasibility conditions. And Time Availability all the time!
	
	int ev1, time1, room1;
	int ev2, time2, room2;
	List<Integer> Times; 
	
	public MoveSwapMutator(MutationManager mngr) {
		super(mngr);
	}


	public void mutate() {

		ev1= myRandGen.nextInt(parameters.numEvents);
		time1= convertionManager.intToTime(myMutationManager.indToMutate.Data[ev1]);
		room1= convertionManager.intToRoom(myMutationManager.indToMutate.Data[ev1]);
		
		Times= dataHolder.cTFeasList.get(dataHolder.eventCourseId[ev1]);
		time2 =Times.get(myRandGen.nextInt(Times.size()));
		room2 =myRandGen.nextInt(parameters.numRooms);
		ev2= myMutationManager.indToMutate.dataMatrix[room2][time2];
	
		if (ev2!= parameters.UNUSED_EVENT && dataHolder.eventCourseId[ev1]== dataHolder.eventCourseId[ev2])
			return;
		tryCurrentMove(myMutationManager.indToMutate, ev1, time2, room2, ev2, time1, room1); 
	}

}
	

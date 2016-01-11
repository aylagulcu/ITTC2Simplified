package orderingMethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import initializer.CP.CPIndInitializer;
import data.dataHolder;
import data.parameters;

public class SaturationDegreeRooms extends OrderingBase{
	// Event with the smallest number of rooms available has the highest degree 
	// (If a room is not empty at any feasible time then this room is not included )
	// Dynamic!

	// Used in initializer
	@SuppressWarnings("unchecked")
	public int selectEvent(List<Integer> tournament, CPIndInitializer initializer) {
		HashSet<Integer> tempTimes;
		int cId1; int count;
		int[] eventCounts= new int[tournament.size()]; 
		int maxCount= 0;
		
		tempTimes= (HashSet<Integer>) initializer.eventCurrentlyAvailableTimes[tournament.get(0)];
		cId1= dataHolder.eventCourseId[tournament.get(0)];
		maxCount= parameters.numRooms;
		for (int r: dataHolder.cRoomFeasList.get(cId1)) {
			// if any feasible time is available for this room, then count--
			for (int time: tempTimes) {
				if (initializer.myIndiv.dataMatrix[r][time] == parameters.UNUSED_EVENT) {
					maxCount--;
					break;
				}
			} // end time for		
		} // end r for
		eventCounts[0]= maxCount; // keep count for each event
		
		for (int e=1; e< tournament.size(); e++) {
			tempTimes= (HashSet<Integer>) initializer.eventCurrentlyAvailableTimes[tournament.get(e)];
			cId1= dataHolder.eventCourseId[tournament.get(e)];
			count= parameters.numRooms;
			for (int r: dataHolder.cRoomFeasList.get(cId1)) {
				// if any feasible time is available for this room, then count--
				for (int time: tempTimes) {
					if (initializer.myIndiv.dataMatrix[r][time] == parameters.UNUSED_EVENT) {
						assert count >0;
						count--;
						break;
					}
				} // end time for		
			} // end r for
			if (count> maxCount)
				maxCount= count; // find max value
			eventCounts[e]= count; // keep count for each event
		} // end e for
		
		// Now return the event with the max value. But what happens in case of draws?
		List<Integer> eventsWithMaxCount= new ArrayList<Integer>();
		for (int e=0; e< eventCounts.length; e++) 
			if (eventCounts[e] == maxCount)
				eventsWithMaxCount.add(tournament.get(e));
		
		return resolveDraws(eventsWithMaxCount, initializer);
	}
	
	// Used in initializer
	private int resolveDraws(List<Integer> events, CPIndInitializer initializer) {
		// Resolve draws by using largest degree method:
		LargestDegree LD= new LargestDegree();
		int event= LD.selectEvent(events, initializer);
		return event;
	}



}

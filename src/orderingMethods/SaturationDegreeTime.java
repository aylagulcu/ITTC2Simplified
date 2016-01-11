package orderingMethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import crosser.crosserBase;

import initializer.CP.CPIndInitializer;
import data.parameters;

public class SaturationDegreeTime extends OrderingBase{
	// Event with the smallest number of time slots available (in terms of instructor's available times and clashes) 
	// has the highest degree
	// Here, Dynamic version is implemented!

	// Used in initializer
	@SuppressWarnings("unchecked")
	public int selectEvent(List<Integer> tournament, CPIndInitializer initializer) {
		int maxCount= parameters.numTimeSlots - ((HashSet<Integer>) initializer.eventCurrentlyAvailableTimes[tournament.get(0)]).size();
		int[] eventCounts= new int[tournament.size()]; 
		eventCounts[0]= maxCount; // keep count for each event
		int count;
		
		for (int e=1; e< tournament.size(); e++) {
			count= parameters.numTimeSlots -((HashSet<Integer>) initializer.eventCurrentlyAvailableTimes[tournament.get(e)]).size(); // events conflicting with e1
			if (count> maxCount) 
				maxCount= count;
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
//		// Resolve draws by using largest degree method:
//		LargestDegree LD= new LargestDegree();
//		int event= LD.selectEvent(events, initializer);
		
		// Resolve draws by using saturation degree rooms method:
		SaturationDegreeRooms SDR= new SaturationDegreeRooms();
		int event= SDR.selectEvent(events, initializer);
		return event;
	}


}

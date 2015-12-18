package orderingMethods;

import initializer.CP.CPIndInitializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import crosser.crosserBase;

import data.dataHolder;

public class LargestDegree extends OrderingBase  {
	// Event which is in conflict with the greatest number of other events is selected
	// Static

	// Used in initializer
	@SuppressWarnings("unchecked")
	public int selectEvent(List<Integer> tournament, CPIndInitializer initializer) {
		int[] eventCounts= new int[tournament.size()]; 
		int maxCount= ((HashSet<Integer>) dataHolder.eventConflictingEvents[tournament.get(0)]).size();
		eventCounts[0]= maxCount; // keep count for each event
		int count;
		
		for (int e=1; e< tournament.size(); e++) {
			count= ((HashSet<Integer>) dataHolder.eventConflictingEvents[tournament.get(e)]).size(); // events conflicting with e1
			if (count> maxCount) {
				maxCount= count;
				eventCounts[e]= count; // keep count for each event
			}
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
		int rndEvent= events.get(initializer.myRandom.nextInt(events.size()));
		return rndEvent;
	}


}

package orderingMethods;

import initializer.CP.CPIndInitializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import data.dataHolder;

public class ColorDegree extends OrderingBase{
	// Event which is in conflict with the greatest number of other events 
	// that have already been scheduled has the highest degree
	// Dynamic

	// Used in initializer
	@SuppressWarnings("unchecked")
	public int selectEvent(List<Integer> tournament, CPIndInitializer initializer) {
		int[] eventCounts= new int[tournament.size()];
		int count;
		HashSet<Integer> temp;
		int maxCount= 0;
		
		temp= (HashSet<Integer>) dataHolder.eventConflictingEvents[tournament.get(0)];
		maxCount= temp.size();
		for (int i: temp) {
			if (initializer.NAEvents.contains(i))
				maxCount--;	
		}
		eventCounts[0]= maxCount; // keep count for each event
		
		for (int e=1; e< tournament.size(); e++) {
			temp= (HashSet<Integer>) dataHolder.eventConflictingEvents[tournament.get(e)];
			count= temp.size();
			for (int i: temp) {
				if (initializer.NAEvents.contains(i)) {
					assert count >0;
					count--;	
				}
			}
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

package initializer;

import ga.PopulationParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class tournamentManager {
	
	public List<Integer> createTournamentCourses(List<Integer> courses, Random rand) {
		List<Integer> crsList= new ArrayList<Integer>();
		int index; int crs;
		int min= Math.min(PopulationParameters.tournamentSize, courses.size());
		for (int i=0; i< min; i++){
			index= rand.nextInt(courses.size()); // 0 inclusive, upper bound exclusive
			crs= courses.get(index);
			crsList.add(crs);	
		}
		return crsList;
	}
	
	public List<Integer> createTournament(HashSet<Integer> events, Random rand) {
		List<Integer> evtList= new ArrayList<Integer>();
		List<Integer> temp= new ArrayList<Integer>();

		Iterator<Integer> iter= events.iterator();
		for (int i=0; i< events.size(); i++)
			temp.add(iter.next());
			
		int index; int event;
		int min= Math.min(PopulationParameters.tournamentSize, events.size());
		for (int i=0; i< min; i++){
			index= rand.nextInt(temp.size()); // 0 inclusive, upper bound exclusive
			event= temp.get(index);
			evtList.add(event);	
			temp.remove(index);
		}
		
//		if (PopulationParameters.tournamentSize > (int)data.parameters.numEvents* 0.2)
//			PopulationParameters.tournamentSize= (int) (PopulationParameters.tournamentSize * 0.8);
		
		return evtList;
	}

	public List<Integer> createTournament(List<Integer> events, Random rand) {
		List<Integer> evtList= new ArrayList<Integer>();
		List<Integer> temp= new ArrayList<Integer>();

		Iterator<Integer> iter= events.iterator();
		for (int i=0; i< events.size(); i++)
			temp.add(iter.next());
			
		int index; int event;
		int min= Math.min(PopulationParameters.tournamentSize, events.size());
		for (int i=0; i< min; i++){
			index= rand.nextInt(temp.size()); // 0 inclusive, upper bound exclusive
			event= temp.get(index);
			evtList.add(event);	
			temp.remove(index);
		}
	
		return evtList;
	}
}

package robustnessEvaluators;

import java.util.List;
import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;

public class DisruptEventSC extends RobustnessEvaluatorBase {
	// This is our real robustness measure. But it takes too much time to compute it many times during GA run
	// Move an event to another timeslot other than the current one. 
	// A swap cannot be performed between the events of the same course.
	// if event e1 is moved; compute Pe1
	// else if event e1 is swapped with e2; compute Pe1+Pe2. Moreover, add a Peavg to this sum to penalize swap
	// if an event cannot be moved or swapped with feasibility, then add a penalty of (Peavg*Peavg)

	List<Integer> feasTimes;
	
	public DisruptEventSC(RobustnessManager manager) {
		super(manager);
	}

	public void evaluateIndividual(Individual indiv) {
		this.currentIndividual= indiv;
		this.avgEventPenalty= (float)this.currentIndividual.totalPenalty / parameters.numEvents;
		indiv.createMatrix();
		float total= 0; // event-based robustness: sum over events.
		for (int e=0; e< parameters.numEvents; e++)
			total+= evaluateEvent(e);
		indiv.robustValueMin= total;
	}

	// Try to assign this event to a timeslot different than the current one.
	// If it is possible to find a new timeslot then return 1; if it is not possible then return zero.
	private float evaluateEvent(int ev) {
		this.bestMove= new newMove();
		
		Event event= convertionManager.intToEvent(ev, this.currentIndividual.Data[ev]);
		int timeOriginal= event.time; // this time is forbidden!
		int roomOriginal= event.room;
		feasTimes= dataHolder.cTFeasList.get(dataHolder.eventCourseId[ev]);
		int ev2;
		for (int t: feasTimes){
			if (t== timeOriginal)
				continue;
			for (int r=0; r< parameters.numRooms; r++ ){			
				ev2= this.currentIndividual.dataMatrix[r][t]; // it may be empty= unused event
				tryCurrentMove(ev, t, r, ev2, timeOriginal, roomOriginal); // move related data is recorded if it updates the current best move
			} // end r for
		} // end t for
	
		if (!this.bestMove.updated)
			return this.avgEventPenalty*this.avgEventPenalty; // no alternative feasible position
		else{
			return this.bestMove.penalty;
		} // end else
	} // end method evaluateEvent


	public void evaluateIndividualPartial(Individual ind, int ev1, int time2,
			int room2, int ev2, int time1, int room1) {
		evaluateIndividual(ind);
		
	}
		
}

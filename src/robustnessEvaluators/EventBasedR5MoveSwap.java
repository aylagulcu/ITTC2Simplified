package robustnessEvaluators;

import java.util.List;

import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;

public class EventBasedR5MoveSwap extends RobustnessEvaluatorBase {
	// R5.a
	// This is our another heuristic robustness measure. Maximization.
	// Soft constraints have not been considered
	// Only move operation is considered
	// For each event, the number of alternative positions are counted
	// Maximizes the sum of alternative positions
	// Converted to minimization by taking 1/(1+sum of alternative positions)
	// the worst event takes a maximum value of 1 in this case
	
	List<Integer> feasTimes; 
	
	public EventBasedR5MoveSwap(RobustnessManager manager) {
		super(manager);	
	}

	public void evaluateIndividual(Individual indiv) {
		this.currentIndividual= indiv;
		this.avgEventPenalty= (float)this.currentIndividual.totalPenalty / parameters.numEvents;
		indiv.createMatrix();
		float total= 0; 
		for (int e=0; e< parameters.numEvents; e++)
			total+= evaluateEvent(e);
		indiv.robustnessValue= total;
	}

	private float evaluateEvent(int ev) {
		this.bestMove= new newMove();
		
		Event event= convertionManager.intToEvent(ev, this.currentIndividual.Data[ev]);
		int timeOriginal= event.time; // this time is forbidden!
		int roomOriginal= event.room;
		feasTimes= dataHolder.cTFeasList.get(dataHolder.eventCourseId[ev]);
		int ev2;
		float alternativeCount= 0;
		float moveResult= 0;
		for (int t: feasTimes){
			if (t== timeOriginal)
				continue;
			for (int r=0; r< parameters.numRooms; r++ ){			
				ev2= this.currentIndividual.dataMatrix[r][t]; // it may be empty= unused event
				moveResult= tryCurrentMoveSwapFeasibility(ev, t, r, ev2, timeOriginal, roomOriginal);
				alternativeCount+= moveResult;
			} // end r for
		} // end t for
		// number of alternative feasible positions is returned:
		float r= (1/ (1+alternativeCount));
		return r;
	}



}

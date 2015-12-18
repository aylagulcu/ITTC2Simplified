package robustnessEvaluators;

import java.text.DecimalFormat;
import java.util.List;

import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;

public class EventBasedR5Move extends RobustnessEvaluatorBase {
	// R5.a
	// This is our another heuristic robustness measure.
	// Soft constraints have not been considered
	// Only move operation is considered
	// For each event, the number of alternative positions are counted
	// Maximizes the sum of alternative positions
	// Converted to minimization by taking 1/(1+sum of alternative positions)
	// the worst event takes a maximum value of 1 in this case
	
	// An event Time matrix is kept for each individual
	
	List<Integer> feasTimes;
	DecimalFormat df2 = new DecimalFormat("###.##");
	
	public EventBasedR5Move(RobustnessManager manager) {
		super(manager);	
	}

	// Important: The following operation does not change the individual's robustness arrays.
	// It modifies only a single field: robustValueMin
	// Given the following move, update the events' robustness value for only the affected times
	public void evaluateIndividualPartial(Individual indiv, int ev1, int time2,
			int room2, int ev2, int time1, int room1) {
		
		this.currentIndividual= indiv;
		if (ev1== parameters.UNUSED_EVENT && ev2== parameters.UNUSED_EVENT)
			return;			
		int timeOriginal; int roomOriginal;
		// check for each event if its alternative positions at time1 and time2 changes:
		int timeResult;
		int tempAlternativeCount;
		this.currentIndividual.robustValueMin= 0;
		for (int event=0; event< parameters.numEvents; event++){
			tempAlternativeCount= this.currentIndividual.rEventTotal[event];
			timeOriginal= convertionManager.intToTime(this.currentIndividual.Data[event]);
			roomOriginal= convertionManager.intToRoom(this.currentIndividual.Data[event]);
			
			// for time2:
			if (time2!= parameters.UNUSED_TSS){
				timeResult= evaluateEventTimeR(event, time2, timeOriginal, roomOriginal);
				if (timeResult== 1){
					if (this.currentIndividual.rForEventTimeMove[event][time2]== 0){ 
						tempAlternativeCount+= 1;
					}
				} // end if
				else if (timeResult== 0){
					if (this.currentIndividual.rForEventTimeMove[event][time2]== 1){
						tempAlternativeCount-= 1;
					}
				} // end else if
			} // end if
			// for time1:
			if (time1!= parameters.UNUSED_TSS){
				timeResult= evaluateEventTimeR(event, time1, timeOriginal, roomOriginal);
				timeResult= evaluateEventTimeR(event, time1, timeOriginal, roomOriginal);
				if (timeResult== 1){
					if (this.currentIndividual.rForEventTimeMove[event][time1]== 0 && time1!= timeOriginal){ // increase
						tempAlternativeCount+= 1;
					}
				} // end if
				else if (timeResult== 0){
					if (this.currentIndividual.rForEventTimeMove[event][time1]== 1){ // decrease
						tempAlternativeCount-= 1;
					}
				} // end else if			
			} // end if
			this.currentIndividual.robustValueMin+= (float)1 / (1+ tempAlternativeCount);
		} // end event for
	}

	private float computeSingleR() {
		this.currentIndividual.robustValueMin= 0;
		for (int e=0; e< parameters.numEvents; e++)
			this.currentIndividual.robustValueMin+=(float)1 / (1+ this.currentIndividual.rEventTotal[e]);
//		this.currentIndividual.robustValueMin= RoundTo2Decimals(this.currentIndividual.robustValueMin);
		return this.currentIndividual.robustValueMin;
	}
	
	float RoundTo2Decimals(float fVal) {
//        DecimalFormat df2 = new DecimalFormat("###.##");
        return Float.valueOf(df2.format(fVal));
	}

	private int evaluateEventTimeR(int e, int timeNew, int timeOriginal, int roomOriginal) {
		if (timeNew== timeOriginal)
			return 0;
		float moveResult= 0;
		int ev2; 
		for (int r=0; r< parameters.numRooms; r++ ){
			ev2= this.currentIndividual.dataMatrix[r][timeNew];
			moveResult= tryCurrentMoveFeasibility(e, timeNew, r, ev2, timeOriginal, roomOriginal);
			if (moveResult > 0)
				return 1;
		} // end r for
		return 0;
	}

	private void clearCurIndRMatrix() {
		this.currentIndividual.robustValueMin= 0;
		for (int e=0; e< parameters.numEvents; e++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				this.currentIndividual.rForEventTimeMove[e][t]= 0;
		
		for (int e=0; e< parameters.numEvents; e++)
			this.currentIndividual.rEventTotal[e]= 0;
	}

	// eventTime matrix has been re-created.
	public void evaluateIndividual(Individual indiv) {
		this.currentIndividual= indiv;
		clearCurIndRMatrix(); // event time move alternative matrix elements are set to zero
		indiv.createMatrix(); // Re-create data matrix from data array
		// To do: need for the above statement???? Check!
		
		Event event; int timeOriginal; int roomOriginal;
		int timeResult;
		for (int e=0; e< parameters.numEvents; e++){
			event= convertionManager.intToEvent(e, this.currentIndividual.Data[e]);
			timeOriginal= event.time;
			roomOriginal= event.room;
			feasTimes= dataHolder.cTFeasList.get(dataHolder.eventCourseId[e]);
			for (int t: feasTimes){
				timeResult= evaluateEventTimeR(e, t, timeOriginal, roomOriginal);
				if (timeResult== 1){
					this.currentIndividual.rForEventTimeMove[e][t]= 1;
					this.currentIndividual.rEventTotal[e]+= 1; 
				} // end if
			} // end t for
		} // end e for
		this.currentIndividual.robustValueMin= computeSingleR();
	}

}

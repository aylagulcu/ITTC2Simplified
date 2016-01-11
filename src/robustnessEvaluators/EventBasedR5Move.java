package robustnessEvaluators;

import java.math.BigDecimal;
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
	BigDecimal bd;
	
	public EventBasedR5Move(RobustnessManager manager) {
		super(manager);	
	}

	private double computeSingleR() {
		double r= 0;
		for (int e=0; e< parameters.numEvents; e++)
			r+= 1 / (1+ this.currentIndividual.rEventTotal[e]);
		
		bd= new BigDecimal(r);
		bd= bd.setScale(5, BigDecimal.ROUND_UP);
		return bd.doubleValue();
	}
	
	private int evaluateEventTimeR(int e, int timeNew, int timeOriginal, int roomOriginal) {
		if (timeNew== timeOriginal)
			return 0;
		double moveResult= 0;
		int ev2; 
		for (int r=0; r< parameters.numRooms; r++ ){
			ev2= this.currentIndividual.dataMatrix[r][timeNew];
			// return 1 if there has been found a feasible position on the given time:
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

		Event event; int timeOriginal; int roomOriginal;
		int timeResult;
		for (int e=0; e< parameters.numEvents; e++){
			event= convertionManager.intToEvent(e, this.currentIndividual.Data[e]);
			timeOriginal= event.time;
			roomOriginal= event.room;
			feasTimes= dataHolder.cTFeasList.get(dataHolder.eventCourseId[e]);
			for (int t: feasTimes){
				if (t== timeOriginal) continue;
				timeResult= evaluateEventTimeR(e, t, timeOriginal, roomOriginal);
				if (timeResult== 1){
					this.currentIndividual.rForEventTimeMove[e][t]= 1;
					this.currentIndividual.rEventTotal[e]+= 1; 
				} // end if
			} // end t for
		} // end e for
		this.currentIndividual.robustValueMin= computeSingleR();
	}

	
	// Important: The following operation does not change the individual's robustness arrays.
	// It modifies only a single field: robustValueMin
	// Given the following move, update the events' robustness value for only the affected times
	public void evaluateIndividualPartial(Individual indiv, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		
		this.currentIndividual= indiv;
		if (ev1== parameters.UNUSED_EVENT && ev2== parameters.UNUSED_EVENT)
			return;			
		int timeOriginal; int roomOriginal;
		// check for each event if its alternative positions at time1 and time2 changes:
		int timeResult;
		double tempAlternativeCount;
		this.currentIndividual.robustValueMin= 0;
		// check for ev1:
		if (ev1!= parameters.UNUSED_EVENT){
			tempAlternativeCount= this.currentIndividual.rEventTotal[ev1];
			if (this.currentIndividual.rForEventTimeMove[ev1][time2]== 1){ // current time
//				this.currentIndividual.rForEventTimeMove[ev1][time2]= 0;
//				this.currentIndividual.rEventTotal[ev1]--;
				tempAlternativeCount--;
			}
			if (this.currentIndividual.rForEventTimeMove[ev1][time1]== 0){ // old time, it should be zero assert!
				timeResult= evaluateEventTimeR(ev1, time1, time2, room2);
				if (timeResult== 1){
//					this.currentIndividual.rForEventTimeMove[ev1][time1]= 1;
//					this.currentIndividual.rEventTotal[ev1]++;
					tempAlternativeCount++;
				} // end if
			}
			this.currentIndividual.robustValueMin+= 1.0 / (1+ tempAlternativeCount);
//			this.currentIndividual.robustValueMin+= 1 / (1+ this.currentIndividual.rEventTotal[ev1]);
		}
		
		// check for ev2:
		if (ev2!= parameters.UNUSED_EVENT){
			tempAlternativeCount= this.currentIndividual.rEventTotal[ev2];
			if (this.currentIndividual.rForEventTimeMove[ev2][time1]== 1){ // current time
//				this.currentIndividual.rForEventTimeMove[ev2][time1]= 0;
//				this.currentIndividual.rEventTotal[ev2]--;
				tempAlternativeCount--;
			}
			if (this.currentIndividual.rForEventTimeMove[ev2][time2]== 0){ // old time, it should be zero assert!
				timeResult= evaluateEventTimeR(ev2, time2, time1, room1);
				if (timeResult== 1){
//					this.currentIndividual.rForEventTimeMove[ev2][time2]= 1;
//					this.currentIndividual.rEventTotal[ev2]++;
					tempAlternativeCount++;
				} // end if
			}
			this.currentIndividual.robustValueMin+= 1 / (1+ tempAlternativeCount);
//			this.currentIndividual.robustValueMin+= 1 / (1+ this.currentIndividual.rEventTotal[ev2]);
		}
		for (int event=0; event< parameters.numEvents; event++){
			if (event== ev1 || event == ev2) continue;
			tempAlternativeCount= this.currentIndividual.rEventTotal[event];
			timeOriginal= convertionManager.intToTime(this.currentIndividual.Data[event]);
			roomOriginal= convertionManager.intToRoom(this.currentIndividual.Data[event]);
			
			// for time1:
			timeResult= evaluateEventTimeR(event, time1, timeOriginal, roomOriginal);
			if (timeResult== 1){
				if (this.currentIndividual.rForEventTimeMove[event][time1] == 0){
//					this.currentIndividual.rForEventTimeMove[event][time1]++;
//					this.currentIndividual.rEventTotal[event]++;
					tempAlternativeCount++;
				}
			} // end if timeResult
			else if (timeResult== 0){
				if (this.currentIndividual.rForEventTimeMove[event][time1] == 1){
//					this.currentIndividual.rForEventTimeMove[event][time1]--;
//					this.currentIndividual.rEventTotal[event]--;
					tempAlternativeCount--;
				}
			} // end if timeResult
			
			// for time2:
			timeResult= evaluateEventTimeR(event, time2, timeOriginal, roomOriginal);
			if (timeResult== 1){
				if (this.currentIndividual.rForEventTimeMove[event][time2] == 0){
//					this.currentIndividual.rForEventTimeMove[event][time2]++;
//					this.currentIndividual.rEventTotal[event]++;
					tempAlternativeCount++;
				}
			} // end if timeResult
			else if (timeResult== 0){
				if (this.currentIndividual.rForEventTimeMove[event][time2] == 1){
//					this.currentIndividual.rForEventTimeMove[event][time2]--;
//					this.currentIndividual.rEventTotal[event]--;
					tempAlternativeCount--;
				}
			} // end if timeResult
			
			this.currentIndividual.robustValueMin+= 1.0 / (1+ tempAlternativeCount);
//			this.currentIndividual.robustValueMin+= 1 / (1+ this.currentIndividual.rEventTotal[event]);
		} // end event for
		
		bd= new BigDecimal( this.currentIndividual.robustValueMin);
		bd= bd.setScale(5, BigDecimal.ROUND_UP);
		this.currentIndividual.robustValueMin= bd.doubleValue();
	}
	
	
	// Important: The following operation changes the individual's robustness arrays!!
	public void evaluateIndividualPartialUpdateMatrix(Individual indiv, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		
		this.currentIndividual= indiv;
		if (ev1== parameters.UNUSED_EVENT && ev2== parameters.UNUSED_EVENT)
			return;			
		int timeOriginal; int roomOriginal;
		// check for each event if its alternative positions at time1 and time2 changes:
		int timeResult;
		this.currentIndividual.robustValueMin= 0;
		// check for ev1:
		if (ev1!= parameters.UNUSED_EVENT){
			if (this.currentIndividual.rForEventTimeMove[ev1][time2]== 1){ // current time
				this.currentIndividual.rForEventTimeMove[ev1][time2]= 0;
				this.currentIndividual.rEventTotal[ev1]--;
			}
			if (this.currentIndividual.rForEventTimeMove[ev1][time1]== 0){ // old time, it should be zero assert!
				timeResult= evaluateEventTimeR(ev1, time1, time2, room2);
				if (timeResult== 1){
					this.currentIndividual.rForEventTimeMove[ev1][time1]= 1;
					this.currentIndividual.rEventTotal[ev1]++;
				} // end if
			}
			this.currentIndividual.robustValueMin+= 1 / (1+ this.currentIndividual.rEventTotal[ev1]);
		}
		
		// check for ev2:
		if (ev2!= parameters.UNUSED_EVENT){
			if (this.currentIndividual.rForEventTimeMove[ev2][time1]== 1){ // current time
				this.currentIndividual.rForEventTimeMove[ev2][time1]= 0;
				this.currentIndividual.rEventTotal[ev2]--;
			}
			if (this.currentIndividual.rForEventTimeMove[ev2][time2]== 0){ // old time, it should be zero assert!
				timeResult= evaluateEventTimeR(ev2, time2, time1, room1);
				if (timeResult== 1){
					this.currentIndividual.rForEventTimeMove[ev2][time2]= 1;
					this.currentIndividual.rEventTotal[ev2]++;
				} // end if
			}
			this.currentIndividual.robustValueMin+= 1 / (1+ this.currentIndividual.rEventTotal[ev2]);
		}
		for (int event=0; event< parameters.numEvents; event++){
			if (event== ev1 || event == ev2) continue;
			timeOriginal= convertionManager.intToTime(this.currentIndividual.Data[event]);
			roomOriginal= convertionManager.intToRoom(this.currentIndividual.Data[event]);
			
			// for time1:
			timeResult= evaluateEventTimeR(event, time1, timeOriginal, roomOriginal);
			if (timeResult== 1){
				if (this.currentIndividual.rForEventTimeMove[event][time1] == 0){
					this.currentIndividual.rForEventTimeMove[event][time1]++;
					this.currentIndividual.rEventTotal[event]++;
				}
			} // end if timeResult
			else if (timeResult== 0){
				if (this.currentIndividual.rForEventTimeMove[event][time1] == 1){
					this.currentIndividual.rForEventTimeMove[event][time1]--;
					this.currentIndividual.rEventTotal[event]--;
				}
			} // end if timeResult
			
			// for time2:
			timeResult= evaluateEventTimeR(event, time2, timeOriginal, roomOriginal);
			if (timeResult== 1){
				if (this.currentIndividual.rForEventTimeMove[event][time2] == 0){
					this.currentIndividual.rForEventTimeMove[event][time2]++;
					this.currentIndividual.rEventTotal[event]++;
				}
			} // end if timeResult
			else if (timeResult== 0){
				if (this.currentIndividual.rForEventTimeMove[event][time2] == 1){
					this.currentIndividual.rForEventTimeMove[event][time2]--;
					this.currentIndividual.rEventTotal[event]--;
				}
			} // end if timeResult
			
			this.currentIndividual.robustValueMin+= 1 / (1+ this.currentIndividual.rEventTotal[event]);
		} // end event for
		
		bd= new BigDecimal( this.currentIndividual.robustValueMin);
		bd= bd.setScale(5, BigDecimal.ROUND_UP);
		this.currentIndividual.robustValueMin= bd.doubleValue();
	}

}

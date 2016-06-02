package robustnessEvaluators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import constraints.ClashSoftConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import data.Event;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.GlobalVars;
import ga.Individual;

public class DisruptEventSC extends RobustnessEvaluatorBase {
	// This is our real robustness measure. But it takes too much time to compute it many times during GA run
	// Move an event to another timeslot other than the current one. 
	// A swap cannot be performed between the events of the same course.
	// if event e1 is moved; compute Pe1
	// else if event e1 is swapped with e2; compute Pe1+Pe2. Moreover, add a Peavg to this sum to penalize swap
	// if an event cannot be moved or swapped with feasibility, then add a penalty of (Peavg*Peavg)

	double avgEventP; // in initial population, average event penalty: Total population penalty / numbrOfIndividuals*numbrOfEvents;
	double minDiffP; // for a single event
	List<Integer> feasTimes;
	Event event; int timeOriginal; int roomOriginal;
	List<Integer> curList= new ArrayList<Integer>();
	int ev2OrigVal; int ev1OrigVal;
	
	public int origTotalP; // Value of the individual before a VNS move is applied.
	public int origClashP, origCurCompP, origMinWorkDaysP, origRoomCapP, origRoomStabP;
	
	public int newTotalP;
	public int newClashP, newCurCompP, newMinWorkDaysP, newRoomCapP, newRoomStabP;
	
	int diffCourse;
	
	public int coursesOriginalRoomStabP, coursesNewRoomStabP; // Two courses are affected
	public int eventsOrigRoomCapP, eventsNewRoomCapP; // Two events are affected
	public int coursesOrigMinWorkDaysP, coursesNewMinWorkDaysP; // Two courses are affected
	public int curriculumsOrigCompP, curriculumsNewCompP; // All the curriculums for the given two courses are affected
	public int eventsOriginalClashP, eventsNewClashP;

	public CurriculumCompactnessConstraint curComConstr;
	public ConstraintBase minWorkDaysConstr;
	public ConstraintBase roomCapConstr;
	public ConstraintBase roomStabConstr;
	
	public ClashSoftConstraint clashConstr;
	public InstructorTimeAvailabilityConstraint timeAvailConstr;
	
	BigDecimal bd;
	
	public DisruptEventSC(RobustnessManager manager) {
		super(manager);
		
		clashConstr= new ClashSoftConstraint(100);
		timeAvailConstr= new InstructorTimeAvailabilityConstraint(100);
		curComConstr= new CurriculumCompactnessConstraint();
		minWorkDaysConstr= new MinimumWorkingDaysConstraint();
		roomCapConstr= new RoomCapacityConstraint();
		roomStabConstr= new RoomStabilityConstraint();

	}

	public void evaluateIndividual(Individual indiv) {
		this.currentIndividual= indiv;
		this.avgEventP= getInitPopAvgP();
		
		double total= 0; // event-based robustness: sum over events.
		double eventResult= 0;
		for (int e=0; e< parameters.numEvents; e++){
			eventResult= evaluateEvent(e);
			if (eventResult > 0)
				total+= eventResult;
		}
		
		double r=  total / (double)parameters.numEvents; // Average is used
		bd= new BigDecimal(r);
		bd= bd.setScale(2, BigDecimal.ROUND_UP);

		indiv.robustValueMin= bd.doubleValue();
	}
	
	private double getInitPopAvgP() {
		return GlobalVars.initialPopAvgEventP;
	}
	

	// Try to assign this event to a timeslot different than the current one.
	// If it is possible to find a new timeslot then return 1; if it is not possible then return zero.
	private double evaluateEvent(int ev) {
		minDiffP= this.currentIndividual.totalPenalty;
		
		event= convertionManager.intToEvent(ev, this.currentIndividual.Data[ev]);
		timeOriginal= event.time; 
		roomOriginal= event.room;
		feasTimes= dataHolder.cTFeasList.get(dataHolder.eventCourseId[ev]);
		int ev2;
		// All positions will be evaluated: and the min new P value will be recorded
		for (int t: feasTimes){
			if (t== timeOriginal)
				continue;
			for (int r=0; r< parameters.numRooms; r++ ){			
				ev2= this.currentIndividual.dataMatrix[r][t]; // it may be empty= unused event
				if (ev2 == ev) continue;
				tryCurrentMove(ev, t, r, ev2, timeOriginal, roomOriginal); // move related data is recorded if it updates the current best move
			
				if (minDiffP== 0)
					return 0;
			} // end r for
		} // end t for
	
		return minDiffP;
	} // end method evaluateEvent


	public double tryCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) { 
		// ev1 should be evaluated for: time2, room2
		// ev2 should be evaluated for: time1, room1
		if (ev1== ev2)
			return 0;	
		
		updateOriginalValue();
		computeOriginalPartialValues(ev1, time2, room2, ev2, time1, room1);

		// update matrix:
		currentIndividual.dataMatrix[room2][time2]= ev1;
		currentIndividual.dataMatrix[room1][time1]= ev2;
		// update curriculum compactness matrix:
		if (ev1!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				if (currentIndividual.timeCurriculum[time1][cur]> 0)
					currentIndividual.timeCurriculum[time1][cur]--; // old position
				currentIndividual.timeCurriculum[time2][cur]++; // new position
			} // end for each
		}
		if (ev2!= parameters.UNUSED_EVENT){
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				if (currentIndividual.timeCurriculum[time2][cur]> 0)
					currentIndividual.timeCurriculum[time2][cur]--; // old position
				currentIndividual.timeCurriculum[time1][cur]++; // new position
			} // end for each
		}
		
		// Now try the current move:
		if (ev1== parameters.UNUSED_EVENT){
			ev2OrigVal= currentIndividual.Data[ev2];
			currentIndividual.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ev2, time1, room1)){
				computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				acceptCurrentMove(ev1, time2, room2, ev2, time1, room1);
			} // end if checkFeas
			// if not returned true:
			currentIndividual.Data[ev2] = ev2OrigVal; // To original values
			// matrix to original values:
			currentIndividual.dataMatrix[room2][time2]= ev2;
			currentIndividual.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				currentIndividual.timeCurriculum[time2][cur]++; // original position
				currentIndividual.timeCurriculum[time1][cur]--; // new position
			} // end for each
			
			return 0;
		} // end if
		
		if (ev2== parameters.UNUSED_EVENT){
			ev1OrigVal= currentIndividual.Data[ev1];
			currentIndividual.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			if (checkFeas(ev1, time2, room2)){
				computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				acceptCurrentMove(ev1, time2, room2, ev2, time1, room1);
			} // end if
			// if not returned true:
			currentIndividual.Data[ev1] = ev1OrigVal; // To original values
			// matrix to original values:
			currentIndividual.dataMatrix[room2][time2]= ev2;
			currentIndividual.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				currentIndividual.timeCurriculum[time1][cur]++; // original position
				currentIndividual.timeCurriculum[time2][cur]--; // new position
			} // end for each
			return 0;
		} // end if
		
		if (ev2!= parameters.UNUSED_EVENT && ev1!= parameters.UNUSED_EVENT){
			ev2OrigVal= currentIndividual.Data[ev2];
			ev1OrigVal= currentIndividual.Data[ev1];
			currentIndividual.Data[ev1] = convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev1], 1, time2, room2);
			currentIndividual.Data[ev2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[ev2], 1, time1, room1);
			if (checkFeas(ev1, time2, room2, ev2, time1, room1)){
				computeNewPartialValues(ev1, time2, room2, ev2, time1, room1);
				acceptCurrentMove(ev1, time2, room2, ev2, time1, room1);
			}
			// if not returned true:
			currentIndividual.Data[ev1] = ev1OrigVal; // To original values
			currentIndividual.Data[ev2] = ev2OrigVal;	// To original values		
			// matrix to original values:
			currentIndividual.dataMatrix[room2][time2]= ev2;
			currentIndividual.dataMatrix[room1][time1]= ev1;
			
			// curriculum compactness matrix to original:
			curList= dataHolder.eventCurriculums.get(ev1);
			for (int cur: curList){
				currentIndividual.timeCurriculum[time1][cur]++; // original position
				currentIndividual.timeCurriculum[time2][cur]--; // new position
			} // end for each
			curList= dataHolder.eventCurriculums.get(ev2);
			for (int cur: curList){
				currentIndividual.timeCurriculum[time2][cur]++; // original position
				currentIndividual.timeCurriculum[time1][cur]--; // new position
			} // end for each
			return 0;
		} // end if

		return 0;
	} // end method evaluateTempMove

	private boolean acceptCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.newTotalP= 0;
		
		this.newClashP= this.origClashP;
		diffCourse= this.eventsOriginalClashP - this.eventsNewClashP;
		this.newClashP-= diffCourse; // diff will be added up, if it is negative, it will increase newValue; it will decrease o.w.
		
		this.newRoomStabP= this.origRoomStabP;
		diffCourse= this.coursesOriginalRoomStabP - this.coursesNewRoomStabP;
		this.newRoomStabP-= diffCourse;
		
		this.newRoomCapP= this.origRoomCapP;
		diffCourse= this.eventsOrigRoomCapP - this.eventsNewRoomCapP;
		this.newRoomCapP-= diffCourse;

		this.newMinWorkDaysP= this.origMinWorkDaysP;
		diffCourse= this.coursesOrigMinWorkDaysP - this.coursesNewMinWorkDaysP;
		this.newMinWorkDaysP-= diffCourse;

		this.newCurCompP= this.origCurCompP;
		diffCourse= this.curriculumsOrigCompP - this.curriculumsNewCompP;
		this.newCurCompP-= diffCourse;

		this.newTotalP= newClashP + newCurCompP+ newMinWorkDaysP + newRoomCapP + newRoomStabP;
		
		double diff= this.newTotalP - this.origTotalP;
		if (ev1!= parameters.UNUSED_EVENT && ev2!= parameters.UNUSED_EVENT){ // a swap move has been performed
			diff+= this.avgEventP;
		}
		if (diff < this.minDiffP){
			this.minDiffP= diff;
		}
	
		this.currentIndividual.totalPenalty= this.origTotalP;
		
		this.currentIndividual.ClashP= this.origClashP;
		this.currentIndividual.CurCompP= this.origCurCompP;
		this.currentIndividual.MinWorkDaysP= this.origMinWorkDaysP;
		this.currentIndividual.RoomCapP= this.origRoomCapP;
		this.currentIndividual.RoomStabP= this.origRoomStabP;
		
		return false; // With false return, vns searcher takes back the changes!
	}

	private void computeNewPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1) {

		this.coursesNewRoomStabP= 0;
		this.eventsNewRoomCapP= 0;
		this.coursesNewMinWorkDaysP= 0;
		
		if (ev1!= parameters.UNUSED_EVENT){
			this.coursesNewRoomStabP= this.roomStabConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev1]);
			this.eventsNewRoomCapP= this.roomCapConstr.computeEvent(this.currentIndividual, ev1, time2, room2);
			this.coursesNewMinWorkDaysP= this.minWorkDaysConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev1]);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.coursesNewRoomStabP+= this.roomStabConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev2]);
			this.eventsNewRoomCapP+= this.roomCapConstr.computeEvent(this.currentIndividual, ev2, time1, room1);
			this.coursesNewMinWorkDaysP+= this.minWorkDaysConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev2]);
		}
		
		this.curriculumsNewCompP= 0;
		for (int cur = 0; cur < parameters.numCurriculums; cur++) {
			if (ev1!= parameters.UNUSED_EVENT && ev2!= parameters.UNUSED_EVENT) {
				if (dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur] == dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur])
					continue;
			}
			if ((ev1!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur])
					|| (ev2!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur]) ){
				this.curriculumsNewCompP+= this.curComConstr.computeCurriculumPartial(this.currentIndividual, cur, time1, time2);	
			}
		} // end cur for
		
		this.eventsNewClashP= 0;
		if (ev1!= parameters.UNUSED_EVENT){
			this.eventsNewClashP= this.clashConstr.computeEvent(this.currentIndividual, ev1, time2, room2);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.eventsNewClashP+= this.clashConstr.computeEvent(this.currentIndividual, ev2, time1, room1);
		}
		
	}

	private void computeOriginalPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1) {
		// Compute the original course values of ev1 and ev2 of the currentInd.

		this.coursesOriginalRoomStabP= 0;
		this.eventsOrigRoomCapP= 0;
		this.coursesOrigMinWorkDaysP= 0; 
		
		if (ev1!= parameters.UNUSED_EVENT){
			this.coursesOriginalRoomStabP= this.roomStabConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev1]);
			this.eventsOrigRoomCapP= this.roomCapConstr.computeEvent(this.currentIndividual, ev1, time1, room1);
			this.coursesOrigMinWorkDaysP= this.minWorkDaysConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev1]);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.coursesOriginalRoomStabP+= this.roomStabConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev2]);
			this.eventsOrigRoomCapP+= this.roomCapConstr.computeEvent(this.currentIndividual, ev2, time2, room2);	
			this.coursesOrigMinWorkDaysP+= this.minWorkDaysConstr.computeSingleCourse(this.currentIndividual, dataHolder.eventCourseId[ev2]);
		}

		// Curriculum should be evaluated, not Course!
		this.curriculumsOrigCompP= 0;
		for (int cur = 0; cur < parameters.numCurriculums; cur++) {
			if (ev1!= parameters.UNUSED_EVENT && ev2!= parameters.UNUSED_EVENT) {
				if (dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur] == dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur])
					continue;
			}
			if ((ev1!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur])
					|| (ev2!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur]) ){
				this.curriculumsOrigCompP+= this.curComConstr.computeCurriculumPartial(this.currentIndividual, cur, time1, time2);
			}
		} // end cur for

		this.eventsOriginalClashP= 0;
		if (ev1!= parameters.UNUSED_EVENT){
			this.eventsOriginalClashP= this.clashConstr.computeEvent(this.currentIndividual, ev1, time1, room1);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.eventsOriginalClashP+= this.clashConstr.computeEvent(this.currentIndividual, ev2, time2, room2);
		}		
		
	}

	private void updateOriginalValue() {
		this.origTotalP= this.currentIndividual.totalPenalty;
		
		this.origClashP= this.currentIndividual.ClashP;		
		this.origCurCompP= this.currentIndividual.CurCompP;	
		this.origMinWorkDaysP= this.currentIndividual.MinWorkDaysP;
		this.origRoomCapP= this.currentIndividual.RoomCapP;
		this.origRoomStabP= this.currentIndividual.RoomStabP;
		
	}

	public boolean checkFeas(int ev1, int t1, int r1, int ev2, int t2, int r2) {
		return checkFeas(ev1, t1, r1) && checkFeas(ev2, t2, r2);
	}
	
	public boolean checkFeas(int ev, int t, int r) {
		for (HardConstraint hc: feasConstraints){
			if (!hc.checkEventFeasibilityInSA(currentIndividual, ev, t, r))
				return false;
		}
		return true;
		
	}
	

	@Override
	public void evaluateIndividualPartial(Individual ind, int ev1, int time2, int room2, int ev2, int time1,
			int room1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void evaluateIndividualPartialUpdateMatrix(Individual ind, int ev1, int time2, int room2, int ev2, int time1,
			int room1) {
		// TODO Auto-generated method stub
		
	}
		
}

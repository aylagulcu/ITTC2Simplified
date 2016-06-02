package crosser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import constraints.ClashConstraint;
import constraints.ClashSoftConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;
import ga.PopulationParameters;
import util.RandomNumberGenerator;

@SuppressWarnings("unused")
public class TimeBasedCX extends crosserBase {
	
	int ev2; int currentTime; int currentRoom;
	int t1; int t2;
	
	int eventParent; int eventChild;

	ArrayList<HardConstraint> myHardConstraints= new ArrayList<HardConstraint>();
	Individual child1= new Individual();
	Individual child2= new Individual();
	
	// Robsutness-related values:
	public double origTotalRobustness;
	public double newTotalRobustness;

	// for partial penalty computation:
	public CurriculumCompactnessConstraint curComConstr;
	public ConstraintBase minWorkDaysConstr;
	public ConstraintBase roomCapConstr;
	public ConstraintBase roomStabConstr;
	
	public ClashSoftConstraint clashConstr;
	public InstructorTimeAvailabilityConstraint timeAvailConstr;
	
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
	
		
	public TimeBasedCX(crossoverManager mngr) {
		super(mngr);
		myOffSprings= new Individual[2];
		
		clashConstr= new ClashSoftConstraint(100);
		timeAvailConstr= new InstructorTimeAvailabilityConstraint(100);
		curComConstr= new CurriculumCompactnessConstraint();
		minWorkDaysConstr= new MinimumWorkingDaysConstraint();
		roomCapConstr= new RoomCapacityConstraint();
		roomStabConstr= new RoomStabilityConstraint();
			
		// used for feasibility check:
		this.myHardConstraints.add(new ClashConstraint(100));
		this.myHardConstraints.add(new InstructorTimeAvailabilityConstraint(100)); // will be checked during CX
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {
		// All matrices and penalty values etc. should be up to date!
		this.myOffSprings= new Individual[2];
		child1= ind1.clone(); 
		child2= ind2.clone();
		
		// Now copy from ind1 to child 2:
		int time= myRandom.nextInt(parameters.numTimeSlots);
//		int time= selectTimeSlotRW(ind1);
		
		for (int room = 0; room < parameters.numRooms; room++) {
			eventParent = ind1.dataMatrix[room][time];
			if (eventParent == parameters.UNUSED_EVENT)
				continue;
			ev2= child2.dataMatrix[room][time];
			if (ev2== eventParent)
				continue;
			currentTime = convertionManager.intToTime(child2.Data[eventParent]);
			currentRoom = convertionManager.intToRoom(child2.Data[eventParent]);
			tryCurrentMove(child2, eventParent, time, room, ev2, currentTime, currentRoom);
		} // end room for

		// Now individual 2 to child 1:
		// Now copy from ind2 to child 1:
		time= myRandom.nextInt(parameters.numTimeSlots);
//		time= selectTimeSlotRW(ind2);
		for (int room = 0; room < parameters.numRooms; room++) {
			eventParent = ind2.dataMatrix[room][time];
			if (eventParent == parameters.UNUSED_EVENT)
				continue;
			ev2= child1.dataMatrix[room][time];
			if (ev2== eventParent)
				continue;
			currentTime = convertionManager.intToTime(child1.Data[eventParent]);
			currentRoom = convertionManager.intToRoom(child1.Data[eventParent]);
			tryCurrentMove(child1, eventParent, time, room, ev2, currentTime, currentRoom);
		} // end room for
		
		myOffSprings[0]= child1;
		myOffSprings[1]= child2;
		return myOffSprings;
	}

	private int selectTimeSlotRW(Individual indiv) {
		int[] timePenalties= new int[parameters.numTimeSlots]; 
		int event= parameters.UNUSED_EVENT;
		
		for (int time=0; time< timePenalties.length; time++){
			timePenalties[time]= 0;
			for (int room=0; room< parameters.numRooms; room++){
				event= indiv.dataMatrix[room][time];
				if (event== parameters.UNUSED_EVENT) continue;
				for (ConstraintBase constr: myCXManager.constraints){
					timePenalties[time]+= constr.computeEvent(indiv, event, time, room);
				} // end constr for each
			} // end room for
		} // end time for
		
		// now return time acc to RW: the less penalty the higher the selection probability
		//	Assign each individual a fitness value as: fitness i= sum penalty /penalty i
		//	Then, sort the individuals acc. to the fitness values
		int totalPenalty= 0; 
		for(int i=0; i< timePenalties.length; i++)
			totalPenalty= totalPenalty +  timePenalties[i];
		
		double totalFitness= 0;
		double[] fitnessValues= new double[timePenalties.length];
		
		for(int i=0; i< timePenalties.length; i++)
			fitnessValues[i]= (totalPenalty /  timePenalties[i]);
		
		// The lower the penalty, the higher the fitness; the higher the fitness, the better the individual
		for(int i=0; i< timePenalties.length; i++)
			totalFitness+= fitnessValues[i];
		
		int[] timeSlotsSorted= new int[timePenalties.length];
		
		// now sort individuals acc to fitness values:
		for (int i=0; i< timeSlotsSorted.length; i++)
			timeSlotsSorted[i]= i;
		int temp;
		for (int i=0; i< timeSlotsSorted.length; i++)
			for (int j=i+1; j< timeSlotsSorted.length; j++){
				if (fitnessValues[timeSlotsSorted[j]]> fitnessValues[timeSlotsSorted[i]]){
					temp= timeSlotsSorted[i];
					timeSlotsSorted[i]= timeSlotsSorted[j];
					timeSlotsSorted[j]= temp;
				} // end if
			} // end j for
		
		
		// now apply RW:
		double randomFitness; 
		double partialTotal;
		
		for (int count=0; count< timeSlotsSorted.length; count++){
			randomFitness= (RandomNumberGenerator.getRandomDouble()* totalFitness);
			partialTotal=0;
			for(int i=0; i< timeSlotsSorted.length; i++){
				partialTotal+= fitnessValues[timeSlotsSorted[i]];
				if (partialTotal >= randomFitness){
					// found the individual:
					return timeSlotsSorted[i];
				}
			} // end i for
		} // end count for
		
		
		return 0;
	}

	@Override
	public boolean checkFeas(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		return checkFeas(ind, ev1, time2, room2) && checkFeas(ind, ev2, time1, room1);
	}
	

	@Override
	public boolean checkFeas(Individual ind, int ev, int time, int room) {
		for (HardConstraint hc: this.myHardConstraints){
			if (!hc.checkEventFeasibilityInSA(ind, ev, time, room))
				return false;
		}
		return true;
	}
	
	@Override
	public void updateOriginalValues(Individual ind) {
		// robustness:
		this.origTotalRobustness= ind.robustValueMin;
		
		// penalty:
		this.origTotalP= ind.totalPenalty;
		
		this.origClashP= ind.ClashP;		
		this.origCurCompP= ind.CurCompP;	
		this.origMinWorkDaysP= ind.MinWorkDaysP;
		this.origRoomCapP= ind.RoomCapP;
		this.origRoomStabP= ind.RoomStabP;
	}
	
	@Override
	public void computeOriginalPartialValues(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		// no partial computation is available for robustness
		
		// Penalty-related:
		// Compute the original course values of ev1 and ev2 of the currentInd.

		this.coursesOriginalRoomStabP= 0;
		this.eventsOrigRoomCapP= 0;
		this.coursesOrigMinWorkDaysP= 0; 
		
		if (ev1!= parameters.UNUSED_EVENT){
			this.coursesOriginalRoomStabP= this.roomStabConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev1]);
			this.eventsOrigRoomCapP= this.roomCapConstr.computeEvent(ind, ev1, time1, room1);
			this.coursesOrigMinWorkDaysP= this.minWorkDaysConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev1]);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.coursesOriginalRoomStabP+= this.roomStabConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev2]);
			this.eventsOrigRoomCapP+= this.roomCapConstr.computeEvent(ind, ev2, time2, room2);	
			this.coursesOrigMinWorkDaysP+= this.minWorkDaysConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev2]);
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
				this.curriculumsOrigCompP+= this.curComConstr.computeCurriculumPartial(ind, cur, time1, time2);
			}
		} // end cur for

		this.eventsOriginalClashP= 0;
		if (ev1!= parameters.UNUSED_EVENT){
			this.eventsOriginalClashP+= this.clashConstr.computeEvent(ind, ev1, time1, room1);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.eventsOriginalClashP+= this.clashConstr.computeEvent(ind, ev2, time2, room2);
		}
		
	}
	


	@Override
	public void computeNewPartialValues(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		// robustness:
		// Important: The following operation does not change the individual's robustness arrays.
		// It modifies only a single field: robustValueMin		
		this.myCXManager.rm.evalIndivRobustnessForCurrentOp(ind, ev1, time2, room2, ev2, time1, room1);
		this.newTotalRobustness= ind.robustValueMin;
		
		// penalty:
		this.coursesNewRoomStabP= 0;
		this.eventsNewRoomCapP= 0;
		this.coursesNewMinWorkDaysP= 0;
		
		if (ev1!= parameters.UNUSED_EVENT){
			this.coursesNewRoomStabP= this.roomStabConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev1]);
			this.eventsNewRoomCapP= this.roomCapConstr.computeEvent(ind, ev1, time2, room2);
			this.coursesNewMinWorkDaysP= this.minWorkDaysConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev1]);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.coursesNewRoomStabP+= this.roomStabConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev2]);
			this.eventsNewRoomCapP+= this.roomCapConstr.computeEvent(ind, ev2, time1, room1);
			this.coursesNewMinWorkDaysP+= this.minWorkDaysConstr.computeSingleCourse(ind, dataHolder.eventCourseId[ev2]);
		}
		
		this.curriculumsNewCompP= 0;
		for (int cur = 0; cur < parameters.numCurriculums; cur++) {
			if (ev1!= parameters.UNUSED_EVENT && ev2!= parameters.UNUSED_EVENT) {
				if (dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur] == dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur])
					continue;
			}
			if ((ev1!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur])
					|| (ev2!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur]) ){
				this.curriculumsNewCompP+= this.curComConstr.computeCurriculumPartial(ind, cur, time1, time2);	
			}
		} // end cur for
		
		this.eventsNewClashP= 0;
		if (ev1!= parameters.UNUSED_EVENT){
			this.eventsNewClashP= this.clashConstr.computeEvent(ind, ev1, time2, room2);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.eventsNewClashP+= this.clashConstr.computeEvent(ind, ev2, time1, room1);
		}
		
	}
	
	@Override
	public boolean acceptCurrentMove(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) {
		this.myCXManager.rm.evalIndivRobustnessForCurrentOpUpdateMatrix(ind, ev1, time2, room2, ev2, time1, room1);
		
		/*
		double r1= ind.robustValueMin;
		BigDecimal bd= new BigDecimal(r1);
		bd= bd.setScale(5, BigDecimal.ROUND_UP);
		
		this.myCXManager.rm.evalIndivRobustness(ind);
		double r2= ind.robustValueMin;
		// assert r1== ind.robustValueMin;
		
		BigDecimal bd2= new BigDecimal(r2);
		bd2= bd2.setScale(5, BigDecimal.ROUND_UP);
		
		assert bd.equals(bd2);
		*/
		

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
		
		/*
		this.myCXManager.pEvaluator.evaluateIndividual(ind);
		assert ind.MinWorkDaysP== this.newMinWorkDaysP; // System.out.println("MinWorkDays: "+ ind.MinWorkDaysP+ "Fast computed:"+ this.newMinWorkDaysP);
		assert ind.RoomCapP== this.newRoomCapP; // System.out.println("Room capacity: "+ ind.RoomCapP+ "Fast computed:"+ this.newRoomCapP);
		assert ind.RoomStabP== this.newRoomStabP; // System.out.println("Room stability: "+ ind.RoomStabP+ "Fast computed:"+ this.newRoomStabP);
		assert ind.CurCompP== this.newCurCompP; // System.out.println("CurCompactess: "+ ind.CurCompP+ "Fast computed:"+ this.newCurCompP);
		assert ind.ClashP== this.newClashP;  
		assert ind.totalPenalty== this.newTotalP;
		*/

		ind.totalPenalty= this.newTotalP;
		
		ind.ClashP= this.newClashP;
		ind.CurCompP= this.newCurCompP;
		ind.MinWorkDaysP= this.newMinWorkDaysP;
		ind.RoomCapP= this.newRoomCapP;
		ind.RoomStabP= this.newRoomStabP;
		return true;
	}





}

package crosser;

import java.util.ArrayList;
import java.util.HashSet;

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

public class DayBasedCX extends crosserBase {
	
	int ev2; int currentTime; int currentRoom;
	int t1; int t2;
	
	int dayP; int[] dayPen= new int[parameters.numDays]; 
	int nmbDays= 1; 
	HashSet<Integer> selectedDays= new HashSet<Integer>();
	int eventParent; int eventChild;

	ArrayList<ConstraintBase> myConstraints= new ArrayList<ConstraintBase>();
	ArrayList<HardConstraint> myHardConstraints= new ArrayList<HardConstraint>();
	Individual child1= new Individual();
	Individual child2= new Individual();
	

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
	
	
	
	public DayBasedCX(crossoverManager mngr) {
		super(mngr);
		myOffSprings= new Individual[2];
		
		clashConstr= new ClashSoftConstraint(100);
		timeAvailConstr= new InstructorTimeAvailabilityConstraint(100);
		curComConstr= new CurriculumCompactnessConstraint();
		minWorkDaysConstr= new MinimumWorkingDaysConstraint();
		roomCapConstr= new RoomCapacityConstraint();
		roomStabConstr= new RoomStabilityConstraint();
		
		// used for day selection:
		this.myConstraints.add(new InstructorTimeAvailabilityConstraint(100));
		this.myConstraints.add(new CurriculumCompactnessConstraint());
		
		// used for feasibility check:
//		this.myHardConstraints.add(new ClashConstraint(100));
		this.myHardConstraints.add(new InstructorTimeAvailabilityConstraint(100)); // will be checked during CX
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {
		// All matrices and penalty values etc. should be up to date!
		this.myOffSprings= new Individual[2];
		child1= ind1.clone(); 
		child2= ind2.clone();
		
		// Step1 : Select the best days in each of the parents.
		// For each day, find a total penalty of all the events scheduled on that day for the constraints:
		// Clash, TimeAvailability, CurriculumCompactness ---> Constraints that may be improved by time change in the child
		// Why not others: Room is not changed; 
		// MinWorkingDays for the current day returns 0 penalty if the event is scheduled on the current day. How to evalute if it is not???
		
		// Step 2:
		// Create Chil A: Copy Parent A's best day to Parent B. 
		// Move only to empty and feasible positions. 
		// Remove duplicates
		// Create Child B the same way.
		
//		// Evaluate Day penalties
//		for (int day=0; day< parameters.numDays; day++){
//			dayP= 0;
//			for (int room=0; room< parameters.numRooms; room++){
//				for (int time= day* parameters.numDailyPeriods; time< (day+1)*parameters.numDailyPeriods; time++){
//					eventParent= ind1.dataMatrix[room][time];
//					if (eventParent== parameters.UNUSED_EVENT) continue;
//					for (ConstraintBase constr: myConstraints){
//						dayP+= constr.computeEvent(ind1, eventParent, time, room);
//					} // end constr for each
//				} // end time for
//			} // end room for
//			dayPen[day]= dayP;
//		} // end day for
//		selectDays(dayPen);
		
//		for (int day: selectedDays) {
			// Now copy from ind1 to child 2:
		int t= myRandom.nextInt(parameters.numTimeSlots);
		int limit= Math.min(parameters.numTimeSlots, t+1);
			for (int room = 0; room < parameters.numRooms; room++) {
				for (int time = t; time < limit ; time++) {
//				for (int time = day * parameters.numDailyPeriods; time < (day + 1)* parameters.numDailyPeriods; time++) {
					eventParent = ind1.dataMatrix[room][time];
					if (eventParent == parameters.UNUSED_EVENT)
						continue;
					ev2= child2.dataMatrix[room][time];
					if (ev2== eventParent)
						continue;
					currentTime = convertionManager.intToTime(child2.Data[eventParent]);
					currentRoom = convertionManager.intToRoom(child2.Data[eventParent]);
					tryCurrentMove(child2, eventParent, time, room, ev2, currentTime, currentRoom);
					if (ev2!= parameters.UNUSED_EVENT)
						myCXManager.applyMicroSA(child2, ev2);
				} // end time for
			} // end room for
//		} // en day for
		myCXManager.applyMicroSA(child2);
		
		// Now individual 2 to child 1:
//		// Find the best day in ind2:
//		for (int day=0; day< parameters.numDays; day++){
//			dayP= 0;
//			for (int room=0; room< parameters.numRooms; room++){
//				for (int time= day* parameters.numDailyPeriods; time< (day+1)*parameters.numDailyPeriods; time++){
//					eventParent= ind2.dataMatrix[room][time];
//					if (eventParent== parameters.UNUSED_EVENT) 
//						continue;
//					for (ConstraintBase constr: myConstraints){
//						dayP+= constr.computeEvent(ind2, eventParent, time, room);
//					} // end constr for each
//				} // end time for
//			} // end room for
//			dayPen[day]= dayP;
//		} // end day for
//		selectDays(dayPen);

//		for (int day: selectedDays) {
			// Now copy from ind2 to child 1:
			t= myRandom.nextInt(parameters.numTimeSlots);
			limit= Math.min(parameters.numTimeSlots, t+1);
			for (int room = 0; room < parameters.numRooms; room++) {
//				for (int time = day * parameters.numDailyPeriods; time < (day + 1)* parameters.numDailyPeriods; time++) {
				for (int time = t; time < limit ; time++) {
					eventParent = ind2.dataMatrix[room][time];
					if (eventParent == parameters.UNUSED_EVENT)
						continue;
					ev2= child1.dataMatrix[room][time];
					if (ev2== eventParent)
						continue;
					currentTime = convertionManager.intToTime(child1.Data[eventParent]);
					currentRoom = convertionManager.intToRoom(child1.Data[eventParent]);
					tryCurrentMove(child1, eventParent, time, room, ev2, currentTime, currentRoom);
					if (ev2!= parameters.UNUSED_EVENT)
						myCXManager.applyMicroSA(child2, ev2);
				} // end time for
			} // end room for
//		} // end day for
		
		myCXManager.applyMicroSA(child1);
		
		myOffSprings[0]= child1;
		myOffSprings[1]= child2;
		return myOffSprings;
	}

	private void selectDays(int[] dayPen2) {
//		System.out.println("Day penalties: ");
//		for (int p: dayPen2)
//			System.out.print("\tPenalty:\t"+ p);
//		System.out.println();
		
		selectedDays.clear();
		
		double total=0;
		for (int i: dayPen2)
			total+= i;
		
		double[] newPen= new double[dayPen2.length];
		for (int d=0; d< newPen.length; d++){
			newPen[d]= total / dayPen2[d];
		} // end d for
		
		total=0; // total of new penalty values:
		for (double i: newPen)
			total+= i;
		
		int[] sortedDays= new int[newPen.length];
		for (int d=0; d< newPen.length; d++)
			sortedDays[d]= d;

		int temp;
		for (int i=0; i< sortedDays.length; i++)
			for (int j=i+1; j< sortedDays.length; j++){
				if (newPen[sortedDays[j]]> newPen[sortedDays[i]]){
					temp= sortedDays[i];
					sortedDays[i]= sortedDays[j];
					sortedDays[j]= temp;
				} // end if
			} // end j for
				
//		for (int i: sortedDays)
//			System.out.println("sorted day: "+ i+ " new penalty(=actual total/actual ind penalty):"+ newPen[i]);
		
		double randPenalty;
		int day;
		while(selectedDays.size()< nmbDays){
			randPenalty= (myRandom.nextFloat() * total);
			double partialTotal=0;
			for(int i=0; i< newPen.length; i++){
				partialTotal+= newPen[sortedDays[i]];
				if (partialTotal >= randPenalty){
					day = sortedDays[i];
					selectedDays.add(day);
					break;
				}
			} // end i for
		} // end while
	
//		System.out.println("Selected Days");
//		for (int d : selectedDays)
//			System.out.print("\tDay:\t"+d);
//		
//		System.out.println();
//		System.out.print("Total : "+ total+ "  Random penalty: "+ randPen+ "  Second day: "+ days[1]);
//		System.out.println();
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
		this.origTotalP= ind.totalPenalty;
		
		this.origClashP= ind.ClashP;		
		this.origCurCompP= ind.CurCompP;	
		this.origMinWorkDaysP= ind.MinWorkDaysP;
		this.origRoomCapP= ind.RoomCapP;
		this.origRoomStabP= ind.RoomStabP;
	}
	
	@Override
	public void computeOriginalPartialValues(Individual ind, int ev1, int time2, int room2, int ev2, int time1, int room1) {
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
	public boolean acceptCurrentMove(Individual ind) {
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
		
		
//		this.myCXManager.mySimpleEvaluator.evaluateIndividual(ind);
//		assert ind.MinWorkDaysP== this.newMinWorkDaysP; // System.out.println("MinWorkDays: "+ ind.MinWorkDaysP+ "Fast computed:"+ this.newMinWorkDaysP);
//		assert ind.RoomCapP== this.newRoomCapP; // System.out.println("Room capacity: "+ ind.RoomCapP+ "Fast computed:"+ this.newRoomCapP);
//		assert ind.RoomStabP== this.newRoomStabP; // System.out.println("Room stability: "+ ind.RoomStabP+ "Fast computed:"+ this.newRoomStabP);
//		assert ind.CurCompP== this.newCurCompP; // System.out.println("CurCompactess: "+ ind.CurCompP+ "Fast computed:"+ this.newCurCompP);
//		assert ind.ClashP== this.newClashP;  
//		assert ind.totalPenalty== this.newTotalP;
		

		ind.totalPenalty= this.newTotalP;
		
		ind.ClashP= this.newClashP;
		ind.CurCompP= this.newCurCompP;
		ind.MinWorkDaysP= this.newMinWorkDaysP;
		ind.RoomCapP= this.newRoomCapP;
		ind.RoomStabP= this.newRoomStabP;
		return true;
	}





}

package vnSearchers;

import ga.Individual;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import robustnessEvaluators.RobustnessManager;
import util.FileOperations;
import constraints.ClashSoftConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.HardConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import data.dataHolder;
import data.parameters;
import evaluators.PenaltyEvaluator;

public class SAforP extends SABase {
	public CurriculumCompactnessConstraint curComConstr;
	public ConstraintBase minWorkDaysConstr;
	public ConstraintBase roomCapConstr;
	public ConstraintBase roomStabConstr;
	
	public ClashSoftConstraint clashConstr;
	public InstructorTimeAvailabilityConstraint timeAvailConstr;
	
	// Feasibility will always be maintained.
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
	
	double Tinit= 15.25;
	double Tcurrent;
	double Tfinal= 0.1567;
	double coolratio= 0.99;
	double acceptRatio= 0.0364;
	
	double outerLimit= Math.log(Tfinal / Tinit )/ Math.log(coolratio); 
	// total number of iterations= number of outer * number of inner
	double totalAllowedIterations= 70821864 * 0.2; // benchmarking result for HP is 216 seconds. 
	// In 1 seconds, 327879 iterations are performed.
	// inner iteration count 
	// steps in the inner loop (loop for each T level)
	double innerLimit= (totalAllowedIterations/ outerLimit);
	
	ArrayList<Integer> penaltyAtEachTemperature= new ArrayList<Integer>();
	
	Individual bestIndiv= new Individual();
	
	public SAforP(List<ConstraintBase> constr) {
		super(constr);
		this.constraints= new ArrayList<ConstraintBase>();
		clashConstr= new ClashSoftConstraint(100);
		timeAvailConstr= new InstructorTimeAvailabilityConstraint(100);
		curComConstr= new CurriculumCompactnessConstraint();
		minWorkDaysConstr= new MinimumWorkingDaysConstraint();
		roomCapConstr= new RoomCapacityConstraint();
		roomStabConstr= new RoomStabilityConstraint();
		this.constraints.add(clashConstr);
		this.constraints.add(timeAvailConstr);
		this.constraints.add(curComConstr);
		this.constraints.add(minWorkDaysConstr);
		this.constraints.add(roomCapConstr);
		this.constraints.add(roomStabConstr);
	
		feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: this.constraints)
			if (con instanceof HardConstraint)
				feasConstraints.add((HardConstraint) con);		
		
		optConstraints= new ArrayList<ConstraintBase>();
		for (ConstraintBase con: this.constraints)
			if (! (con instanceof HardConstraint))
				optConstraints.add((ConstraintBase) con);
		
		this.pEvaluator= new PenaltyEvaluator(this.constraints);
		this.rm= new RobustnessManager(this.originalConstraints);

		VNSList.clear();
		VNSList.add(new MoveNew(this, 0)); // put Move in the first order!
		VNSList.add(new SwapNew(this, 1));

	}

	public void applySA(Individual indiv){	
//		if (!indiv.isFeasible)
//			return;
//		System.out.println("Outer loop count: +" +outerLimit+ " Inner loop count: "+ innerLimit);
//		System.out.println("Before SA, Current penalty: " + indiv.totalPenalty);
		
		// Attention: VNS searcher should return up to date values of penalty and robustness!!!
		this.currentInd= indiv; // with the same reference. This reference should not be changed!!!
		copyCurrentToBest(); // copy the fields of to current individual to the best individual

		penaltyAtEachTemperature.clear();
	
		boolean result= false;

		int innerCounter= 0;
		int countAccepted= 0;
		Tcurrent= Tinit;
		penaltyAtEachTemperature.add(this.currentInd.totalPenalty);
		int counter= 0;
//		Date startLS = new Date();
		do{
			innerCounter= 0; countAccepted= 0;
			while(continueSearch(innerCounter, (int)innerLimit, countAccepted)){
				this.searcher= selectSearcher();
				result= this.searcher.search();
				counter++;
				if (result){ 
					countAccepted+= 1; 
					updateBestIndiv();
				}
				innerCounter+= 1;
			} // end while
			penaltyAtEachTemperature.add(this.currentInd.totalPenalty);
//			System.out.println("For the current temperature, "+ Tcurrent+",  inner counter and count accepted are: "+ innerCounter+"  and "+ countAccepted);
			Tcurrent*= coolratio;
		}while (Tcurrent >= Tfinal);

		// Complete the above total number of iterations (use the iteration budget):
//		System.out.println("Iterations remaining: "+ (totalAllowedIterations- counter));
		while (counter< totalAllowedIterations){
			this.searcher= selectSearcher();
			result= this.searcher.search();
			counter++;
		}
			
//		Date endLS= new Date();
//		float diff= (endLS.getTime()- startLS.getTime())/1000; // to get time in seconds
//		System.out.println(counter+ "iterations in SA took "+ diff + " seconds.");
		try {
			FileOperations.writeToFile(penaltyAtEachTemperature);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		updateBestIndiv();
		
		System.out.println("SA Best Individual penalty:  "+ this.bestIndiv.totalPenalty);
		if (this.bestIndiv.totalPenalty < this.currentInd.totalPenalty){
			bestIndiv.createMatrix();
			bestIndiv.createTimeCurMatrix();
			this.bestIndiv.clone(this.currentInd); // Copy the fields of best to the current
		} // end if
		System.out.println("After SA Current Individual penalty:  "+ this.currentInd.totalPenalty);
		this.rm.evalIndivRobustness(currentInd);
	}

	private void copyCurrentToBest() {
		for(int i=0;i< this.currentInd.Data.length;i++)
			this.bestIndiv.Data[i]= this.currentInd.Data[i];
		
		this.bestIndiv.isFeasible= this.currentInd.isFeasible;
		this.bestIndiv.totalPenalty= this.currentInd.totalPenalty;
		this.bestIndiv.ClashP= this.currentInd.ClashP;
		this.bestIndiv.ComletenessP= this.currentInd.ComletenessP;
		this.bestIndiv.CurCompP= this.currentInd.CurCompP;
		this.bestIndiv.MinWorkDaysP= this.currentInd.MinWorkDaysP;
		this.bestIndiv.RoomCapP= this.currentInd.RoomCapP;
		this.bestIndiv.RoomStabP= this.currentInd.RoomStabP;
		this.bestIndiv.TimeAvailP= this.currentInd.TimeAvailP;
		this.bestIndiv.RoomUniquenessP= this.currentInd.RoomUniquenessP;
	}


	private void updateBestIndiv() {
		if (this.currentInd.totalPenalty< this.bestIndiv.totalPenalty){
			for(int i=0;i< this.bestIndiv.Data.length;i++)
				this.bestIndiv.Data[i]= this.currentInd.Data[i];
			
			this.bestIndiv.isFeasible= this.currentInd.isFeasible;
			this.bestIndiv.totalPenalty= this.currentInd.totalPenalty;
			
			this.bestIndiv.ClashP= this.currentInd.ClashP;
			this.bestIndiv.ComletenessP= this.currentInd.ComletenessP;
			this.bestIndiv.CurCompP= this.currentInd.CurCompP;
			this.bestIndiv.MinWorkDaysP= this.currentInd.MinWorkDaysP;
			this.bestIndiv.RoomCapP= this.currentInd.RoomCapP;
			this.bestIndiv.RoomStabP= this.currentInd.RoomStabP;
			this.bestIndiv.TimeAvailP= this.currentInd.TimeAvailP;
			this.bestIndiv.RoomUniquenessP= this.currentInd.RoomUniquenessP;
		} // end if			
	}

	private boolean continueSearch(int innerCounter, int limit, int countAccepted) {
		if (innerCounter>= limit)
			return false;
		else if (((double)countAccepted/ limit) >= acceptRatio)
			return false; // do not continue in the same Temperature level
		return true;
	}

	
	@Override
	public void updateOriginalValue() {
		this.origTotalP= this.currentInd.totalPenalty;
		
		this.origClashP= this.currentInd.ClashP;		
		this.origCurCompP= this.currentInd.CurCompP;	
		this.origMinWorkDaysP= this.currentInd.MinWorkDaysP;
		this.origRoomCapP= this.currentInd.RoomCapP;
		this.origRoomStabP= this.currentInd.RoomStabP;
	}
	
	@Override
	public void computeOriginalPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1){
		// Compute the original course values of ev1 and ev2 of the currentInd.

		this.coursesOriginalRoomStabP= 0;
		this.eventsOrigRoomCapP= 0;
		this.coursesOrigMinWorkDaysP= 0; 
		
		if (ev1!= parameters.UNUSED_EVENT){
			this.coursesOriginalRoomStabP= this.roomStabConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev1]);
			this.eventsOrigRoomCapP= this.roomCapConstr.computeEvent(this.currentInd, ev1, time1, room1);
			this.coursesOrigMinWorkDaysP= this.minWorkDaysConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev1]);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.coursesOriginalRoomStabP+= this.roomStabConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev2]);
			this.eventsOrigRoomCapP+= this.roomCapConstr.computeEvent(this.currentInd, ev2, time2, room2);	
			this.coursesOrigMinWorkDaysP+= this.minWorkDaysConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev2]);
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
				this.curriculumsOrigCompP+= this.curComConstr.computeCurriculumPartial(this.currentInd, cur, time1, time2);
			}
		} // end cur for

		this.eventsOriginalClashP= 0;
		if (ev1!= parameters.UNUSED_EVENT){
			this.eventsOriginalClashP= this.clashConstr.computeEvent(this.currentInd, ev1, time1, room1);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.eventsOriginalClashP+= this.clashConstr.computeEvent(this.currentInd, ev2, time2, room2);
		}		
		
	}
	
	@Override
	public void computeNewPartialValues(int ev1, int time2, int room2, int ev2, int time1, int room1){
		this.coursesNewRoomStabP= 0;
		this.eventsNewRoomCapP= 0;
		this.coursesNewMinWorkDaysP= 0;
		
		if (ev1!= parameters.UNUSED_EVENT){
			this.coursesNewRoomStabP= this.roomStabConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev1]);
			this.eventsNewRoomCapP= this.roomCapConstr.computeEvent(this.currentInd, ev1, time2, room2);
			this.coursesNewMinWorkDaysP= this.minWorkDaysConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev1]);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.coursesNewRoomStabP+= this.roomStabConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev2]);
			this.eventsNewRoomCapP+= this.roomCapConstr.computeEvent(this.currentInd, ev2, time1, room1);
			this.coursesNewMinWorkDaysP+= this.minWorkDaysConstr.computeSingleCourse(this.currentInd, dataHolder.eventCourseId[ev2]);
		}
		
		this.curriculumsNewCompP= 0;
		for (int cur = 0; cur < parameters.numCurriculums; cur++) {
			if (ev1!= parameters.UNUSED_EVENT && ev2!= parameters.UNUSED_EVENT) {
				if (dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur] == dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur])
					continue;
			}
			if ((ev1!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev1]][cur])
					|| (ev2!= parameters.UNUSED_EVENT && dataHolder.course_Curriculum[dataHolder.eventCourseId[ev2]][cur]) ){
				this.curriculumsNewCompP+= this.curComConstr.computeCurriculumPartial(this.currentInd, cur, time1, time2);	
			}
		} // end cur for
		
		this.eventsNewClashP= 0;
		if (ev1!= parameters.UNUSED_EVENT){
			this.eventsNewClashP= this.clashConstr.computeEvent(this.currentInd, ev1, time2, room2);
		}
		if (ev2!= parameters.UNUSED_EVENT){
			this.eventsNewClashP+= this.clashConstr.computeEvent(this.currentInd, ev2, time1, room1);
		}
	}

	
	
	@Override
	public boolean acceptCurrentMove(int ev1, int time2, int room2, int ev2, int time1, int room1) {
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
		
//		this.pEvaluator.evaluateIndividual(currentInd);
//		assert this.currentInd.MinWorkDaysP== this.newMinWorkDaysP; // System.out.println("MinWorkDays: "+ this.currentInd.MinWorkDaysP+ "Fast computed:"+ this.newMinWorkDaysP);
//		assert this.currentInd.RoomCapP== this.newRoomCapP; // System.out.println("Room capacity: "+ this.currentInd.RoomCapP+ "Fast computed:"+ this.newRoomCapP);
//		assert this.currentInd.RoomStabP== this.newRoomStabP; // System.out.println("Room stability: "+ this.currentInd.RoomStabP+ "Fast computed:"+ this.newRoomStabP);
//		assert this.currentInd.CurCompP== this.newCurCompP; // System.out.println("CurCompactess: "+ this.currentInd.CurCompP+ "Fast computed:"+ this.newCurCompP);
//		assert this.currentInd.ClashP== this.newClashP;
//		assert currentInd.totalPenalty== this.newTotalP;
			
		if (this.newTotalP <= this.origTotalP){ 
			this.currentInd.totalPenalty= this.newTotalP;
			
			this.currentInd.ClashP= this.newClashP;
			this.currentInd.CurCompP= this.newCurCompP;
			this.currentInd.MinWorkDaysP= this.newMinWorkDaysP;
			this.currentInd.RoomCapP= this.newRoomCapP;
			this.currentInd.RoomStabP= this.newRoomStabP;
			return true;
		}
		else {
			double acceptProbability= Math.exp(- (this.newTotalP - this.origTotalP)/ Tcurrent);
			double rnd= this.myRandom.nextDouble(); // [0,1)
			if (rnd < acceptProbability){
				this.currentInd.totalPenalty= this.newTotalP;
				
				this.currentInd.ClashP= this.newClashP;
				this.currentInd.CurCompP= this.newCurCompP;
				this.currentInd.MinWorkDaysP= this.newMinWorkDaysP;
				this.currentInd.RoomCapP= this.newRoomCapP;
				this.currentInd.RoomStabP= this.newRoomStabP;
				return true;
			}
			else{
				this.currentInd.totalPenalty= this.origTotalP;
				
				this.currentInd.ClashP= this.origClashP;
				this.currentInd.CurCompP= this.origCurCompP;
				this.currentInd.MinWorkDaysP= this.origMinWorkDaysP;
				this.currentInd.RoomCapP= this.origRoomCapP;
				this.currentInd.RoomStabP= this.origRoomStabP;
				
				return false; // With false return, vns searcher takes back the changes!
			}
		} // end else
	}
	
}

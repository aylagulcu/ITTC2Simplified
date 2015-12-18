package robustnessEvaluators;

import java.util.ArrayList;
import java.util.HashSet;

import constraints.ConstraintBase;
import constraints.HardConstraint;
import constraints.Unary;
import data.parameters;
import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

public class TimeBasedR3 extends RobustnessEvaluatorBase {

	// R_t= (C_t-U_t) * E_t^1/2/ C_t
	// C_t= Sum C_t,r
	// C_t,r= N_t,r / N_t
	// N_t: # of events demanding time slot t
	// E_t: # of empty rooms at time slot t
	
	public ArrayList<Unary> unaryConstraints;
	public Object[] eventFeasTimes;
	public Object[] eventFeasRooms;
	// The following arrays keeps the number of events schedulable:
	public int[][] Nroomtime; // number of events schedulable at given room-timeslot
	public int[] Ntime; // number of events schedulable at given timeslot
	public int Ntotal; // product sum of robustness of each timeslot and corresponding demand is divided by this value
	public float[] Ctime; // Capacity at hand at each timeslot
	public float[][] Croomtime; 
	public float[] robustnessTime;
	
	public TimeBasedR3(RobustnessManager manager) {
		super(manager);
		this.feasConstraints= new ArrayList<HardConstraint>();
		for (ConstraintBase con: manager.constraints)
			if (con instanceof HardConstraint){
				this.feasConstraints.add((HardConstraint) con);
			}

		initializeFeasResources();
		createResourceMatrix();
	}

	@SuppressWarnings("unchecked")
	private void createResourceMatrix() {
		Nroomtime= new int[parameters.numRooms][parameters.numTimeSlots];
		Ntime= new int[parameters.numTimeSlots];
		for (int t= 0; t< parameters.numTimeSlots; t++){
			Ntime[t]= 0;
			for (int r= 0; r< parameters.numRooms; r++){
				Nroomtime[r][t]= 0;
			} // end r for
		} // end t for
		
		HashSet<Integer> times;
		HashSet<Integer> rooms;
		for (int event= 0; event< parameters.numEvents; event++){
			times= (HashSet<Integer>) this.eventFeasTimes[event];
			rooms= (HashSet<Integer>) this.eventFeasRooms[event];
			for (int t: times){
				this.Ntime[t]+= 1;
				for (int r: rooms){
					this.Nroomtime[r][t]+= 1;
				}
			}
		} // end event for		
		
		Ntotal= 0;
		for (int t= 0; t< parameters.numTimeSlots; t++)
			Ntotal+= this.Ntime[t];
				
		Ctime= new float[parameters.numTimeSlots];
		Croomtime= new float[parameters.numRooms][parameters.numTimeSlots];
		for (int t= 0; t< parameters.numTimeSlots; t++){
			Ctime[t]= 0;
			for (int r=0; r< parameters.numRooms; r++){
				Croomtime[r][t]= (float)Nroomtime[r][t] / (float)Ntime[t];
				assert Croomtime[r][t]<=1;
				Ctime[t]+= Croomtime[r][t];
			}
		}			
	}

	private void initializeFeasResources() {
		eventFeasTimes= new Object[parameters.numEvents]; // set of feasible time slots for each event
		eventFeasRooms= new Object[parameters.numEvents]; // set of feasible rooms for each event

		// According to the unary constraints given above, 
		// finds the feasible time slots and rooms for each event
		unaryConstraints = new ArrayList<Unary>();	
		for (HardConstraint con: this.feasConstraints)
			if (con instanceof Unary)
				this.unaryConstraints.add((Unary) con);
		// Below feasible timeslots lists are filled:
		// Only starting time slots that an event can be scheduled at is included in this list.
		HashSet<Integer> tempStartTimes;
		boolean result;
		for (int event=0; event <parameters.numEvents; event++){ 
			tempStartTimes= new HashSet<Integer>(); 
			for (int time=0; time< parameters.numTimeSlots; time++){ 
				result= true;
				for (Unary uc: this.unaryConstraints){
					result= uc.checkEventTime(event, time); // according to the constraints in event initializer 
					if (!result)
						break;
				} // end uc for
				if (result)
					tempStartTimes.add(time); // True if all the hours can be placed in the consecutive timeslots.
			} // end time for
			this.eventFeasTimes[event]= tempStartTimes; 
		} // end event for 

		// Now, feasible room lists are filled:
		HashSet<Integer> rooms;
		for (int event=0; event <parameters.numEvents; event++){ 
			rooms= new HashSet<Integer>(); 
			for (int r=0; r< parameters.numRooms; r++){ 
				result= true;
				for (Unary uc: this.unaryConstraints){
					result= uc.checkEventRoom(event, r); // according to the constraints in event initializer 
					if (!result)
						break;
				} // end uc for
				if (result)
					rooms.add(r); // True if all the hours can be placed in the consecutive timeslots.
			} // end time for
			this.eventFeasRooms[event]= rooms; 
		} // end event for 
	}
	
	public void evaluatePop(Population pop) {
		for (int ind=0; ind< PopulationParameters.populationSize; ind++)
			evaluateIndividual(pop.individuals[ind]);

	}

	public void evaluateIndividual(Individual indiv) {
		float Rtotal= 0; // total robustness
		robustnessTime= new float[parameters.numTimeSlots];
		float[] Utime= new float[parameters.numTimeSlots];
		float[] Etime= new float[parameters.numTimeSlots]; 
		for (int t=0; t< parameters.numTimeSlots; t++){
			robustnessTime[t]= 0;
			Utime[t]= 0;
			Etime[t]= 0;
		}
		// Compute utilization acc.to current assignment:
		indiv.createMatrix();
		for (int time= 0; time< parameters.numTimeSlots; time++){
			Utime[time]= 0;
			robustnessTime[time]= 0;
			Etime[time]= 0;
			for (int room= 0; room< parameters.numRooms; room++){
				if (indiv.dataMatrix[room][time]!= parameters.UNUSED_EVENT)
					Utime[time]+= Croomtime[room][time];
				else
					Etime[time]+= 1;
			} // end room for
			robustnessTime[time]= (float) ((Ctime[time] - Utime[time]) / (Ctime[time]* Math.sqrt(Etime[time])));
			Rtotal+= robustnessTime[time] * Ntime[time];
		} // end t for
		
		Rtotal= Rtotal / (float)this.Ntotal;
		indiv.robustnessValue= Rtotal;
	}

}

package ga;

import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class Individual implements Cloneable{

//	int index;
	
	public int[] Data= new int[parameters.dataArraySize];
	// 12 bits: course id + (now for distinct lectures)
	// 4 bits:lectures + 8 bits: time slot start + 8 bits: room
	public int[][] dataMatrix= new int[parameters.numRooms][parameters.numTimeSlots]; // Matrix representation of Data
	
	// number of events scheduled at time that belongs to curriculum cur:
	// required for fast computation of curriculum compactness constraint:
	public int[][] timeCurriculum= new int[parameters.numTimeSlots][parameters.numCurriculums]; 

	public boolean isFeasible= true;

	public int totalPenalty;
	
	public int ClashP;
	public int ComletenessP;
	public int TimeAvailP;
	public int RoomUniquenessP;
	
	public int CurCompP;
	public int MinWorkDaysP;
	public int RoomCapP;
	public int RoomStabP;
	
//	// The followings will be filled out for the constraints
	public int[] curCompP= new int[parameters.numCourses];
	public int[] minWorkDaysP= new int[parameters.numCourses];
	public int[] roomCapP= new int[parameters.numCourses];
	public int[] roomStabP= new int[parameters.numCourses];
	
	
	public double robustValueMin; // = 1/ 1+robustValue
	public int[][] rForEventTimeMove= new int[parameters.numEvents][parameters.numTimeSlots];
	public double[] rEventTotal= new double[parameters.numEvents];
	
	public int rank; // Individuals with rank=1 form the 1st non-dominated front
	public float crowdDistance;
	
	public Individual() {	
		// Set default values to fields:
		// Data:
		for (int i=0; i<this.Data.length; i++)
			Data[i]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[i], 1, 
					parameters.UNUSED_TSS, parameters.UNUSED_ROOM);
		// Data Matrix:
		for (int r=0; r< parameters.numRooms; r++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				dataMatrix[r][t]= parameters.UNUSED_EVENT;
		// Time-Curriculum Matrix:
		for (int t= 0; t< parameters.numTimeSlots; t++){
			for (int cur= 0; cur< parameters.numCurriculums; cur++ ){
				this.timeCurriculum[t][cur]= 0;
			} // end cur for
		} // end t for
		
		// Penalty-related fields:
		isFeasible= true;
		totalPenalty= 0;
		ClashP= 0;
		ComletenessP= 0;
		TimeAvailP= 0;
		RoomUniquenessP= 0;
		CurCompP= 0;
		MinWorkDaysP= 0;
		RoomCapP= 0;
		RoomStabP= 0;
				
		// Robustness-related fields:
		robustValueMin= 0;
		for (int e=0; e< parameters.numEvents; e++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				rForEventTimeMove[e][t]= 0;
		
		for (int e=0; e< parameters.numEvents; e++)
			rEventTotal[e]= 0;	
		
		// Pareto-evaluation related fields:
		
		rank= 0;; // Individuals with rank=1 form the 1st non-dominated front
		crowdDistance= 0;
	}
	

	public Individual clone(){
		Individual ind= new Individual();
		
		// Copy data:
		for(int i=0;i< this.Data.length;i++)
			ind.Data[i]= this.Data[i];
		
		// Copy data matrix:
		for (int r=0; r< parameters.numRooms; r++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				ind.dataMatrix[r][t]= this.dataMatrix[r][t];
		
		// Copy time-curriculum matrix:
		for (int t= 0; t< parameters.numTimeSlots; t++){
			for (int cur= 0; cur< parameters.numCurriculums; cur++ ){
				ind.timeCurriculum[t][cur]= this.timeCurriculum[t][cur];
			} // end cur for
		} // end t for
		
		ind.isFeasible= this.isFeasible;
		
		ind.totalPenalty= this.totalPenalty;
		ind.ClashP= this.ClashP;
		ind.ComletenessP= this.ComletenessP;
		ind.CurCompP= this.CurCompP;
		ind.MinWorkDaysP= this.MinWorkDaysP;
		ind.RoomCapP= this.RoomCapP;
		ind.RoomStabP= this.RoomStabP;
		ind.TimeAvailP= this.TimeAvailP;
		ind.RoomUniquenessP= this.RoomUniquenessP;
		
		// Robustness values:
		ind.robustValueMin= this.robustValueMin;
		
		for (int e=0; e< parameters.numEvents; e++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				ind.rForEventTimeMove[e][t]= this.rForEventTimeMove[e][t];
		
		for (int e=0; e< parameters.numEvents; e++)
			ind.rEventTotal[e]= this.rEventTotal[e];
		
		ind.rank= this.rank;
		ind.crowdDistance= this.crowdDistance;
		
		return ind;		
	}
	
	// Copy the fields of the current individual to the given individual:
	public Individual clone(Individual ind){
		// Copy data:
		for(int i=0;i< this.Data.length;i++)
			ind.Data[i]= this.Data[i];
		
		// Copy data matrix:
		for (int r=0; r< parameters.numRooms; r++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				ind.dataMatrix[r][t]= this.dataMatrix[r][t];
		
		// Copy time-curriculum matrix:
		for (int t= 0; t< parameters.numTimeSlots; t++){
			for (int cur= 0; cur< parameters.numCurriculums; cur++ ){
				ind.timeCurriculum[t][cur]= this.timeCurriculum[t][cur];
			} // end cur for
		} // end t for
		
		ind.isFeasible= this.isFeasible;
		
		ind.totalPenalty= this.totalPenalty;
		ind.ClashP= this.ClashP;
		ind.ComletenessP= this.ComletenessP;
		ind.CurCompP= this.CurCompP;
		ind.MinWorkDaysP= this.MinWorkDaysP;
		ind.RoomCapP= this.RoomCapP;
		ind.RoomStabP= this.RoomStabP;
		ind.TimeAvailP= this.TimeAvailP;
		ind.RoomUniquenessP= this.RoomUniquenessP;
		
		// Robustness values:
		ind.robustValueMin= this.robustValueMin;
		
		for (int e=0; e< parameters.numEvents; e++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				ind.rForEventTimeMove[e][t]= this.rForEventTimeMove[e][t];
		
		for (int e=0; e< parameters.numEvents; e++)
			ind.rEventTotal[e]= this.rEventTotal[e];
		
		ind.rank= this.rank;
		ind.crowdDistance= this.crowdDistance;
		
		return ind;		
	}
	
	
	public void createMatrix() {
		for (int r=0; r< parameters.numRooms; r++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				this.dataMatrix[r][t]= parameters.UNUSED_EVENT;
		
		int time; int room; 
		for (int i=0; i< this.Data.length; i++) {
			if (this.Data[i] == 0) {
				System.out.println("Int value of the " + i + " th Event is zero!!!");
				try {
					throw new Exception("Zero event data");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
			time= convertionManager.intToTime(this.Data[i]);
			room= convertionManager.intToRoom(this.Data[i]);
			if (this.dataMatrix[room][time] != parameters.UNUSED_EVENT){
				if (this.dataMatrix[room][time] != i)
				{
					System.out.println("Error creating matrix. There is already an event!!'");
					try {
						throw new Exception(" There is already an event!!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			this.dataMatrix[room][time]= i;
		} // end i for
	}
	
	public void convertMatrixToData() throws Exception {
		// Set default values:
		for (int i=0; i<this.Data.length; i++)
			Data[i]= 0;
		
		int event;
		for (int r=0; r< parameters.numRooms; r++){
			for (int t=0; t< parameters.numTimeSlots; t++) {
				event= this.dataMatrix[r][t];
				if (event== parameters.UNUSED_EVENT)
					continue;
				this.Data[event]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event], 1, t, r);
			} // end t for
		} // end r for
		
	}

	// This will be called at the beginning 
	public void createTimeCurMatrix(){
		for (int t= 0; t< parameters.numTimeSlots; t++){
			for (int cur= 0; cur< parameters.numCurriculums; cur++ ){
				this.timeCurriculum[t][cur]= 0;
			} // end cur for
		} // end t for
		
		int event;
		for (int r= 0; r< parameters.numRooms; r++){
			for (int t= 0; t< parameters.numTimeSlots; t++){
				event= this.dataMatrix[r][t];
				if (event!= parameters.UNUSED_EVENT){
					for (int cur= 0; cur< parameters.numCurriculums; cur++){
						if (dataHolder.event_Curriculum[event][cur])
							this.timeCurriculum[t][cur]++;
					} // end cur for
				} // end if
			} // end t for
		} // end r for
		
	}
	
		
}

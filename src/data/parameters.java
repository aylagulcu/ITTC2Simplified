package data;

public class parameters {
//	Obtained from the file dynamically!
	
	public static int numInstructors;
	public static int numCurriculums;
	public static int numRooms;
	public static int numDailyPeriods;
	public static int numDays;
	public static int numTimeSlots;
	public static int numCourses;
	
	public static int numConstraints;  // Read from data file: Availabilities one by one not the number of hard and soft constraints!
	
	public static int dataArraySize; // equals to the number of events
	public static int numEvents;
	
//	public static int numConstraintTypes= 8;
	public static int numHardConstTypes= 4;
	public static int numSoftConstTypes= 4;
	
	// The followings are defined only here!	
	public static final int UNUSED_TSS= 255;
	public static final int UNUSED_ROOM= 255;
	public static final int ENDOFARRAY= 4095; // course_id = 2^12-1

	public static int UNUSED_EVENT= 1000;
}

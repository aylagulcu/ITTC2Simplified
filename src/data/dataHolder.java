package data;

import java.util.List;

public class dataHolder {
	
	public static int[] numLectures= new int[parameters.numCourses];
	public static int[] courseStartIndex= new int[parameters.numCourses];
	public static int[] eventCourseId;
	
	public static boolean[][] courseCourseClash= new boolean[parameters.numCourses][parameters.numCourses];
	// true if two events should not have clashing hours

	public static boolean[][] courseTimeSlotUnavailability;
	// true if course c cannot be taught at time slot t; false o.w.
	
	public static int[] timeslotDays;
	
	public static boolean[][] course_Curriculum= new boolean[parameters.numCourses][parameters.numCurriculums];
	public static boolean[][] event_Curriculum;
	
	// A course can be in more than one curriculum!
	public static Object[] courseSameCurriculumCourses; // Array of Sets! not list of list as above.
	
	public static int[] roomCapacity= new int[parameters.numRooms];
	public static int[] numStudents= new int[parameters.numCourses];
	
	public static int[] numMinDays= new int[parameters.numCourses];
	
	public static boolean[][] cRFeas= new boolean[parameters.numCourses][parameters.numRooms];
	public static boolean[][] cTFeas= new boolean[parameters.numCourses][parameters.numTimeSlots];
	public static boolean[][] cDFeas= new boolean[parameters.numCourses][parameters.numDays];
	public static int[][] cNumFeasTDaily= new int[parameters.numCourses][parameters.numDays];
	public static int[] numFeasDays= new int[parameters.numCourses];
	public static List<List<Integer>> cDFeasList; // List of feasible days for each course
	public static List<List<Integer>> cTFeasList; // List of feasible time slots for each course
	public static List<List<Integer>> cRoomFeasList; // List of feasible time slots for each course

	public static Object[] eventConflictingEvents; // Array of Sets! not list of list as above.
	public static Object[] courseConflictingCourses; // Array of Sets! not list of list as above.	
	
	public static List<List<Integer>> curriculumCourses;
	public static List<List<Integer>> curriculumEvents;
	public static List<List<Integer>> eventCurriculums; // List of curriculums for each event
}

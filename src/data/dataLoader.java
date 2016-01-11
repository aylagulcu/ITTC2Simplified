package data;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class dataLoader {

	public static File f;
	
	public static void loadData() throws Exception {
	
		f= new File("../ITTC2Simplified/ITC-2007_ctt/comp02.ctt");
		
		loadParameters();
		
		LoadCourses();
		LoadRooms();
		LoadCurriculums();
		
		LoadCourseCourseClash();
		LoadDaysPeriods();
		LoadUnavailabilities();
		
		LoadFeasibleResources();
		
		fillSetOfConflicting();
	} // end loadData

	private static void loadParameters() throws NumberFormatException, IOException {
		String sCurrentLine;
		BufferedReader br= new BufferedReader(new FileReader(f));
		String delims = "[ :]+"; // sign (+) is used to indicate that consecutive delimiters should be treated as one.
		String[] temp;
		while ((sCurrentLine = br.readLine()) != null) {
			temp= sCurrentLine.split(delims);
			if (temp[0].equals("Courses")) // case sensitive
				parameters.numCourses=Integer.parseInt(temp[1]);
			else if (temp[0].equals("Rooms"))  // case sensitive
				parameters.numRooms=Integer.parseInt(temp[1]);
			else if (temp[0].equals("Days"))  // case sensitive
				parameters.numDays=Integer.parseInt(temp[1]);
			else if (temp[0].equals("Periods_per_day"))  // case sensitive
				parameters.numDailyPeriods=Integer.parseInt(temp[1]);
			else if (temp[0].equals("Curricula"))  // case sensitive
				parameters.numCurriculums=Integer.parseInt(temp[1]);
			else if (temp[0].equals("Constraints"))  // case sensitive
				parameters.numConstraints=Integer.parseInt(temp[1]);
		}
		br.close();	

	}
	
	private static void LoadCourses() throws IOException {
		String sCurrentLine;
		BufferedReader br= new BufferedReader(new FileReader(f));
		String delims = "[ :]+"; // sign (+) is used to indicate that consecutive delimiters should be treated as one.
		String[] temp;
		while ((sCurrentLine = br.readLine()) != null) {
			temp= sCurrentLine.split(delims);
			if (temp[0].equals("COURSES"))
			{
				for(int c=0;c<parameters.numCourses;c++){
					sCurrentLine= br.readLine();
					temp= sCurrentLine.split(delims);
					TemporaryData.courseCode[c]= temp[0]; // course code is read
					TemporaryData.course_instrCode[c]= temp[1]; // course code is read
					dataHolder.numLectures[c]= Integer.parseInt(temp[2]); // course code is read
					dataHolder.numMinDays[c]= Integer.parseInt(temp[3]); // course code is read
					dataHolder.numStudents[c]= Integer.parseInt(temp[4]); // course code is read					
				}
				break;
			}
		} // end while
		br.close();
		
		// Load course start indices:
		
		
		for (int c=0; c< parameters.numCourses; c++) {
			if (c==0)
				dataHolder.courseStartIndex[0]=0;
			else
				dataHolder.courseStartIndex[c]= dataHolder.courseStartIndex[c-1]+ dataHolder.numLectures[c-1];	
		} // end c for
		parameters.dataArraySize= dataHolder.courseStartIndex[parameters.numCourses-1]
				+dataHolder.numLectures[parameters.numCourses-1];
		
		parameters.numEvents= parameters.dataArraySize;
		dataHolder.eventCourseId= new int[parameters.numEvents];
		for (int c=0; c< parameters.numCourses; c++) {
			for (int h=0; h< dataHolder.numLectures[c]; h++)
				dataHolder.eventCourseId[dataHolder.courseStartIndex[c]+h]= c;	
		} // end c for
		
		
	}
	
	private static void LoadRooms() throws IOException {
		String sCurrentLine;
		BufferedReader br= new BufferedReader(new FileReader(f));
		String delims = "[ :]+"; // sign (+) is used to indicate that consecutive delimiters should be treated as one.
		String[] temp;
		while ((sCurrentLine = br.readLine()) != null) {
			temp= sCurrentLine.split(delims);
			if (temp[0].equals("ROOMS"))
			{
				for(int c=0;c<parameters.numRooms;c++){
					sCurrentLine= br.readLine();
					temp= sCurrentLine.split(delims);
					TemporaryData.roomCode[c]= temp[0]; // room code is read
					dataHolder.roomCapacity[c]= Integer.parseInt(temp[1]); // room capacity is read				
				}
				break;
			}
		} // end while
		br.close();	
	}

	private static void LoadCurriculums() throws IOException {
		// initialize the array:
		for (int e=0; e< parameters.numCourses; e++)
			for (int c=0; c<parameters.numCurriculums; c++){
				dataHolder.course_Curriculum[e][c]= false;
			}
		String sCurrentLine;
		BufferedReader br= new BufferedReader(new FileReader(f));
		String delims = "[ :]+"; // sign (+) is used to indicate that consecutive delimiters should be treated as one.
		String[] temp;
		while ((sCurrentLine = br.readLine()) != null) {
			temp= sCurrentLine.split(delims);
			if (temp[0].equals("CURRICULA"))
			{
				for(int c=0;c<parameters.numCurriculums;c++){
					sCurrentLine= br.readLine();
					temp= sCurrentLine.split(delims);
					TemporaryData.curriculumCode[c]= temp[0]; // curriculum code is read
					TemporaryData.curriculum_CourseCount[c]= Integer.parseInt(temp[1]); // curriculum course count is read				
					for (int i=0; i< TemporaryData.curriculum_CourseCount[c]; i++){
						int course= findCourseId(temp[2+i]);
						dataHolder.course_Curriculum[course][c]= true;
					}
				}
				break;
			}
		} // end while
		br.close();
		
		dataHolder.courseSameCurriculumCourses= new Object[parameters.numCourses];
		HashSet<Integer> tempCourses;
		for (int c1=0; c1< parameters.numCourses; c1++) {
			tempCourses= new HashSet<Integer>();
			for(int cur=0;cur<parameters.numCurriculums;cur++){
				if (dataHolder.course_Curriculum[c1][cur]) {
					for (int c2=0; c2< parameters.numCourses; c2++) {
						if (c2== c1) continue;
						if (dataHolder.course_Curriculum[c2][cur]) {
							tempCourses.add(c2); // Another curriculum course
						} // end if
					} // end c2 for	
				} // end if	
			} // end cur for
			dataHolder.courseSameCurriculumCourses[c1]= tempCourses;
		} // end c1 for
		
		dataHolder.curriculumCourses= new ArrayList<>();
		ArrayList<Integer> tempC;
		for (int cur= 0; cur< parameters.numCurriculums; cur++){
			tempC= new ArrayList<Integer>();
			for (int c= 0; c< parameters.numCourses; c++){
				if (dataHolder.course_Curriculum[c][cur])
					tempC.add(c);
			} // end c for
			dataHolder.curriculumCourses.add(tempC);
		} // end cur for
		
		dataHolder.curriculumEvents= new ArrayList<>();
		ArrayList<Integer> tempE;
		for (int cur= 0; cur< parameters.numCurriculums; cur++){
			tempE= new ArrayList<Integer>();
			for (int e= 0; e< parameters.numEvents; e++){
				if (dataHolder.course_Curriculum[dataHolder.eventCourseId[e]][cur])
					tempE.add(e);
			} // end c for
			dataHolder.curriculumEvents.add(tempE);
		} // end cur for
		
		dataHolder.event_Curriculum= new boolean[parameters.numEvents][parameters.numCurriculums];
		for (int e=0; e< parameters.numEvents; e++)
			for (int c=0; c<parameters.numCurriculums; c++){
				dataHolder.event_Curriculum[e][c]= false;
			}
		
		for (int event= 0; event< parameters.numEvents; event++){
			for (int cur= 0; cur< parameters.numCurriculums; cur++){
				if (dataHolder.course_Curriculum[dataHolder.eventCourseId[event]][cur])
					dataHolder.event_Curriculum[event][cur]= true;
				else
					dataHolder.event_Curriculum[event][cur]= false;
			} // end cur for
		} // end event for
		
		
		// event Curriculums, an event mey belong to multiple curriculums
		dataHolder.eventCurriculums= new ArrayList<>();
		ArrayList<Integer> tempCurs;
		for (int event= 0; event< parameters.numEvents; event++){
			tempCurs= new ArrayList<Integer>();
			for (int cur= 0; cur< parameters.numCurriculums; cur++){
				if (dataHolder.event_Curriculum[event][cur])
					tempCurs.add(cur);
			} // end c for
			dataHolder.eventCurriculums.add(tempCurs);
		} // end cur for
		
	}

	private static int findCourseId(String code) {
		 for( int t=0; t< TemporaryData.courseCode.length; t++)
			 if (TemporaryData.courseCode[t].equals(code)){
				 return t;
			 } // end if
		 return 0;
	}

	private static void LoadCourseCourseClash() {
		// AMONG COURSES IN THE SAME CURRICULUM
		// ACCORDING TO "course_Curriculum" ARRAY
		for (int e1=0; e1< parameters.numCourses; e1++)
			for (int e2=0; e2<parameters.numCourses; e2++)
				dataHolder.courseCourseClash[e1][e2]= false;
		
		for (int cur=0; cur< parameters.numCurriculums; cur++){
			for (int e1=0; e1< parameters.numCourses-1; e1++)
				for (int e2=e1+1; e2<parameters.numCourses; e2++){
					if(dataHolder.course_Curriculum[e1][cur] && dataHolder.course_Curriculum[e2][cur]){
						dataHolder.courseCourseClash[e1][e2]=true;
						dataHolder.courseCourseClash[e2][e1]=true;
					}
				} // end e2 for
		} // end cur for	

		for (int e1=0; e1< parameters.numCourses-1; e1++)
			for (int e2=e1+1; e2<parameters.numCourses; e2++){
					if (TemporaryData.course_instrCode[e1].equals(TemporaryData.course_instrCode[e2])){
						dataHolder.courseCourseClash[e1][e2]=true;
						dataHolder.courseCourseClash[e2][e1]=true;
					}
			} // end e2 for
		
	}
	
	private static void LoadDaysPeriods() {
		parameters.numTimeSlots= parameters.numDays*parameters.numDailyPeriods;
		dataHolder.timeslotDays= new int[parameters.numTimeSlots];
		int t=0;
		for (int d=0;d< parameters.numDays; d++){
			for (int p=0; p< parameters.numDailyPeriods;p++){
				dataHolder.timeslotDays[t]=(short) d;
				t++;
			}
		}
	}
	
	private static void LoadUnavailabilities() throws NumberFormatException, IOException {
		// initialize arrays:
		dataHolder.courseTimeSlotUnavailability= new boolean[parameters.numCourses][parameters.numTimeSlots];
		for (int c=0; c<parameters.numCourses; c++){
			for (int t=0; t< parameters.numTimeSlots; t++){
				dataHolder.courseTimeSlotUnavailability[c][t]=false;
			}
		}
		String sCurrentLine;
		BufferedReader br= new BufferedReader(new FileReader(f));
		String delims = "[ :]+"; // sign (+) is used to indicate that consecutive delimiters should be treated as one.
		String[] temp;
		int course;
		int timeslot;
		
		while ((sCurrentLine = br.readLine()) != null) {
			temp= sCurrentLine.split(delims);
			if (temp[0].equals("UNAVAILABILITY_CONSTRAINTS"))
			{
				for(int c=0;c<parameters.numConstraints;c++){
					sCurrentLine= br.readLine();
					temp= sCurrentLine.split(delims);
					course= findCourseId(temp[0]);
					timeslot= findTimeSlotId(Integer.parseInt(temp[1]),Integer.parseInt(temp[2]));
					dataHolder.courseTimeSlotUnavailability[course][timeslot]=true;			
				}
				break;
			}
		} // end while
		br.close();	
	}

	private static int findTimeSlotId(int day, int per) {
		return (per + day*parameters.numDailyPeriods);
	}

	private static void LoadFeasibleResources(){
		// Load course-time slot feasibility array:
		dataHolder.cTFeas= new boolean[parameters.numCourses][parameters.numTimeSlots];
		for (int c=0; c<parameters.numCourses; c++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				if (dataHolder.courseTimeSlotUnavailability[c][t])
					dataHolder.cTFeas[c][t]= false;
				else{
					dataHolder.cTFeas[c][t]= true;
				}
		List<Integer> temp;
		dataHolder.cTFeasList= new ArrayList<List<Integer>>();
		for (int c=0; c<parameters.numCourses; c++){
			temp= new ArrayList<Integer>();
			for (int t=0; t< parameters.numTimeSlots; t++)
				if (dataHolder.cTFeas[c][t]) 
					temp.add(t);
			dataHolder.cTFeasList.add(temp);
		} // end c for
	
		for (int c=0; c<parameters.numCourses; c++)
			for (int d=0; d< parameters.numDays; d++)
				dataHolder.cDFeas[c][d]= false;
		for (int c=0; c<parameters.numCourses; c++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				if (dataHolder.cTFeas[c][t])
					dataHolder.cDFeas[c][dataHolder.timeslotDays[t]]= true;
		
		// cNumFeasTDaily
		
		for (int c=0; c<parameters.numCourses; c++)
			for (int d=0; d< parameters.numDays; d++)
				dataHolder.cNumFeasTDaily[c][d]= 0;
		for (int c=0; c<parameters.numCourses; c++)
			for (int t=0; t< parameters.numTimeSlots; t++)
				if (dataHolder.cTFeas[c][t])
					dataHolder.cNumFeasTDaily[c][dataHolder.timeslotDays[t]]++;
		
		
		int count;
		for (int c=0; c<parameters.numCourses; c++){
			count=0;
			for (int d=0; d< parameters.numDays; d++)
				if (dataHolder.cDFeas[c][d])
					count++;					
			dataHolder.numFeasDays[c]= count;
		}
		// Course-day feasible list:
		dataHolder.cDFeasList= new ArrayList<List<Integer>>();
		for (int c=0; c<parameters.numCourses; c++){
			temp= new ArrayList<Integer>();
			for (int d=0; d< parameters.numDays; d++)
				if (dataHolder.cDFeas[c][d]) 
					temp.add(d);
			dataHolder.cDFeasList.add(temp);
		} // end c for
		
		// Load course-room feasibility array:
		dataHolder.cRFeas= new boolean[parameters.numCourses][parameters.numRooms];
		for (int c=0; c<parameters.numCourses; c++){
			for (int r=0; r< parameters.numRooms; r++){
				if (dataHolder.roomCapacity[r] < dataHolder.numStudents[c])
					dataHolder.cRFeas[c][r]= false;
				else
					dataHolder.cRFeas[c][r]= true;
			}
		}
		dataHolder.cRoomFeasList= new ArrayList<List<Integer>>();
		for (int c=0; c<parameters.numCourses; c++){
			temp= new ArrayList<Integer>();
			for (int r=0; r< parameters.numRooms; r++)
				if (dataHolder.cRFeas[c][r]) 
					temp.add(r);
			dataHolder.cRoomFeasList.add(temp);
		} // end c for
	}

	private static void fillSetOfConflicting() {
		// Same course events included:
		dataHolder.eventConflictingEvents= new Object[parameters.dataArraySize];
	
		int eventIndex1;
		int eventIndex2;
		HashSet<Integer> temp;
		HashSet<Integer> inner;
		for (int c1=0; c1< parameters.numCourses; c1++) {
			temp= new HashSet<Integer>();
			for (int c2=0; c2< parameters.numCourses; c2++) {
				if (dataHolder.courseCourseClash[c1][c2]) {
					temp.add(c2); // Set of conflicting courses for each course!
				}	
			} // end c2 for	
			
			for(int e=0; e< dataHolder.numLectures[c1]; e++) {
				inner= new HashSet<Integer>();
				eventIndex1= dataHolder.courseStartIndex[c1]+ e;
				// For conflicting course events:
				for (int c2: temp) {
					for(int e2=0; e2< dataHolder.numLectures[c2]; e2++) {
						eventIndex2= dataHolder.courseStartIndex[c2]+ e2;
						inner.add(eventIndex2); // Set of conflicting events for each event!
					} // end e2 for
				} // end c2 for each
				// For the same course events:
				int indexTemp;
				for (int sameCEvents=0; sameCEvents< dataHolder.numLectures[c1]; sameCEvents++ ) {
					indexTemp= dataHolder.courseStartIndex[c1]+ sameCEvents;
					if (indexTemp== eventIndex1)
						continue;
					inner.add(indexTemp);
				}
				dataHolder.eventConflictingEvents[eventIndex1]= inner;
			} // end e for
		} // end c1 for
		
		// For Courses:
		dataHolder.courseConflictingCourses= new Object[parameters.numCourses];
		HashSet<Integer> tempCourses;
		for (int c1=0; c1< parameters.numCourses; c1++) {
			tempCourses= new HashSet<Integer>();
			for (int c2=0; c2< parameters.numCourses; c2++) {
				if (c2== c1) continue;
				if (dataHolder.courseCourseClash[c1][c2]) {
					tempCourses.add(c2); // Set of conflicting courses for each course!
				}	
			} // end c2 for	
			dataHolder.courseConflictingCourses[c1]= tempCourses;
		} // end c1 for
		
	
	}
}

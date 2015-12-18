package constraints;

import ga.Individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import data.Course;
import data.Event;
import data.TemporaryData;
import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class CurriculumCompactnessConstraint extends ConstraintBase implements SoftConstraint{

	HashSet<Integer> timesToCompute= new HashSet<Integer>();
		
	public CurriculumCompactnessConstraint(){
		super();	
	}

	public CurriculumCompactnessConstraint(int weight){
		super(weight);	
	}
	
	public int Compute(Individual indiv) {
		
		return Compute2(indiv);
		
		
//		int violations=0;
//		int t;
//		for (int cur= 0; cur< parameters.numCurriculums; cur++){
//			t= computeSingleCurriculum(indiv, cur);
//			violations+= t;
//		} // end cur for
//		indiv.CurCompP= violations;
//		return violations;
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1, int curriculum) {
		// Assigns a penalty to an event if it is an isolated event!
		int penalty= 0;
		int day; boolean hasNeighbour;
		int tempEv;
		
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);
		for (Event event1: crs.myEvents) {
			if (event1.time == parameters.UNUSED_TSS) continue;
			day= dataHolder.timeslotDays[event1.time];
			hasNeighbour = false;
			// assuming that the individual matrix is up to date:
			if (event1.time-1 >= 0){
				if (dataHolder.timeslotDays[event1.time-1]== day){
					for (int r=0; r< parameters.numRooms; r++){
						tempEv= indiv.dataMatrix[r][event1.time-1];
						if (tempEv != parameters.UNUSED_EVENT){
							if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
								hasNeighbour= true;
								break; // r for
							} // end if
						}
					} // end r for
				}
			} // end if event1.time-1
			if (hasNeighbour) continue;
			if (event1.time+1 < parameters.numTimeSlots){
				if (dataHolder.timeslotDays[event1.time+1]== day){
					for (int r=0; r< parameters.numRooms; r++){
						tempEv= indiv.dataMatrix[r][event1.time+1];
						if (tempEv != parameters.UNUSED_EVENT){
							if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
								hasNeighbour= true;
								break; // r for
							} // end if
						}
					} // end r for
				}
			} // end if event1.time-1
			if (!hasNeighbour)
				penalty+= 2;
		} // end event1 for
		return penalty * this.weight;
	}
	
	public int computeSingleCurriculum(Individual indiv, int curriculum) {
		int cp= 0;
		// get curriculum courses:
		List<Integer> courses= dataHolder.curriculumCourses.get(curriculum);
		for (int c: courses){
			cp+= computeSingleCourse(indiv, c, curriculum);
		}
		return cp;
	}
	
	public int computeSingleCourse(Individual indiv, int courseId1) {
		// Assigns a penalty to an event if it is an isolated event!
		int penalty= 0;
		int day; boolean hasNeighbour;
		int tempEv;
		
		Course crs= convertionManager.getCourseFromArray(courseId1, indiv.Data);
		for (int curriculum=0; curriculum< parameters.numCurriculums; curriculum++){
			if (dataHolder.course_Curriculum[courseId1][curriculum]){
				for (Event event1: crs.myEvents) {
					if (event1.time == parameters.UNUSED_TSS) continue;
					day= dataHolder.timeslotDays[event1.time];
					hasNeighbour = false;
					// assuming that the individual matrix is up to date:
					if (event1.time-1 >= 0){
						if (dataHolder.timeslotDays[event1.time-1]== day){
							for (int r=0; r< parameters.numRooms; r++){
								tempEv= indiv.dataMatrix[r][event1.time-1];
								if (tempEv != parameters.UNUSED_EVENT){
									if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
										hasNeighbour= true;
										break; // r for
									} // end if
								}
							} // end r for
						}
					} // end if event1.time-1
					if (hasNeighbour) continue;
					if (event1.time+1 < parameters.numTimeSlots){
						if (dataHolder.timeslotDays[event1.time+1]== day){
							for (int r=0; r< parameters.numRooms; r++){
								tempEv= indiv.dataMatrix[r][event1.time+1];
								if (tempEv != parameters.UNUSED_EVENT){
									if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
										hasNeighbour= true;
										break; // r for
									} // end if
								}
							} // end r for
						}
					} // end if event1.time-1
					if (!hasNeighbour)
						penalty+= 2;
				} // end event1 for
			} // end if
		} // end curriculum for
		return penalty * this.weight;
	}

	
	public CurriculumCompactnessConstraint Clone() {
		CurriculumCompactnessConstraint con = new CurriculumCompactnessConstraint(this.weight);
		return con;
	}
	
	public List<String> AnayzeFinalSol(int[] dataArray) {
		int violations=0;
		List<String> results= new ArrayList<String>();
		// Assigns a penalty to an event if it is an isolated event!
		Event evt1;
		int courseId1;
		for(int cur =0; cur< parameters.numCurriculums; cur++ ){
			for (int d=0; d< parameters.numDays; d++){
				for (int i=0; i< dataArray.length; i++){
					courseId1= convertionManager.intToCourseId(dataArray[i]);
					if(!dataHolder.course_Curriculum[courseId1][cur])
						continue; // Not in that curriculum
					evt1= convertionManager.intToEvent(i, dataArray[i]);
					boolean hasNeighbour=false;
					if (dataHolder.timeslotDays[evt1.time]==d){ // in the current day	
						for (int j=0; j< dataArray.length; j++){
							if (i==j)
								continue;
							int courseId2= convertionManager.intToCourseId(dataArray[j]);
							if(!dataHolder.course_Curriculum[courseId2][cur])
								continue; // Not in that curriculum
							Event tempEvent= convertionManager.intToEvent(j, dataArray[j]);
							if (dataHolder.timeslotDays[tempEvent.time]==d){ // in the current day	
								if (tempEvent.time+1==evt1.time
										|| tempEvent.time==evt1.time+1){
									hasNeighbour=true;
									break;
								}
							}
						} // end j for
						if (!hasNeighbour){
							violations+= 2;
							results.add("An isolated lecture at curriculum: "+ TemporaryData.curriculumCode[cur]+ "at time: "+ evt1.time);
						}
					} // end if
				}// end i for
			} // end d for
		} // end cur for
		results.add("Penalty for isolated lectures constraint "+ violations);
		return results;
	}
	
	// Computation based on individual's timecurriculum matrix 
	// which holds the number of events belonging to each curriculum scheduled at each time 
	public int Compute2(Individual indiv) {
		int p=0;
		for (int cur= 0; cur< parameters.numCurriculums; cur++)
			p+= computeSingleCurriculum2(indiv, cur);
		indiv.CurCompP= p;
		return p;
	}
	
	public int computeSingleCurriculum2(Individual indiv, int curriculum) {
		int penalty= 0; int time;
		// get curriculum events:
		List<Integer> events= dataHolder.curriculumEvents.get(curriculum);
		for (int e: events){
			time= convertionManager.intToTime(indiv.Data[e]);
			penalty+= computeSingleEvent2(indiv, curriculum, e, time);
		}
		return penalty;
	}
	
	public int computeSingleEvent2(Individual indiv, int curId, int eventId, int time) {
		// assuming that the individual matrix is up to date
		// event is known to be assigned at the given time, so check only the time-1 && time+1
		// Assigns a penalty to an event if it is an isolated event!

		int day= dataHolder.timeslotDays[time];
		if (time-1 >= 0 && dataHolder.timeslotDays[time-1]== day){
			if (indiv.timeCurriculum[time-1][curId] >0)
				return 0; // no isolated event
		} // end if event1.time-1

		if (time+1 < parameters.numTimeSlots && dataHolder.timeslotDays[time+1]== day){
			if (indiv.timeCurriculum[time+1][curId] >0)
				return 0; // no isolated event
		} // end if event1.time-1
		
		return 2* this.weight; // penalty of 2 for each isolated event
	}
	
	public int computeCurriculumPartial(Individual indiv, int curriculum, int time1, int time2) {
		timesToCompute.clear();
		
		if (time1-1 >= 0 && dataHolder.timeslotDays[time1-1]== dataHolder.timeslotDays[time1])
			timesToCompute.add(time1-1);
		timesToCompute.add(time1);
		if (time1+1 < parameters.numTimeSlots && dataHolder.timeslotDays[time1+1]== dataHolder.timeslotDays[time1])
			timesToCompute.add(time1+1);
		
		if (time2-1 >= 0 && dataHolder.timeslotDays[time2-1]== dataHolder.timeslotDays[time2])
			timesToCompute.add(time2-1);
		timesToCompute.add(time2);
		if (time2+1 < parameters.numTimeSlots && dataHolder.timeslotDays[time2+1]== dataHolder.timeslotDays[time2])
			timesToCompute.add(time2+1);
		
		int eventP= 0;
		int day;
		for (int timeslot: timesToCompute){
			day= dataHolder.timeslotDays[timeslot];
			if (timeslot-1 >= 0 && dataHolder.timeslotDays[timeslot-1]== day){
				if (indiv.timeCurriculum[timeslot-1][curriculum] >0)
					continue; // no isolated for timeslot
			} // end if event1.time-1
			if (timeslot+1 < parameters.numTimeSlots && dataHolder.timeslotDays[timeslot+1]== day){
				if (indiv.timeCurriculum[timeslot+1][curriculum] >0)
					continue; // no isolated for timeslot
			} // end if event1.time-1
			eventP+= indiv.timeCurriculum[timeslot][curriculum] * 2 * this.weight;		
		
		} // end timeslot for 
		return eventP;
	}

	@Override
	public int computeEventForInitializer(Individual indiv, int eventId, int time, int room) {
		// Assigns a penalty to an event if it is an isolated event!
		int penalty= 0;
		int day; boolean hasNeighbour;
		int tempEv;
		int courseId= dataHolder.eventCourseId[eventId];

		for (int curriculum=0; curriculum< parameters.numCurriculums; curriculum++){
			if (dataHolder.course_Curriculum[courseId][curriculum]){
				if (time == parameters.UNUSED_TSS) continue;
				day= dataHolder.timeslotDays[time];
				hasNeighbour = false;
				// assuming that the individual matrix is up to date:
				if (time-1 >= 0){
					if (dataHolder.timeslotDays[time-1]== day){
						for (int r=0; r< parameters.numRooms; r++){
							tempEv= indiv.dataMatrix[r][time-1];
							if (tempEv != parameters.UNUSED_EVENT){
								if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
									hasNeighbour= true;
									break; // r for
								} // end if
							}
						} // end r for
					}
				} // end if event1.time-1
				if (hasNeighbour) continue;
				if (time+1 < parameters.numTimeSlots){
					if (dataHolder.timeslotDays[time+1]== day){
						for (int r=0; r< parameters.numRooms; r++){
							tempEv= indiv.dataMatrix[r][time+1];
							if (tempEv != parameters.UNUSED_EVENT){
								if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
									hasNeighbour= true;
									break; // r for
								} // end if
							}
						} // end r for
					}
				} // end if event1.time-1
				if (!hasNeighbour)
					penalty+= 2;
			} // end if
		} // end curriculum for
		return penalty * this.weight;
	}
	
	
	@Override
	public int computeEvent(Individual indiv, int eventId, int time, int room) {
		// Assigns a penalty to an event if it is an isolated event!
		int penalty= 0;
		int day; boolean hasNeighbour;
		int tempEv;
		int courseId= dataHolder.eventCourseId[eventId];

		for (int curriculum=0; curriculum< parameters.numCurriculums; curriculum++){
			if (dataHolder.course_Curriculum[courseId][curriculum]){
				if (time == parameters.UNUSED_TSS) continue;
				day= dataHolder.timeslotDays[time];
				hasNeighbour = false;
				// assuming that the individual matrix is up to date:
				if (time-1 >= 0){
					if (dataHolder.timeslotDays[time-1]== day){
						for (int r=0; r< parameters.numRooms; r++){
							tempEv= indiv.dataMatrix[r][time-1];
							if (tempEv != parameters.UNUSED_EVENT){
								if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
									hasNeighbour= true;
									break; // r for
								} // end if
							}
						} // end r for
					}
				} // end if event1.time-1
				if (hasNeighbour) continue;
				if (time+1 < parameters.numTimeSlots){
					if (dataHolder.timeslotDays[time+1]== day){
						for (int r=0; r< parameters.numRooms; r++){
							tempEv= indiv.dataMatrix[r][time+1];
							if (tempEv != parameters.UNUSED_EVENT){
								if (dataHolder.course_Curriculum[dataHolder.eventCourseId[tempEv]][curriculum]){
									hasNeighbour= true;
									break; // r for
								} // end if
							}
						} // end r for
					}
				} // end if event1.time-1
				if (!hasNeighbour)
					penalty+= 2;
			} // end if
		} // end curriculum for
		return penalty * this.weight;
	}

	@Override
	public void ComputeCoursePenalties(Individual indiv) {
		Course crs;
		
		for (int c=0; c< parameters.numCourses; c++){
			indiv.curCompP[c]= 0;
			crs= convertionManager.getCourseFromArray(c, indiv.Data);
			for (int curr=0; curr< parameters.numCurriculums; curr++){
				if (dataHolder.course_Curriculum[c][curr]){
					for (Event event1: crs.myEvents)
						indiv.curCompP[c]+= computeSingleEvent2(indiv, curr, event1.id, event1.time);
				} // end if
			} // end cur for
		} // end c for
	}



}

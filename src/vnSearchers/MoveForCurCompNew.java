package vnSearchers;

import java.util.List;

import data.convertionManager;
import data.dataHolder;
import data.parameters;

public class MoveForCurCompNew extends SearcherBase {
	
	int curriculum;
	int ev, time, room, ev2, time2, room2;
	
	int day;
	List<Integer> Times; 

	public MoveForCurCompNew(SABase mngr, int index) {
		super(mngr, index);
	}

	public boolean search() {		
		// select a random curriculum, and a time
		// if the curriculum has events in time-1 or time+1 but not in time;
		// then select a position in this time and put a curriculum event here:
		curriculum= myRandGen.nextInt(parameters.numCurriculums);
		time = myRandGen.nextInt(parameters.numTimeSlots);
		if (mySA.currentInd.timeCurriculum[time][curriculum] >0){
			return false;
		}
		day= dataHolder.timeslotDays[time];
		if (time-1 >= 0 && dataHolder.timeslotDays[time-1]== day){
			if (mySA.currentInd.timeCurriculum[time-1][curriculum] ==0){
				// select a random room:
				room= myRandGen.nextInt(parameters.numRooms);
				ev= mySA.currentInd.dataMatrix[room][time];
				// select a curriculum event from another time and place it here.
				ev2= dataHolder.curriculumEvents.get(curriculum).get(myRandGen.nextInt(dataHolder.curriculumEvents.get(curriculum).size()));
				time2= convertionManager.intToTime(mySA.currentInd.Data[ev2]);
				room2= convertionManager.intToRoom(mySA.currentInd.Data[ev2]);
				return tryCurrentMove(ev, time2, room2, ev2, time, room); // move related data is recorded if it updates the current best move
			}
		}
		if (time+1 < parameters.numTimeSlots && dataHolder.timeslotDays[time+1]== day){
			if (mySA.currentInd.timeCurriculum[time+1][curriculum] ==0){
				// select a random room:
				room= myRandGen.nextInt(parameters.numRooms);
				ev= mySA.currentInd.dataMatrix[room][time];
				// select a curriculum event from another time and place it here.
				ev2= dataHolder.curriculumEvents.get(curriculum).get(myRandGen.nextInt(dataHolder.curriculumEvents.get(curriculum).size()));
				time2= convertionManager.intToTime(mySA.currentInd.Data[ev2]);
				room2= convertionManager.intToRoom(mySA.currentInd.Data[ev2]);
				return tryCurrentMove(ev, time2, room2, ev2, time, room); // move related data is recorded if it updates the current best move
			}
		}
		
		return false;
	}

}
	

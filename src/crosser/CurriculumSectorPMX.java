package crosser;

import data.convertionManager;
import data.dataHolder;
import data.parameters;
import ga.Individual;

public class CurriculumSectorPMX extends PMX {
	
	// Select a sector randomly and apply CX only in that sector between parents
	// Step1: define 2 sectors
	// Keep a copy of the original sectors
	// Step2: for both full positions: define matchings
	// Swap matching events
	// Step3: for only one full positions: use original sectors to put events

	private int curriculum;

	public CurriculumSectorPMX(crossoverManager mngr) {
		super(mngr);
	}
	
	public Individual[] cross(Individual ind1, Individual ind2) {
	
		ind1.createMatrix(); ind2.createMatrix();
		myOffSprings= new Individual[2];
		myOffSprings[0]= ind1.clone(); 
		myOffSprings[1]= ind2.clone();
		
		// Create sector:
		curriculum= myRandom.nextInt(parameters.numCurriculums); // 0 included
		
		// Step1: create matchings
		createMatchings();
		// Step2: exchange ALL events acc. to matchings.
		exchangeMatchingEvents();
		
		// Step3: now use original version of the matrices (parents) to inherit events to empty positions:
		// These events have no matchings but this time we should deal with duplicate events!
		superimposeParents(ind1, ind2);

//		repair(myOffSprings[0]);
//		repair(myOffSprings[1]);
		
		myCXManager.mySimpleEvaluator.evaluateIndividual(myOffSprings[0]);
		myCXManager.mySimpleEvaluator.evaluateIndividual(myOffSprings[1]);
		//return myOffSprings;
		
		Individual[] myOffSpringsReturn= selectBestTwo(ind1, ind2, myOffSprings[0], myOffSprings[1]);
		
		return myOffSpringsReturn;
	}

	public void superimposeParents(Individual ind1, Individual ind2) {
		int event1; int event2;
		int oldTime; int oldRoom;
		for( int r= 0; r< parameters.numRooms; r++){
			for ( int t= 0; t< parameters.numTimeSlots; t++){ 
				event1= ind1.dataMatrix[r][t];
				event2= ind2.dataMatrix[r][t];
				if (event1== parameters.UNUSED_EVENT && event2== parameters.UNUSED_EVENT)
					continue;
				else if (event1!= parameters.UNUSED_EVENT && event2!= parameters.UNUSED_EVENT)
					continue;
				// only one of them is empty
				else if(event1== parameters.UNUSED_EVENT){
					if (dataHolder.course_Curriculum[dataHolder.eventCourseId[event2]][curriculum]){
						// copy event2
						myOffSprings[0].dataMatrix[r][t]= event2;
						myOffSprings[0].Data[event2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event2],
								1, t, r);
						// now remove duplicates
						oldTime= convertionManager.intToTime(ind1.Data[event2]);
						oldRoom= convertionManager.intToRoom(ind1.Data[event2]);
						myOffSprings[0].dataMatrix[oldRoom][oldTime]= parameters.UNUSED_EVENT;
					} // end if
				} // end else if
				else if(event2== parameters.UNUSED_EVENT){
					if (dataHolder.course_Curriculum[dataHolder.eventCourseId[event1]][curriculum]){
						myOffSprings[1].dataMatrix[r][t]= event1;
						myOffSprings[1].Data[event1]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event1],
								1, t, r);
						// now remove duplicates
						oldTime= convertionManager.intToTime(ind2.Data[event1]);
						oldRoom= convertionManager.intToRoom(ind2.Data[event1]);
						myOffSprings[1].dataMatrix[oldRoom][oldTime]= parameters.UNUSED_EVENT;
					} // end if
				} // end else if
			} // end t for 
		} // end r for
		
	}

	public void exchangeMatchingEvents() {
		boolean[] isMappedOS1= new boolean[parameters.numEvents];
		boolean[] isMappedOS2= new boolean[parameters.numEvents];
		for (int i=0; i< parameters.numEvents; i++){
			isMappedOS1[i]= false;
			isMappedOS2[i]= false;
		}
		int event1; int event2; int matchingEvent;
		for( int r= 0; r< parameters.numRooms; r++){
			for ( int t= 0; t< parameters.numTimeSlots; t++){ 
				event1= myOffSprings[0].dataMatrix[r][t];
				if (event1!= parameters.UNUSED_EVENT){ // Not empty position
					matchingEvent= matchings[event1];
					if (matchingEvent!= parameters.UNUSED_EVENT){ // has been matched
						if (!isMappedOS1[event1]){
							int o2Room= convertionManager.intToRoom(myOffSprings[0].Data[matchingEvent]);
							int o2Time= convertionManager.intToTime(myOffSprings[0].Data[matchingEvent]);
							myOffSprings[0].dataMatrix[o2Room][o2Time]= event1;	
							myOffSprings[0].Data[event1]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event1],
									1, o2Time, o2Room);
							
							myOffSprings[0].dataMatrix[r][t]= matchingEvent;	
							myOffSprings[0].Data[matchingEvent]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[matchingEvent],
									1, t, r);
							isMappedOS1[event1]= true;
							isMappedOS1[matchingEvent]= true;
						} // end if
					}
				} // end if
				event2= myOffSprings[1].dataMatrix[r][t];
				if (event2!= parameters.UNUSED_EVENT){ // Not empty position
					matchingEvent= matchings[event2];
					if (matchingEvent!= parameters.UNUSED_EVENT){ // has been matched
						if (!isMappedOS2[event2]){
							int o2Room= convertionManager.intToRoom(myOffSprings[1].Data[matchingEvent]);
							int o2Time= convertionManager.intToTime(myOffSprings[1].Data[matchingEvent]);
							myOffSprings[1].dataMatrix[o2Room][o2Time]= event2;	
							myOffSprings[1].Data[event2]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[event2],
									1, o2Time, o2Room);
							
							myOffSprings[1].dataMatrix[r][t]= matchingEvent;
							myOffSprings[1].Data[matchingEvent]= convertionManager.eventValuesToInt(dataHolder.eventCourseId[matchingEvent],
									1, t, r);
							isMappedOS2[event2]= true;
							isMappedOS2[matchingEvent]= true;
						}
					}
					
				} // end if
			} // end t for 
		} // end r for
		
	}

	public void createMatchings() {

		for (int i=0; i< parameters.numEvents; i++)
			matchings[i]= parameters.UNUSED_EVENT;
		
		int event1; int event2;
		for( int r= 0; r< parameters.numRooms; r++){
			for ( int t= 0; t< parameters.numTimeSlots; t++){
				event1= myOffSprings[0].dataMatrix[r][t];
				event2= myOffSprings[1].dataMatrix[r][t];
				if (event1== event2) 
					continue; // do nothing
				else if (event1== parameters.UNUSED_EVENT || event2== parameters.UNUSED_EVENT)
					continue; // do nothing
				else if (dataHolder.course_Curriculum[dataHolder.eventCourseId[event1]][curriculum] &&
						dataHolder.course_Curriculum[dataHolder.eventCourseId[event2]][curriculum]){ // add to matching if they are both in the selected curriculum
					assert event1!= parameters.UNUSED_EVENT;
					assert event2!= parameters.UNUSED_EVENT;
					// create a matching only if events matched from (parent1 to parent2) and (parent2 to parent1):
					// find event1 in myOffSpring2. If it has event2 in the corresponding position in myOffSpring then matches!
					if (matchings[event1]== parameters.UNUSED_EVENT && matchings[event2]== parameters.UNUSED_EVENT){
						matchings[event1]= event2;
						matchings[event2]= event1;
					} // end if
				} // end else if
			} // end t for 
		} // end r for
		
	}

}

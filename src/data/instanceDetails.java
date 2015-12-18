package data;

public class instanceDetails {
	public static float CO= 0;
	public static float TA= 0;
	public static float CL= 0;
	public static float RO= 0;
	
	public static void computeInstanceDetails() {
		CO= computeCO();
		TA= computeTA();
		RO= computeRO();
		printInstanceDetails();
	}

	public static float combinationWith2(float n){
		if (n<=1)
			return 0;
		return n*(n-1)/2;
	}
	
	private static float computeCO() {
		int count= 0;
		for(int c1=0; c1< parameters.numCourses; c1++){
			count+= combinationWith2(dataHolder.numLectures[c1]);
//			System.out.print(TemporaryData.courseCode[c1]+ " should not have clashes with:");
			for(int c2=c1+1; c2< parameters.numCourses; c2++){	
				if(dataHolder.courseCourseClash[c1][c2]){
					count+= dataHolder.numLectures[c1] * dataHolder.numLectures[c2];
//					System.out.print(TemporaryData.courseCode[c2]+ " ");
				}
			} // end j for
//			System.out.println();
		} // end i for
		float totalDistictPairs= combinationWith2(parameters.numEvents);
		return count / totalDistictPairs;
	}
	
	private static float computeTA() {
		int count= 0;
		for (int c=0; c< parameters.numCourses; c++){
			for (int t=0; t< parameters.numTimeSlots; t++){
				if (dataHolder.cTFeas[c][t]){
					count+= dataHolder.numLectures[c];
				} // end if
			} // end t for
		} // end c for
		return (float)count / (parameters.numEvents* parameters.numTimeSlots);
	}
	
	private static float computeRO() {
		return (float)parameters.numEvents / (parameters.numRooms* parameters.numTimeSlots);
	}
	
	private static void printInstanceDetails() {
		System.out.println("Conflict Density (CO): "+ CO);
		System.out.println("Teacher's Availability (TA): "+ TA);
		System.out.println("Room Occupation (RO): "+ RO);
	}
	
}

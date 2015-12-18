package data;

public class convertionManager {
	// dataArraySize= dataHolder.courseStartIndex[parameters.numCourses-1]+dataHolder.numLectures[parameters.numCourses-1];
	// parameters.numEvents= parameters.dataArraySize;

	// public int[] Data= new int[parameters.dataArraySize];
	// 12 bits: course id + (now for distinct lectures)
	// 4 bits:lectures + 8 bits: time slot start + 8 bits: room
	
	public static int eventValuesToInt(int courseId, int hours, int time, int room) {
		boolean[] temp= new boolean[32]; 
		
		for (boolean b: temp) 
			b=false;
		int i=0;
		while (room > 0){
			if (room % 2 == 0) temp[i]=false; else temp[i]= true; 
			i++;
			room= (byte) (room >> 1);		
		}
		i=8;
		while (time>0){
			if (time % 2 == 0) temp[i]=false; else temp[i]=true; 
			i++;
			time= (byte) (time >> 1);		
		}
		i=16;
		while (hours>0){
			if (hours % 2 == 0)temp[i]=false;
			else temp[i]=true;
			i++;
			hours= (byte) (hours >> 1);		
		}
		i=20;
		while (courseId>0){
			if (courseId % 2 == 0)temp[i]=false;
			else temp[i]=true;
			i++;
			courseId= (byte) (courseId >> 1);		
		}
		
		int wholeNumber=0;
		for (i=0; i<temp.length;i++){
			if (temp[i]==true) wholeNumber+= Math.pow(2, i);
		}
		return wholeNumber;
	}
	
	public static int eventToInt(int courseId, Event event) {
		boolean[] temp= new boolean[32]; 
		for (boolean b: temp) 
			b=false;
		int room= event.room;
		int time= event.time;
		int hours= event.hours;

		int i=0;
		while (room > 0){
			if (room % 2 == 0) temp[i]=false; else temp[i]= true; 
			i++;
			room= (byte) (room >> 1);		
		}
		i=8;
		while (time>0){
			if (time % 2 == 0) temp[i]=false; else temp[i]=true; 
			i++;
			time= (byte) (time >> 1);		
		}
		i=16;
		while (hours>0){
			if (hours % 2 == 0)temp[i]=false;
			else temp[i]=true;
			i++;
			hours= (byte) (hours >> 1);		
		}
		i=20;
		while (courseId>0){
			if (courseId % 2 == 0)temp[i]=false;
			else temp[i]=true;
			i++;
			courseId= (byte) (courseId >> 1);		
		}
		
		int wholeNumber=0;
		for (i=0; i<temp.length;i++){
			if (temp[i]==true) wholeNumber+= Math.pow(2, i);
		}
		return wholeNumber;
	}
	
	public static int intToCourseId(int data){
		int temp= data;
		int result= (temp >> 20);
		return result;
	}
	
	public static int intToTime(int data) {
		data= data & 1048575;// mask course id
		int temp= data;
		int time= (temp >>8) & 255;
		return time;
	}
	
	public static int intToRoom(int data) {
		data= data & 1048575;// mask course id
		int temp=data;
		int room= temp & 255;
		return room;
	}
	
	public static Event intToEvent(int ID, int data){
		data= data & 1048575;// mask course id
		int temp= data;
		int hours= temp >> 16;
		temp=data;
		int time= (temp >>8) & 255;
		temp=data;
		int room= temp & 255;
		Event evt= new Event(ID, hours, time, room );
		return evt;
	}
	
	public static Course getCourseFromArray(int id, int[] data){
		Course crs= new Course(id);
		for (int t=dataHolder.courseStartIndex[id]; t< (dataHolder.courseStartIndex[id]+ dataHolder.numLectures[id]); t++){
			Event evt= intToEvent(t, data[t]);
			crs.myEvents.add(evt);
		}
		return crs;
	}
	
	public static void putCourseToArray(Course crs, int data[]){ // Reference sent!
		for ( int t=dataHolder.courseStartIndex[crs.ID]; t< (dataHolder.courseStartIndex[crs.ID]+ dataHolder.numLectures[crs.ID]); t++ ){
			data[t]=0;			
		}
		int counter=0;
		for (Event evt: crs.myEvents){
			data[dataHolder.courseStartIndex[crs.ID]+counter]= eventToInt(crs.ID, evt);
			counter++;
		}
	}
	
}

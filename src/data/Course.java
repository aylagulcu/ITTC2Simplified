package data;

import java.util.ArrayList;

public class Course implements Cloneable{
	public int ID;
	public ArrayList<Event> myEvents= new ArrayList<Event>();

	public Course(int id){
		this.ID= id;
	}
	
	public Course clone(){
		Course crs= new Course(this.ID);
		for (Event e: this.myEvents){
			crs.myEvents.add(new Event(e.id, e.hours, e.time, e.room));
		}
		return crs;		
	}
	
}

package data;

public class Event implements Cloneable{
	public int id;
	public int hours;
	public int time;
	public int room;
	
	public Event(int ID, int duration, int timeSlot, int r ){
		this.id= ID;
		this.hours=duration;
		this.time= timeSlot;
		this.room=r;
	}
	
	public Event Clone(){
		return new Event(this.id, this.hours, this.time, this.room);
	}
	
}

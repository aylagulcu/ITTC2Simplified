package constraints;

public interface Unary {
	// Unary constraints: related to absolute positioning of a single event
	public boolean checkEventTime(int event, int time);
	public boolean checkEventRoom(int event, int room);
}

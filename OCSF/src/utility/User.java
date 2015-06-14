package utility;

public class User{

	private String id;
	private boolean inRoom;
	
	/* Constructor */
	public User(String id){
		setID(id);
	}
	
	/* Operation */
	public boolean checkID(String id){
		if(id.length()<2 || 20<id.length()) return false;
		return true;
	}
	
	/* Getter and Setter */
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	public boolean getInRoom() {
		return inRoom;
	}
	public void setInRoom(boolean inRoom) {
		this.inRoom = inRoom;
	}
} // class User END

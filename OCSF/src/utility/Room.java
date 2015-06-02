package utility;


public class Room {

	private String roomName;
	private boolean isPrivacyRoom;
	int inUserNum;
	
	/* Constructor */
	public Room(){
		inUserNum = 1;
	}
	
	/* Operation */
	public boolean checkRoomName(String roomName){
		if(2<=roomName.length() && roomName.length()<=20)return true;
		return false;
	}
	
	public void enterRoom(){
		inUserNum++;
	}
	
	/* Getter and Setter */
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public boolean isPrivacyRoom() {
		return isPrivacyRoom;
	}
	public void setPrivacyRoom(boolean isPrivacyRoom) {
		this.isPrivacyRoom = isPrivacyRoom;
	}
	public int getInUserNum() {
		return inUserNum;
	}
	

}

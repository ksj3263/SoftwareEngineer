package utility;

public class Room {

	private String roomName;
	private boolean isPrivacyRoom;
	private RoomList rmlst = RoomList.getInstance();
	
	/* Operation */
	public boolean checkRoomName(String roomName){
		if(2<=roomName.length() && roomName.length()<=20)return true;
		return false;
	}
	
	public void makeRoom(Room r){
		rmlst.addRoom(r);
	}
	
	public void enterRoom(Room r){
		rmlst.removeRoom(r);
	}
	
	public void exitRoom(Room r){
		
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
}

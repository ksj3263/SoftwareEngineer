package utility;

import java.util.Vector;

public class Room {

	private String roomName = null;
	private String roomType = null;
	private String roomPW = null;
	private Vector<String> usrlstInRoom = null;
	
	/* Constructor */
	public Room(){	}
	public Room(String roomName){
		setRoomName(roomName);
		setRoomType("public");
		usrlstInRoom = new Vector<String>();
	}
	public Room(String roomName, String roomPW){
		setRoomName(roomName);
		setRoomType("private");
		setRoomPW(roomPW);
		usrlstInRoom = new Vector<String>();
	}
	
	/* Operation */
	public boolean checkRoomName(String roomName){
		if(2<=roomName.length() && roomName.length()<=20)return true;
		return false;
	}
	
	private boolean checkPWlength(String roomPW){
		if(4<=roomPW.length() && roomPW.length()<=30) return true;
		return false;
	}
	
	private boolean isCapitalInPW(String roomPW){
		for(int i=0; i<roomPW.length(); i++)
			if('A'<=roomPW.charAt(i) && roomPW.charAt(i)<='Z') return true;
		return false;
	}
	
	private boolean isSmallInPW(String roomPW){
		for(int i=0; i<roomPW.length(); i++)
			if('a'<=roomPW.charAt(i) && roomPW.charAt(i)<='z') return true;
		return false;
	}
	
	private boolean isNumberInPW(String roomPW){
		for(int i=0; i<roomPW.length(); i++)
			if('0'<=roomPW.charAt(i) && roomPW.charAt(i)<='9') return true;
		return false;
	}
	
	public boolean checkSettingPW(String roomPW){
		if(checkPWlength(roomPW) && isCapitalInPW(roomPW) && isSmallInPW(roomPW) && isNumberInPW(roomPW)) {
			setRoomPW(roomPW);
			return true;
		}
		return false;
	}
	
	public boolean checkRoomPW(String roomPW){
		if(roomPW.equals(getRoomPW())) return true;
		return false;
	}
	
	public void enterRoom(String userID){
		usrlstInRoom.add(userID);
	}
	
	private int findUserByUserName(String userID){
		for(int i=0; i<usrlstInRoom.size(); i++){
			if(usrlstInRoom.get(i).equals(userID)) return i;
		}
		return -1;
	}
	
	public void exitRoom(String userID){
		int index = findUserByUserName(userID);
		usrlstInRoom.remove(index);
	}
	
	/* Getter and Setter */
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public String getRoomType() {
		return roomType;
	}
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}
	public String getRoomPW() {
		return roomPW;
	}
	public void setRoomPW(String roomPW) {
		this.roomPW = roomPW;
	}
	public Vector<String> getUsrlstInRoom() {
		return usrlstInRoom;
	}
	public void setUsrlstInRoom(Vector<String> usrlstInRoom) {
		this.usrlstInRoom = usrlstInRoom;
	}
	public int getUserNumInRoom(){
		return usrlstInRoom.size();
	}
}

package client;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class RoomItem {

	private SimpleStringProperty roomType;
	private SimpleStringProperty roomName;
	private SimpleIntegerProperty userNum;
	
	public RoomItem(String roomType, String roomName, int userNum){
		this.roomType = new SimpleStringProperty(roomType);
		this.roomName = new SimpleStringProperty(roomName);
		this.userNum = new SimpleIntegerProperty(userNum);
	}
	
	public String getRoomType() {
		return roomType.get();
	}
	public void setRoomType(SimpleStringProperty roomType) {
		this.roomType = roomType;
	}
	public String getRoomName() {
		return roomName.get();
	}
	public void setRoomName(SimpleStringProperty roomName) {
		this.roomName = roomName;
	}
	public int getUserNum() {
		return userNum.get();
	}
	public void setUserNum(SimpleIntegerProperty userNum) {
		this.userNum = userNum;
	}
}

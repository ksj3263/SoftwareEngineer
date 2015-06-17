package client;

import javafx.beans.property.SimpleStringProperty;

public class RoomItem {

	private SimpleStringProperty roomType;
	private SimpleStringProperty roomName;
	
	public RoomItem(String roomType, String roomName){
		this.roomType = new SimpleStringProperty(roomType);
		this.roomName = new SimpleStringProperty(roomName);
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
}

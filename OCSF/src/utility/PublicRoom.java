package utility;

public class PublicRoom extends Room{

	/* Constructor */
	public PublicRoom(){}
	
	public PublicRoom(String roomName){
		setRoomName(roomName);
		setPrivacyRoom(false);
	}
}

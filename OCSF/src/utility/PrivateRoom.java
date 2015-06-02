package utility;

public class PrivateRoom extends Room{

	private String roomPW;
	
	/*Constructor*/
	public PrivateRoom(){}
	
	public PrivateRoom(String roomName, String roomPW){
		setRoomName(roomName);
		setRoomPW(roomPW);
		setPrivacyRoom(true);
	}

	/* Operation */
	
	public boolean checkSettingPW(String roomPW){
		if(4<=roomPW.length() && roomPW.length()<=30){
			boolean Ualpha = false, Lalpha = false, num = false;
			for(int i=0; i<roomPW.length(); i++){
				if('A'<=roomPW.charAt(i) && roomPW.charAt(i)<='Z') Ualpha = true;
				if('a'<=roomPW.charAt(i) && roomPW.charAt(i)<='z') Lalpha = true;
				if('0'<=roomPW.charAt(i) && roomPW.charAt(i)<='9') num = true;
				if(Ualpha && Lalpha && num) return true;
			}
		}
		return false;
	}
	
	/* Getter and Setter */
	public String getRoomPW() {
		return roomPW;
	}
	public void setRoomPW(String roomPW) {
		this.roomPW = roomPW;
	}
}

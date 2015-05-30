package utility;

public class PrivateRoom extends Room{

	private String roomPW;

	/* Operation */
	public boolean isValidRoomPW(String roomPW){
		if(roomPW==getRoomPW()) return true;
		return false;
	}
	
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

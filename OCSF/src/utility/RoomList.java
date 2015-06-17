package utility;

import java.util.Vector;

import javafx.util.Pair;

public class RoomList {

	private Vector<Room> rmlst;
	private Vector<Pair<String,String>> sendlst;
	
	/* Singleton Pattern */
	private static volatile RoomList roomList;
	private RoomList(){
		rmlst = new Vector<Room>();
		sendlst = new Vector<Pair<String,String>>();
	}
	public static RoomList getInstance(){
		if(roomList == null){
			synchronized(RoomList.class){
				if(roomList == null)
					roomList = new RoomList();
			}
		}
		return roomList;
	}
	
	/* Operation */
	public void addRoom(Room r){
		rmlst.add(r);
		sendlst.add(new Pair<String,String>(r.getRoomType(),r.getRoomName()));
	}
	
	public int findRoomByRoomName(String roomName){
		for(int i=0; i<rmlst.size(); i++){
			if(rmlst.get(i).getRoomName().equals(roomName))
				return i;
		}
		return -1;
	}
	
	public boolean isOverlapRoomName(String roomName){
		int index = findRoomByRoomName(roomName);
		if(index==-1)return false;
		return true;
	}
	
	public void removeRoomByRoomName(String roomName){
		int index = findRoomByRoomName(roomName);
		if(index==-1)
			System.out.println("not exist room");
		else rmlst.remove(index);
	}
	
	public int getRoomNum(){
		return rmlst.size();
	}
	
	public Room getRoomByRoomName(String roomName){
		int index = findRoomByRoomName(roomName);
		return rmlst.get(index);
	}
	
	public Vector<Pair<String, String>> getSendlst() {
		return sendlst;
	}

}

package utility;

import java.util.Vector;

import javafx.util.Pair;

public class RoomList {

	private Vector<Room> rmlst;
	private Vector<Pair<Pair<String,String>,Integer>> sendlst;
	
	/* Singleton Pattern */
	private static volatile RoomList roomList;
	private RoomList(){
		rmlst = new Vector<Room>();
		sendlst = new Vector<Pair<Pair<String,String>,Integer>>();
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
		if(r.isPrivacyRoom()){
			sendlst.add(new Pair<Pair<String,String>, Integer>(new Pair("private", r.getRoomName()), r.getInUserNum())); 			
		}
		else{
			sendlst.add(new Pair<Pair<String,String>, Integer>(new Pair("public", r.getRoomName()), r.getInUserNum())); 			
		}
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
	
	public void removeRoom(Room r){
		int index = findRoomByRoomName(r.getRoomName());
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
	public Vector<Pair<Pair<String, String>, Integer>> getSendlst() {
		return sendlst;
	}

}

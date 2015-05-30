package utility;

import java.util.Vector;

public class UserList {

	private Vector<User> usrlst;
	
	/* Singleton Pattern */
	private static volatile UserList userList;
	private UserList(){
		usrlst = new Vector<User>();
	}
	public static UserList getInstance(){
		if(userList == null){
			synchronized(UserList.class){
				if(userList == null)
					userList = new UserList();
			}
		}
		return userList;
	}
	
	/* Operation */
	public void addUser(User u){
		if(usrlst.add(u))System.out.println("add "+u.getID());
		else System.out.println("not add "+u.getID());
	}
	
	public int findUserByUserId(String userID){
		for(int i=0; i<usrlst.size(); i++){
			if(usrlst.get(i).getID().equals(userID))return i;
		}
		return -1;
	}
	
	public boolean isOverlapId(String id){
		int index = findUserByUserId(id);
		if(index==-1)return false;
		return true;
	}
	
	public void removeUser(User u){
		int index = findUserByUserId(u.getID());
		if(index==-1)System.out.println("not exist "+u.getID());
		else usrlst.remove(index);
	}
	
	public User getUser(int index){
		return usrlst.get(index);
	}

	public int getUserNum(){
		return usrlst.size();
	}
}

package utility;

public class User{

	private String id;
	private UserList usrlst = UserList.getInstance();
	
	/* Constructor */
	public User(String id){
		setID(id);
	}
	
	/* Operation */
	public void login(User u){
		usrlst.addUser(u);
	}
	public void logout(User u){
		usrlst.removeUser(u);
	}

	/* Getter and Setter */
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
}

package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utility.Data;
import utility.Room;
import utility.RoomList;
import utility.User;
import utility.UserList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Server extends Application implements Initializable{

	private ExecutorService executorService;
	private ServerSocket serverSocket;
	private List<ToClient> connections = new Vector<ToClient>();
	private int port;
	
	private RoomList rmlst = RoomList.getInstance();
	private UserList usrlst = UserList.getInstance();
	
	private void startServer(){
		executorService = Executors.newFixedThreadPool(1234567890);
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
			if(serverSocket.isClosed()){
				stopServer();
			}
			return;
		}
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(()->{
					displayText("[Server Start ( " + port + " )]");
					btnStartStop.setText("stop");
				});
				while(true){
					try{
						Socket socket = serverSocket.accept();
						String message = "[Connection Accept: " + socket.getRemoteSocketAddress() + 
								": " + Thread.currentThread().getName() + "]";
						Platform.runLater(()->{
							displayText(message);
						});
						
						ToClient client = new ToClient(socket);
						connections.add(client);
						Platform.runLater(()->{
							displayText("[Connection Num: " + connections.size() + "]");
						});
					}catch(Exception e){
						if(!serverSocket.isClosed()){
							stopServer();
						} 
						break;
					}
				}
			}
		};
		executorService.submit(runnable);
	} // method startServer END
	
	private void stopServer(){
		try {
			Iterator<ToClient> iterator = connections.iterator();
			while(iterator.hasNext()){
				ToClient client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket!=null && !serverSocket.isClosed()){
				serverSocket.close();
			}
			if(executorService!=null && !executorService.isShutdown()){
				executorService.shutdown();
			}
			Platform.runLater(()->{
				displayText("[Server Stop]");
				btnStartStop.setText("start");
			});
		} catch (Exception e) {}
	} // method stopServer END
	
	private class ToClient{
		private Socket socket;
		private Socket rm_socket;
		private Socket file_socket;
		private String userID;
		private String inRoomName;

		ToClient(Socket socket){
			this.socket = socket;
			receive();
		}
		
		private void receive(){
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while(true){
							ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
							Data data = (Data)input.readObject();
							System.out.println(data.toString());
							inMessage(data.getProtocol(), data.getData());
						}
					} catch (Exception e) {
						try{
							connections.remove(ToClient.this);
							String message = "[Client DisConnect: " + 
							socket.getRemoteSocketAddress() + ": " + 
									Thread.currentThread().getName() + "]";
							Platform.runLater(()->{
								displayText(message);
							});
							socket.close();
						}catch(IOException e2){}
					}
				}
			};
			executorService.submit(runnable);
		} // method receive END
		
		private void send(String protocol, Object data){
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
						output.writeObject(new Data(protocol, data));
						output.flush();
					} catch (Exception e) {
						try {
							String message = "[Client Disconnect: " + 
						socket.getRemoteSocketAddress() + ": " + 
									Thread.currentThread().getName() + "]";
							Platform.runLater(()->{
								displayText(message);
							});
							connections.remove(ToClient.this);
							socket.close();
						} catch (IOException e2) {
							System.out.println("send() ERROR");
						}
					}
				}
			};
			executorService.submit(runnable);
		} // method send END
		
		private void broadcast(String protocol, Object data){
			for(ToClient client:connections)
				client.send(protocol, data);
		}
		
		private void broadcastInRoom(String inRoomName, String protocol, String data){
			for(ToClient client:connections){
				if(client.getInRoomName().equals(inRoomName) && client!=this){
					Pair<String, String> msg = new Pair<String, String>(getUserID(), data);
					client.send(protocol, msg);
				}
			}
		}
		
		private void inMessage(String protocol, Object data){
			if(protocol.equals("LogIn")){
				String userID = data.toString();
				login(userID);
			}
			else if(protocol.equals("LogOut")){
				String userID = data.toString();
				logout(userID);
			}
			else if(protocol.equals("UpdatRoomlst")){
				send("UpdatRoomlst", rmlst.getSendlst());
			}
			else if(protocol.equals("MakePublic")){
				String inRoomName = data.toString();
				Room inRoom = new Room(inRoomName);
				makePublic(inRoom);				
			}
			else if(protocol.equals("MakePrivate")){
				Pair<String, String> pri_rm = (Pair<String, String>) data;
				Room inRoom = new Room(pri_rm.getKey(),pri_rm.getValue());
				makePrivate(inRoom);				
			}
			else if(protocol.equals("Enterpublic")){
				String inRoomName = data.toString();
				Room inRoom = rmlst.getRoomByRoomName(inRoomName);
				enterPublic(inRoom);
			}
			else if(protocol.equals("Enterprivate")){
				Pair<String, String> pri_rm = (Pair<String, String>) data;
				String inRoomName = pri_rm.getKey(); String inRoomPW = pri_rm.getValue();
				Room inRoom = rmlst.getRoomByRoomName(inRoomName);
				enterPrivate(inRoom, inRoomPW);
			}
			else if(protocol.equals("ExitRoom")){
				exitRoom();
			}
			else if(protocol.equals("Message")){
				broadcastInRoom(getInRoomName(), "Message", data.toString());
			}
			else if(protocol.equals("Imoticon")){
				broadcastInRoom(getInRoomName(), "Imoticon", data.toString());
			}
			else if(protocol.equals("File")){
				
			}
		}
		
		// Operation //
		public void login(String userID){
			boolean lgn_result = usrlst.isOverlapId(userID);
			
			if(lgn_result) send("LogIn","NO");
			else {
				send("LogIn","YES");
				setUserID(userID);
				usrlst.addUser(new User(getUserID()));
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				broadcast("UserListInLobby", usrlst.getIdList());
			}
		}
		
		public void logout(String userID){
			usrlst.removeUserByUserId(userID);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(ToClient client:connections){
				if(client!=this) client.send("UserListInLobby", usrlst.getIdList());
				else client.send("LogOut", null);
			}
		}

		public void makePublic(Room inRoom){
			boolean rm_result = rmlst.isOverlapRoomName(inRoom.getRoomName());
			if(rm_result) send("MakeRoom","NO");
			else {
				send("MakeRoom","YES");
				setInRoomName(inRoom.getRoomName());
				inRoom.enterRoom(getUserID());
				rmlst.addRoom(inRoom);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				broadcast("public", inRoom.getRoomName());
			}
		}

		public void makePrivate(Room inRoom){
			boolean rm_result = rmlst.isOverlapRoomName(inRoom.getRoomName());
			if(rm_result) send("MakeRoom","NO");
			else {
				send("MakeRoom","YES");
				setInRoomName(inRoom.getRoomName());
				inRoom.enterRoom(getUserID());
				rmlst.addRoom(inRoom);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				broadcast("private", inRoom.getRoomName());
			}
		}

		public void enterPublic(Room inRoom){
			setInRoomName(inRoom.getRoomName());
			inRoom.enterRoom(getUserID());
			send("Enterpublic",null);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(ToClient client:connections){
				if(client.getInRoomName().equals(getInRoomName())){
					client.send("UserListInRoom", inRoom.getUsrlstInRoom());
				}
			}
		}
		
		public void enterPrivate(Room inRoom, String inRoomPW){
			boolean pw_result = inRoom.checkRoomPW(inRoomPW);
			if(pw_result) {
				setInRoomName(inRoom.getRoomName());
				inRoom.enterRoom(getUserID());
				send("Enterprivate","YES");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				for(ToClient client:connections){
					if(client.getInRoomName().equals(getInRoomName())){
						client.send("UserListInRoom", inRoom.getUsrlstInRoom());
					}
				}
			}
			else send("Enterprivate","NO");
		}
		
		public void exitRoom(){
			Room inRoom = rmlst.getRoomByRoomName(getInRoomName());
			inRoom.exitRoom(getUserID());
			
			send("ExitRoom",null);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
				
			for(ToClient client:connections){
				if(client.getInRoomName().equals(getInRoomName()) && client!=this){
					client.send("UserListInRoom", inRoom.getUsrlstInRoom());
				}
			}
			
			setInRoomName(null);
		}
		
		// Getter and Setter //
		public String getUserID() {
			return userID;
		}
		public void setUserID(String userID){
			this.userID = userID;
		}
		public String getInRoomName() {
			return inRoomName;
		}
		public void setInRoomName(String inRoomName){
			this.inRoomName = inRoomName;
		}

	} // class ToClient END
	
	/* GUI */
	@FXML private TextArea usrDisplay;
	@FXML private TextArea rmDisplay;
	@FXML private Button btnStartStop;
	@FXML private TextField portInput;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnStartStop.setOnAction(e->{
			try {
				port = Integer.valueOf(portInput.getText());
				if(port<1111 || 9999<port) Integer.valueOf("fail");
				
				if(btnStartStop.getText().equals("start")){
					portInput.setDisable(true);
					portInput.setEditable(false);
					startServer();
				} else if(btnStartStop.getText().equals("stop")){
					portInput.setDisable(false);
					portInput.setEditable(true);
					portInput.clear();
					stopServer();
				}
			} catch (Exception e1) {
				portInput.clear();
			}
		});
	}
	@Override
	public void start(Stage primaryStage) throws Exception{
		Parent parent = FXMLLoader.load(getClass().getResource("Server.fxml"));
		Scene scene = new Scene(parent);
		scene.getStylesheets().add(getClass().getResource("Server.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("k2nm_Server");
		primaryStage.setOnCloseRequest(event->stopServer());
		primaryStage.setResizable(false);
		primaryStage.show();	
	}
	
	void displayText(String text){
		usrDisplay.appendText(text+"\n");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
} // class Server END

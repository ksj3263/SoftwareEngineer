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
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.RoomItem;
import utility.Data;
import utility.PrivateRoom;
import utility.PublicRoom;
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
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Pair;


public class Server extends Application implements Initializable{

	ExecutorService executorService;
	ServerSocket serverSocket;
	List<ToClient> connections = new Vector<ToClient>();
	int port;
	StringTokenizer st;
	RoomList rmlst = RoomList.getInstance();
	UserList usrlst = UserList.getInstance();
	
	void startServer(){
		executorService = Executors.newFixedThreadPool(1000);
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
	}	
	void stopServer(){
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
	}
	
	class ToClient{
		Socket socket;
		private String userID;
		private String inRoomName;

		ToClient(Socket socket){
			this.socket = socket;
			receive();
		}
		
		void receive(){
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
		}
		
		void send(String protocol, Object data){
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
		}
		
		void inMessage(String protocol, Object data){
			
			if(protocol.equals("LogIn")){
				boolean lgn_result = usrlst.isOverlapId(data.toString());
				if(lgn_result) send("LogIn","NO");
				else {
					send("LogIn","YES");
					userID = data.toString();
					usrlst.addUser(new User(userID));
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					for(ToClient client:connections){
						if(client!=this) client.send("NewUser", userID);
						else{
							send("OldUser", usrlst.getIdList());
						}
					}
				}
			}
			else if(protocol.equals("LogOut")){
				usrlst.removeUserByUserId(data.toString());
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				for(ToClient client:connections){
					if(client!=this) client.send("UserOut", data.toString());
					else client.send("LogOut", "OK");
				}
			}
			else if(protocol.equals("OldRoom") && data.toString().equals("OK")){
				send("OldRoom", rmlst.getSendlst());
			}
			else if(protocol.equals("MakePublic")){
				boolean rm_result = rmlst.isOverlapRoomName(data.toString());
				if(rm_result) send("MakePublic","NO");
				else {
					send("MakePublic","YES");
					inRoomName = data.toString();
					rmlst.addRoom(new PublicRoom(inRoomName));
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					for(ToClient client:connections){
						Pair<String, Integer> rm = new Pair(inRoomName,rmlst.getRoomByRoomName(inRoomName).getInUserNum());
						client.send("public", rm);
					}
				}
			}
			else if(protocol.equals("MakePrivate")){
				Pair<String, String> pri_rm = (Pair<String, String>) data;
				boolean rm_result = rmlst.isOverlapRoomName(pri_rm.getKey());
				if(rm_result) send("MakePrivate","NO");
				else {
					send("MakePrivate","YES");
					inRoomName = pri_rm.getKey();
					rmlst.addRoom(new PrivateRoom(inRoomName, pri_rm.getValue()));
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					for(ToClient client:connections){
						Pair<String, Integer> rm = new Pair(inRoomName,rmlst.getRoomByRoomName(inRoomName).getInUserNum());
						client.send("private", rm);
					}
				}
			}
			else if(protocol.equals("EnterRoom")){
				
			}
			else if(protocol.equals("ExitRoom")){
				
			}
			else if(protocol.equals("Message")){
				for(ToClient client:connections){
					if(client.getInRoomName().equals(inRoomName) /*&& client!=this*/){
						Pair<String, String> msg = new Pair<String, String>(userID, data.toString());
						client.send("Message", msg);
					}
				}
			}
			else if(protocol.equals("Imoticon")){
				for(ToClient client:connections){
					if(client.getInRoomName().equals(inRoomName) /*&& client!=this*/){
						Pair<String, String> msg = new Pair<String, String>(userID, data.toString());
						client.send("Imoticon", msg);
					}
				}
			}
			else if(protocol.equals("FileRequest")){
				for(ToClient client:connections){
					if(client.getUserID().equals(data) /*&& client!=this*/){
						Pair<String, String> msg = new Pair<String, String>(userID, data.toString());
						client.send("FileRequest", msg);
					}
				}
			}
			else if(protocol.equals("FileAccept")){
				st = new StringTokenizer((String) data, "/");
				String target = st.nextToken();
 
				for(ToClient client:connections){
					if(client.getUserID().equals(target) /*&& client!=this*/){
						Pair<String, String> msg = new Pair<String, String>(userID, data.toString());
						client.send("FileAccept", msg);
					}
				}
			}
		}
		
		// Getter //
		public String getUserID() {
			return userID;
		}
		public String getInRoomName() {
			return inRoomName;
		}
	} // class ToClient END
	
	/* GUI */
	@FXML TextArea usrDisplay;
	@FXML TextArea rmDisplay;
	@FXML Button btnStartStop;
	@FXML TextField portInput;
	
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

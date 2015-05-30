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

public class Server extends Application implements Initializable{

	ExecutorService executorService;
	ServerSocket serverSocket;
	ObjectInputStream input;
	ObjectOutputStream output;
	List<ToClient> connections = new Vector<ToClient>();
	int port;
	
	RoomList rmlst = RoomList.getInstance();
	UserList usrlst = UserList.getInstance();
	
	void startServer(){
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			if(serverSocket.isClosed()){
				stopServer();
			}
			return;
		}
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(()->{
					displayText("[Server Start (" + port + ")]");
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
							/*byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();
							
							int readByteCount = inputStream.read(byteArr);
							
							if(readByteCount==-1){
								throw new IOException();
							}
							
							String message = "[Request: " + socket.getRemoteSocketAddress() + 
									": " + Thread.currentThread().getName() + "]";
							Platform.runLater(()->{
								displayText(message);
							});
							
							String data = new String(byteArr,0,readByteCount,"UTF-8");*/
							input = new ObjectInputStream(socket.getInputStream());
							String protocol = (String)input.readObject();
							Object data = input.readObject();
							inMessage(protocol, data);
							
							Thread.sleep(2000);
							System.out.println("sleep");
							/*for(ToClient client:connections){
								client.send("Message", data);
							}*/
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
						/*byte[] byteArr = data.getBytes("UTF-8");
						OutputStream outputStream = socket.getOutputStream();
						outputStream.write(byteArr);
						outputStream.flush();*/
						output = new ObjectOutputStream(socket.getOutputStream());
						output.writeObject(protocol);
						output.writeObject(data);
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
				if(lgn_result) send(protocol,"NO");
				else {
					send(protocol,"YES");
					usrlst.addUser(new User(data.toString()));
				}
			}
			else if(protocol.equals("MakeRoom")){
				
			}
			else if(protocol.equals("EnterRoom")){
				
			}
			else if(protocol.equals("ExitRoom")){
				
			}
			else if(protocol.equals("Message")){
				
			}
			else if(protocol.equals("Imoticon")){
				
			}
			else if(protocol.equals("File")){
				
			}
		}
	}
	
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
}

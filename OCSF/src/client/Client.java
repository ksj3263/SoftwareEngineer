package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;



import javax.jws.Oneway;



import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Client extends Application implements Initializable{

	Socket socket;
	ObjectInputStream input;
	ObjectOutputStream output;
	
	String userID;
	String ip;
	int port;
	
	Vector usrlst = new Vector();
	Vector rmlst = new Vector();
	
	LoginGUI lgngui = new LoginGUI();
	LobbyGUI lbbgui = new LobbyGUI();
	RoomGUI rmgui = new RoomGUI();

	void startClient(){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(ip,port));
					System.out.println("startClient()");
				} catch (Exception e) {
					System.out.println("startClient() ERROR");
					Platform.runLater(()->{
						lgngui.getIdInput().clear();
						lgngui.getIpInput().clear();
						lgngui.getPortInput().clear();
					});
					if(!socket.isClosed()){
						stopClient();
					}
				}
				send("LogIn", userID);
				receive();
			}
		};
		thread.start();
	}
	void stopClient(){
		try {
			System.out.println("stopClient()");
			if(socket!=null && !socket.isClosed()){
				socket.close();
			}
			if(input!=null) input.close();
			if(output!=null) output.close();
		} catch (IOException e) {
			System.out.println("stropClient() ERROR");
		}
	}
	void receive(){
		while(true){
			try {
				input = new ObjectInputStream(socket.getInputStream());
				String protocol = (String)input.readObject();
				Object data = input.readObject();
				System.out.println("receive() protocol: " + protocol);
				System.out.println("receive() data: " + data.toString());
				inMessage(protocol, data);
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("receive() ERROR");
				stopClient();
				break;
			}
		}
	}
	void send(String protocol, Object data){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try {
					/*byte[] byteArr = data.getBytes("UTF-8");
					OutputStream outputStream = socket.getOutputStream();
					outputStream.write(byteArr);
					outputStream.flush();*/
					output = new ObjectOutputStream(socket.getOutputStream());
					output.writeObject(protocol);
					output.writeObject(data);
					System.out.println("send()");
				} catch (Exception e) {
					System.out.println("send() ERROR");
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	void inMessage(String protocol, Object data){
		if(protocol.equals("LogIn")){
			if(data.toString().equals("YES")){
				Platform.runLater(()->{
					lgngui.hide();
					lbbgui.show();
					rmgui.show();
				});
			}
			else if(data.toString().equals("NO")){
				stopClient();
				Platform.runLater(()->{
					lgngui.getIdInput().clear();
					lgngui.getIpInput().clear();
					lgngui.getPortInput().clear();
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information");
					alert.setHeaderText(null);
					alert.setContentText("Others are using the ID you input.\nPlease input other ID.");
					alert.showAndWait();
				});
			}
		}
		else if(protocol.equals("NewUser")){
			usrlst.add(data.toString());
			
		}
		else if(protocol.equals("OldUser")){
			
		}
	}
	
	/* GUI */
	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	@Override
	public void start(Stage primaryStage) throws Exception {
		lgngui.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
	

	/* LoginGUI */
	class LoginGUI extends Stage implements Initializable{
		
		AnchorPane root;
		Label k2nm_lbl, id_lbl, ip_lbl, port_lbl;
		private TextField idInput, ipInput, portInput;
		Button btnLogin;
		
		public LoginGUI(){
			root = new AnchorPane(); root.setId("root");
			root.setPrefSize(400, 280);
			
			k2nm_lbl = new Label("K2NMchat"); k2nm_lbl.setId("k2nm_lbl");
			k2nm_lbl.setLayoutX(25.0); k2nm_lbl.setLayoutY(15.0); k2nm_lbl.setPrefSize(350.0, 50.0);
			root.setTopAnchor(k2nm_lbl, 15.0);
			root.getChildren().add(k2nm_lbl);
			
			id_lbl = new Label("ID"); id_lbl.setId("id_lbl");
			id_lbl.setLayoutX(25.0); id_lbl.setLayoutY(100.0); id_lbl.setPrefSize(130.0, 25.0);
			root.setLeftAnchor(id_lbl, 25.0);
			root.getChildren().add(id_lbl);
			
			ip_lbl = new Label("Server IP"); ip_lbl.setId("ip_lbl");
			ip_lbl.setLayoutX(25.0); ip_lbl.setLayoutY(140.0); ip_lbl.setPrefSize(130.0, 25.0);
			root.setLeftAnchor(ip_lbl, 25.0);
			root.getChildren().add(ip_lbl);
			
			port_lbl = new Label("Server port"); port_lbl.setId("port_lbl");
			port_lbl.setLayoutX(25.0); port_lbl.setLayoutY(180.0); port_lbl.setPrefSize(130.0, 25.0);
			root.setLeftAnchor(port_lbl, 25.0);
			root.getChildren().add(port_lbl);
			
			idInput = new TextField(); idInput.setId("idInput");
			idInput.setLayoutX(110.0); idInput.setLayoutY(100.0); idInput.setPrefSize(200.0, 25.0);
			idInput.setPromptText("length of ID: 2 ~ 20");
			root.setRightAnchor(idInput, 25.0);
			root.getChildren().add(idInput);
			
			ipInput = new TextField(); ipInput.setId("ipInput");
			ipInput.setLayoutX(110.0); ipInput.setLayoutY(140.0); ipInput.setPrefSize(200.0, 25.0);
			ipInput.setPromptText("0.0.0.0 ~ 255.255.255.255");
			root.setRightAnchor(ipInput, 25.0);
			root.getChildren().add(ipInput);
			
			portInput = new TextField(); portInput.setId("portInput");
			portInput.setLayoutX(110.0); portInput.setLayoutY(180.0); portInput.setPrefSize(200.0, 25.0);
			portInput.setPromptText("1111 ~ 9999");
			root.setRightAnchor(portInput, 25.0);
			root.getChildren().add(portInput);
			
			btnLogin = new Button("Log In"); btnLogin.setId("btnLogin");
			btnLogin.setLayoutX(155); btnLogin.setLayoutY(245); btnLogin.setPrefSize(90,30);
			btnLogin.setOnAction(event->handlebntLogin(event));
			root.setBottomAnchor(btnLogin,25.0);
			root.getChildren().add(btnLogin);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("LoginGUI.css").toString());
			this.setResizable(false);
			this.setScene(scene);
		}
		
		@Override
		public void initialize(URL location, ResourceBundle resources) {}
		
		public void handlebntLogin(ActionEvent event) {
			System.out.println("handleBntLogin()");
			try {
				userID = idInput.getText();
				ip = ipInput.getText();
				port = Integer.valueOf(portInput.getText());
				
				if(userID.length()<2 || 20<userID.length()) Integer.valueOf("fail");
				if(port<1111 || 9999<port) Integer.valueOf("fail");
				
				startClient();		
			} catch (Exception e) {
				System.out.println("handlebtnLogin ERROR");
				idInput.clear();
				ipInput.clear();
				portInput.clear();
			}
		}
		
		// Getter
		public TextField getIdInput() {
			return idInput;
		}
		public TextField getIpInput() {
			return ipInput;
		}
		public TextField getPortInput() {
			return portInput;
		}

	}

	/* LobbyGUI */
	class LobbyGUI extends Stage implements Initializable{

		AnchorPane root;
		Label rmlst_lbl; TableView rmlst_tv; TableColumn rmType, rmName, rmNum;
		Label usrlst_lbl; ListView usrlst_lv;
		Button btnEnterRm, btnMakeRm, btnLogout;
		
		public LobbyGUI(){
			root = new AnchorPane(); root.setId("root");
			root.setPrefSize(800.0, 600.0);
			
			rmlst_lbl = new Label("Room List"); rmlst_lbl.setId("rmlst_lbl");
			rmlst_lbl.setLayoutX(30.0); rmlst_lbl.setLayoutY(14.0); rmlst_lbl.setPrefSize(500.0, 50.0);
			root.setRightAnchor(rmlst_lbl, 30.0); root.setTopAnchor(rmlst_lbl, 10.0);
			root.getChildren().add(rmlst_lbl);
			
			rmlst_tv = new TableView(); rmlst_tv.setId("rmlst_tv");
			rmlst_tv.setLayoutX(14.0); rmlst_tv.setLayoutY(14.0); rmlst_tv.setPrefSize(500.0, 420.0);
			root.setLeftAnchor(rmlst_tv, 30.0); root.setTopAnchor(rmlst_tv, 70.0);
			rmlst_tv.setEditable(false);
			root.getChildren().add(rmlst_tv);
			
			rmType = new TableColumn("Room TYPE"); rmType.setId("rmType");
			rmType.setPrefWidth(150.0); rmType.setResizable(false); rmType.setEditable(false);
			rmName = new TableColumn("Room NAME"); rmName.setId("rmName");
			rmName.setPrefWidth(250.0); rmName.setResizable(false); rmName.setEditable(false);
			rmNum = new TableColumn("Room NUM"); rmNum.setId("rmNum");
			rmNum.setPrefWidth(100.0); rmNum.setResizable(false); rmNum.setEditable(false);
			rmlst_tv.getColumns().addAll(rmType, rmName, rmNum);			
			
			usrlst_lbl = new Label("User List"); usrlst_lbl.setId("usrlst_lbl");
			usrlst_lbl.setLayoutX(739.0); usrlst_lbl.setLayoutY(14.0); usrlst_lbl.setPrefSize(200.0, 50.0);
			root.setRightAnchor(usrlst_lbl, 30.0); root.setTopAnchor(usrlst_lbl, 10.0);
			root.getChildren().add(usrlst_lbl);
			
			usrlst_lv = new ListView(); usrlst_lv.setId("usrlst_lv");
			usrlst_lv.setLayoutX(435.0); usrlst_lv.setLayoutY(50.0); usrlst_lv.setPrefSize(200.0, 420.0);
			root.setRightAnchor(usrlst_lv, 30.0); root.setTopAnchor(usrlst_lv, 70.0);
			root.getChildren().add(usrlst_lv);
			
			btnEnterRm = new Button("Enter Room"); btnEnterRm.setId("btnEnterRm");
			btnEnterRm.setLayoutX(30.0); btnEnterRm.setLayoutY(474.0); btnEnterRm.setPrefSize(200.0, 50.0);
			root.setBottomAnchor(btnEnterRm, 30.0); root.setLeftAnchor(btnEnterRm, 30.0);
			root.getChildren().add(btnEnterRm);
			
			btnMakeRm = new Button("Make Room"); btnMakeRm.setId("btnMakeRm");
			btnMakeRm.setLayoutX(239.0); btnMakeRm.setLayoutY(474.0); btnMakeRm.setPrefSize(200.0, 50.0);
			root.setBottomAnchor(btnMakeRm, 30.0); root.setLeftAnchor(btnMakeRm, 250.0);
			root.getChildren().add(btnMakeRm);
			
			btnLogout = new Button("Log Out"); btnLogout.setId("btnLogout");
			btnLogout.setLayoutX(570.0); btnLogout.setLayoutY(474.0); btnLogout.setPrefSize(200.0, 50.0);
			root.setBottomAnchor(btnLogout, 30.0); root.setRightAnchor(btnLogout, 30.0);
			root.getChildren().add(btnLogout);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("LobbyGUI.css").toString());
			this.setResizable(false);
			this.setScene(scene);
		}
		
		@Override
		public void initialize(URL location, ResourceBundle resources) {}
	}
	
	/* RoomGUI */
	class RoomGUI extends Stage implements Initializable{
		AnchorPane root;
		ScrollPane msgScrl; VBox msgDisplay;
		TextArea msgInput; Button btnSndMsg;
		TilePane imtcInput; Button btnSndImtc;
		Label info_lbl; TextField info;
		Label usrlst_lbl; ListView usrlst_view;
		Button btnSndFile;
		Button btnExitRm;
		
		static final int imtcNum = 82;
		Button[] btn = new Button[imtcNum];
		String selectedImtc;
	
		public RoomGUI(){
			root = new AnchorPane(); root.setId("root");
			root.setPrefSize(800.0, 600.0);
			
			VBox vb1 = new VBox();
			vb1.setLayoutX(14.0); vb1.setLayoutY(14.0); vb1.setPrefSize(550.0, 200.0); vb1.setSpacing(15.0); 
			root.setBottomAnchor(vb1, 20.0); root.setLeftAnchor(vb1, 20.0); root.setTopAnchor(vb1, 20.0);
			root.getChildren().add(vb1);
			
			msgScrl = new ScrollPane(); msgScrl.setId("msgScrl");
			msgScrl.setPrefSize(200.0, 250.0);
			msgScrl.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
			vb1.getChildren().add(msgScrl);
			
			msgDisplay = new VBox(); msgDisplay.setId("msgDisplay");
			msgDisplay.setPrefWidth(500.0); msgDisplay.setSpacing(10.0);
			msgScrl.setContent(msgDisplay);
			
			HBox _hb1 = new HBox();
			_hb1.setPrefSize(550.0, 90.0); _hb1.setSpacing(10.0);
			vb1.getChildren().add(_hb1);
			
			msgInput = new TextArea(); msgInput.setId("msgInput");
			msgInput.setPrefSize(480.0, 90.0);
			_hb1.getChildren().add(msgInput);
			
			btnSndMsg = new Button("Send"); btnSndMsg.setId("btnSndMsg");
			btnSndMsg.setPrefSize(60.0, 30.0);
			btnSndMsg.setOnAction(event->handleBtnSndMsg(event));
			_hb1.getChildren().add(btnSndMsg);
			
			HBox _hb2 = new HBox();
			_hb2.setPrefSize(200.0, 200.0); _hb2.setSpacing(10.0);
			vb1.getChildren().add(_hb2);
			
			ScrollPane sp = new ScrollPane();
			sp.setPrefSize(480.0, 200.0); sp.setPrefViewportHeight(200.0); sp.setPrefViewportWidth(480.0);
			_hb2.getChildren().add(sp);
			
			imtcInput = new TilePane(); imtcInput.setId("imtcInput");
			imtcInput.setPrefSize(465.0, 200.0); imtcInput.setHgap(5.0); imtcInput.setVgap(5.0);
			for(int i=0; i<imtcNum; i++){
				String image = RoomGUI.class.getResource("imoticon/"+String.valueOf(i+1)+".png").toExternalForm();
				btn[i] = new Button();
				btn[i].setPrefSize(40.0, 40.0);
				btn[i].setStyle("-fx-background-image: url(" + image + ");" + "-fx-background-position: center center;" + "-fx-background-size: 40 40;");
				btn[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e){
						selectedImtc = image;
					}
				});
				imtcInput.getChildren().add(btn[i]);
			}
			sp.setContent(imtcInput);
			
			btnSndImtc = new Button("Send"); btnSndImtc.setId("btnSndImtc");
			btnSndImtc.setPrefSize(60.0, 30.0);
			_hb2.getChildren().add(btnSndImtc);
			
			VBox vb2 = new VBox();
			vb2.setLayoutX(580.0); vb2.setLayoutY(20.0); vb2.setSpacing(15.0);
			root.setBottomAnchor(vb2, 20.0); root.setRightAnchor(vb2, 20.0); root.setTopAnchor(vb2, 20.0);
			root.getChildren().add(vb2);
			
			VBox _vb1 = new VBox();
			vb1.setSpacing(5.0);
			vb2.getChildren().add(_vb1);
			
			info_lbl = new Label("Info"); info_lbl.setId("info_lbl");
			info_lbl.setPrefSize(200.0, 20.0);
			_vb1.getChildren().add(info_lbl);
			
			info = new TextField(); info.setId("info");
			info.setPrefSize(200.0, 30.0);
			info.setEditable(false);
			_vb1.getChildren().add(info);
			
			VBox _vb2 = new VBox();
			_vb2.setSpacing(5.0);
			vb2.getChildren().add(_vb2);
			
			usrlst_lbl = new Label("User List"); usrlst_lbl.setId("usrlst_lbl");
			usrlst_lbl.setPrefSize(200.0, 30.0);
			_vb2.getChildren().add(usrlst_lbl);
			
			usrlst_view = new ListView(); usrlst_view.setId("usrlst_view");
			usrlst_view.setPrefSize(200.0, 320.0);
			_vb2.getChildren().add(usrlst_view);
			
			VBox _vb3 = new VBox();
			_vb3.setSpacing(10.0);
			vb2.getChildren().add(_vb3);
			
			btnSndFile = new Button("Send File"); btnSndFile.setId("btnSndFile");
			btnSndFile.setPrefSize(200.0, 50.0);
			_vb3.getChildren().add(btnSndFile);
			
			btnExitRm = new Button("Exit Room"); btnExitRm.setId("btnExitRm");
			btnExitRm.setPrefSize(200.0, 50.0);
			_vb3.getChildren().add(btnExitRm);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("RoomGUI.css").toString());
			this.setResizable(false);
			this.setScene(scene);
		}
	
		@Override
		public void initialize(URL location, ResourceBundle resources) {}

		/* Event */
		public void handleBtnSndMsg(ActionEvent event){
			String data = msgInput.getText();
			send("Message", data);
		}
	}


} // class Client END


package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

import utility.Data;
import utility.PrivateRoom;
import utility.PublicRoom;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class Client extends Application implements Initializable{

	Socket socket;
	String userID;
	String ip;
	int port;
	
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
		} catch (IOException e) {
			System.out.println("stropClient() ERROR");
		}
	}
	void receive(){
		while(true){
			try {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				Data data = (Data)input.readObject();
				System.out.println("receive()\n" + data.toString());
				inMessage(data.getProtocol(), data.getData());
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
					ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
					output.writeObject(new Data(protocol, data));
					output.flush();
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
					lgngui.close();
					lbbgui.show();
				});
			}
			else if(data.toString().equals("NO")){
				stopClient();
				Platform.runLater(()->{
					lgngui.getIdInput().clear();
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information");
					alert.setHeaderText(null);
					alert.setContentText("Others are using the ID you input.\nPlease input other ID.");
					alert.showAndWait();
				});
			}
		}
		else if(protocol.equals("LogOut")){
			if(data.toString().equals("OK")){
				Platform.runLater(()->{
					lbbgui.getUsrlst().clear();
					lbbgui.close();
					lgngui.show();
				});
				stopClient();
			}
		}
		else if(protocol.equals("NewUser")){
			Platform.runLater(()->{
				lbbgui.getUsrlst().add(data.toString());
				lbbgui.getUsrlst_lv().setItems(null);
				lbbgui.getUsrlst_lv().setItems(lbbgui.getUsrlst());
			});
		}
		else if(protocol.equals("OldUser")){
			Vector<String> usr = (Vector<String>) data;
			Platform.runLater(()->{
				lbbgui.getUsrlst().addAll(usr);
				lbbgui.getUsrlst_lv().setItems(lbbgui.getUsrlst());
			});
			send("OldRoom","OK");
		}
		else if(protocol.equals("UserOut")){
			Platform.runLater(()->{
				lbbgui.getUsrlst().remove(data.toString());
				lbbgui.getUsrlst_lv().setItems(lbbgui.getUsrlst());
			});
		}
		else if(protocol.equals("MakePublic") || protocol.equals("MakePrivate")){
			if(data.toString().equals("YES")){
				Platform.runLater(()->{
					lbbgui.getDialog().close();
					lbbgui.close();
					rmgui.show();
				});
			}
			else if(data.toString().equals("NO")){
				Platform.runLater(()->{
					lbbgui.getNameInput().clear();
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information");
					alert.setHeaderText(null);
					alert.setContentText("Others are using the ROOM NAME you input.\nPlease input other ROOM NAME.");
					alert.showAndWait();
				});
			}
		}
		else if(protocol.equals("public") || protocol.equals("private")){
			Pair<String, Integer> msg = (Pair<String, Integer>)data;
			RoomItem item = new RoomItem(protocol, msg.getKey(), msg.getValue());
			Platform.runLater(()->{
				lbbgui.getRmlst().add(item);
				lbbgui.getRmlst_tv().setItems(null);
				lbbgui.getRmlst_tv().setItems(lbbgui.getRmlst());
			});
		}
		else if(protocol.equals("OldRoom")){
			Vector<Pair<Pair<String, String>, Integer>> msg = (Vector<Pair<Pair<String, String>, Integer>>) data;
			Platform.runLater(()->{
				for(int i=0; i<msg.size(); i++){
					Pair<Pair<String, String>, Integer> _msg = msg.get(i);
					RoomItem item = new RoomItem(_msg.getKey().getKey(),_msg.getKey().getValue(),_msg.getValue());
					lbbgui.getRmlst().add(item);
				}
				lbbgui.getRmlst_tv().setItems(lbbgui.getRmlst());
			});
		}
		else if(protocol.equals("Message")){
			Pair<String, String> msg = (Pair<String, String>) data;
			String message = "FROM_" + msg.getKey() + "\n" + msg.getValue();
			Platform.runLater(()->{
				Label lbl = new Label(message);
				lbl.setFont(new Font("08서울한강체 M", 16));
				lbl.setTextAlignment(TextAlignment.LEFT);
				lbl.setStyle("-fx-text-fill: white;" + "-fx-background-radius: 5; " + "-fx-background-color: linear-gradient(to bottom, rgb(182,232,251), rgb(27,183,241));" + 
						"-fx-border-radius: 5; -fx-border-width: 5; " + "-fx-border-color: linear-gradient(to bottom, rgb(182,232,251), rgb(27,183,241));");
				
				rmgui.getMsgScrl().setVvalue(Double.MAX_VALUE);
				rmgui.getMsgDisplay().getChildren().add(lbl);
				rmgui.setScrollBarDown(false);
				rmgui.setScrlBarDown();
			});
		}
		else if(protocol.equals("Imoticon")){
			Pair<String, String> msg = (Pair<String, String>) data;
			Platform.runLater(()->{
				Label lbl = new Label("FROM_" + msg.getKey());
				lbl.setFont(new Font("08서울한강체 M", 16));
				lbl.setTextAlignment(TextAlignment.LEFT);
				lbl.setStyle("-fx-text-fill: white;" + "-fx-background-radius: 5; " + "-fx-background-color: linear-gradient(to bottom, rgb(182,232,251), rgb(27,183,241));" + 
						"-fx-border-radius: 5; -fx-border-width: 5; " + "-fx-border-color: linear-gradient(to bottom, rgb(182,232,251), rgb(27,183,241));");
				String _image = RoomGUI.class.getResource("imoticon/"+msg.getValue()+".png").toExternalForm();
				Image image = new Image(_image);
				ImageView iv = new ImageView();
				iv.setImage(image);
				iv.setFitHeight(100);
				iv.setPreserveRatio(true);
				
				rmgui.getMsgScrl().setVvalue(Double.MAX_VALUE);
				rmgui.getMsgDisplay().getChildren().add(lbl);
				rmgui.getMsgDisplay().getChildren().add(iv);
				rmgui.setScrollBarDown(false);
				rmgui.setScrlBarDown();
			});
		}
		else if(protocol.equals("EnterRoom")){
			
		}
		else if(protocol.equals("ExitRoom")){
			
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
		@FXML private TextField idInput, ipInput, portInput;
		@FXML Button btnLogin;
		
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
			this.setScene(scene);
			this.setResizable(false);
			this.setOnCloseRequest(event->stopClient());
			
		}
		
		@Override
		public void initialize(URL location, ResourceBundle resources) {}
		
		public void handlebntLogin(ActionEvent event) {
			System.out.println("handlebntLogin()");
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
		Label rmlst_lbl, usrlst_lbl; 
		private TableView<RoomItem> rmlst_tv; 
		TableColumn<RoomItem, String> rmType_col; 
		TableColumn<RoomItem, String> rmName_col; 
		TableColumn<RoomItem, Integer> usrNum_col;
		private ListView<String> usrlst_lv;
		Button btnEnterRm, btnMakeRm, btnLogout;
		
		private ObservableList<String> usrlst;
		private ObservableList<RoomItem> rmlst;		
		private String selectedRoomName = null;

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
			rmlst_tv.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RoomItem>() {
				@Override
				public void changed(ObservableValue<? extends RoomItem> observable, RoomItem oldValue, RoomItem newValue) {
					if(newValue!=null){
						selectedRoomName = newValue.getRoomName();
					}
				}
			});
			rmlst_tv.setEditable(false);
			root.getChildren().add(rmlst_tv);
			
			rmType_col = new TableColumn("Room Type"); rmType_col.setId("rmType");
			rmType_col.setPrefWidth(150.0); rmType_col.setResizable(false); rmType_col.setEditable(false);
			rmType_col.setCellValueFactory(new PropertyValueFactory<RoomItem,String>("roomType"));
			rmName_col = new TableColumn("Room Name"); rmName_col.setId("rmName");
			rmName_col.setPrefWidth(250.0); rmName_col.setResizable(false); rmName_col.setEditable(false);
			rmName_col.setCellValueFactory(new PropertyValueFactory<RoomItem,String>("roomName"));
			usrNum_col = new TableColumn("User Num"); usrNum_col.setId("usrNum_col");
			usrNum_col.setPrefWidth(50.0); usrNum_col.setResizable(false); usrNum_col.setEditable(false);
			usrNum_col.setCellValueFactory(new PropertyValueFactory<RoomItem,Integer>("userNum"));
			rmlst_tv.getColumns().addAll(rmType_col, rmName_col, usrNum_col);
			rmlst = FXCollections.observableArrayList();
			
			usrlst_lbl = new Label("User List"); usrlst_lbl.setId("usrlst_lbl");
			usrlst_lbl.setLayoutX(739.0); usrlst_lbl.setLayoutY(14.0); usrlst_lbl.setPrefSize(200.0, 50.0);
			root.setRightAnchor(usrlst_lbl, 30.0); root.setTopAnchor(usrlst_lbl, 10.0);
			root.getChildren().add(usrlst_lbl);
			
			usrlst_lv = new ListView<String>(); usrlst_lv.setId("usrlst_lv");
			usrlst_lv.setLayoutX(435.0); usrlst_lv.setLayoutY(50.0); usrlst_lv.setPrefSize(200.0, 420.0);
			root.setRightAnchor(usrlst_lv, 30.0); root.setTopAnchor(usrlst_lv, 70.0);
			root.getChildren().add(usrlst_lv);
			usrlst = FXCollections.observableArrayList();
			
			btnEnterRm = new Button("Enter Room"); btnEnterRm.setId("btnEnterRm");
			btnEnterRm.setLayoutX(30.0); btnEnterRm.setLayoutY(474.0); btnEnterRm.setPrefSize(200.0, 50.0);
			root.setBottomAnchor(btnEnterRm, 30.0); root.setLeftAnchor(btnEnterRm, 30.0);
			btnEnterRm.setOnAction(event->handlebtnEnterRm(event));
			root.getChildren().add(btnEnterRm);
			
			btnMakeRm = new Button("Make Room"); btnMakeRm.setId("btnMakeRm");
			btnMakeRm.setLayoutX(239.0); btnMakeRm.setLayoutY(474.0); btnMakeRm.setPrefSize(200.0, 50.0);
			btnMakeRm.setOnAction(event->handlebtnMakeRm(event));
			root.setBottomAnchor(btnMakeRm, 30.0); root.setLeftAnchor(btnMakeRm, 250.0);
			root.getChildren().add(btnMakeRm);
			
			btnLogout = new Button("Log Out"); btnLogout.setId("btnLogout");
			btnLogout.setLayoutX(570.0); btnLogout.setLayoutY(474.0); btnLogout.setPrefSize(200.0, 50.0);
			btnLogout.setOnAction(event->handlebtnLogout(event));
			root.setBottomAnchor(btnLogout, 30.0); root.setRightAnchor(btnLogout, 30.0);
			root.getChildren().add(btnLogout);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("LobbyGUI.css").toString());
			this.setScene(scene);
			this.setResizable(false);
			this.setOnCloseRequest(event->send("LogOut",userID));
		}

		// Event
		void handlebtnEnterRm(ActionEvent event) {
			System.out.println("handlebtnEnterRm");
			if(selectedRoomName!=null){
				System.out.println(selectedRoomName);
				send("EnterRoom",selectedRoomName);
			}
		}
		
		// dialog //
		Label txt_lbl;
		CheckBox check;
		private TextField nameInput;
		private TextField pwInput;
		private Button btnMake;
		private Stage dialog;
		void handlebtnMakeRm(ActionEvent event) {
			System.out.println("handlebtnMakeRm");	
			try {
				dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(this);
				dialog.setTitle("Make Room");
				
				Parent parent = FXMLLoader.load(getClass().getResource("custom_dialog.fxml"));
				txt_lbl = (Label) parent.lookup("#txt_lbl");
				txt_lbl.setText("You use all Captiabl and Small letter\n and Number when you input PASSWORD");
				check = (CheckBox) parent.lookup("#check");
				check.setOnAction(event1->handlecheck(event1));
				nameInput = (TextField) parent.lookup("#nameInput");
				pwInput = (TextField) parent.lookup("#pwInput");
				pwInput.setDisable(true);
				btnMake = (Button) parent.lookup("#btnMake");
				btnMake.setOnAction(event2->handlebtnMake(event2));
				Scene scene = new Scene(parent);
				dialog.setScene(scene);
				dialog.setResizable(false);
				dialog.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String protocol="MakePublic", name, pw;
		void handlecheck(ActionEvent event) {
			System.out.println("_handlecheck");
			if(check.isSelected()) {
				protocol = "MakePrivate";
				pwInput.setDisable(false);
			}
			else {
				protocol = "MakePublic";
				pwInput.setDisable(true);
			}
		}	
		void handlebtnMake(ActionEvent event4) {
			System.out.println("_handlebtnMake");
			name = nameInput.getText();
			pw = pwInput.getText();
			if(protocol.equals("MakePrivate")){
				if(new PrivateRoom().checkRoomName(name) && new PrivateRoom().checkSettingPW(pw))
					send(protocol,new Pair<String, String>(name, pw));
				else {
					nameInput.clear();
					pwInput.clear();
				}
			}
			else if(protocol.equals("MakePublic")){
				if(new PublicRoom().checkRoomName(name))
					send(protocol,name);
				else nameInput.clear();
			}
		}
		
		void handlebtnLogout(ActionEvent event) {
			System.out.println("handlebtnLogout");
			send("LogOut",userID);
		}

		@Override
		public void initialize(URL location, ResourceBundle resources) {}
		
		// Getter
		public ListView<String> getUsrlst_lv() {
			return usrlst_lv;
		}
		public ObservableList<String> getUsrlst() {
			return usrlst;
		}
		public Stage getDialog() {
			return dialog;
		}
		public TextField getNameInput() {
			return nameInput;
		}
		public ObservableList<RoomItem> getRmlst() {
			return rmlst;
		}
		public TableView<RoomItem> getRmlst_tv() {
			return rmlst_tv;
		}
		
	} // class LobbyGUI END
	
	/* RoomGUI */
	class RoomGUI extends Stage implements Initializable{
		AnchorPane root;
		private ScrollPane msgScrl; private VBox msgDisplay;
		TextArea msgInput; Button btnSndMsg;
		TilePane imtcInput; Button btnSndImtc;
		Label info_lbl; TextField info;
		Label usrlst_lbl; ListView usrlst_view;
		Button btnSndFile;
		Button btnExitRm;
		
		static final int imtcNum = 82;
		Button[] btn = new Button[imtcNum];
		String selectedImtc;
		String imtcName;
	
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
				String name = String.valueOf(i+1);
				String image = RoomGUI.class.getResource("imoticon/"+name+".png").toExternalForm();
				btn[i] = new Button();
				btn[i].setPrefSize(40.0, 40.0);
				btn[i].setStyle("-fx-background-image: url(" + image + ");" + "-fx-background-position: center center;" + "-fx-background-size: 40 40;");
				btn[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e){
						selectedImtc = image;
						imtcName = name;
					}
				});
				imtcInput.getChildren().add(btn[i]);
			}
			sp.setContent(imtcInput);
			
			btnSndImtc = new Button("Send"); btnSndImtc.setId("btnSndImtc");
			btnSndImtc.setPrefSize(60.0, 30.0);
			btnSndImtc.setOnAction(event->handleSndImtcBtnAction(event));
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
			this.setScene(scene);
			this.setResizable(false);
			this.setOnCloseRequest(event->send("LogOut",userID));
		}
	
		@Override
		public void initialize(URL location, ResourceBundle resources) {}

		// Event //
		private boolean scrollBarDown = true;
		public void setScrlBarDown(){
			msgScrl.vvalueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if(!scrollBarDown){
						msgScrl.setVvalue(msgScrl.getVmax());
						scrollBarDown = true;
					}
				}
				
			});
		}
		private void handleBtnSndMsg(ActionEvent event){
			String msg = msgInput.getText();
			if(!msg.isEmpty()){
				Label lbl = new Label(msg);
				lbl.setFont(new Font("08서울한강체 M", 16));
				lbl.setTextAlignment(TextAlignment.LEFT);
				lbl.setStyle("-fx-text-fill: white;" + "-fx-background-radius: 5; " + "-fx-background-color: linear-gradient(to bottom, rgb(146,200,230), rgb(12,112,169));" + 
						"-fx-border-radius: 5; -fx-border-width: 5; " + "-fx-border-color: linear-gradient(to bottom, rgb(146,200,230), rgb(12,112,169));");

				msgScrl.setVvalue(Double.MAX_VALUE);
				msgDisplay.getChildren().add(lbl);
				msgInput.clear();
				scrollBarDown = false;
				setScrlBarDown();

				send("Message", msg);
			}			
		}
		private void handleSndImtcBtnAction(ActionEvent e){ // when send imoticon
			if(selectedImtc!=null){
				Image image = new Image(selectedImtc);
				ImageView iv = new ImageView();
				iv.setImage(image);
				iv.setFitHeight(100);
				iv.setPreserveRatio(true);
				
				Platform.runLater(()->{
					msgScrl.setVvalue(Double.MAX_VALUE);
					msgDisplay.getChildren().add(iv);
					selectedImtc = null;
					scrollBarDown = false;
				});
				setScrlBarDown();
				
				send("Imoticon",imtcName);
			}
		}

	
		// Getter //
		public ScrollPane getMsgScrl() {
			return msgScrl;
		}
		public VBox getMsgDisplay() {
			return msgDisplay;
		}
		public void setScrollBarDown(boolean scrollBarDown) {
			this.scrollBarDown = scrollBarDown;
		}
		
		
	} // class RoomGUI END


} // class Client END


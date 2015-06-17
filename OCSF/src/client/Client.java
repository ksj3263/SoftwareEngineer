package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import utility.Data;
import utility.FileClient;
import utility.FileServer;
import utility.Room;
import utility.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class Client extends Application implements Initializable {

	private Socket socket;
	private String userID;
	private String ip;
	private int port;

	private LoginGUI lgngui = new LoginGUI();
	private LobbyGUI lbbgui = new LobbyGUI();
	private RoomGUI rmgui = new RoomGUI();

	JFileChooser jfc = new JFileChooser();
	StringTokenizer st;
	
	private void startClient() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("startClient()");
					socket = new Socket();
					socket.connect(new InetSocketAddress(ip, port));

					send("LogIn", userID);
					receive();
				} catch (Exception e) {
					System.out.println("startClient() ERROR");
					Platform.runLater(() -> {
						lgngui.getIdInput().clear();
						lgngui.getIpInput().clear();
						lgngui.getPortInput().clear();
					});
					if (!socket.isClosed()) {
						stopClient();
					}
				}
			}
		};
		thread.start();
	} // method startClient END

	private void stopClient() {
		try {
			System.out.println("stopClient()");
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			System.out.println("stopClient() ERROR");
		}
	} // method stopClient END

	private void receive() {
		while (true) {
			System.out.println("receive()");
			try {
				ObjectInputStream input = new ObjectInputStream(
						socket.getInputStream());
				Data data = (Data) input.readObject();
				System.out.println(data.toString());
				inMessage(data.getProtocol(), data.getData());
			} catch (Exception e) {
				System.out.println("receive() ERROR");
				stopClient();
				break;
			}
		}
	} // method receive END

	private void send(String protocol, Object data) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				System.out.println("send()");
				try {
					ObjectOutputStream output = new ObjectOutputStream(
							socket.getOutputStream());
					output.writeObject(new Data(protocol, data));
					output.flush();
				} catch (Exception e) {
					System.out.println("send() ERROR");
					stopClient();
				}
			}
		};
		thread.start();
	} // method send END

	private void inMessage(String protocol, Object data) {
		if (protocol.equals("LogIn")) {
			String lgn_result = data.toString();
			login(lgn_result);
		} else if (protocol.equals("LogOut")) {
			logout();
		} else if (protocol.equals("UserListInLobby")) {
			Vector<String> usrlst = (Vector<String>) data;
			updateUserListInLobby(usrlst);
		} else if (protocol.equals("UserListInRoom")) {
			Vector<String> usrlst = (Vector<String>) data;
			updateUserListInRoom(usrlst);
		} else if (protocol.equals("UpdatRoomlst")) {
			Vector<Pair<String, String>> roomlst = (Vector<Pair<String, String>>) data;
			updateRoomlst(roomlst);
		} else if (protocol.equals("MakeRoom")) {
			String makeRoom_result = data.toString();
			makeRoom(makeRoom_result);
		} else if (protocol.equals("public") || protocol.equals("private")) {
			String type = protocol;
			String name = data.toString();
			RoomItem item = new RoomItem(type, name);
			addRoomlst(item);
		} else if (protocol.equals("Message")) {
			Pair<String, String> msg = (Pair<String, String>) data;
			String from = msg.getKey();
			String message = msg.getValue();
			messageFromServer(from, message);
		} else if (protocol.equals("Imoticon")) {
			Pair<String, String> msg = (Pair<String, String>) data;
			String from = msg.getKey();
			String imoticon = msg.getValue();
			imoticonFromServer(from, imoticon);
		} else if (protocol.equals("Enterpublic")) {
			enterPublic();
		} else if (protocol.equals("Enterprivate")) {
			String pw_result = data.toString();
			enterPrivate(pw_result);
		} else if (protocol.equals("ExitRoom")) {
			exitRoom();
		} else if (protocol.equals("FileRequest")) {
			/*
			 * String localHostIpAddress = null; try { localHostIpAddress =
			 * InetAddress.getLocalHost().getHostAddress(); } catch
			 * (UnknownHostException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } send("FileAccept",localHostIpAddress);
			 */
			send("FileAccept", data+"/"+"172.0.0.1");
			FileServer ob = new FileServer();
			ob.serverStart();
			

		} else if (protocol.equals("FileAccept")) {
			JFrame window = new JFrame();
			int result = jfc.showOpenDialog(window);
			if (result == JFileChooser.APPROVE_OPTION) {
				FileClient temp = new FileClient("127.0.0.1", jfc
						.getSelectedFile().toString());
			}
		}

	} // method inMessage END

	/* Operation */
	public void login(String lgn_result) {
		if (lgn_result.equals("YES")) { // userID is not overlap
			Platform.runLater(() -> {
				lgngui.close();
				lbbgui.show();
			});
		} else if (lgn_result.equals("NO")) { // userID is overlap
			stopClient();
			Platform.runLater(() -> {
				lgngui.getIdInput().clear();
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText(null);
				alert.setContentText("Others are using the ID you input.\nPlease input other ID.");
				alert.showAndWait();
			});
		}
	}

	public void logout() {
		Platform.runLater(() -> {
			lbbgui.getUsrlst().clear();
			lbbgui.getRmlst().clear();
			lbbgui.close();
			lgngui.show();
		});
		oldRoom = false;
		stopClient();
	}

	public void makeRoom(String makeRoom_result) {
		if (makeRoom_result.equals("YES")) { // room name is not overlap
			Platform.runLater(() -> {
				lbbgui.getMakeDialog().close();
				lbbgui.close();
				rmgui.show();
				rmgui.getUsrlst().add(userID);
				rmgui.getUsrlst_lv().setItems(rmgui.getUsrlst());
			});
		} else if (makeRoom_result.equals("NO")) { // room name is overlap
			Platform.runLater(() -> {
				lbbgui.getNameInput().clear();
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText(null);
				alert.setContentText("Others are using the ROOM NAME you input.\nPlease input other ROOM NAME.");
				alert.showAndWait();
			});
		}
	}

	public void enterPublic() {
		Platform.runLater(() -> {
			lbbgui.close();
			rmgui.show();
		});
	}

	public void enterPrivate(String pw_result) {
		if (pw_result.equals("YES")) {
			Platform.runLater(() -> {
				lbbgui.getPwDialog().close();
				lbbgui.close();
				rmgui.show();
			});
		} else if (pw_result.equals("NO")) {
			Platform.runLater(() -> {
				lbbgui.getPw_pwInput().clear();
			});
		}
	}

	public void exitRoom() {
		Platform.runLater(() -> {
			rmgui.getMsgDisplay().getChildren().clear();
			rmgui.close();
			lbbgui.show();
		});
	}

	private boolean oldRoom = false;

	public void updateUserListInLobby(Vector<String> usrlst) {
		Platform.runLater(() -> {
			lbbgui.getUsrlst().clear();
			lbbgui.getUsrlst().addAll(usrlst);
			lbbgui.getUsrlst_lv().setItems(lbbgui.getUsrlst());
		});
		if (!oldRoom) {
			send("UpdatRoomlst", null);
			oldRoom = true;
		}
	}

	public void updateUserListInRoom(Vector<String> usrlst) {
		Platform.runLater(() -> {
			rmgui.getUsrlst().clear();
			rmgui.getUsrlst().addAll(usrlst);
			rmgui.getUsrlst_lv().setItems(rmgui.getUsrlst());
		});

	}

	public void updateRoomlst(Vector<Pair<String, String>> roomlst) {
		Platform.runLater(() -> {
			lbbgui.getRmlst().clear();
			for (int i = 0; i < roomlst.size(); i++) {
				Pair<String, String> room = roomlst.get(i);
				RoomItem item = new RoomItem(room.getKey(), room.getValue());
				lbbgui.getRmlst().add(item);
			}
			lbbgui.getRmlst_tv().setItems(lbbgui.getRmlst());
		});
	}

	public void addRoomlst(RoomItem item) {
		Platform.runLater(() -> {
			lbbgui.getRmlst().add(item);
			lbbgui.getRmlst_tv().setItems(null);
			lbbgui.getRmlst_tv().setItems(lbbgui.getRmlst());
		});
	}

	public void messageFromServer(String from, String message) {
		Platform.runLater(() -> {
			Label from_lbl = rmgui.setFrom_lbl(from);
			Label message_lbl = rmgui.setFrom_lbl(message);

			HBox msgBox = new HBox();
			msgBox.setSpacing(5.0);
			msgBox.getChildren().add(from_lbl);
			msgBox.getChildren().add(message_lbl);

			rmgui.getMsgScrl().setVvalue(Double.MAX_VALUE);
			rmgui.getMsgDisplay().getChildren().add(msgBox);
			rmgui.setScrollBarDown(false);
			rmgui.setScrlBarDown();
		});
	}

	public void imoticonFromServer(String from, String imoticon) {
		Platform.runLater(() -> {
			Label from_lbl = rmgui.setFrom_lbl(from);

			String imageURL = RoomGUI.class.getResource(
					"imoticon/" + imoticon + ".png").toExternalForm();
			Image image = new Image(imageURL);
			ImageView imoticon_iv = new ImageView();
			imoticon_iv.setImage(image);
			imoticon_iv.setFitHeight(70);
			imoticon_iv.setPreserveRatio(true);

			HBox imoticonBox = new HBox();
			imoticonBox.setSpacing(5.0);
			imoticonBox.getChildren().add(from_lbl);
			imoticonBox.getChildren().add(imoticon_iv);

			rmgui.getMsgScrl().setVvalue(Double.MAX_VALUE);
			rmgui.getMsgDisplay().getChildren().add(imoticonBox);
			rmgui.setScrollBarDown(false);
			rmgui.setScrlBarDown();
		});
	}

	/* GUI */
	/* GUI */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		lgngui.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	/* LoginGUI */

	/* LoginGUI */
	private class LoginGUI extends Stage implements Initializable {

		private ImageView k2nm_logo;
		private TextField idInput, ipInput, portInput;
		private Button btnLogin;

		public LoginGUI() {
			try {
				Parent parent = FXMLLoader.load(getClass().getResource(
						"LoginGUI.fxml"));

				k2nm_logo = (ImageView) parent.lookup("#k2nm_logo");
				String url = LoginGUI.class.getResource("style/logo.jpg")
						.toExternalForm();
				k2nm_logo.setImage(new Image(url));

				idInput = (TextField) parent.lookup("#idInput");
				ipInput = (TextField) parent.lookup("#ipInput");
				portInput = (TextField) parent.lookup("#portInput");

				btnLogin = (Button) parent.lookup("#btnLogin");
				btnLogin.setOnAction(event1 -> handlebtnLogin(event1));

				Scene scene = new Scene(parent);
				scene.getStylesheets().add(
						getClass().getResource("LoginGUI.css").toString());
				this.setScene(scene);
				this.setResizable(false);
				this.setOnCloseRequest(event -> stopClient());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void initialize(URL location, ResourceBundle resources) {
		}

		// Event //
		public void handlebtnLogin(ActionEvent event) {
			System.out.println("handlebntLogin()");
			try {
				userID = idInput.getText();
				ip = ipInput.getText();
				port = Integer.valueOf(portInput.getText());

				if (!new User().checkID(userID))
					Integer.valueOf("fail");
				if (port < 1111 || 9999 < port)
					Integer.valueOf("fail");

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

	} // class LoginGUI END

	/* LobbyGUI */

	/* LobbyGUI */
	private class LobbyGUI extends Stage implements Initializable {

		private TableView<RoomItem> rmlst_tv;
		private TableColumn<RoomItem, String> rmType_col;
		private TableColumn<RoomItem, String> rmName_col;
		private ListView<String> usrlst_lv;
		private Button btnEnterRm, btnMakeRm, btnLogout;

		private ObservableList<String> usrlst;
		private ObservableList<RoomItem> rmlst;
		private String selectedRoomName = null;
		private String selectedRoomType = null;

		public LobbyGUI() {
			try {
				Parent parent = FXMLLoader.load(getClass().getResource(
						"LobbyGUI.fxml"));

				rmType_col = new TableColumn<RoomItem, String>("Room Type");
				rmType_col.setId("rmType_col");
				rmType_col.setPrefWidth(150.0);
				rmType_col.setResizable(false);
				rmType_col.setEditable(false);
				rmType_col.setSortable(false);
				rmType_col
						.setCellValueFactory(new PropertyValueFactory<RoomItem, String>(
								"roomType"));

				rmName_col = new TableColumn<RoomItem, String>("Room Name");
				rmName_col.setId("rmName_col");
				rmName_col.setPrefWidth(250.0);
				rmName_col.setResizable(false);
				rmName_col.setEditable(false);
				rmName_col.setSortable(false);
				rmName_col
						.setCellValueFactory(new PropertyValueFactory<RoomItem, String>(
								"roomName"));

				rmlst_tv = (TableView<RoomItem>) parent.lookup("#rmlst_tv");
				rmlst_tv.getColumns().addAll(rmType_col, rmName_col);
				rmlst_tv.getSelectionModel().selectedItemProperty()
						.addListener(new ChangeListener<RoomItem>() {
							@Override
							public void changed(
									ObservableValue<? extends RoomItem> observable,
									RoomItem oldValue, RoomItem newValue) {
								if (newValue != null) {
									selectedRoomName = newValue.getRoomName();
									selectedRoomType = newValue.getRoomType();
								}
							}
						});
				rmlst = FXCollections.observableArrayList();

				usrlst_lv = (ListView<String>) parent.lookup("#usrlst_lv");
				usrlst = FXCollections.observableArrayList();

				btnEnterRm = (Button) parent.lookup("#btnEnterRm");
				btnEnterRm.setOnAction(event -> handlebtnEnterRm(event));

				btnMakeRm = (Button) parent.lookup("#btnMakeRm");
				btnMakeRm.setOnAction(event -> handlebtnMakeRm(event));

				btnLogout = (Button) parent.lookup("#btnLogout");
				btnLogout.setOnAction(event -> handlebtnLogout(event));

				Scene scene = new Scene(parent);
				scene.getStylesheets().add(
						getClass().getResource("LobbyGUI.css").toString());
				this.setScene(scene);
				this.setResizable(false);
				this.setOnCloseRequest(event -> send("LogOut", userID));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// makeDialog //
		private Label txt_lbl;
		private CheckBox check;
		private TextField nameInput;
		private TextField make_pwInput;
		private Button btnMake;
		private Stage makeDialog;
		private String protocol = "MakePublic", name, make_pw;

		// pwDialog //
		private PasswordField pw_pwInput;
		private Button btnOK;
		private Stage pwDialog;
		private String pw_pw;

		// Event //
		private void handlebtnEnterRm(ActionEvent event) {
			System.out.println("handlebtnEnterRm");
			if (selectedRoomName != null) {
				if (selectedRoomType.equals("public")) {
					send("Enterpublic", selectedRoomName);
				} else if (selectedRoomType.equals("private")) {
					try {
						pwDialog = new Stage(StageStyle.UTILITY);
						pwDialog.initModality(Modality.WINDOW_MODAL);
						pwDialog.initOwner(this);
						pwDialog.setTitle("Input Room PW");
						Parent parent = FXMLLoader.load(getClass().getResource(
								"pwDialog.fxml"));

						pw_pwInput = (PasswordField) parent.lookup("#pwInput");

						btnOK = (Button) parent.lookup("#btnOK");
						btnOK.setOnAction(event1 -> handlebtnOK(event1));

						Scene scene = new Scene(parent);
						pwDialog.setScene(scene);
						pwDialog.setResizable(false);
						pwDialog.show();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void handlebtnOK(ActionEvent event) {
			pw_pw = pw_pwInput.getText();
			Pair<String, String> data = new Pair<String, String>(
					selectedRoomName, pw_pw);
			send("Enterprivate", data);
		}

		private void handlebtnMakeRm(ActionEvent event) {
			System.out.println("handlebtnMakeRm");
			try {
				makeDialog = new Stage(StageStyle.UTILITY);
				makeDialog.initModality(Modality.WINDOW_MODAL);
				makeDialog.initOwner(this);
				makeDialog.setTitle("Make Room");
				Parent parent = FXMLLoader.load(getClass().getResource(
						"makeDialog.fxml"));

				txt_lbl = (Label) parent.lookup("#txt_lbl");
				txt_lbl.setText("You use all Captiabl and Small letter\n and Number when you input PASSWORD");

				check = (CheckBox) parent.lookup("#check");
				check.setOnAction(event1 -> handlecheck(event1));

				nameInput = (TextField) parent.lookup("#nameInput");

				make_pwInput = (TextField) parent.lookup("#pwInput");
				make_pwInput.setDisable(true);

				btnMake = (Button) parent.lookup("#btnMake");
				btnMake.setOnAction(event2 -> handlebtnMake(event2));

				Scene scene = new Scene(parent);
				makeDialog.setScene(scene);
				makeDialog.setResizable(false);
				makeDialog.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handlecheck(ActionEvent event) {
			System.out.println("_handlecheck");
			if (check.isSelected()) {
				protocol = "MakePrivate";
				make_pwInput.setDisable(false);
			} else {
				protocol = "MakePublic";
				make_pwInput.setDisable(true);
			}
		}

		private void handlebtnMake(ActionEvent event) {
			System.out.println("_handlebtnMake");
			name = nameInput.getText();
			make_pw = make_pwInput.getText();
			if (protocol.equals("MakePrivate")) {
				if (new Room().checkRoomName(name)
						&& new Room().checkSettingPW(make_pw))
					send(protocol, new Pair<String, String>(name, make_pw));
				else {
					nameInput.clear();
					make_pwInput.clear();
				}
			} else if (protocol.equals("MakePublic")) {
				if (new Room().checkRoomName(name))
					send(protocol, name);
				else
					nameInput.clear();
			}
		}

		private void handlebtnLogout(ActionEvent event) {
			System.out.println("handlebtnLogout");
			send("LogOut", userID);
		}

		@Override
		public void initialize(URL location, ResourceBundle resources) {
		}

		// Getter //
		public ListView<String> getUsrlst_lv() {
			return usrlst_lv;
		}

		public ObservableList<String> getUsrlst() {
			return usrlst;
		}

		public Stage getMakeDialog() {
			return makeDialog;
		}

		public TextField getNameInput() {
			return nameInput;
		}

		public Stage getPwDialog() {
			return pwDialog;
		}

		public TextField getPw_pwInput() {
			return pw_pwInput;
		}

		public ObservableList<RoomItem> getRmlst() {
			return rmlst;
		}

		public TableView<RoomItem> getRmlst_tv() {
			return rmlst_tv;
		}

	} // class LobbyGUI END

	/* RoomGUI */
	private class RoomGUI extends Stage implements Initializable {

		private ScrollPane msgScrl;
		private VBox msgDisplay;
		private TextArea msgInput;
		private Button btnSndMsg;
		private ScrollPane imtcScrl;
		private TilePane imtcInput;
		private Button btnSndImtc;
		private ListView<String> usrlst_lv;
		private Button btnSndFile;
		private Button btnExitRm;

		private ObservableList<String> usrlst;

		private static final int imtcNum = 82;
		private Button[] btn = new Button[imtcNum];
		private String selectedImtc = null;
		private String selectedImtcName = null;

		public RoomGUI() {
			try {
				Parent parent = FXMLLoader.load(getClass().getResource(
						"RoomGUI.fxml"));

				msgScrl = (ScrollPane) parent.lookup("#msgScrl");
				msgDisplay = new VBox();
				msgDisplay.setId("msgDisplay");
				msgDisplay.setPrefWidth(500.0);
				msgDisplay.setSpacing(10.0);
				msgScrl.setContent(msgDisplay);

				msgInput = (TextArea) parent.lookup("#msgInput");
				msgInput.setId("msgInput");
				btnSndMsg = (Button) parent.lookup("#btnSndMsg");
				btnSndMsg.setOnAction(event -> handlebtnSndMsg(event));

				imtcScrl = (ScrollPane) parent.lookup("#imtcScrl");
				imtcInput = new TilePane();
				imtcInput.setId("imtcInput");
				imtcInput.setPrefSize(465.0, 200.0);
				imtcInput.setHgap(5.0);
				imtcInput.setVgap(5.0);
				for (int i = 0; i < imtcNum; i++) {
					String imtcName = String.valueOf(i + 1);
					String imtc = RoomGUI.class.getResource(
							"imoticon/" + imtcName + ".png").toExternalForm();
					btn[i] = new Button();
					btn[i].setPrefSize(40.0, 40.0);
					btn[i].setStyle("-fx-background-image: url(" + imtc + ");"
							+ "-fx-background-position: center center;"
							+ "-fx-background-size: 40 40;");
					btn[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							selectedImtc = imtc;
							selectedImtcName = imtcName;
						}
					});
					imtcInput.getChildren().add(btn[i]);
				}
				imtcScrl.setContent(imtcInput);

				btnSndImtc = (Button) parent.lookup("#btnSndImtc");
				btnSndImtc.setOnAction(event -> handlebtnSndImtc(event));

				usrlst_lv = (ListView<String>) parent.lookup("#usrlst_lv");
				usrlst = FXCollections.observableArrayList();

				btnSndFile = (Button) parent.lookup("#btnSndFile");
				btnSndFile.setOnAction(event -> handlebtnSndFile(event));

				btnExitRm = (Button) parent.lookup("#btnExitRm");
				btnExitRm.setOnAction(event -> handlebtnExitRm(event));

				Scene scene = new Scene(parent);
				scene.getStylesheets().add(
						getClass().getResource("RoomGUI.css").toString());
				this.setScene(scene);
				this.setResizable(false);
				this.setOnCloseRequest(event -> send("LogOut", userID));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void initialize(URL location, ResourceBundle resources) {
		}

		// Event //
		private boolean scrollBarDown = true;

		public void setScrlBarDown() {
			msgScrl.vvalueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(
						ObservableValue<? extends Number> observable,
						Number oldValue, Number newValue) {
					if (!scrollBarDown) {
						msgScrl.setVvalue(msgScrl.getVmax());
						scrollBarDown = true;
					}
				}
			});
		}

		private void handlebtnSndMsg(ActionEvent event) {
			String msg = msgInput.getText();
			if (!msg.isEmpty()) {
				Label lbl = new Label(msg);
				lbl.setTextAlignment(TextAlignment.LEFT);
				lbl.setStyle("-fx-font-size: 16; -fx-text-fill: white;"
						+ "-fx-background-radius: 5; -fx-background-color: linear-gradient(to bottom, rgb(146,200,230), rgb(12,112,169));"
						+ "-fx-border-radius: 5; -fx-border-width: 5; -fx-border-color: linear-gradient(to bottom, rgb(146,200,230), rgb(12,112,169));");

				msgScrl.setVvalue(Double.MAX_VALUE);
				msgDisplay.getChildren().add(lbl);
				msgInput.clear();
				scrollBarDown = false;
				setScrlBarDown();

				send("Message", msg);
			}
		}

		private void handlebtnSndImtc(ActionEvent e) { // when send imoticon
			if (selectedImtc != null) {
				Image image = new Image(selectedImtc);
				ImageView iv = new ImageView();
				iv.setImage(image);
				iv.setFitHeight(70);
				iv.setPreserveRatio(true);

				msgScrl.setVvalue(Double.MAX_VALUE);
				msgDisplay.getChildren().add(iv);
				selectedImtc = null;
				scrollBarDown = false;
				setScrlBarDown();

				send("Imoticon", selectedImtcName);
			}
		}

		private void handlebtnSndFile(ActionEvent event) {
			System.out.println("FileSnd");

			send("FileRequest", usrlst_lv.getSelectionModel().getSelectedItem());
		}

		private void handlebtnExitRm(ActionEvent event) {
			System.out.println("hanldebtnExitRm()");
			send("ExitRoom", userID);
		}

		// Getter and Setter //
		public ScrollPane getMsgScrl() {
			return msgScrl;
		}

		public VBox getMsgDisplay() {
			return msgDisplay;
		}

		public ListView<String> getUsrlst_lv() {
			return usrlst_lv;
		}

		public void setScrollBarDown(boolean scrollBarDown) {
			this.scrollBarDown = scrollBarDown;
		}

		public ObservableList<String> getUsrlst() {
			return usrlst;
		}

		public Label setFrom_lbl(String message) {
			Label from_lbl = new Label();
			from_lbl.setTextAlignment(TextAlignment.LEFT);
			from_lbl.setStyle("-fx-font-size: 16; -fx-text-fill: white;"
					+ "-fx-background-radius: 5; -fx-background-color: linear-gradient(to bottom, rgb(182,232,251), rgb(27,183,241));"
					+ "-fx-border-radius: 5; -fx-border-width: 5; -fx-border-color: linear-gradient(to bottom, rgb(182,232,251), rgb(27,183,241));");
			from_lbl.setText(message);
			return from_lbl;
		}

	} // class RoomGUI END

} // class Client END


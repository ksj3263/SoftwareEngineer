package old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

public class Server extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("start");
	private JButton stop_btn = new JButton("stop");

	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc = new Vector();
	private Vector room_vc = new Vector();

	private StringTokenizer st;

	Server() {
		init(); //
		start(); // ActionListener
	}

	private void start() {
		start_btn.addActionListener(this);
		stop_btn.addActionListener(this);
	}

	public void init() // login gui
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 260, 200);
		contentPane.add(scrollPane);

		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);

		JLabel label = new JLabel("port");
		label.setBounds(12, 220, 57, 15);
		contentPane.add(label);

		port_tf = new JTextField();
		port_tf.setBounds(81, 220, 191, 21);
		contentPane.add(port_tf);
		port_tf.setColumns(10);

		start_btn.setBounds(12, 251, 120, 23);
		contentPane.add(start_btn);

		stop_btn.setBounds(152, 251, 120, 23);
		contentPane.add(stop_btn);

		stop_btn.setEnabled(false);
		
		this.setVisible(true);
	}

	private void Server_start() {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "already open port", "info",
					JOptionPane.ERROR_MESSAGE);
		}

		if (server_socket != null) {
			Connection();
		}
	}

	private void Connection() {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						textArea.append("waiting user connection\n");
						socket = server_socket.accept();
						textArea.append("connection success!!!!\n");

						UserInfo user = new UserInfo(socket);
						user.start();

					} catch (IOException e) {
						e.printStackTrace();
						//break;
					}
				}
			}
		});

		th.start();
	}

	public static void main(String[] args) {

		new Server();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == start_btn) {
			System.out.println("start btn click");
			port = Integer.parseInt(port_tf.getText().trim());
			Server_start();
			
			start_btn.setEnabled(false);
			port_tf.setEditable(false);
			stop_btn.setEnabled(true);
		} else if (e.getSource() == stop_btn) {
			stop_btn.setEnabled(false);
			start_btn.setEnabled(true);
			port_tf.setEditable(true);
			
			try {
				server_socket.close();
				user_vc.removeAllElements();
				room_vc.removeAllElements();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("stop btn click");
		}
	}

	class UserInfo extends Thread {
		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;

		private Socket user_socket;
		private String Nickname = "";

		private boolean RoomCh = true;

		UserInfo(Socket soc) {
			this.user_socket = soc;
			UserNetwork();
		}

		private void UserNetwork() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);

				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

				Nickname = dis.readUTF();
				textArea.append(Nickname + " : user login\n");

				BroadCast("NewUser/" + Nickname); 

				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);

					Send_message("OldUser/" + u.Nickname);
				}
				
				// send me original room list 
				for(int i=0; i<room_vc.size(); i++){
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					
					Send_message("OldRoom/"+r.Room_name);
				}
				
				Send_message("room_list_update/ ");
				
				user_vc.add(this); 
				
				BroadCast("user_list_update/ ");
				
			} catch (IOException e) {

				JOptionPane.showMessageDialog(null, "stream setting error", "info",
						JOptionPane.ERROR_MESSAGE);
			}

		}

		public void run() // do in thread
		{
			while (true) {
				try {
					String msg = dis.readUTF();
					textArea.append("message from " + Nickname + " : " + msg
							+ "\n");
					InMessage(msg);
				} catch (IOException e) {
					textArea.append(Nickname+": user connection disconnect\n");
					try{
					dos.close();
					dis.close();
					user_socket.close();
					user_vc.remove(this);
					BroadCast("User_out/"+Nickname);
					BroadCast("user_list_update/");
					}
					catch(IOException e1){};
					break;
				}

			}
		} // run() END

		private void InMessage(String str) {
			st = new StringTokenizer(str, "/");

			String protocol = st.nextToken();
			String message = st.nextToken(); 

			System.out.println("Protocol : " + protocol);
			System.out.println("Message : " + message);

			if (protocol.equals("Note")) {
														
				String note = st.nextToken();
				
				System.out.println("receiver : "+message);
				System.out.println("message : "+note);
				
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);
					if (u.Nickname.equals(message)) {
						u.Send_message("Note/" + Nickname + "/" + note);
					}
				}
				System.out.println(note);
			} else if (protocol.equals("CreateRoom")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					if (r.Room_name.equals(message)) 
					{
						Send_message("CreateRoomFail/ok");
						RoomCh = false;
						break;
					}

				}
				if (RoomCh) {
					RoomInfo new_room = new RoomInfo(message, this);
					room_vc.add(new_room);
					Send_message("CreateRoom/" + message);

					BroadCast("New_Room/" + message);
				}
				RoomCh = true;
			} else if (protocol.equals("Chatting")) {
				String msg = st.nextToken(); 

				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					if (r.Room_name.equals(message)) {
						r.BroadCast_Room("Chatting/" + Nickname + "/" + msg);
					}
				}
			} else if(protocol.equals("JoinRoom")){
				for(int i=0; i<room_vc.size(); i++){
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if(r.Room_name.equals(message)){
						// new user alarm
						r.BroadCast_Room("Chatting/info/**** "+Nickname+" enter****");
						
						// user add
						r.Add_User(this);
						Send_message("JoinRoom/"+message);
					}
				}
			}
		}

		private void BroadCast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo) user_vc.elementAt(i);

				u.Send_message(str);
			}

		}

		private void Send_message(String str) // send message
		{
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class RoomInfo {
		private String Room_name;
		private Vector Room_user_vc = new Vector();

		RoomInfo(String str, UserInfo u) {
			this.Room_name = str;
			this.Room_user_vc.add(u);
		}

		public void BroadCast_Room(String str) {
			for (int i = 0; i < Room_user_vc.size(); i++) {
				UserInfo u = (UserInfo) Room_user_vc.elementAt(i);

				u.Send_message(str);
			}
		}
		
		private void Add_User(UserInfo u){
			this.Room_user_vc.add(u);
		}
	}

}
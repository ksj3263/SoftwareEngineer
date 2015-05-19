package old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame implements ActionListener, KeyListener {

	// Login GUI
	private JFrame Login_GUI = new JFrame();
	private JPanel LoginPane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField id_tf;
	private JButton login_btn = new JButton("Login");

	// Main GUI
	private JPanel contentPane;
	private JTextField message_tf;
	private JButton notesend_btn = new JButton("send message");
	private JButton joinroom_btn = new JButton("join room");
	private JButton createroom_btn = new JButton("create room");
	private JButton send_btn = new JButton("send");
	private JList User_list = new JList();
	private JList Room_list = new JList();
	private JTextArea Chat_area = new JTextArea();

	// login data
	private Socket socket;
	private String ip;
	private int port;
	private String id = "";
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	// 벡터
	Vector user_list = new Vector();
	Vector room_list = new Vector();
	StringTokenizer st;

	private String My_Room;

	Client() {
		Login_init(); // login screen
		Main_init(); // main screen
		start();
	}

	private void Main_init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 555);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("user list");
		lblNewLabel.setBounds(12, 10, 80, 15);
		contentPane.add(lblNewLabel);

		notesend_btn.setBounds(12, 205, 125, 23);
		contentPane.add(notesend_btn);

		Room_list.setBounds(12, 269, 125, 160);
		contentPane.add(Room_list);

		JLabel lblNewLabel_1 = new JLabel("room list");
		lblNewLabel_1.setBounds(12, 244, 80, 15);
		contentPane.add(lblNewLabel_1);

		User_list.setBounds(12, 35, 125, 160);
		contentPane.add(User_list);

		joinroom_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		joinroom_btn.setBounds(12, 439, 125, 23);
		contentPane.add(joinroom_btn);

		createroom_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		createroom_btn.setBounds(12, 472, 125, 23);
		contentPane.add(createroom_btn);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(180, 10, 492, 440);
		contentPane.add(scrollPane);

		scrollPane.setViewportView(Chat_area);
		
		Chat_area.setEditable(false);

		message_tf = new JTextField();
		message_tf.setBounds(180, 472, 375, 22);
		contentPane.add(message_tf);
		message_tf.setColumns(10);
		message_tf.setEnabled(false);

		send_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		send_btn.setBounds(575, 472, 97, 23);
		contentPane.add(send_btn);
		send_btn.setEnabled(false);
		
		
		this.setVisible(false);
	}

	private void inmessage(String str)
	{
		st = new StringTokenizer(str, "/");

		String protocol = st.nextToken();
		String Message = st.nextToken();

		System.out.println("Protocol :" + protocol);
		System.out.println("Message:" + Message);

		if (protocol.equals("NewUser")) {
			user_list.add(Message);
			User_list.setListData(user_list);

			User_list.updateUI();

		} else if (protocol.equals("OldUser")) {
			user_list.add(Message);
			User_list.setListData(user_list);
		} else if (protocol.equals("Note")) {
			st = new StringTokenizer(Message, "@");
			String user = st.nextToken();
			String note = st.nextToken();

			System.out.println(user + ":" + note);

			JOptionPane.showMessageDialog(null, note, user + " Note",
					JOptionPane.CLOSED_OPTION);
		} else if (protocol.equals("CreateRoom")) {
			My_Room = Message;
			message_tf.setEnabled(true);
			send_btn.setEnabled(true);
			joinroom_btn.setEnabled(false);
			createroom_btn.setEnabled(false);
		} else if (protocol.equals("CreateRoomFail")) {
			JOptionPane.showMessageDialog(null, "CreateRoomFail", "Fail",
					JOptionPane.ERROR_MESSAGE);
		} else if (protocol.equals("New_Room")) {
			room_list.add(Message);
			Room_list.setListData(room_list);
		} else if (protocol.equals("Chatting")) {
			String msg = st.nextToken();

			Chat_area.append(Message + ":" + msg + "\n");
		} else if(protocol.equals("OldRoom")){
			room_list.add(Message);
		} else if(protocol.equals("room_list_update")){
			Room_list.setListData(room_list);
			
		} else if(protocol.equals("JoinRoom")){
			My_Room = Message;
			message_tf.setEnabled(true);
			send_btn.setEnabled(true);
			joinroom_btn.setEnabled(false);
			createroom_btn.setEnabled(false);
			JOptionPane.showMessageDialog(null, "enter chatting room", "info",
					JOptionPane.INFORMATION_MESSAGE);
		} else if(protocol.equals("User_out")){
			user_list.remove(Message);
			
		}

	}

	private void start() {
		login_btn.addActionListener(this); //
		notesend_btn.addActionListener(this); //
		joinroom_btn.addActionListener(this); //
		createroom_btn.addActionListener(this); //
		send_btn.addActionListener(this); //
		message_tf.addKeyListener(this);
	}

	private void Login_init() {
		Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Login_GUI.setBounds(100, 100, 300, 350);
		LoginPane = new JPanel();
		LoginPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		Login_GUI.setContentPane(LoginPane);
		LoginPane.setLayout(null);

		JLabel lblServerIp = new JLabel("Server IP");
		lblServerIp.setBounds(12, 65, 57, 15);
		LoginPane.add(lblServerIp);

		ip_tf = new JTextField();
		ip_tf.setBounds(81, 62, 116, 21);
		LoginPane.add(ip_tf);
		ip_tf.setColumns(10);

		JLabel lblSeverPort = new JLabel("Sever port");
		lblSeverPort.setBounds(12, 110, 57, 15);
		LoginPane.add(lblSeverPort);

		port_tf = new JTextField();
		port_tf.setBounds(81, 107, 116, 21);
		LoginPane.add(port_tf);
		port_tf.setColumns(10);

		JLabel lblId = new JLabel("ID");
		lblId.setBounds(12, 160, 57, 15);
		LoginPane.add(lblId);

		id_tf = new JTextField();
		id_tf.setBounds(81, 157, 116, 21);
		LoginPane.add(id_tf);
		id_tf.setColumns(10);

		login_btn.setBounds(81, 230, 116, 23);
		LoginPane.add(login_btn);

		Login_GUI.setVisible(true);
	}

	private void Network() {
		try {
			socket = new Socket(ip, port);
			if (socket != null) {
				Connection();
			}
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "connection fail", "info",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "connection fail", "info",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void Connection() {
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);

			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "connection fail", "info",
					JOptionPane.ERROR_MESSAGE);
		}

		this.setVisible(true); //mina ui show
		this.Login_GUI.setVisible(false);
		
		Send_message(id);

		// User_list占쏙옙 占쏙옙占쏙옙占� 占쌩곤옙
		user_list.add(id);
		User_list.setListData(user_list);

		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String msg = dis.readUTF();
						System.out.println("message from server : " + msg);
						inmessage(msg);
					} catch (IOException e) {
						
						try{
						os.close();
						is.close();
						dos.close();
						dis.close();
						socket.close();
						JOptionPane.showMessageDialog(null, "disconnection server", "info",
								JOptionPane.ERROR_MESSAGE);
						}
						catch(IOException e1){}
						break;
					}

				}
			}

		});
		th.start();
	}

	private void Send_message(String str) // send message to server
	{
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		new Client();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == login_btn) {
			System.out.println("logint button click");
			
			if(ip_tf.getText().length()==0){
				ip_tf.setText("input IP");
				ip_tf.requestFocus();
			}else if(port_tf.getText().length()==0){
				port_tf.setText("input port");
				port_tf.requestFocus();
			}else if(id_tf.getText().length()==0){
				id_tf.setText("input id");
				id_tf.requestFocus();
			}else{
				ip = ip_tf.getText().trim();
				port = Integer.parseInt(port_tf.getText().trim());
				id = id_tf.getText().trim();
				Network();
			}
			
			
		} else if (e.getSource() == notesend_btn) { 
			System.out.println("note send button click");
			String user = (String) User_list.getSelectedValue(); 

			String note = JOptionPane.showInputDialog("占쏙옙占쏙옙占쌨쏙옙占쏙옙"); 

			if (note != null) {
				Send_message("Note/" + user + "@" + note);
			}
			System.out.println("占쌨는삼옙占�:" + user + " 占쏙옙占쏙옙 占쏙옙占쏙옙:" + note);
		} else if (e.getSource() == joinroom_btn) {
			String JoinRoom = (String)Room_list.getSelectedValue();
			Send_message("JoinRoom/"+JoinRoom);
			
			System.out.println("join room button click");
		} else if (e.getSource() == createroom_btn) {
			String roomname = JOptionPane.showInputDialog("占쏙옙 占싱몌옙");
			if (roomname != null) {
				Send_message("CreateRoom/" + roomname);
			}
			System.out.println("create room button click");
		} else if (e.getSource() == send_btn) {
			Send_message("Chatting/" + My_Room + "/"
					+ message_tf.getText().trim());
			message_tf.setText("");
			message_tf.requestFocus();
			
			System.out.println("send button click");
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		System.out.println(e);
		if(e.getKeyCode()==10){
			Send_message("Chatting/" + My_Room + "/"
					+ message_tf.getText().trim());
			message_tf.setText("");
			message_tf.requestFocus();
		// TODO Auto-generated method stub
		
	}

}
}

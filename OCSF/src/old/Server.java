package old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener{

	
	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("start");
	private JButton stop_btn = new JButton("stop");
	
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc=new Vector();
	
	Server()
	{
		
		init(); // 
		start(); // ActionListener
	}
	
	private void start()
	{
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
		
		this.setVisible(true);
	}
	
	
	private void Server_start()
	{
		try {
			server_socket=new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(server_socket!=null)
		{
			Connection();
		}
	}
	
	private void Connection()
	{
		Thread th=new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true)	{
					try {
						textArea.append("waiting user connection\n");
						socket=server_socket.accept();
						textArea.append("connection success!!!!\n");
						
						UserInfo user=new UserInfo(socket);
						user.start();
						
					} catch (IOException e) {
						e.printStackTrace();
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
		
		if(e.getSource()==start_btn){
			System.out.println("start btn click");
			port = Integer.parseInt(port_tf.getText().trim());
			Server_start();
		}
		else if(e.getSource()==stop_btn){
			System.out.println("stop btn click");
		}
	}
	
	class UserInfo extends Thread
	{
		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;
		
		private Socket user_socket;
		private String Nickname="";
		
		UserInfo(Socket soc)
		{
			this.user_socket=soc;
			UserNetwork();
		}
		
		private void UserNetwork()
		{
			try
			{
				is=user_socket.getInputStream();
				dis=new DataInputStream(is);
			
				os=user_socket.getOutputStream();
				dos=new DataOutputStream(os);

				Nickname=dis.readUTF();
				textArea.append(Nickname+" : user login\n");
			}
			catch(IOException e)
			{
				
			}
			
		}
		
		public void run() //do in thread
		{
			while(true)
			{
				try {
					String msg=dis.readUTF();
					textArea.append("message from "+Nickname+" : "+msg+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

}

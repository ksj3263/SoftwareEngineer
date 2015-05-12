package old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	
	
	Server()
	{
		
		init(); // 화면 생성
		start(); // ActionListener 설정
	}
	
	private void start()
	{
		start_btn.addActionListener(this);
		stop_btn.addActionListener(this);
	}
	
	public void init()
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
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new Server();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource()==start_btn){
			System.out.println("start btn click");
		}
		else if(e.getSource()==stop_btn){
			System.out.println("stop btn click");
		}
	}

}

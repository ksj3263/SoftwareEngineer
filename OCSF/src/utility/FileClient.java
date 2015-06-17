package utility;

import java.io.*;
import java.net.*;

// ����� �������̽� ���� ���� �ܼ� ������� �ۼ���.
public class FileClient {
	private int port = 5555; // ������ ��Ʈ ��ȣ
	private String host = null; // ������ ip �ּ�
	private Socket sc = null;
	// ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private BufferedReader br = new BufferedReader(new InputStreamReader(
			System.in));
	private String file;

	public FileClient(String ipInfo,String fileInfo) {
		host = ipInfo;
		try {
			file = fileInfo;
			File dir = new File(file);
			if (!dir.exists()) {
				System.out.println("������ �������� �ʽ��ϴ�!");
				System.exit(0);
			}
			sc = new Socket(host, port);
			oos = new ObjectOutputStream(sc.getOutputStream());
			oos.flush();
			FileInfo info = new FileInfo();
			info.setCode(100);
			info.setData(dir.getName().getBytes());
			info.setSize((int) dir.length());
			oos.writeObject(info);
			System.out.println(dir.getName() + "���� ���� ����!");
			Thread.sleep(30);
			FileInputStream fis = new FileInputStream(dir);
			int bytesRead = 0;
			byte[] buffer = new byte[1024]; // �������� ������ ũ��� ��ġ.
			Thread.sleep(30);
			while ((bytesRead = fis.read(buffer, 0, 1024)) != -1) {
				info = new FileInfo();
				info.setCode(110);
				info.setSize(bytesRead);
				info.setData(buffer);
				oos.writeObject(info);
				System.out.print(".");
				buffer = new byte[1024]; // �������� ������ ũ��� ��ġ.
				Thread.sleep(30);
			}
			fis.close();
			info = new FileInfo();
			info.setCode(200);
			info.setData(dir.getName().getBytes());
			info.setSize((int) dir.length());
			oos.writeObject(info);
			System.out.println();
			System.out.println(dir.getName() + " ���� ���� �Ϸ�!");
			oos.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	public static void main(String[] args) {
		FileClient temp = new FileClient("127.0.0.1","c:\\Test\\123.txt");
	}
}
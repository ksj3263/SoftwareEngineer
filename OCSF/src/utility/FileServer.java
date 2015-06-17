package utility;

import java.io.*;
import java.net.*;

public class FileServer {
	// �����带 ���� ���� ���� Ŭ���� �ۼ�
	class WorkerThread extends Thread {
		private Socket sc = null;

		public WorkerThread(Socket sc) {
			this.sc = sc;
		}

		public void run() {
			try {
				// Ŭ���̾�Ʈ�� InputStream�� ObjectStream���� ��ȯ
				ObjectInputStream ois = new ObjectInputStream(
						sc.getInputStream());
				// ObjectOutputStream oos = new
				// ObjectOutputStream(sc.getOutputStream());
				String ip = sc.getInetAddress().getHostAddress();
				System.out.println("Ŭ���̾�Ʈ(" + ip + ")���ӵ�!");
				// ���ŵ� ������ ������ ��ũ�� �����ϱ� ���� FileStream �غ�
				FileOutputStream fos = null;
				Object ob = null;
				while ((ob = ois.readObject()) != null) {
					// ���ŵ� ���� ��ü�� ���������� ����ȭ�� FileInfo ��ü�̴�.
					// �׷��� ObjectStream ���� �޴� ��ü�� FileInfo ��ü���� Ȯ���Ѵ�.
					if (ob instanceof FileInfo) {

						// FileInfo ��ü�� ���� �����Ѵ�.
						FileInfo info = (FileInfo) ob;

						// Ŭ���̾�Ʈ�� ���� Code ���� �м��Ѵ�.
						if (info.getCode() == 100) {
							// 100�� ���� ���� �۽��� ù �κ����� �˸�.
							String str = new String(info.getData());
							// ������ ��ũ�� ������ ����� �غ� �Ѵ�.
							// Ŭ���̾�Ʈ�� ���� �����̸��� �������� ����.
							fos = new FileOutputStream("c:\\" + str);
							System.out.println(str + "���� ���� ����. . .");
						} else if (info.getCode() == 110) {
							// 110�� ���� ���̳ʸ� ���� �κ��� ���۵��� �˸�.
							if (fos == null)
								break;
							// ������ ��ũ�� ���̳ʸ� ������ ���������� ���
							fos.write(info.getData(), 0, info.getSize());
							// ��ũ�� ���ư��� ����� �����ֱ����� .�� ����Ʈ�� ����ɶ����� ����
							System.out.println(".");
						} else if (info.getCode() == 200) {
							// 200�� ���� ���� �Ϸ�Ǿ����� �˸�. ��ũ�� ����ϴ°� ���� (fos.close)
							if (fos == null)
								break;
							String str = new String(info.getData());
							// ������ ��ũ�� ���̳ʸ� ������ ���������� ����ϴ� ������ �ߴ���.
							fos.close();
							System.out.println();
							System.out.println(str + "���� ���� �Ϸ�!");
						}
					}
				}
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	public void serverStart() {
		try {
			ServerSocket ss = new ServerSocket(5555);
			System.out.println("Ŭ���̾�Ʈ ���� ���. . .");
			Socket sc = ss.accept();
			WorkerThread wt = new WorkerThread(sc);
			wt.start();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static void main(String[] args) {
		FileServer ob = new FileServer();
		ob.serverStart();
	}
}
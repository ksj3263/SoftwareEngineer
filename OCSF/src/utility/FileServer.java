package utility;

import java.io.*;
import java.net.*;

public class FileServer {
	// 스레드를 위한 전용 내부 클래스 작성
	class WorkerThread extends Thread {
		private Socket sc = null;

		public WorkerThread(Socket sc) {
			this.sc = sc;
		}

		public void run() {
			try {
				// 클라이언트의 InputStream을 ObjectStream으로 변환
				ObjectInputStream ois = new ObjectInputStream(
						sc.getInputStream());
				// ObjectOutputStream oos = new
				// ObjectOutputStream(sc.getOutputStream());
				String ip = sc.getInetAddress().getHostAddress();
				System.out.println("클라이언트(" + ip + ")접속됨!");
				// 수신된 파일을 서버의 디스크에 저장하기 위한 FileStream 준비
				FileOutputStream fos = null;
				Object ob = null;
				while ((ob = ois.readObject()) != null) {
					// 수신된 파일 객체는 내부적으로 직렬화된 FileInfo 객체이다.
					// 그래서 ObjectStream 으로 받는 객체가 FileInfo 객체인지 확인한다.
					if (ob instanceof FileInfo) {

						// FileInfo 객체로 원상 복구한다.
						FileInfo info = (FileInfo) ob;

						// 클라이언트가 보낸 Code 값을 분석한다.
						if (info.getCode() == 100) {
							// 100인 경우는 파일 송신의 첫 부분임을 알림.
							String str = new String(info.getData());
							// 서버의 디스크에 파일을 기록할 준비를 한다.
							// 클라이언트가 보낸 파일이름을 기준으로 생성.
							fos = new FileOutputStream("c:\\" + str);
							System.out.println(str + "파일 전송 시작. . .");
						} else if (info.getCode() == 110) {
							// 110인 경우는 바이너리 파일 부분이 시작됨을 알림.
							if (fos == null)
								break;
							// 서버의 디스크에 바이너리 파일을 물리적으로 기록
							fos.write(info.getData(), 0, info.getSize());
							// 디스크가 돌아가는 모습을 보여주기위해 .을 라이트가 실행될때마다 증가
							System.out.println(".");
						} else if (info.getCode() == 200) {
							// 200인 경우는 전송 완료되었음을 알림. 디스크에 기록하는걸 멈춤 (fos.close)
							if (fos == null)
								break;
							String str = new String(info.getData());
							// 서버의 디스크에 바이너리 파일을 물리적으로 기록하던 행위를 중단함.
							fos.close();
							System.out.println();
							System.out.println(str + "파일 수신 완료!");
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
			System.out.println("클라이언트 접속 대기. . .");
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
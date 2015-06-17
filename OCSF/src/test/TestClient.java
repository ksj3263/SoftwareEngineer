package test;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestClient {
	
	/* Test Case1: Log In */
	@Test
	public void test1_1() {  // normal
		String userID = "user1";
		String ip = "127.0.0.1";
		int port = 1234;
		
	}
	@Test
	public void test1_2(){ // usreID is overlap
		String userID = "user1";
		String ip = "127.0.0.1";
		int port = 1234;
	}
	@Test
	public void test1_3(){ // userID length < 2
		String userID = "a";
		String ip = "127.0.0.1";
		int port = 1234;
	}
	@Test
	public void test1_4(){ // userID length > 20
		String userID = "user01234567890123456789";
		String ip = "127.0.0.1";
		int port = 1234;
	}
	@Test
	public void test1_5(){ // serverIP is incorrect
		String userID = "user2";
		String ip = "1.2.3.4.5";
		int port = 1234;
	}
	@Test
	public void test1_6(){ // serverPort is incorrect
		String userID = "user2";
		String ip = "127.0.0.1";
		int port = 12345;
	}

}

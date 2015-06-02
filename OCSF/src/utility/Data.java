package utility;

import java.io.Serializable;

public class Data implements Serializable{

	private String protocol;
	private Object data;

	public Data(String protocol, Object data){
		setProtocol(protocol);
		setData(data);
	}
	
	@Override
	public String toString() {
		return "protocol: "+getProtocol()+" data: "+getData();
	}
	
	/* Getter and Setter */
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
} // class Data END


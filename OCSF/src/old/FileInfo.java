package old;

import java.io.Serializable;
public class FileInfo implements Serializable {
 private static final long serialVersionUID = 1L;
 
 private int code;
 private int size;
 private byte[] data = new byte[1024]; //1k byte , 보통 2의 x승만큼 
 
 //getter, setter 만들기
 public int getCode() {
  return code;
 }
 public void setCode(int code) {
  this.code = code;
 }
 public int getSize() {
  return size;
 }
 public void setSize(int size) {
  this.size = size;
 }
 public byte[] getData() {
  return data;
 }
 public void setData(byte[] data) {
  this.data = data;
 }
 
 
public static void main(String[] args) {
  
 }
}
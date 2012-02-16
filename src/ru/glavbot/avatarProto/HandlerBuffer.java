package ru.glavbot.avatarProto;

import java.util.Date;


public class HandlerBuffer {

	private byte[] data;
	Date d;
	private volatile boolean locked;
	HandlerBuffer(int size)
	{
		data=new byte[size];
		locked=false;
	}
	public synchronized boolean isLocked() {
		return locked;
	}
	public synchronized void lock() {
		this.locked = true;
	}
	public synchronized void unlock() {
		this.locked = false;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		 d = new Date();
		 System.arraycopy(data, 0,this.data , 0, data.length);
	
		// = data;
	}
}

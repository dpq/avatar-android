package edu.gvsu.masl.asynchttp;

public class ReadedResponce {
	private int status;
	private Object data;
	
	
	
	public ReadedResponce (int status, Object data)
	{
		this.status=status;
		this.setData(data);
	}



	public int getStatus() {
		return status;
	}



	public void setStatus(int status) {
		this.status = status;
	}





	public Object getData() {
		return data;
	}



	public void setData(Object data) {
		this.data = data;
	}
}

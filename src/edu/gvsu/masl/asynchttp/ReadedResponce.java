package edu.gvsu.masl.asynchttp;

class ReadedResponce {
	private int status;
	private String text;
	
	
	
	ReadedResponce (int status, String text)
	{
		this.status=status;
		this.text=text;
	}



	public int getStatus() {
		return status;
	}



	public void setStatus(int status) {
		this.status = status;
	}



	public String getText() {
		return text;
	}



	public void setText(String text) {
		this.text = text;
	}
}

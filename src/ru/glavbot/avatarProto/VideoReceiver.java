package ru.glavbot.avatarProto;


import de.mjpegsample.MjpegView.MjpegView;

public class VideoReceiver {
	private MjpegView view;
	private String token;
	private String address;
	boolean isPlaying=false;
	
	void setAddress(String host, String httpPort)
	{
		
		this.address="http://"+host+":"+httpPort+"/restreamer?oid=%s";
	}
	
	VideoReceiver(MjpegView view)
	{
		this.view=view;
		
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = "web-"+token;
	}

	public void startReceiveVideo()
	{
		if(token.length()==0)
		{
			throw new RuntimeException("VideoReceiver started without token!");
		}
		view.requestRead(String.format(address, token));
		//MjpegInputStream.read(,);

	}
	
	public void stopReceiveVideo()
	{
		view.stopPlayback();
	}
	
	
//http://dev.glavbot.ru/restreamer?oid=web-~~TOKEN~~
}

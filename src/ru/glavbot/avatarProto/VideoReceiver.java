package ru.glavbot.avatarProto;


import de.mjpegsample.MjpegView.MjpegView;

public class VideoReceiver {
	private MjpegView view;
	private String token;
	private String address;
	boolean isPlaying=false;
	
	void setAddress(String host, int videoPort)
	{
		
		this.address=String.format("http://%s:%d",host,videoPort)+"/restreamer?oid=%s";
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
		OnScreenLogger.setVideoIn(true);

	}
	
	public void stopReceiveVideo()
	{
		view.stopPlayback();
		OnScreenLogger.setVideoIn(false);
	}
	
	
//http://dev.glavbot.ru/restreamer?oid=web-~~TOKEN~~
}

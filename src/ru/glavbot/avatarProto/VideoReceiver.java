package ru.glavbot.avatarProto;


import de.mjpegsample.MjpegView.MjpegView;

public class VideoReceiver {
	private MjpegView view;
	private String token;
	private String address;
	boolean isPlaying=false;
	VideoReceiver(MjpegView view, String address)
	{
		this.view=view;
		this.address=address;
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

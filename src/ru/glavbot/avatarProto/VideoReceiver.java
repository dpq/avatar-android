package ru.glavbot.avatarProto;

import de.mjpegsample.MjpegView.MjpegInputStream;
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
		view.setSource(MjpegInputStream.read(String.format(address, token)));
		view.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		view.showFps(true);
	}
	
	public void stopReceiveVideo()
	{
		view.stopPlayback();
	}
	
	
//http://dev.glavbot.ru/restreamer?oid=web-~~TOKEN~~
}

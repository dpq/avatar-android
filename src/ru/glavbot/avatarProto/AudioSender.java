package ru.glavbot.avatarProto;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioSender extends Thread{
	private AudioGroup group;
	private AudioStream stream;
	//private AudioCodec codec;
	private final String host;
	private final int port;
	private AudioManager aManager;
	//private Object waiter = new Object();
	
	
	protected AudioSender(Activity base,String host,int port)
	{
		this.host=host;
		this.port=port;
		start();
		aManager = (AudioManager) base.getSystemService(Context.AUDIO_SERVICE);
		/*try {
			waiter.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			
		}*/
	}
	
	public void startVoice()
	{
		
		aManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		Message msg = mChildHandler.obtainMessage(START_RTP);
		mChildHandler.sendMessage(msg);
	}
	
	public void stopVoice()
	{
		aManager.setMode(AudioManager.MODE_NORMAL);
		Message msg = mChildHandler.obtainMessage(STOP_RTP);
		mChildHandler.sendMessage(msg);
	}
	
	Handler mChildHandler;
	
	private static final int START_RTP=1;
	private static final int STOP_RTP=2;
	
	
	 public void run() {

		// codec = AudioCodec.getCodec(, "AMR/8000", "mode-set=1");
			group= new AudioGroup();
			//group.
			try {
				stream = new AudioStream(InetAddress.getByName("0.0.0.0"));
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				Log.e("AudioSender","run",e);
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("AudioSender","run",e);
				
			}
			group.setMode(AudioGroup.MODE_ON_HOLD);
			//group.
			stream.setCodec(AudioCodec.AMR);
			stream.setMode(AudioStream.MODE_NORMAL);
			try {
				stream.associate(InetAddress.getByName(host), port);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("","",e);
				
			}
	        Looper.prepare();
	        
	        mChildHandler = new Handler() {

	        	boolean isRunning = false;
	        	
	        	
	        	        	
	        	
	            public void handleMessage(Message msg) {
	            	
	            	switch (msg.what)
	            	{
	            		case START_RTP:
	            			startRtp();
	            			break;
	            		case STOP_RTP:
	            			stopRtp();
	            			break;
	            		default:
	            			throw new RuntimeException("Unknown command to video writer thread");
	            	};
					
	            }




				private void stopRtp() {
					// TODO Auto-generated method stub
					group.setMode(AudioGroup.MODE_ON_HOLD);
					group.clear();
				}




				private void startRtp() {
					// TODO Auto-generated method stub
					group.setMode(AudioGroup.MODE_ON_HOLD);
					stream.join(group);
					group.setMode(AudioGroup.MODE_NORMAL);
					group.sendDtmf(1);
				}





	        };
	    //    waiter.notify();
	        Looper.loop();
	    };
	
	
}
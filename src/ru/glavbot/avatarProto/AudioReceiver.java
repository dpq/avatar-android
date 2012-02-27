package ru.glavbot.avatarProto;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioReceiver extends Thread {

	private final String host;
	private final int port;
	private String token;
	
	
	AudioTrack player = null;
    private static final int sampleRate = 44100;
    private static final int CHUNK_SIZE = 320*2;
    private Activity owner; 
      
    
    
    Object sync= new Object();
	
	protected AudioReceiver(Activity base,String host,int port)
	{
		this.host=host;
		this.port=port;
		owner=base;
		start();
		try {
			synchronized(sync)
			{
				sync.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			
		}
	}
	
	public void startVoice()
	{
		AudioManager audiomanager = (AudioManager) owner.getSystemService(Activity.AUDIO_SERVICE);
		audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		audiomanager.setSpeakerphoneOn(true);
		Message msg = mChildHandler.obtainMessage(START_AUDIO);
		mChildHandler.sendMessage(msg);
	}
	
	public void stopVoice()
	{
		AudioManager audiomanager = (AudioManager) owner.getSystemService(Activity.AUDIO_SERVICE);
		audiomanager.setSpeakerphoneOn(false);
		audiomanager.setMode(AudioManager.MODE_NORMAL);
		Message msg = mChildHandler.obtainMessage(STOP_AUDIO);
		mChildHandler.sendMessage(msg);

	}
	
	Handler mChildHandler;
	
	private static final int START_AUDIO=0;
	protected static final int PROCESS_AUDIO = 1;
	private static final int STOP_AUDIO=2;
	protected static final int AUDIO_IN_ERROR = -3;
	protected static final int MAX_RCV_BUFFER = 32768;

    private volatile boolean isPlaying = false;

	
	 public void run() {

		 	synchronized(sync)
		 	{
	        Looper.prepare();
	        
	        mChildHandler = new Handler() {

	        //	boolean isRunning = false;
	        	
	        	Socket socket = null;
	        	        	

	            private byte[] audioData; //= new short[bufferSize];
	            private int bufferSize;//= bufferSize;

	            
	            public void handleMessage(Message msg) {
	            	
	            	switch (msg.what)
	            	{
	            		case START_AUDIO:
	            			//startPlay();
	            			break;
	            		case PROCESS_AUDIO:
	            			//doPlay();
	            			break;
	            		case STOP_AUDIO:
	            			//stopPlay();
	            			break;
	            		default:
	            			throw new RuntimeException("Unknown command to video writer thread");
	            	};
					
	            }




				private void startPlay() {

					bufferSize =AudioTrack.getMinBufferSize(sampleRate,
			                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
			                        AudioFormat.ENCODING_PCM_16BIT);
					player = new AudioTrack( AudioManager.STREAM_VOICE_CALL , sampleRate,
				                    AudioFormat.CHANNEL_OUT_MONO,
				                    AudioFormat.ENCODING_PCM_16BIT, bufferSize,AudioTrack.MODE_STREAM);

				        
				     InetAddress addr=null;
						try {
							addr = InetAddress.getByName(host);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							Log.e("","",e);
							
						}
						catch (Exception e) {
							// TODO Auto-generated catch block
							Log.e("","",e);
						}
						
						OutputStream s;
						
							try {
								socket = new Socket(addr, port);
							
								socket.setKeepAlive(true);
								socket.setTcpNoDelay(true);
								socket.setSoTimeout(100000);
								socket.setReceiveBufferSize(MAX_RCV_BUFFER);
							
								s = socket.getOutputStream();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								s=null;
								errorHandler.obtainMessage(AUDIO_IN_ERROR).sendToTarget();
							}
							
							
							String ident = "ava-"+getToken();
							try {
								if(s!=null)
									s.write(ident.getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								errorHandler.obtainMessage(AUDIO_IN_ERROR).sendToTarget();
								
							}
							
							isPlaying=(socket!=null);
							
						if(isPlaying)
						{
							player.play();
							//recorder.startRecording();
							audioData= new byte[CHUNK_SIZE];
							/*int qt = 1;
							while (qt>0)
							{
								try {
									qt=socket.getInputStream().read(audioData);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									Log.e("","",e);
									qt=-1;
								}
							}*/
							Message msg = mChildHandler.obtainMessage(PROCESS_AUDIO);
							mChildHandler.sendMessage(msg);
						}
				}




				private void stopPlay() {
					mChildHandler.removeMessages(PROCESS_AUDIO);
					if(player!=null)
					{
						player.stop();
						player.release();
						player=null;
					}
					try {
						if(socket!=null)
						socket.close();
					} catch (IOException e) {
						Log.e("","",e);
					}
					socket=null;
					isPlaying=false;

				}




				private void doPlay() {
					boolean reconnect = false;
					if(isPlaying)
					{
						try {
						int bytes_read=socket.getInputStream().read(audioData, 0, CHUNK_SIZE);
						//recorder.read();
							if(bytes_read>0)
							{
								player.write(audioData,0,bytes_read);
								//socket.getOutputStream().write(audioData,0,bytes_read);
								//socket.getOutputStream().flush();
							
							}
						} catch (IOException e) {
							Log.e("","",e);
							reconnect = true;
							
						}
						
						Message msg = mChildHandler.obtainMessage(PROCESS_AUDIO);
						mChildHandler.sendMessage(msg);
						mChildHandler.removeMessages(START_AUDIO);
					}
					else
					{
						reconnect=true;
					}
					
					if(reconnect)
					{
						errorHandler.obtainMessage(AUDIO_IN_ERROR).sendToTarget();
					}
				}





	        };
	        sync.notifyAll();
		 	}
	       
	        Looper.loop();
	    }

	private String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	};
	
	private Handler errorHandler = new Handler()
    {
    	@Override
    	 public void handleMessage(Message msg) {
            	
            	switch (msg.what)
            	{
            	case AUDIO_IN_ERROR:
            		/*	if(isRecording)
            			{*/
            				stopVoice();
            				startVoice();
            		/*	}*/
            			break;
            		default:
            			throw new RuntimeException("Unknown command to incoming video error handler");
            	};
				
            }
    };
	
}

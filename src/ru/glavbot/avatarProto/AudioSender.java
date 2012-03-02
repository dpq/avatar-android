package ru.glavbot.avatarProto;
 
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioSender extends Thread{

	private final String host;
	private final int port;
	private String token;
	
	
    AudioRecord recorder = null;
    private static final int sampleRate = 44100;
    private static final int CHUNK_SIZE = 640*2;
    
      
    Object sync= new Object();
    
	
	
	protected AudioSender(Activity base,String host,int port)
	{
		this.host=host;
		this.port=port;
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
	
	private boolean isRunning=false;
	public void startVoice()
	{

		isRunning=true;
		internalStart();
	}
	
	protected void internalStart()
	{
		Message msg = mChildHandler.obtainMessage(START_AUDIO);
		mChildHandler.sendMessage(msg);
	}
	
	
	public void stopVoice()
	{

		isRunning=false;
		internalStop();
	}
	
	protected void internalStop()
	{
		Message msg = mChildHandler.obtainMessage(STOP_AUDIO);
		mChildHandler.sendMessage(msg);
	}
	
	Handler mChildHandler;
	
	private static final int START_AUDIO=0;
	protected static final int PROCESS_AUDIO = 1;
	private static final int STOP_AUDIO=2;
	protected static final int AUDIO_OUT_ERROR = -2;
	protected static final int MAX_SEND_BUFFER = 32768;

    private volatile boolean isRecording = false;

	
	 public void run() {

		 synchronized(sync){
		 
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
	            			startRecord();
	            			break;
	            		case PROCESS_AUDIO:
	            			doRecord();
	            			break;
	            		case STOP_AUDIO:
	            			stopRecord();
	            			break;
	            		default:
	            			throw new RuntimeException("Unknown command to video writer thread");
	            	};
					
	            }




				private void startRecord() {

					bufferSize =AudioRecord.getMinBufferSize(sampleRate,
			                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
			                        AudioFormat.ENCODING_PCM_16BIT);
				     recorder = new AudioRecord(AudioSource.VOICE_COMMUNICATION, sampleRate,
				                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
				                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
				      
				      boolean error=false;  
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
							error=true;
						}
						
						OutputStream s;
						
							try {
								socket = new Socket(addr, port);
							
								socket.setKeepAlive(true);
								socket.setTcpNoDelay(true);
								socket.setSoTimeout(100000);
								socket.setSendBufferSize(MAX_SEND_BUFFER);
							
								s = socket.getOutputStream();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								s=null;
								error=true;
							//	errorHandler.obtainMessage(AUDIO_OUT_ERROR,e).sendToTarget();
							}
							
							
							String ident = "ava-"+getToken();
							try {
								if(s!=null)
								{
									s.write(ident.getBytes());
									s.flush();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								error=true;
								//errorHandler.obtainMessage(START_AUDIO,e).sendToTarget();
								
							}
							
							isRecording=(socket!=null);
							
						if(isRecording)
						{
							recorder.startRecording();
							audioData= new byte[CHUNK_SIZE];
							Message msg = mChildHandler.obtainMessage(PROCESS_AUDIO);
							mChildHandler.sendMessage(msg);
						//	mChildHandler.removeMessages(START_AUDIO);
						}
						else
						{
							error=true;
						}
						
						if(error)
						{
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								
							}
							errorHandler.obtainMessage(AUDIO_OUT_ERROR).sendToTarget();
						}
						
				}




				private void stopRecord() {
					mChildHandler.removeMessages(PROCESS_AUDIO);
					mChildHandler.removeMessages(START_AUDIO);
					if(recorder!=null)
					{
						recorder.stop();
						recorder.release();
						recorder=null;
					}
					try {
						if(socket!=null)
						socket.close();
					} catch (IOException e) {
						Log.e("","",e);
					}
					socket=null;
					isRecording=false;
					
				}




				private void doRecord() {
					boolean reconnect = false;
					if(isRecording)
					{
						int bytes_read=recorder.read(audioData, 0, CHUNK_SIZE);
						if(bytes_read>0)
						{
							try {
								socket.getOutputStream().write(audioData,0,bytes_read);
								socket.getOutputStream().flush();
							} catch (IOException e) {
								Log.e("","",e);
								reconnect=true;
							}
						}
						
						Message msg = mChildHandler.obtainMessage(PROCESS_AUDIO);
						mChildHandler.sendMessage(msg);
					}
					else
					{
						reconnect=true;
						
					}
					if(reconnect)
					{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.e("","",e);
							
						}
						errorHandler.obtainMessage(AUDIO_OUT_ERROR).sendToTarget();
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
            		case AUDIO_OUT_ERROR:
            			if(isRecording)
            			{
            				internalStop();
            				internalStart();
            			}
            			break;
            		default:
            			throw new RuntimeException("Unknown command to incoming video error handler");
            	};
				
            }
    };
	
}
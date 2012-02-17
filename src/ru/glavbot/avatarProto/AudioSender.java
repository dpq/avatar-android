package ru.glavbot.avatarProto;
 
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
//import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
//import android.content.Context;
import android.media.AudioFormat;
//import android.media.AudioManager;
import android.media.AudioRecord;
//import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
//import android.net.rtp.AudioCodec;
//import android.net.rtp.AudioGroup;
//import android.net.rtp.AudioStream;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioSender extends Thread{
	//private AudioGroup group;
	//private AudioStream stream;
	//private AudioCodec codec;
	private final String host;
	private final int port;
	private String token;
	
	
    AudioRecord recorder = null;
  //  short[][]   buffers  = new short[256][160];
   // int         ix       = 0;
    private static final int sampleRate = 44100;
    private static final int CHUNK_SIZE = 320*2; //  frames * sizeof(short)
    
      
    
    
	
	
	protected AudioSender(Activity base,String host,int port)
	{
		this.host=host;
		this.port=port;
		//this.token=token;
		start();
	//	aManager = (AudioManager) base.getSystemService(Context.AUDIO_SERVICE);
		/*try {
			waiter.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			
		}*/
	}
	
	public void startVoice()
	{
		
	//	aManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		Message msg = mChildHandler.obtainMessage(START_AUDIO);
		mChildHandler.sendMessage(msg);
	}
	
	public void stopVoice()
	{
	//	aManager.setMode(AudioManager.MODE_NORMAL);
		Message msg = mChildHandler.obtainMessage(STOP_AUDIO);
		mChildHandler.sendMessage(msg);
	}
	
	Handler mChildHandler;
	
	private static final int START_AUDIO=0;
	protected static final int PROCESS_AUDIO = 1;
	private static final int STOP_AUDIO=2;

    private volatile boolean isRecording = false;

	
	 public void run() {

		
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
				      /*  recorder.setPositionNotificationPeriod (CHUNK_SIZE);

				        OnRecordPositionUpdateListener positionUpdater = new OnRecordPositionUpdateListener()
				        {

							   
				            public void onPeriodicNotification(AudioRecord recorder)
				            {
				                
				                recorder.read(audioData, 0, CHUNK_SIZE);

				                //do something amazing with audio data
				            }

				            
				            public void onMarkerReached(AudioRecord recorder)
				            {
				                Log.d("", "marker reached");
				            }
				        };
				        recorder.setRecordPositionUpdateListener(positionUpdater);
					*/
				        
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
							
							
								s = socket.getOutputStream();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								s=null;
							}
							
							
							String ident = "ava-"+getToken();
							try {
								if(s!=null)
									s.write(ident.getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								
							}
							
							isRecording=(socket!=null);
							
						if(isRecording)
						{
							recorder.startRecording();
							audioData= new byte[CHUNK_SIZE];
							Message msg = mChildHandler.obtainMessage(PROCESS_AUDIO);
							mChildHandler.sendMessage(msg);
						}
				}




				private void stopRecord() {
					mChildHandler.removeMessages(PROCESS_AUDIO);
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
						// TODO Auto-generated catch block
						Log.e("","",e);
						
					}
					socket=null;
					isRecording=false;
					
				}




				private void doRecord() {
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
							}
						}
						
						Message msg = mChildHandler.obtainMessage(PROCESS_AUDIO);
						mChildHandler.sendMessage(msg);
					}
				}





	        };
	    //    waiter.notify();
	        Looper.loop();
	    }

	private String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	};
	
	
}
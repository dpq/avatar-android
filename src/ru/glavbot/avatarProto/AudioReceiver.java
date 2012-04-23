package ru.glavbot.avatarProto;

//import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioReceiver extends Thread {

	private String host;
	private int port;
	private String token;

	AudioTrack player = null;
	private static final int SAMPLE_RATE = 8000;
	private static final int CHUNK_SIZE_BASE = 320;
	private static final int SIZEOF_SHORT = 2;
	private static final int SIZEOF_FLOAT = 4;

	private static final int CHUNK_SIZE_SHORT = CHUNK_SIZE_BASE * SIZEOF_SHORT;
	private static final int BUFF_SIZE = CHUNK_SIZE_BASE * SIZEOF_FLOAT*2;
	private static final int STD_DELAY = 10000;


	Object sync = new Object();

	void setHostAndPort(String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	
	
	protected AudioReceiver(Activity base) {
		start();
		try {
			synchronized (sync) {
				sync.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("", "", e);

		}
	}

	public void startVoice() {
		isRecording = true;
		internalStart();
	}

	protected void internalStart() {

		Message msg = mChildHandler.obtainMessage(START_AUDIO);
		mChildHandler.sendMessage(msg);
		Log.v("avatar audio in","internal start");
	}

	public void stopVoice() {
		isRecording = false;
		internalStop();
	}

	protected void internalStop() {
		
		Message msg = mChildHandler.obtainMessage(STOP_AUDIO);
		mChildHandler.removeMessages(START_AUDIO);
		mChildHandler.sendMessageAtFrontOfQueue(msg);
		Log.v("avatar audio in","internal stop");
	}

	Handler mChildHandler;

	private static final int START_AUDIO = 0;
	protected static final int PROCESS_AUDIO = 1;
	private static final int STOP_AUDIO = 2;
	protected static final int AUDIO_IN_ERROR = -3;
	private volatile boolean isPlaying = false;
	private boolean isRecording = false;

	public void run() {

		synchronized (sync) {
			Looper.prepare();
			setName("AudioReceiver");
			mChildHandler = new Handler() {

				Socket socket = null;

				private short[] shortAudioData = new short[CHUNK_SIZE_BASE];
				
				private int bufferSize;
				{
					bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					bufferSize = CHUNK_SIZE_SHORT > bufferSize ? CHUNK_SIZE_SHORT: bufferSize;
				}
				
				DataInputStream floatStream;
				
				public void handleMessage(Message msg) {

					switch (msg.what) {
					case START_AUDIO:
						startPlay();
						break;
					case PROCESS_AUDIO:
						doPlay();
						break;
					case STOP_AUDIO:
						stopPlay();
						break;
					default:
						throw new RuntimeException("Unknown command to video writer thread");
					};

				}

				private void startPlay() {
					
					if (hasMessages(START_AUDIO)) {
						removeMessages(START_AUDIO);
					}
					if(isPlaying)
					{
						return;
					}
					
					
					Log.v("avatar audio in","starting play");
					closeSocket();
					
					InetAddress addr = null;
					try {
						addr = InetAddress.getByName(host);
					} catch (Exception e) {
						Log.e("", "", e);
						errorHandler.sendMessageDelayed(errorHandler.obtainMessage(AUDIO_IN_ERROR),STD_DELAY);
						return;
					}

					try {
						socket = new Socket(addr, port);
						socket.setKeepAlive(true);
						socket.setSoTimeout(10000);
						socket.setReceiveBufferSize(BUFF_SIZE);
						OutputStream s = socket.getOutputStream();
						String ident = "web-" + getToken();
						s.write(ident.getBytes());
						floatStream = new DataInputStream(socket.getInputStream());
					} catch (IOException e) {
						closeSocket();
						errorHandler.sendMessageDelayed(errorHandler.obtainMessage(AUDIO_IN_ERROR),STD_DELAY);
						return;
					}

					isPlaying = true;
					OnScreenLogger.setAudioIn(true);
					player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
							SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
							AudioFormat.ENCODING_PCM_16BIT, bufferSize,
							AudioTrack.MODE_STREAM);
					player.play();
					mChildHandler.obtainMessage(PROCESS_AUDIO).sendToTarget();
					
				}

				private void stopPlay() {
					
					Log.v("avatar audio in","stopping play");
					mChildHandler.removeMessages(PROCESS_AUDIO);
					mChildHandler.removeMessages(STOP_AUDIO);
					
					if (player != null) {
						player.stop();
						player.release();
						player = null;
					}
					closeSocket();
					isPlaying = false;
				}

				private void closeSocket()
				{
					try {
						if (socket != null)
							socket.close();
					} catch (IOException e) {
						Log.e("", "", e);
					}
					socket = null;
					OnScreenLogger.setAudioIn(false);
				}
				
				private void doPlay() {
					if (isPlaying) {
							int dataRead;
							
							for (dataRead = 0; dataRead < CHUNK_SIZE_BASE; dataRead++) {
								try {
									shortAudioData[dataRead] = (short) (floatStream.readFloat() * (float) Short.MAX_VALUE);
								} catch (EOFException e) {
									break;
								} catch (IOException e1) {
									Log.e("", "", e1);
									isPlaying = false;
									closeSocket();
									errorHandler.sendMessageDelayed(errorHandler.obtainMessage(AUDIO_IN_ERROR),STD_DELAY);
									break;
								}
							}
							
							if (dataRead > 0) {
								Log.v("avatar audio in" ,String.format("readed %d bytes",dataRead*4));
								player.write(shortAudioData, 0, dataRead);
							}
							
							if(isPlaying)
							{
								mChildHandler.obtainMessage(PROCESS_AUDIO).sendToTarget();
							}
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

	private Handler errorHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case AUDIO_IN_ERROR:
				errorHandler.removeMessages(AUDIO_IN_ERROR);
				if (isRecording) {
					mChildHandler.removeMessages(START_AUDIO);
					Log.v("avatar audio in","reconnecting on error");
					internalStop();
					internalStart();
				}
				break;
			default:
				throw new RuntimeException(
						"Unknown command to incoming video error handler");
			}
			

		}
	};

}

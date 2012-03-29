package ru.glavbot.avatarProto;

//import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
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

	private String host;
	private int port;
	private String token;

	AudioTrack player = null;
	private static final int sampleRate = 8000;
	private static final int CHUNK_SIZE_BASE = 320;
	private static final int SIZEOF_SHORT = 2;
	private static final int SIZEOF_FLOAT = 4;

	private static final int CHUNK_SIZE_SHORT = CHUNK_SIZE_BASE * SIZEOF_SHORT;
	private static final int CHUNK_SIZE_FLOAT = CHUNK_SIZE_BASE * SIZEOF_FLOAT;

	private Activity owner;

	Object sync = new Object();

	void setHostAndPort(String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	
	
	protected AudioReceiver(Activity base) {
		owner = base;
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
		AudioManager audiomanager = (AudioManager) owner
				.getSystemService(Activity.AUDIO_SERVICE);
		audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		audiomanager.setSpeakerphoneOn(true);
		Message msg = mChildHandler.obtainMessage(START_AUDIO);
		mChildHandler.sendMessage(msg);
	}

	public void stopVoice() {
		isRecording = false;
		internalStop();
	}

	protected void internalStop() {
		AudioManager audiomanager = (AudioManager) owner
				.getSystemService(Activity.AUDIO_SERVICE);
		audiomanager.setSpeakerphoneOn(false);
		audiomanager.setMode(AudioManager.MODE_NORMAL);
		Message msg = mChildHandler.obtainMessage(STOP_AUDIO);
		mChildHandler.sendMessage(msg);
	}

	Handler mChildHandler;

	private static final int START_AUDIO = 0;
	protected static final int PROCESS_AUDIO = 1;
	private static final int STOP_AUDIO = 2;
	protected static final int AUDIO_IN_ERROR = -3;
	protected static final int MAX_RCV_BUFFER = 32768;

	private volatile boolean isPlaying = false;
	private boolean isRecording = false;

	public void run() {

		synchronized (sync) {
			Looper.prepare();

			mChildHandler = new Handler() {

				// boolean isRunning = false;

				Socket socket = null;

				// private byte[] audioData; //= new short[bufferSize];
				// private float[] floatAudioData;
				private short[] shortAudioData;
				private int bufferSize;// = bufferSize;
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
						throw new RuntimeException(
								"Unknown command to video writer thread");
					}
					;

				}

				private void startPlay() {

					// prev_offset=0;

					bufferSize = AudioTrack.getMinBufferSize(sampleRate,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					bufferSize = CHUNK_SIZE_SHORT > bufferSize ? CHUNK_SIZE_SHORT
							: bufferSize;
					player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
							sampleRate, AudioFormat.CHANNEL_OUT_MONO,
							AudioFormat.ENCODING_PCM_16BIT, bufferSize,
							AudioTrack.MODE_STREAM);

					InetAddress addr = null;
					try {
						addr = InetAddress.getByName(host);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						Log.e("", "", e);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("", "", e);
					}

					OutputStream s;

					try {
						if(socket!=null)
						{
							socket.close();
							socket=null;
						}
						socket = new Socket(addr, port);

						socket.setKeepAlive(true);
						//socket.setTcpNoDelay(true);
						socket.setSoTimeout(10000);
						socket.setReceiveBufferSize(MAX_RCV_BUFFER);

						s = socket.getOutputStream();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("", "", e);
						s = null;
						try {
							if (socket != null)
								socket.close();
						} catch (IOException e1) {
							Log.e("", "", e1);
						}
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							Log.e("", "", e1);

						}
						errorHandler.obtainMessage(AUDIO_IN_ERROR)
								.sendToTarget();
					}

					String ident = "web-" + getToken();
					try {
						if (s != null)
							s.write(ident.getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("", "", e);
						try {
							if (socket != null)
								socket.close();
						} catch (IOException e1) {
							Log.e("", "", e1);
						}
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							Log.e("", "", e1);

						}
						errorHandler.obtainMessage(AUDIO_IN_ERROR)
								.sendToTarget();

					}

					isPlaying = (socket != null);

					if (isPlaying) {
						player.play();
						// recorder.startRecording();
						// floatAudioData= new float[CHUNK_SIZE_BASE];
						// audioData= new byte[CHUNK_SIZE_FLOAT];
						shortAudioData = new short[CHUNK_SIZE_BASE];
						try {
							floatStream = new DataInputStream(
									socket.getInputStream());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							try {
								if (socket != null)
									socket.close();
							} catch (IOException e1) {
								Log.e("", "", e1);
							}
							errorHandler.obtainMessage(AUDIO_IN_ERROR)
									.sendToTarget();

						}
						/*
						 * int qt = 1; while (qt>0) { try {
						 * qt=socket.getInputStream().read(audioData); } catch
						 * (IOException e) { // TODO Auto-generated catch block
						 * Log.e("","",e); qt=-1; } }
						 */
						Message msg = mChildHandler
								.obtainMessage(PROCESS_AUDIO);
						mChildHandler.sendMessage(msg);
					}
				}

				private void stopPlay() {
					mChildHandler.removeMessages(PROCESS_AUDIO);
				//	mChildHandler.removeMessages(START_AUDIO);
					if (player != null) {
						player.stop();
						player.release();
						player = null;
					}
					try {
						if (socket != null)
							socket.close();
					} catch (IOException e) {
						Log.e("", "", e);
					}
					socket = null;
					isPlaying = false;

				}

				// int prev_offset;

				private void doPlay() {
					boolean reconnect = false;
					if (isPlaying) {
						// try {
						// int
						// bytes_read=socket.getInputStream().read(audioData, 0,
						// CHUNK_SIZE_FLOAT);

						// recorder.read();
						// if(bytes_read>0)
						// {
						// DataInputStream s =new DataInputStream( new
						// ByteArrayInputStream(audioData));
						try {
							socket.getOutputStream().write(' ');
						} catch (IOException e3) {
							// TODO Auto-generated catch block
							Log.e("","",e3);
							reconnect = true;
						}
						
						
						
						int i;
					/*	try {
							do {
								if((!reconnect)&&(!socket.isConnected()))
								{
									reconnect = true;
								}
							} while ((floatStream.available() < CHUNK_SIZE_FLOAT)
									&& (!isInterrupted())&&(!reconnect));
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							Log.e("", "", e2);
							reconnect = true;
							try {
								if (socket != null)
									socket.close();
							} catch (IOException e1) {
								Log.e("", "", e1);
							}

						}*/
						if((!reconnect)&&(!socket.isConnected()))
						{
							reconnect = true;
						}
						
						if (!reconnect) {
							for (i = 0; i < CHUNK_SIZE_BASE; i++) {
								try {
									shortAudioData[i] = (short) (floatStream
											.readFloat() * (float) Short.MAX_VALUE);
								} catch (EOFException e) {
									break;
								} catch (IOException e1) {
									Log.e("", "", e1);
									reconnect = true;
									try {
										if (socket != null)
											socket.close();
									} catch (IOException e2) {
										Log.e("", "", e2);
									}
									break;
								}

							}
							if (i > 0) {
								player.write(shortAudioData, 0, i);
							}

							// socket.getOutputStream().write(audioData,0,bytes_read);
							// socket.getOutputStream().flush();

							// }
							// } catch (IOException e) {
							// Log.e("","",e);
							// reconnect = true;

							// }

							Message msg = mChildHandler
									.obtainMessage(PROCESS_AUDIO);
							mChildHandler.sendMessage(msg);
							mChildHandler.removeMessages(START_AUDIO);
						}
					} else {
						reconnect = true;
					}

					if (reconnect) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.e("", "", e);

						}
						errorHandler.obtainMessage(AUDIO_IN_ERROR)
								.sendToTarget();
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
				if (isRecording) {
					mChildHandler.removeMessages(START_AUDIO);
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

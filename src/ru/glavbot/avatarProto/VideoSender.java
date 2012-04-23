package ru.glavbot.avatarProto;

import java.io.ByteArrayOutputStream;
//import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
//import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

//import java.text.SimpleDateFormat;
//import java.util.List;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PreviewCallback;
//import android.hardware.Camera.Size;
//import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.VideoView;
import android.app.Activity;

public class VideoSender extends Thread {

	private Context context;
	private SurfaceView preview;
	private VideoView foreignStream;
	private int port;
	private String host;

	private HandlerBuffer[] dataBuff;
	private static final int PREVIEW_WIDTH = 800;
	private static final int PREVIEW_HEIGHT = 600;
	private static final int NUM_FRAMES = 30;
	private static final int NUM_BUFFERS = 5;
	private static final int STD_DELAY = 100;
	private String token;

	Object sync = new Object();

	ArrayList<byte[]> buffers;

	void setHostAndPort(String host, int port) {
		this.host = host;
		this.port = port;
	}

	volatile boolean isOn=false;
	
	VideoSender(final Context context, SurfaceView preview) {
		this.context = context;
		this.preview = preview;
		// this.foreignStream=foreignStream;
		// this.setToken(token);

		buffers = new ArrayList<byte[]>();
		int size = PREVIEW_WIDTH * PREVIEW_HEIGHT
				* ImageFormat.getBitsPerPixel(ImageFormat.NV21);
		for (int i = 0; i < NUM_BUFFERS; i++) {
			buffers.add(new byte[size]);
		}
		dataBuff = new HandlerBuffer[] { new HandlerBuffer(size),
				new HandlerBuffer(size) };

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

	public void run() {

		synchronized (sync) {
			Looper.prepare();
			setName("VideoSender");
			mChildHandler = new Handler() {

				boolean isRunning = false;
				Socket socket = null;

				Rect imgRect = new Rect(0, 0, PREVIEW_WIDTH - 1,PREVIEW_HEIGHT - 1);

				OutputStream socketOutputStream = null;
				
				private void processFrame(Message msg) {
					Log.v("VideoSender","sending image to server");
					if (isRunning&&!socket.isConnected()) {
						closeSocket();
						OnScreenLogger.setVideoOut(false);
						sendMessageDelayed(obtainMessage(INITIALIZE_VIDEO_SOCKET),STD_DELAY);
					} else {
						HandlerBuffer data = (HandlerBuffer) msg.obj;
						data.lock();
						YuvImage img = new YuvImage(data.getData(),
								ImageFormat.NV21, PREVIEW_WIDTH,
								PREVIEW_HEIGHT, null);

						ByteArrayOutputStream os = new ByteArrayOutputStream();
						
						img.compressToJpeg(imgRect, 50, os);
						

						data.unlock();
/*
						Bitmap b = BitmapFactory.decodeByteArray(os.toByteArray(),0,os.size());
						Matrix matrix = new Matrix();
						matrix.postRotate(270);
						Bitmap rotated = Bitmap.createBitmap(b, 0, 0, 
						                              b.getWidth(), b.getHeight(), 
						                              matrix, true);
						os.reset();
						rotated.compress(Bitmap.CompressFormat.JPEG, 100, os);*/
						
						String s = String.format("--boundarydonotcross" + eol
								+ "Content-Type: image/jpeg" + eol
								+ "Content-Length: %d" + eol + eol, os.size());
						try {
							if(socketOutputStream!=null)
							{
								socketOutputStream.write(s.getBytes());
								socketOutputStream.write(os.toByteArray());
								socketOutputStream.write(eol.getBytes());
								socketOutputStream.flush();
							}else
							{
								throw new IOException("socket output stream not defined!");
							}

						} catch (IOException e) {
							//Log.e("", "", e);
							closeSocket();
							//isRunning=false;
							sendMessageDelayed(obtainMessage(INITIALIZE_VIDEO_SOCKET),STD_DELAY);
						}
					}
				}

				private void initializeSocket() {	
					if (hasMessages(INITIALIZE_VIDEO_SOCKET)) {
						removeMessages(INITIALIZE_VIDEO_SOCKET);
					}
					if(!isOn)
						return;
					try {
						InetAddress addr = InetAddress.getByName(host);
						socket = new Socket(addr, port);
						socket.setKeepAlive(true);
						socket.setSoTimeout(10000);
						socketOutputStream = socket.getOutputStream();
						String ident = "ava-" + token;
						String header = /*
											 * String.format(
											 * "POST /restreamer?oid=%s HTTP/1.1"
											 * +eol +"Server: %s:%d"+eol
											 * +"User-Agent: avatar/0.2"+eol +
											 * "Content-Type: multipart/x-mixed-replace; boundary=--boundarydonotcross"
											 * +eol +eol +eol ,
											 */ident/*
													 * ,hostname,SERVER_VIDEO_PORT
													 * )
													 */;
						socketOutputStream.write(header.getBytes());
						isRunning = true;
						OnScreenLogger.setVideoOut(true);
					} catch (Exception e) {
						Log.e(this.getClass().getName(), this.toString(), e);
						closeSocket();
						sendMessageDelayed(obtainMessage(INITIALIZE_VIDEO_SOCKET),STD_DELAY);
					}
				}

				private void closeSocket() {

					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							Log.e("", "", e);
						}
					}
					socketOutputStream=null;
					socket = null;
					isRunning = false;
					OnScreenLogger.setVideoOut(false);
					if (mChildHandler.hasMessages(PROCESS_FRAME)) {
						mChildHandler.removeMessages(PROCESS_FRAME);
					}
				}

				public void handleMessage(Message msg) {

					switch (msg.what) {
					case PROCESS_FRAME:
						processFrame(msg);
						break;
					case INITIALIZE_VIDEO_SOCKET:
						initializeSocket();
						break;
					case CLOSE_VIDEO_SOCKET:
						closeSocket();
						break;
					default:
						throw new RuntimeException(
								"Unknown command to video writer thread");
					}
					;

				}
			};
			sync.notifyAll();
		}

		Looper.loop();
	};

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public SurfaceView getPreview() {
		return preview;
	}

	public void setPreview(SurfaceView preview) {
		this.preview = preview;
	}

	public VideoView getForeignStream() {
		return foreignStream;
	}

	public void setForeignStream(VideoView foreignStream) {
		this.foreignStream = foreignStream;
	}

	private Camera frontCamera;

	private void setCameraDisplayOrientation(Camera.CameraInfo info,
			Camera camera) {

		int rotation = ((Activity) context).getWindowManager()
				.getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing*/
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	private Handler mChildHandler = null;

	public Handler getVideoHandler() {
		return mChildHandler;
	};

	private static final String eol = "\r\n"; // for http should be so
												// //System.getProperty("line.separator");

	private static final int INITIALIZE_VIDEO_SOCKET = 0;
	private static final int PROCESS_FRAME = 1;
	private static final int CLOSE_VIDEO_SOCKET = 2;

	protected void finalize() {
		if (mChildHandler != null) {
			mChildHandler.getLooper().quit();
		}
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			Log.e("", "", e);

		}
	}

	private void setupCamera(Camera camera) {
		Camera.Parameters p = camera.getParameters();
		// List<Integer> formats =p.getSupportedPreviewFormats () ;
		p.setPreviewFormat(ImageFormat.NV21);
		p.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		p.setPreviewFrameRate(NUM_FRAMES);
		p.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
		// p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		// List<String> l = p.getSupportedColorEffects();
		// p.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
		// if(p.is)
		// List<Size> mSupportedPreviewSizes = p.getSupportedPreviewSizes();
		// p.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		camera.setParameters(p);

		// reuse
		for (int i = 0; i < NUM_BUFFERS; i++) {
			camera.addCallbackBuffer(buffers.get(i));
		}
		dataBuff[0].unlock();
		dataBuff[1].unlock();

		camera.setPreviewCallbackWithBuffer(new PreviewCallback() {

			public void onPreviewFrame(byte[] data, Camera camera) {
				// TODO Auto-generated method stub

				if (mChildHandler.hasMessages(PROCESS_FRAME)) {
					mChildHandler.removeMessages(PROCESS_FRAME);
				}
				HandlerBuffer ourBuff = dataBuff[0].isLocked() ? dataBuff[1]
						: dataBuff[0];
				ourBuff.lock();
				// byte[] anotherImg = data.clone();
				ourBuff.setData(data);
				ourBuff.unlock();

				Message msg = mChildHandler.obtainMessage(PROCESS_FRAME, 0, 0,
						ourBuff);
				mChildHandler.sendMessage(msg);
				camera.addCallbackBuffer(data);
			}

		});

	}

	protected void startCamera() {
		// start socket;
		Log.v("VideoSender::startCamera", "starting camera");
		isOn=true;
		Message msg = mChildHandler.obtainMessage(INITIALIZE_VIDEO_SOCKET);
		mChildHandler.sendMessage(msg);
		
		// now camera;
		frontCamera = getFrontCamera();

		if ((frontCamera != null)) {

			//preview.setVisibility(View.VISIBLE);
			SurfaceHolder holder = preview.getHolder();
			try {
				holder.removeCallback(cameraCallback);
			} catch (Exception e) {
				Log.e("VideoSender::startCamera", "remove callback failed");
				// do nothing
			}

			holder.addCallback(cameraCallback);

			if (!holder.isCreating()) {
				startShow(holder);
			}
		}

	}

	SurfaceHolder.Callback cameraCallback = new SurfaceHolder.Callback() {

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub

		}

		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			startShow(holder);
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			if (frontCamera != null) {
				// try {
				// frontCamera.setPreviewDisplay(null);
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// Log.e("","",e);
				//
				// }
			}
		}
	};

	private void startShow(SurfaceHolder holder) {
		holder.setFixedSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		try {
			frontCamera.setPreviewDisplay(holder);
		} catch (IOException e1) {
			Log.e("", "", e1);
		}

		setupCamera(frontCamera);
		frontCamera.startPreview();
	}

	protected void stopCamera() {
		Log.v("VideoSender::stopCamera", "stop camera");
		isOn=false;
		Message msg = mChildHandler.obtainMessage(CLOSE_VIDEO_SOCKET);
		mChildHandler.sendMessage(msg);

		if (frontCamera != null) {
			frontCamera.stopPreview();

			//preview.setVisibility(View.INVISIBLE);
			releaseCamera();
		}

	}

	private void releaseCamera() {
		if (frontCamera != null) {
			frontCamera.release(); // release the camera for other applications
			frontCamera = null;
		}
	}

	private Camera getFrontCamera() {
		Camera.CameraInfo inf = new Camera.CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {

			Camera.getCameraInfo(i, inf);
			if (inf.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				Camera c = Camera.open(i);
				setCameraDisplayOrientation(inf, c);
				c.setErrorCallback(new ErrorCallback() {

					public void onError(int error, Camera camera) {
						// TODO Auto-generated method stub
						switch (error) {
						case Camera.CAMERA_ERROR_SERVER_DIED:
						case Camera.CAMERA_ERROR_UNKNOWN:
							stopCamera();
							startCamera();
							break;
						}
					}

				});
				return c;
			}
		}
		return null;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}

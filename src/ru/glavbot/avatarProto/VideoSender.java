package ru.glavbot.avatarProto;

import java.io.ByteArrayOutputStream;
//import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
//import java.text.SimpleDateFormat;
//import java.util.List;


import android.content.Context;

import android.graphics.ImageFormat;
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

public class VideoSender extends Thread{

	private Context context;
	private SurfaceView preview;
	private VideoView foreignStream;
	
	private HandlerBuffer[] dataBuff;
	private static final int PREVIEW_WIDTH=320;
	private static final int PREVIEW_HEIGHT=240;
	private static final int NUM_FRAMES=30;
	private static final int NUM_BUFFERS=5; 
	private String token;
	
	Object sync= new Object();
	
	
	
	
	
	VideoSender (final Context context, SurfaceView preview)
	{
		this.context=context;
		this.preview=preview;
		//this.foreignStream=foreignStream;
		//this.setToken(token);

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

	 public void run() {

		 synchronized(sync)
		 {
	        Looper.prepare();
	        
	        mChildHandler = new Handler() {

	        	boolean isRunning = false;
	        	Socket socket = null;
	        	private int counter = 0;
	        	private String hostname; 
	        	
	        	private void processFrame(Message msg)
	        	{
	        		boolean reconnect=false;
	        		if((!isRunning)||(socket == null)||(!socket.isConnected()))
	        		{
	        			Log.e("senderThread.processFrame","Sending video to uninitialized socket");
	        			reconnect=true;
	        		}
	        		else
	        		{
	        		HandlerBuffer data = (HandlerBuffer)msg.obj;
	        		data.lock();
					YuvImage img = new YuvImage(data.getData(),ImageFormat.NV21,PREVIEW_WIDTH,PREVIEW_HEIGHT,null);
					
					ByteArrayOutputStream os= new ByteArrayOutputStream();
					//Bitmap m;
					
					
					/*boolean result=	*/img.compressToJpeg(new Rect(0,0,PREVIEW_WIDTH-1,PREVIEW_HEIGHT-1), 50, os);
				
					//Date d2 = new Date();
					//Date d1=data.d;
					//Log.v("",String.format("time to convert: %d", d2.getTime()-d1.getTime()));
					data.unlock();

					String s =String.format(
							"--boundarydonotcross"+eol+
							"Content-Type: image/jpeg"+eol+
							"Content-Length: %d"+eol+eol,os.size());
					try {
						OutputStream socketOutputStream = socket.getOutputStream();
						//socketOutputStream.flush();
						socketOutputStream.write(s.getBytes());
						socketOutputStream.write(os.toByteArray());
						socketOutputStream.write(eol.getBytes());
						socketOutputStream.flush();
						
					//Date d3 = new Date();
					//Log.v("",String.format("time to send: %d", d3.getTime()-d2.getTime()));
					//Log.v("",String.format("time total: %d", d3.getTime()-d1.getTime()));
					
					} 
					catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("","",e);
						reconnect=true;
						
						
					}
					/*counter++;
					if(counter>NUM_FRAMES)
					{
						System.gc();
						counter = 0;
					}*/
	        		}
	        		if(isRunning&&reconnect)
	        		{
	        			initializeSocket(hostname);
	        		}
	        	}
	        	
	        	private void initializeSocket(String hostname)
	        	{
	        		
	        			this.hostname=hostname;
						InetAddress addr=null;
						try {
							addr = InetAddress.getByName(hostname);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							Log.e("","",e);
							
						}
						catch (Exception e) {
							// TODO Auto-generated catch block
							Log.e("","",e);
						}
						try {
							socket = new Socket(addr, SERVER_VIDEO_PORT);
							socket.setKeepAlive(true);
							//socket.setTcpNoDelay(true);
							socket.setSoTimeout(100000);
							isRunning=true;
							OutputStream s = socket.getOutputStream();
							String ident = "ava-"+token;
							String header = /*String.format(
							"POST /restreamer?oid=%s HTTP/1.1"+eol
							+"Server: %s:%d"+eol
							+"User-Agent: avatar/0.2"+eol
							+"Content-Type: multipart/x-mixed-replace; boundary=--boundarydonotcross"+eol
							+eol
							+eol
							, */ident/*,hostname,SERVER_VIDEO_PORT)*/;
							s.write(header.getBytes());
						} 
						catch (Exception e) {
							// TODO Auto-generated catch block
							Log.e(this.getClass().getName(),this.toString(),e);
							
							this.obtainMessage(INITIALIZE_VIDEO_SOCKET).sendToTarget();
							isRunning=false;
						}
			
	        	}
	        	private void closeSocket()
	        	{
	        		
	        		if(socket!=null)
	        		{
	        			try {
							socket.close();
						} catch (IOException e) {
							Log.e("","",e);
						}
	        		}
	        		socket = null;
					isRunning=false;
					if(mChildHandler.hasMessages(PROCESS_FRAME))
					{
						mChildHandler.removeMessages(PROCESS_FRAME);
					}
					if(mChildHandler.hasMessages(INITIALIZE_VIDEO_SOCKET))
					{
						mChildHandler.removeMessages(INITIALIZE_VIDEO_SOCKET);
					}
			
	        	}
	        	
	        	
	            public void handleMessage(Message msg) {
	            	
	            	switch (msg.what)
	            	{
	            		case PROCESS_FRAME: 
	            			processFrame(msg);
	            			break;
	            		case INITIALIZE_VIDEO_SOCKET:
	            			initializeSocket((String)msg.obj);
	            			break;
	            		case CLOSE_VIDEO_SOCKET:
	            			closeSocket();
	            			break;
	            		default:
	            			throw new RuntimeException("Unknown command to video writer thread");
	            	};
					
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
	
	

	
	 private  void setCameraDisplayOrientation(
			 Camera.CameraInfo info, Camera camera) {

	     int rotation = ((Activity)context).getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing*/
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }

//	 public static final int MEDIA_TYPE_IMAGE = 1;
//	 public static final int MEDIA_TYPE_VIDEO = 2;

	 /** Create a file Uri for saving an image or video */
/*	 private static Uri getOutputMediaFileUri(int type){
	       return Uri.fromFile(getOutputMediaFile(type));
	 }*/

	 /** Create a File for saving an image or video */
/*	 private File getOutputMediaFile(int type){
	     // To be safe, you should check that the SDCard is mounted
	     // using Environment.getExternalStorageState() before doing this.
		 File mediaStorageDir;
		 if (type == MEDIA_TYPE_IMAGE){
			 mediaStorageDir= new File(Environment.getExternalStoragePublicDirectory(
	               Environment.DIRECTORY_PICTURES), "MyCameraApp");
		 }
		 else
		if(type == MEDIA_TYPE_VIDEO) { 
		
		 mediaStorageDir= new File(Environment.getExternalStoragePublicDirectory(
	               Environment.DIRECTORY_MOVIES), "MyCameraApp");
		}
	 	else
		 return null;
	     // This location works best if you want the created images to be shared
	     // between applications and persist after your app has been uninstalled.

	     // Create the storage directory if it does not exist
	     if (! mediaStorageDir.exists()){
	         if (! mediaStorageDir.mkdirs()){
	             Log.d("MyCameraApp", "failed to create directory");
	             return null;
	         }
	     }

	     // Create a media file name
	     String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    
	     String filename;
	     if (type == MEDIA_TYPE_IMAGE){
	    	 filename = mediaStorageDir.getPath() + File.separator +
	         "IMG_"+ timeStamp + ".jpg";
	     } else if(type == MEDIA_TYPE_VIDEO) {
	    	 filename = mediaStorageDir.getPath() + File.separator +
	         "VID_"+ timeStamp + ".mp4";
	     } else {
	         return null;
	     }
	     File mediaFile=new File(filename);
	     return mediaFile;
	 }
	*/

	 private Handler mChildHandler=null;
	 
	 public Handler getVideoHandler(){return mChildHandler;};
	 
	 private static final String eol = "\r\n"; //for http should be so //System.getProperty("line.separator"); 
	 
	 private static final int INITIALIZE_VIDEO_SOCKET=0;
	 private static final int PROCESS_FRAME=1;
	 private static final int CLOSE_VIDEO_SOCKET=2;
	 
	 private static final int SERVER_VIDEO_PORT = 10000;
	 
	 
	protected void  finalize()
	{
		if(mChildHandler!=null)
		{
			mChildHandler.getLooper().quit();
		}
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			
		}
	}

	
	private void setupCamera(Camera camera)
	{
		Camera.Parameters p = camera.getParameters();
		//List<Integer> formats =p.getSupportedPreviewFormats () ;
		p.setPreviewFormat (ImageFormat.NV21);
		p.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		p.setPreviewFrameRate(NUM_FRAMES);
	//	List<Size> mSupportedPreviewSizes = p.getSupportedPreviewSizes();
		//p.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		camera.setParameters(p);
		
		int size = PREVIEW_WIDTH*PREVIEW_HEIGHT*ImageFormat.getBitsPerPixel(ImageFormat.NV21);
		for(int i = 0; i<NUM_BUFFERS;i++)
		{
			camera.addCallbackBuffer(new byte[size]);
		}
		dataBuff= new HandlerBuffer[2];
		
		dataBuff[0]=new HandlerBuffer(size);
		dataBuff[1]=new HandlerBuffer(size);
		
		//int format = p.getPictureFormat();
		camera.setPreviewCallbackWithBuffer(new PreviewCallback()
		{

			public void onPreviewFrame(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				
				
				if(mChildHandler.hasMessages(PROCESS_FRAME))
				{
					mChildHandler.removeMessages(PROCESS_FRAME);
				}
				HandlerBuffer ourBuff=dataBuff[0].isLocked()?dataBuff[1]:dataBuff[0];
				ourBuff.lock();
				//byte[] anotherImg = data.clone();
				ourBuff.setData(data);
				ourBuff.unlock();
				
				
				Message msg = mChildHandler.obtainMessage(PROCESS_FRAME,0,0,ourBuff);
				mChildHandler.sendMessage(msg);
				camera.addCallbackBuffer(data);
			}
			
		});
		
		
	}
	 
	protected void startCamera()
	{
		// start socket;
		Message msg = mChildHandler.obtainMessage(INITIALIZE_VIDEO_SOCKET,0,0,AvatarMainActivity.SERVER_AUTHORITY);
		mChildHandler.sendMessage(msg);	
		
		// now camera;
		frontCamera = getFrontCamera();

		if((frontCamera!=null))
		{

			preview.setVisibility(View.VISIBLE);
			SurfaceHolder holder =preview.getHolder();
			try
			{
				holder.removeCallback(cameraCallback);
			}
			catch (Exception e)
			{
				Log.e("VideoSender::startCamera","remove callback failed");
				// do nothing
			}
			
			holder.addCallback (cameraCallback); 
			
			
			if(!holder.isCreating())
			{
				startShow(holder);
			}
		}
		
	}
	
	SurfaceHolder.Callback cameraCallback = new SurfaceHolder.Callback (){

		public void surfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			// TODO Auto-generated method stub
			
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			startShow(holder);
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			if(frontCamera!=null)
			{
				try {
					frontCamera.setPreviewDisplay(null);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("","",e);
					
				}
			}
		}
		};
	
	
	private void startShow(SurfaceHolder holder)
	{
		holder.setFixedSize (PREVIEW_WIDTH, PREVIEW_HEIGHT);
		try {
			frontCamera.setPreviewDisplay(holder);
		} catch (IOException e1) {
			Log.e("","",e1);
		}
		
		setupCamera(frontCamera);
		frontCamera.startPreview();
	}
	
	protected void stopCamera()
	{
		Message msg = mChildHandler.obtainMessage(CLOSE_VIDEO_SOCKET);
		mChildHandler.sendMessage(msg);	
		
		if(frontCamera!=null)
		{
			frontCamera.stopPreview();
			preview.setVisibility(View.INVISIBLE);
			releaseCamera();
		}
		
	}
	
   private void releaseCamera(){
       if (frontCamera != null){
       	frontCamera.release();        // release the camera for other applications
       	frontCamera = null;
       }
   }
   
   private Camera getFrontCamera()
	{
		Camera.CameraInfo inf = new Camera.CameraInfo();
		for(int i = 0; i< Camera.getNumberOfCameras(); i++)
		{
			
			Camera.getCameraInfo(i, inf);
			if(inf.facing==Camera.CameraInfo.CAMERA_FACING_FRONT)
			{
				Camera c =Camera.open(i);
				setCameraDisplayOrientation(inf,c);
				c.setErrorCallback(new ErrorCallback(){

					public void onError(int error, Camera camera) {
						// TODO Auto-generated method stub
						switch(error)
						{
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

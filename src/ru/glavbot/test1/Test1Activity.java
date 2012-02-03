package ru.glavbot.test1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.gvsu.masl.asynchttp.ConnectionManager;
import edu.gvsu.masl.asynchttp.ConnectionResponceHandler;
import edu.gvsu.masl.asynchttp.HttpConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Test1Activity extends Activity {
    /** Called when the activity is first created. */
	 private  TextView leftEngineForward;
	 private  TextView leftEngineBackward;
     private  TextView rightEngineForward;
     private  TextView rightEngineBackward;
     private  TextView yawUp;
     private  TextView yawDown;
     private  TextView pitchLeft;
     private  TextView pitchRight;
     private  TextView wave;
     private  ToggleButton startButton;
     private Button sendLinkButton;
     private SurfaceView cameraPreview;
 
     private static final int SEND_CONTROL_LINK_DIALOG = 1001;
     private static final String SHARED_PREFS = "RobotSharedPrefs";
     private static final String SHARED_PREFS_EMAIL = "email";
     private static final String SHARED_PREFS_TTL = "ttl";
     private static final String SHARED_PREFS_TOKEN = "token";    
     
     private static final String SERVER_SCHEME = "http";    
     private static final String SERVER_AUTHORITY = "auth.glavbot.ru"; 
     private static final String SHARE_PATH = "share"; 
     private static final String CMD_PATH = "cmd"; 
     private static final String MACADDR_PARAM = "macaddr";
     private static final String EMAIL_PARAM = "email";
     //email: кому послать ссылку на использование робота?
     private static final String TTL_PARAM = "ttl";
     private static final String TOKEN_PARAM = "token";
     private static final String MODE_PARAM = "mode";
     private static final String MODE_PARAM_VALUE = "read";
     private static final int SERVER_VIDEO_PORT = 10000;

     
     
     private  String email;
     private  String session_token;
     private  int  ttl;
     private boolean isRunning=false;
     
   //  protected PowerManager.WakeLock mWakeLock;



     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
        leftEngineForward= (TextView)findViewById(R.id.LeftEngineForward);
   	 	leftEngineBackward= (TextView)findViewById(R.id.LeftEngineBackward);
     	rightEngineForward= (TextView)findViewById(R.id.RightEngineForward);
     	rightEngineBackward= (TextView)findViewById(R.id.RightEngineBackward);
     	yawUp= (TextView)findViewById(R.id.YawUp);
     	yawDown= (TextView)findViewById(R.id.YawDown);
     	pitchLeft= (TextView)findViewById(R.id.PitchLeft);
     	pitchRight= (TextView)findViewById(R.id.PitchRight);
     	wave= (TextView)findViewById(R.id.Wave);
    	cameraPreview = (SurfaceView)findViewById(R.id.CameraPreview);
    	startButton= (ToggleButton)findViewById(R.id.StartButton);
    	sendLinkButton = (Button)findViewById(R.id.SendLinkButton);
    	sendLinkButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(SEND_CONTROL_LINK_DIALOG);
			}
    		
    	});
		startButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked)
				{
					runCommands();
					isRunning=true;
				}
				else
				{
					ConnectionManager.getInstance().stopCurrent();
					doHangup(true);
					isRunning=false;
				}
			}
			
		});
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        senderThread.start();
        /* This code together with the one in onDestroy() 
         * will make the screen be always on until this Activity gets destroyed. */
      //  final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      // this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
      //  this.mWakeLock.acquire();

    }
    


    @Override
    protected void onPause() {
        super.onPause();
     //   releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
        ConnectionManager.getInstance().stopCurrent();
        

    }

    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
    	email=prefs.getString(SHARED_PREFS_EMAIL, null);
    	ttl=prefs.getInt(SHARED_PREFS_TTL, 0);
    	session_token = prefs.getString( SHARED_PREFS_TOKEN, null);
    	
    }
	
    
    @Override
    public void onDestroy() {
       // this.mWakeLock.release();
        if(mChildHandler!= null)
        {
        	mChildHandler.getLooper().quit();
        }
        releaseCamera();
        super.onDestroy();
    //    releaseMediaRecorder();

    }

    
	EditText emailET;
	EditText timeout;
	@Override
	protected Dialog  onCreateDialog(int id)
	{
		Dialog d= null;
		switch (id)
		{
			case SEND_CONTROL_LINK_DIALOG:
			{
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;

				//Context mContext = getApplicationContext();
				LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.send_invite_dialog,
				                               (ViewGroup) findViewById(R.id.layout_root));


				builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				builder.setTitle("Enter email!");
				alertDialog = builder.create();
				emailET = (EditText) layout.findViewById(R.id.editTextEmail);
				timeout = (EditText) layout.findViewById(R.id.editTextTimeout);
				Button buttonOk = (Button)layout.findViewById(R.id.buttonOk);
				Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
				buttonOk.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						//v.getParent()
						setEmail(emailET.getText().toString());
						String s = timeout.getText().toString();
						Integer iClass=Integer.decode(s);
						int i =iClass.intValue();
						
						setTtl(i);
						if(isRunning)
						{
							startButton.toggle();
						}
						shareRobot();
						alertDialog.dismiss();
					}});
				buttonCancel.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						alertDialog.cancel();
					}});
				d= alertDialog;
			}
		}
		return d;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putString(SHARED_PREFS_EMAIL, email).apply();
		
	}


	public int getTtl() {
		return ttl;
		
	}


	public void setTtl(int ttl) {
		this.ttl = ttl;
		SharedPreferences pref = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
		SharedPreferences.Editor e=		pref.edit();
		e.putInt(SHARED_PREFS_TTL, ttl);
		e.apply();
	}
	



	public void shareRobot() {
		WifiManager wifiMan = (WifiManager) this.getSystemService(
	            Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		String macAddr = wifiInf.getMacAddress();
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SERVER_SCHEME).authority(SERVER_AUTHORITY).path(SHARE_PATH)
		.appendQueryParameter(MACADDR_PARAM, macAddr)
		.appendQueryParameter(EMAIL_PARAM, email)
		.appendQueryParameter(TTL_PARAM, String.format("%d", ttl*60));
		Uri uri=builder.build();
		HttpConnection connection = new HttpConnection(shareConnectionHandler);
		connection.get(uri.toString());
	}
	
	ConnectionResponceHandler shareConnectionHandler = new ConnectionResponceHandler()
	{

		@Override
		protected void onConnectionSuccessful(String responce) {
			// TODO Auto-generated method stub
			try
			{
				JSONObject r = new JSONObject(responce);
				String status = r.getString("status");
				if(status.equalsIgnoreCase("ok"))
				{
					setSession_token(r.getString("token"));
					Toast.makeText(Test1Activity.this, "Invite sent successfully, waiting for commands", Toast.LENGTH_LONG).show();
					startButton.toggle();
				}
				else
				{
					Toast.makeText(Test1Activity.this, "Invite sending failed with message \r"+r.getString("message"), Toast.LENGTH_LONG).show();
				}
			}
			catch(JSONException e)
			{
				Log.e("ConnectionResponceHandler", "onConnectionSuccessful", e);
				Toast.makeText(Test1Activity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
				
			}
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			// TODO Auto-generated method stub
			Toast.makeText(Test1Activity.this, String.format("Server returned %d, try again later",statusCode), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onConnectionFail(Exception e) {
			// TODO Auto-generated method stub
			Toast.makeText(Test1Activity.this, String.format("Connection failed with message %d!",e.getMessage()), Toast.LENGTH_LONG).show();
		
		}

		@Override
		protected void onDataPart(String responce) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	Socket socket = null; 

	ParcelFileDescriptor pfd = null; 

	MediaRecorder recorder =  new MediaRecorder();
	
	protected Camera getFrontCamera()
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
								camera.release();
								reallyStartCamera();
							break;
						}
					}
					
				});
				return c;
			}
		}
		return null;
	}
	
	
	Camera frontCamera;
	
	

	
	 public  void setCameraDisplayOrientation(
			 Camera.CameraInfo info, Camera camera) {

	     int rotation = getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	    /* if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing*/
	         result = (info.orientation - degrees + 360) % 360;
	    /* }*/
	     camera.setDisplayOrientation(result);
	 }

	 public static final int MEDIA_TYPE_IMAGE = 1;
	 public static final int MEDIA_TYPE_VIDEO = 2;

	 /** Create a file Uri for saving an image or video */
/*	 private static Uri getOutputMediaFileUri(int type){
	       return Uri.fromFile(getOutputMediaFile(type));
	 }*/

	 /** Create a File for saving an image or video */
	 private File getOutputMediaFile(int type){
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
	
	 private static final int NUM_FRAMES=15;
	 private Handler mChildHandler;
	 private static final String eol = System.getProperty("line.separator"); 
	 private Thread senderThread = new Thread()
	 {
		 public void run() {

		        /*
		         * You have to prepare the looper before creating the handler.
		         */
		        Looper.prepare();

		        /*
		         * Create the child handler on the child thread so it is bound to the
		         * child thread's message queue.
		         */
		        mChildHandler = new Handler() {

		            public void handleMessage(Message msg) {
		            	byte[] data = (byte[])msg.obj;
						YuvImage img = new YuvImage(data,ImageFormat.NV21,320,240,null);
						ByteArrayOutputStream os= new ByteArrayOutputStream();
						//ByteArrayOutputStream s = new ByteArrayOutputStream();
						
							img.compressToJpeg(new Rect(0,0,320,240), 20, os);
						
						String s =String.format(
								"--boundarydonotcross"+eol+
								"Content-Type: image/jpeg"+eol+
								"Content-Length: %d"+eol+eol,os.size());
						try {
							OutputStream socketOutputStream = socket.getOutputStream();
							socketOutputStream.write(s.getBytes());
							socketOutputStream.write(os.toByteArray());
							socketOutputStream.write(eol.getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("","",e);
							
						}
						
		            }
		        };

		        /*
		         * Start looping the message queue of this thread.
		         */
		        Looper.loop();
		    }
	 };
	 
	 
	 
	protected void setupCamera(Camera camera)
	{
		Camera.Parameters p = camera.getParameters();
		//List<Integer> formats =p.getSupportedPreviewFormats () ;
		p.setPreviewFormat (ImageFormat.NV21);
		p.setPreviewSize(320, 240);
		camera.setParameters(p);
		for(int i = 0; i<NUM_FRAMES;i++)
		{
			camera.addCallbackBuffer(new byte[320*240*ImageFormat.getBitsPerPixel(ImageFormat.NV21)]);
		}
		//int format = p.getPictureFormat();
		camera.setPreviewCallbackWithBuffer(new PreviewCallback()
		{

			public void onPreviewFrame(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				
				byte[] anotherImg = data.clone();
				Message msg = mChildHandler.obtainMessage();
                msg.obj =anotherImg;
                mChildHandler.sendMessage(msg);
				camera.addCallbackBuffer(data);
			}
			
		});
		
		
	}
	 
	protected void reallyStartCamera()
	{

		frontCamera = getFrontCamera();
		if((socket!= null)&&(frontCamera!=null))
		{
			try {
				frontCamera.setPreviewDisplay(cameraPreview.getHolder());
			} catch (IOException e1) {
				Log.e("","",e1);
			}
			setupCamera(frontCamera);
			cameraPreview.setVisibility(View.VISIBLE);
			frontCamera.startPreview();
			
			//recorder =  new MediaRecorder();
			//frontCamera.unlock();
			//frontCamera.unlock();
			//recorder.setCamera(frontCamera);

			//recorder.setAudioChannels(1);
		//	recorder.setAudioSamplingRate(96000);
		//	recorder.setCaptureRate(30);
			//recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			//recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			//recorder.setProfile(CamcorderProfile.get( CamcorderProfile.QUALITY_LOW));
			//recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			//recorder.setVideoSize(640, 480);
			//recorder.setVideoFrameRate(30);
			
			//recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			//recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
			
			//pfd = ParcelFileDescriptor.fromSocket(socket);
			//FileDescriptor f = pfd.getFileDescriptor(); 
			//recorder.setOutputFile(/*getOutputMediaFile(MEDIA_TYPE_VIDEO).toString()*/f);
			//recorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
			//try {
			//	recorder.prepare();
			//	recorder.start();
			//} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
			//	Log.e("","",e);
			//} catch (IOException e) {
				// TODO Auto-generated catch block
			//	Log.e("","",e);
			//}
		}
	}
	
	protected void startCamera()
	{
		 AsyncTask<Void, Void, Socket> startConnection = new  AsyncTask<Void, Void, Socket>()
					{

						@Override
						protected Socket doInBackground(Void... params) {
							// TODO Auto-generated method stub
							InetAddress addr=null;
							try {
								addr = InetAddress.getByName(SERVER_AUTHORITY);
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								
							}
							catch (Exception e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
							}
							try {
								return new Socket(addr, SERVER_VIDEO_PORT);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
								
							}
							catch (Exception e) {
								// TODO Auto-generated catch block
								Log.e("","",e);
							}
							 return null;
						}
						@Override
					    protected void onPostExecute(Socket sock) {
							socket = sock;
							//startConnection.(true);
							reallyStartCamera();
					    }

					};
		startConnection.execute();
	}
	
	protected void stopCamera()
	{
//		recorder.stop();
		if(frontCamera!=null)
		{
			frontCamera.stopPreview();
			cameraPreview.setVisibility(View.INVISIBLE);
			releaseCamera();
			//try {
				// recorder.
			//	recorder.stop();

			//} catch (Exception e) {
				// TODO Auto-generated catch block
				//Log.e("", "", e);

			//}
			// frontCamera.stopPreview();
		/*	try {
				socket.shutdownInput();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("", "", e);

			}*/
		//	releaseMediaRecorder();
		}
	}
	

 //   private void releaseMediaRecorder(){
 //       if (recorder != null) {
 //       	recorder.reset();   // clear recorder configuration
 //       	recorder.release(); // release the recorder object
 //       	recorder = null;
 //           frontCamera.lock();           // lock camera for later use
//        }
//    }

    private void releaseCamera(){
        if (frontCamera != null){
        	frontCamera.release();        // release the camera for other applications
        	frontCamera = null;
        }
    }

	protected void runCommands()
	{
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SERVER_SCHEME).authority(SERVER_AUTHORITY).path(CMD_PATH)
		.appendQueryParameter(TOKEN_PARAM, session_token)
		.appendQueryParameter(MODE_PARAM, MODE_PARAM_VALUE);
		Uri uri=builder.build();
		HttpConnection connection = new HttpConnection(cmdConnectionHandler);
		connection.setTimeout(Integer.MAX_VALUE);
		connection.setPollingMode(true);
		connection.get(uri.toString());
		startCamera();
	}
	

	
	public void drawColor(TextView active,TextView passive,int value)
	{
		long lvalue=value;
		long color = 0xff000000+(lvalue<<16)+(lvalue<<8)+lvalue;
		active.setBackgroundColor( (int)color);
		passive.setBackgroundColor(0xff000000);
		active.invalidate();
		passive.invalidate();
	}
	
	
	public void doLeft(int value)
	{
		//TextView active,passive;
		
		if(value>0)
		{
			drawColor(leftEngineForward,leftEngineBackward,value);

		}
		else
		{
			drawColor(leftEngineBackward,leftEngineForward,-value);
		}
	}

	public void doRight(int value)
	{
		if(value>0)
		{
			drawColor(rightEngineForward,rightEngineBackward,value);

		}
		else
		{
			drawColor(rightEngineBackward,rightEngineForward,-value);
		}
	}
	



	
	
	
	public void doYaw(int value)
	{
		if(value>0)
		{
			drawColor(yawUp,yawDown,value);

		}
		else
		{
			drawColor(yawDown,yawUp,-value);
		}
	}
	public void doPitch(int value)
	{
		if(value>0)
		{
			drawColor(pitchLeft,pitchRight,value);

		}
		else
		{
			drawColor(pitchRight,pitchLeft,-value);
		}
	}
	public void doWave(boolean value)
	{
		wave.setBackgroundColor(value?0xffffffff:0xff000000);
		wave.invalidate();
	}
	public void doHangup(boolean value)
	{
		if(value)
		{
			 leftEngineForward.setBackgroundColor(0xff000000);
			 leftEngineForward.invalidate();
			 leftEngineBackward.setBackgroundColor(0xff000000);
			 leftEngineBackward.invalidate();
		     rightEngineForward.setBackgroundColor(0xff000000);
		     rightEngineForward.invalidate();
		     rightEngineBackward.setBackgroundColor(0xff000000);
		     rightEngineBackward.invalidate();
		     yawUp.setBackgroundColor(0xff000000);
		     yawUp.invalidate();
		     yawDown.setBackgroundColor(0xff000000);
		     yawDown.invalidate();
		     pitchLeft.setBackgroundColor(0xff000000);
		     pitchLeft.invalidate();
		     pitchRight.setBackgroundColor(0xff000000);
		     pitchRight.invalidate();
		     wave.setBackgroundColor(0xff000000);
		     wave.invalidate();
		     stopCamera();
		}
		else
		{
		//	runCommands();
		}
	}
	/*
	 *      ажда€ строка может содержать в себе JSON-список (далее УпакетФ) с одним или более из следующих полей: left, right, yaw, pitch, wave, hangup.
    left, right, yaw, pitch принимают значени€ от -255 до 255; каждой команде соответствуют две площадки размером 1х1см на активити приложени€. ќбработка этих полей идет по одинаковой схеме: если значение пол€ n >= 0, погасить вторую площадку и установить цвет первой площадки в #nnnnnn; в противном случае погасить первую площадку и установить цвет второй площадки в #nnnnnn.

	 оманда ѕерва€ площадка ¬тора€ площадка
	left	#1				#2
	right	#3				#4
	yaw		#5				#6
	pitch	#7				#8
	 оманда wave обрабатываетс€ иначе: в случае ее наличи€ в пакете нужно установить цвет площадки #9 в #FFFFFF, в случае ее отсутстви€ - в #000000.
	¬ случае наличи€ в пакете команды hangup цвета всех площадок сбрасываютс€ в #000000 и дальнейшее ожидание ввода не производитс€ (можно закрыть сокет).
*/
	
	ConnectionResponceHandler cmdConnectionHandler = new ConnectionResponceHandler()
	{

		@Override
		protected void onConnectionSuccessful(String responce) {
			// TODO Auto-generated method stub


				doHangup(true);


			//runCommands();
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			// TODO Auto-generated method stub
			runCommands();
		}

		@Override
		protected void onConnectionFail(Exception e) {
			// TODO Auto-generated method stub
			//if(isRunning)
			//{
				Toast.makeText(Test1Activity.this, String.format("Connection failed with message %s!",e.getMessage()), Toast.LENGTH_LONG).show();
				//startButton.toggle();
			//}
		}

		@Override
		protected void onDataPart(String responce) {
			// TODO Auto-generated method stub
			try
			{
				JSONObject r = new JSONObject(responce);
				if (r.has("left"))
				{
					doLeft(r.getInt("left"));
				}
				if (r.has("right"))
				{
					doRight(r.getInt("right"));
				}
				if (r.has("yaw"))
				{
					doYaw(r.getInt("yaw"));
				}
				if (r.has("pitch"))
				{
					doPitch(r.getInt("pitch"));
				}
				doWave(r.has("wave"));
				doHangup(r.has("hangup"));
				
			}
			catch(JSONException e)
			{
				Log.e("ConnectionResponceHandler", "onConnectionSuccessful", e);
				//Toast.makeText(Test1Activity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
			}
		}
		
	};
	
	

	public String getSession_token() {
		return session_token;
	}

	public void setSession_token(String session_token) {
		this.session_token = session_token;
		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putString(SHARED_PREFS_TOKEN, session_token).apply();
	}

}
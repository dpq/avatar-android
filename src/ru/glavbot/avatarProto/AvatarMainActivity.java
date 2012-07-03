package ru.glavbot.avatarProto;



//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;



import org.json.JSONException;
import org.json.JSONObject;


import ru.glavbot.AVRestreamer.AudioReceiver;
import ru.glavbot.AVRestreamer.AudioSender;
import ru.glavbot.AVRestreamer.OnScreenLogger;
import ru.glavbot.AVRestreamer.VideoReceiver;
import ru.glavbot.AVRestreamer.VideoSender;
import ru.glavbot.asyncHttpRequest.ConnectionManager;
import ru.glavbot.asyncHttpRequest.ConnectionRequest;
import ru.glavbot.asyncHttpRequest.ProcessAsyncRequestResponceProrotype;
import ru.glavbot.avatarProto.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;

import android.util.Log;

//import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toast;


public class AvatarMainActivity extends AccessoryProcessor {
	
	private static final String TAG= "AvatarMainActivity";
	boolean DEBUG=true;
    /** Called when the activity is first created. */
     private Button startButton;
     private Button pauseButton;
     private Button settingsButton;
     private Button sendLinkButton;
     private Button resumeButton;
     private Button stopButton;
     private Button volumeButton;
     private SurfaceView cameraPreview;
     private SurfaceView videoView;
     private FrameLayout frameLayoutRun;
     private RelativeLayout relativeLayoutStart;
     
     
     private VideoSender videoSender;
     private AudioSender audioSender;
     private AudioReceiver audioReceiver;
     private VideoReceiver videoReceiver;
 
	  private SensorManager mSensorManager;
	  private Sensor mLuxmeter;
     private String gatewayIp;
     
     private static final int SEND_CONTROL_LINK_DIALOG = 1001;
     private static final int CONFIGURE_SERVER_DIALOG = 1002;
     private static final int VOLUME_REGULATION_DIALOG = 1003;
     
     
     
     private static final String SHARED_PREFS = "RobotSharedPrefs";
     private static final String SHARED_PREFS_EMAIL = "email";
     private static final String SHARED_PREFS_TTL = "ttl";
     private static final String SHARED_PREFS_TOKEN = "token"; 
     
     
     private static final String SERVER_SCHEME = "http";    

     private static final String SHARE_PATH = "share"; 
     private static final String CMD_PATH = "cmd"; 
     private static final String RESTREAMER_PATH = "restreamer"; 
     private static final String MACADDR_PARAM = "macaddr";
     private static final String EMAIL_PARAM = "email";
	 
     //email: ���� ������� ������ �� ������������� ������?
     private static final String TTL_PARAM = "ttl";
     private static final String TOKEN_PARAM = "token";
     private static final String MODE_PARAM = "mode";
     private static final String MODE_PARAM_VALUE = "read";
     
     private static final String SERVER_AUTHORITY_PARAM = "serverAuthority";
     private static final String SERVER_HTTP_PORT_PARAM = "serverHttpPort";
     private static final String VIDEO_PORT_OUT_PARAM = "videoPortOut";
     private static final String AUDIO_PORT_IN_PARAM = "audioPortIn";
     private static final String AUDIO_PORT_OUT_PARAM = "audioPortOut";
     
     private static final String WHEEL_ANGLE_1 = "WheelAngle1";
     private static final String WHEEL_ANGLE_2 = "WheelAngle2";
     private static final String WHEEL_ANGLE_3 = "WheelAngle3";
     
     private static final String WHEEL_DIR_1 = "WheelDir1";
     private static final String WHEEL_DIR_2 = "WheelDir2";
     private static final String WHEEL_DIR_3 = "WheelDir3";
     
     
     private String serverAuthority = "auth.glavbot.ru"; 
     private String serverHttpPort = "1018";
     private int videoPort = 5001;
     private int audioPortIn = 10002;
     private int audioPortOut = 10003;
     ToastBuilder toastBuilder = new ToastBuilder(this);
     private static final float STOPITSOT = 100500; 
     
     
     private  String email;
     private  String session_token;
     private  int  ttl;
    // private boolean turnedOn=false;
     
 //	private WebView webView;
 	private ConnectivityManager network;
 	private ConnectionManager protocolManager;
 	private RoboDriver driver;
 	boolean isNetworkAvailable=false;

 	

 	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
/*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());*/
        }
        

		
        protocolManager= new ConnectionManager();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
/*
        try {
        	Log.v(TAG, "Asking root permission");
        	Process root = Runtime.getRuntime().exec("su");
        	Log.v(TAG, "Root permission gained!");
        	} catch (IOException e) {
        	Log.e(TAG, "Impossible to get the root access... Quitting");
        	e.printStackTrace();

        	}

*/
        
        
        setContentView(R.layout.main);
        
        TextView statusText = (TextView) findViewById(R.id.StatusText);
		logger = OnScreenLogger.init(statusText);
        
        videoView= (SurfaceView)findViewById(R.id.videoView);
    	cameraPreview = (SurfaceView)findViewById(R.id.CameraPreview);
		settingsButton=(Button)findViewById(R.id.SettingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(CONFIGURE_SERVER_DIALOG);
			}
		});
    	startButton= (Button)findViewById(R.id.StartButton);

		startButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
					if(isNetworkAvailable)
					{
						setCurrentState(STATE_ON);
					}
					else
					{
						toastBuilder.makeAndShowToast(R.string.toastNoWifi, ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
					}
			}
		});
		
    	pauseButton= (Button)findViewById(R.id.PauseButton);
	pauseButton.setOnClickListener(new OnClickListener(){

		public void onClick(View v) {
		setCurrentState(STATE_PAUSED);
		}
	});
    	
    	
	    volumeButton=(Button)findViewById(R.id.VolumeButton);
	    volumeButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
					showDialog(VOLUME_REGULATION_DIALOG);
			}
    		
    	});
	    
    	
    	relativeLayoutStart = (RelativeLayout)findViewById(R.id.relativeLayoutStart);
    	frameLayoutRun = (FrameLayout)findViewById(R.id.frameLayoutRun);
    	
    	
    	sendLinkButton = (Button)findViewById(R.id.SendLinkButton);
    	sendLinkButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isNetworkAvailable)
				{
					showDialog(SEND_CONTROL_LINK_DIALOG);
				}
				else
				{
					toastBuilder.makeAndShowToast(R.string.toastNoWifi, ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
				}
			}
    		
    	});
    	resumeButton= (Button)findViewById(R.id.ResumeButton);
    	resumeButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isNetworkAvailable)
				{
					setCurrentState(STATE_ON);
				}
				else
				{
					toastBuilder.makeAndShowToast(R.string.toastNoWifi, ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
				}
			}
    		
    	});
    	
    	

		stopButton = (Button)findViewById(R.id.StopButton);
		stopButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				setCurrentState(STATE_OFF);
			}});
		
		
		videoReceiver = new VideoReceiver(videoView);
        videoSender = new VideoSender(this, cameraPreview);
        audioSender = new AudioSender();
        audioReceiver= new AudioReceiver();
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLuxmeter = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        
        driver= new RoboDriver(this);
    }
    
    SensorEventListener sensorEventListener = new SensorEventListener(){

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if(currentState>STATE_ON)
				driver.updateLuxmeterValue(event.values[0]);
			else
				driver.updateLuxmeterValue(STOPITSOT);
		}
    	
    };
    
    
    
    boolean isListeningNetwork=false;

    private void processNetworkState()
    {
        if(isNetworkAvailable)
        {
        	setCurrentState(Math.abs(currentState));
        	WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE); 
        	DhcpInfo info = wifi.getDhcpInfo();
        	gatewayIp=android.text.format.Formatter.formatIpAddress(info.gateway);
        }
        else
        {
        	setCurrentState(-Math.abs(currentState));
        	gatewayIp="";
        	driver.reset();
        }
    }
    
    
    BroadcastReceiver networkStateListener = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(BroadcastReceiver.class.getSimpleName(), "action: "
                    + intent.getAction());
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
            isNetworkAvailable =state == WifiManager.WIFI_STATE_ENABLED;
            processNetworkState();
        }

    };
    
    public void startListeningNetwork() {
    	if(!isListeningNetwork)
    	{
    		IntentFilter filter = new IntentFilter();
    		filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
    		this.registerReceiver(networkStateListener, filter);
    		isListeningNetwork=true;
    	}
    }

    public void stopListeningNetwork() {
    	if(isListeningNetwork)
    	{
    		this.unregisterReceiver(networkStateListener);
    		isListeningNetwork=false;
    	}
    }  
    
    private void disableAll()
    {
    	stopCommands();
    	stopStreaming();
    	setWelcomeScreen();
    }
    
    boolean isRunning=false;
    private void doResume()
    {
    	setCurrentState(currentState);
    }
   

 	protected static final int STATE_OFF=0;
 	protected static final int STATE_PAUSED=1;
	protected static final int STATE_PAUSED_NO_NETWORK=-1;	
 	protected static final int STATE_ON=2;
 	protected static final int STATE_ON_NO_NETWORK=-2;
 	protected static final int STATE_ENABLED=3;
 	protected static final int STATE_ENABLED_NO_NETWORK=-3;
 	
 	protected int currentState=0;
 	
 	protected void setWelcomeScreen()
 	{
 		frameLayoutRun.setVisibility(View.GONE);
 		relativeLayoutStart.setVisibility(View.VISIBLE);
 	}
 	protected void setWorkerScreen()
 	{
 		frameLayoutRun.setVisibility(View.VISIBLE);
 		relativeLayoutStart.setVisibility(View.GONE);
 	}
 	
 	protected void showProperButton(int state)
 	{
 		if(Math.abs(state)>=STATE_ON)
 		{
 			startButton.setVisibility(View.GONE);
 			pauseButton.setVisibility(View.VISIBLE);
 		}
 		else
 		{
 			startButton.setVisibility(View.VISIBLE);
 			pauseButton.setVisibility(View.GONE);
 		}
 		
 	}
 	
 	protected void setCurrentState(int newState)
 	{
 		int prevState=currentState;
 		currentState= newState;
 		showProperButton(newState);
 		switch(newState)
 		{
 			case STATE_OFF:
 				disableAll();
 				break;
 			case STATE_ENABLED:
 				runStreaming();
 			case STATE_ON:
 				runCommands();
 				if(prevState>currentState)
 				{
 					driver.reset();
 					stopStreaming();
 				}
 				setWorkerScreen();
 				break;
 			case STATE_PAUSED:
 				if(prevState>currentState)
 				{
 					stopStreaming();
 					stopCommands();
 					
 					
 				}
 				setWorkerScreen();
 				driver.updateLuxmeterValue(STOPITSOT);
 				break;
 			case STATE_ENABLED_NO_NETWORK:
 				stopStreaming();
 			case STATE_ON_NO_NETWORK:
 				stopCommands();
 				driver.updateLuxmeterValue(STOPITSOT);
 			case STATE_PAUSED_NO_NETWORK: 				
 				break;

 		}

 		
 		
 	}
 	boolean commandsRunning = false;
	protected void runCommands()
	{
		if(!commandsRunning)
		{
			Uri.Builder builder = new Uri.Builder();
			builder.path(CMD_PATH)
			.appendQueryParameter(TOKEN_PARAM, session_token)
			.appendQueryParameter(MODE_PARAM, MODE_PARAM_VALUE);
			Uri uri=builder.build();
			String realAddress = SERVER_SCHEME+"://"+serverAuthority+":"+serverHttpPort+"/"+uri.toString();
			ConnectionRequest req= new ConnectionRequest(ConnectionRequest.GET, realAddress);
			req.setAnswerProcessor(cmdConnectionResponse);
			req.setProgressProcessor(cmdConnectionResponse);
			req.setProcessingType(ConnectionRequest.READ_STRINGS_ONE_BY_ONE);
			protocolManager.push(req);
			commandsRunning=true;
		}
	}
	
	protected void stopCommands()
	{
		if(commandsRunning)
		{
			protocolManager.stopCurrent();
			driver.reset();
			commandsRunning=false;
			
		}
	}
	protected void reRunCommands()
	{
		if(currentState>STATE_OFF)
		{
			commandsRunning=false;	
			driver.reset();
			runCommands();
		}
	}
	
    
	boolean streamsRunning=false;
	public void stopStreaming()
	{
		if(streamsRunning)
		{
			videoSender.stopCamera();
			videoReceiver.stopReceiveVideo();
			audioSender.stopVoice();
			audioReceiver.stopVoice();
			ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,"http://"+gatewayIp+":6000/stop");
			r.setTimeout(1000);
			bottomCameraStreamManager.push(r);
			streamsRunning=false;
		}
	}
    
	protected void runStreaming()
    {
		if(!streamsRunning)
		{
			videoSender.startCamera();
			
			audioReceiver.startVoice();
			audioSender.startVoice();
			videoReceiver.startReceiveVideo();
			ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,"http://"+gatewayIp+":6000/start?oid=dwn_"+getSession_token());
			r.setTimeout(1000);
			bottomCameraStreamManager.push(r);
			streamsRunning=true;
		}
    }
	
	ConnectionManager bottomCameraStreamManager = new ConnectionManager();
	
	ProcessAsyncRequestResponceProrotype bottomCameraStreamResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			// TODO Auto-generated method stub
				String answer = (String)responce;
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
		
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			
		}

		@Override
		protected void onConnectionFail(Throwable e) {
		
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
		}
		
	};
	
	
    
    @Override
    protected void onPause() {
        super.onPause();
        
       disableAll();
       stopListeningNetwork();
       mSensorManager.unregisterListener(sensorEventListener);
       AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
		if(audiomanager.getMode()!=AudioManager.MODE_NORMAL)
		{
			//audiomanager.setSpeakerphoneOn(false);
			audiomanager.setMode(AudioManager.MODE_NORMAL);
		}
    }
    

    int[] angles = new int[3];
    int[] dirs = new int[3];
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
    	email=prefs.getString(SHARED_PREFS_EMAIL, null);
    	ttl=prefs.getInt(SHARED_PREFS_TTL, 0);
    	session_token = prefs.getString( SHARED_PREFS_TOKEN, null);
    	
   

        
        serverAuthority = prefs.getString( SERVER_AUTHORITY_PARAM, "auth.glavbot.ru");
        serverHttpPort = prefs.getString( SERVER_HTTP_PORT_PARAM, "8080");//"1017";
        videoPort =prefs.getInt( VIDEO_PORT_OUT_PARAM, 5001);
        audioPortIn = prefs.getInt( AUDIO_PORT_IN_PARAM, 10002);
        audioPortOut = prefs.getInt( AUDIO_PORT_OUT_PARAM, 10003);
    	
        
        angles[0]=prefs.getInt( WHEEL_ANGLE_1, 0);
        angles[1]=prefs.getInt( WHEEL_ANGLE_2, 0);
        angles[2]=prefs.getInt( WHEEL_ANGLE_3, 0);
        driver.setCompensationAngles(angles);
        
        dirs[0]=prefs.getInt( WHEEL_DIR_1, 1);
        dirs[1]=prefs.getInt( WHEEL_DIR_2, 1);
        dirs[2]=prefs.getInt( WHEEL_DIR_3, 1);
        driver.setWheelDirs(dirs);

        
        writeTokenToWorkers(session_token);
    	setPortsAndHosts();
    	startListeningNetwork();
    	
		AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
		if(audiomanager.getMode()!=AudioManager.MODE_IN_COMMUNICATION)
		{
			audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
			//audiomanager.setSpeakerphoneOn(true);
			int maxVoice = audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
			audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVoice, 0);
		}
		mSensorManager.registerListener(sensorEventListener, mLuxmeter, SensorManager.SENSOR_DELAY_GAME);
   		doResume();
    }
	
    public void setPortsAndHosts()
    {
    	videoReceiver.setAddress(serverAuthority, videoPort);
    	audioSender.setHostAndPort(serverAuthority, audioPortOut);
    	audioReceiver.setHostAndPort(serverAuthority, audioPortIn);
    	videoSender.setHostAndPort(serverAuthority, videoPort);
    }
    
    
    
    @Override
    public void onDestroy() {
    	disableAll();
    	stopListeningNetwork();
        super.onDestroy();
    }

    
	EditText emailET;

	SeekBar timeSelect;
	TextView textTimeout;
	
	
	EditText editTextServer;
	EditText editTextServerPort;
	EditText editTextVideoOutPort;
	EditText editTextAudioOutPort;
	EditText editTextAudioInPort;
	OnScreenLogger logger;
	EditText editTextWheel1Angle;
	EditText editTextWheel2Angle;
	EditText editTextWheel3Angle;
	SeekBar  volumeSelect;
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

				LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.send_invite_dialog,
				                               (ViewGroup) findViewById(R.id.layout_root));

				builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				builder.setTitle(R.string.sendLinkDlgHeader);
				alertDialog = builder.create();
				emailET = (EditText) layout.findViewById(R.id.editTextEmail);
				emailET.setText(getEmail());
				textTimeout = (TextView)layout.findViewById(R.id.text_timeout);

				timeSelect= (SeekBar) layout.findViewById(R.id.seekBarLength);
				timeSelect.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					public void onStopTrackingTouch(SeekBar seekBar) {}
					
					public void onStartTrackingTouch(SeekBar seekBar) {}
					
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						// TODO Auto-generated method stub
						textTimeout.setText(getResources().getString(R.string.sendLinkDlgExpires,progress+1));
					}
				});
				textTimeout.setText(getResources().getString(R.string.sendLinkDlgExpires,1));
				
				Button buttonOk = (Button)layout.findViewById(R.id.buttonOk);
				Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
				buttonOk.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {

						setEmail(emailET.getText().toString());
						long ttl = (timeSelect.getProgress()+1)*60*60;
						if(ttl<0){ttl=0;}
						try{
							setTtl((int) ttl);
						}catch(NumberFormatException e)
						{
							Log.e("AvatarMainActivity", "AlertDialog.buttonOk.onClick", e);
							setTtl(0);
						}
						shareRobot();
						alertDialog.dismiss();
						/*if(turnedOn)
						{
							startButton.toggle();
						}*/
						//setCurrentState(STATE_ON);
						
					}});
				buttonCancel.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						alertDialog.cancel();
					}});
				d= alertDialog;
				break;
			}
			case CONFIGURE_SERVER_DIALOG:
			{
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;

				LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.configure_server_dialog,
				                               (ViewGroup) findViewById(R.id.layout_config_server_root));

				builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				builder.setTitle(R.string.configSrvDlgHeader);
				alertDialog = builder.create();
				
				editTextServer=(EditText) layout.findViewById(R.id.editTextServer);
				editTextServerPort=(EditText) layout.findViewById(R.id.editTextServerPort);
				editTextVideoOutPort=(EditText) layout.findViewById(R.id.editTextVideoOutPort);
				editTextAudioOutPort=(EditText) layout.findViewById(R.id.editTextAudioOutPort);
				editTextAudioInPort=(EditText) layout.findViewById(R.id.editTextAudioInPort);
				
				editTextWheel1Angle=(EditText) layout.findViewById(R.id.editTextWheel1Angle);
				editTextWheel1Angle.setText(String.format("%d", angles[0]));
				editTextWheel2Angle=(EditText) layout.findViewById(R.id.editTextWheel2Angle);
				editTextWheel2Angle.setText(String.format("%d", angles[1]));
				editTextWheel3Angle=(EditText) layout.findViewById(R.id.editTextWheel3Angle);
				editTextWheel3Angle.setText(String.format("%d", angles[2]));
				Button buttonOk = (Button)layout.findViewById(R.id.buttonOk);
				Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
				buttonOk.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						
						String s = editTextServer.getText().toString();
						if(s.length()==0)
						{
							s = "dev.glavbot.ru";
						}
						serverAuthority=s;
						s =editTextServerPort.getText().toString();
						if(s.length()==0)
						{
							s = "8080";
						}
						serverHttpPort=s;
						
						s =editTextVideoOutPort.getText().toString();
						if(s.length()==0)
						{
							s = "10000";
						}
						videoPort=Integer.decode(s);
						s =editTextAudioInPort.getText().toString();
						if(s.length()==0)
						{
							s = "10003";
						}
						audioPortIn=Integer.decode(s);
						s =editTextAudioOutPort.getText().toString();
						if(s.length()==0)
						{
							s = "10002";
						}
						audioPortOut=Integer.decode(s);
						s=editTextWheel1Angle.getText().toString();
						if(s.length()==0)
						{
							s = "0";
						}
						angles[0]=Integer.decode(s);
						s=editTextWheel2Angle.getText().toString();
						if(s.length()==0)
						{
							s = "0";
						}
						angles[1]=Integer.decode(s);
						s=editTextWheel3Angle.getText().toString();
						if(s.length()==0)
						{
							s = "0";
						}
						angles[2]=Integer.decode(s);
						//driver.setCompensationAngles(angles);
						SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
						SharedPreferences.Editor  editor = prefs.edit();
						editor.putString(SERVER_AUTHORITY_PARAM, serverAuthority);
						editor.putString(SERVER_HTTP_PORT_PARAM, serverHttpPort);
						editor.putInt(VIDEO_PORT_OUT_PARAM, videoPort);
						editor.putInt(AUDIO_PORT_IN_PARAM, audioPortIn);
						editor.putInt(AUDIO_PORT_OUT_PARAM, audioPortOut);
					    editor.putInt( WHEEL_ANGLE_1,  angles[0]);  
					    editor.putInt( WHEEL_ANGLE_2,  angles[1]);   
					    editor.putInt( WHEEL_ANGLE_3,  angles[2]);
					    driver.setCompensationAngles(angles);
				    	editor.apply();
				    	setPortsAndHosts();
				    	alertDialog.dismiss();
						
					}});
				buttonCancel.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						alertDialog.cancel();
					}});
				d= alertDialog;
				break;
			}
			case VOLUME_REGULATION_DIALOG:
			{
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;

				LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.volume_regulation_dialog,
				                               (ViewGroup) findViewById(R.id.volumeRegulationDialogRoot));
				builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				builder.setTitle(R.string.volumeRegulationDlgHeader);
				alertDialog = builder.create();
				Button buttonOk = (Button)layout.findViewById(R.id.buttonOk);
				buttonOk.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						alertDialog.cancel();
					}});
				volumeSelect=(SeekBar)layout.findViewById(R.id.seekBarVolume);
				volumeSelect.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					public void onStopTrackingTouch(SeekBar seekBar) {}
					
					public void onStartTrackingTouch(SeekBar seekBar) {}
					
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						if(fromUser)
						{
							AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
							audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, 0);
						}
					}
				});
				d= alertDialog;
			}
		}
		return d;
	}
/*
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	if(currentState == STATE_OFF )
	    	{
	    		showDialog(CONFIGURE_SERVER_DIALOG);
	    	}
	    	return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
*/
	
	protected void onPrepareDialog (int id, Dialog dialog)
	{
		if(id==CONFIGURE_SERVER_DIALOG)
		{
			editTextServer.setText(serverAuthority);
			editTextServerPort.setText(serverHttpPort);
			editTextVideoOutPort.setText(String.format("%d", videoPort));
			editTextAudioOutPort.setText(String.format("%d", audioPortOut));
			editTextAudioInPort.setText(String.format("%d", audioPortIn));		
		}else if(id==VOLUME_REGULATION_DIALOG)
		{
			AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
			volumeSelect.setMax(audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
			volumeSelect.setProgress(audiomanager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
		}
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
		if(isNetworkAvailable&& (ttl>0))
		{
			WifiManager wifiMan = (WifiManager) this
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInf = wifiMan.getConnectionInfo();
			String macAddr = wifiInf.getMacAddress();
			Uri.Builder builder = new Uri.Builder();
			builder.path(SHARE_PATH)
					.appendQueryParameter(MACADDR_PARAM, macAddr)
					.appendQueryParameter(EMAIL_PARAM, email)
					.appendQueryParameter(TTL_PARAM, String.format("%d", ttl));
			Uri uri = builder.build();
		
			String realAddress = SERVER_SCHEME+"://"+serverAuthority+":"+serverHttpPort+"/"+uri.toString();
			ConnectionRequest req= new ConnectionRequest(ConnectionRequest.GET, realAddress);
			req.setAnswerProcessor(shareConnectionResponce);
			req.setProcessingType(ConnectionRequest.READ_ALL);
			protocolManager.push(req);
		}
	}
	
	
	
	ProcessAsyncRequestResponceProrotype shareConnectionResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			// TODO Auto-generated method stub
			try
			{
				JSONObject r = new JSONObject((String)responce);
				String status = r.getString("status");
				if(status.equalsIgnoreCase("ok"))
				{
					setSession_token(r.getString("token"));
					toastBuilder.makeAndShowToast(R.string.toastInviteOk, ToastBuilder.ICON_OK, ToastBuilder.LENGTH_LONG);
					setCurrentState(STATE_ON);
					//startButton.toggle();
				}
				else
				{
					toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFail, r.getString("message")), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
					
				}
			}
			catch(JSONException e)
			{
				Log.e("ConnectionResponceHandler", "onConnectionSuccessful", e);
				toastBuilder.makeAndShowToast(R.string.toastInviteHz, ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
				
				
			}
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			// TODO Auto-generated method stub
			toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			// TODO Auto-generated method stub
			toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
		}
		
	};

	protected static final int RERUN_COMMANDS = 1;
	protected static final int RERUN_COMMANDS_DELAY = 1000;	
	
	Handler reconnectHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
         	
         	switch (msg.what)
         	{
         		case RERUN_COMMANDS:
         			reRunCommands();
         			break;
         		default:
         			throw new RuntimeException("Unknown command to video writer thread");
         	};
				
         }
	};
	
	


	ProcessAsyncRequestResponceProrotype cmdConnectionResponse = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			reconnectHandler.sendMessageDelayed(reconnectHandler.obtainMessage(RERUN_COMMANDS), RERUN_COMMANDS_DELAY);//  reRunCommands();
			OnScreenLogger.setCommands(false);
			driver.reset();
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			reconnectHandler.sendMessageDelayed(reconnectHandler.obtainMessage(RERUN_COMMANDS), RERUN_COMMANDS_DELAY);
			OnScreenLogger.setCommands(false);
			//driver.reset();
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			reconnectHandler.sendMessageDelayed(reconnectHandler.obtainMessage(RERUN_COMMANDS), RERUN_COMMANDS_DELAY);
			//driver.reset();
			OnScreenLogger.setCommands(false);
			//driver.reset();
		}
		
		/*ByteArrayOutputStream s = new ByteArrayOutputStream(10);
		DataOutputStream ds = new DataOutputStream(s);
		byte[] error={90,90,0,90,0,90,0};*/
		private static final String CMD_SLEEP="sleep";
		private static final String CMD_DIR="dir";
		private static final String CMD_OMEGA="theta";
		private static final String CMD_VOMEGA="phi";
		
		
		
		
		protected void parceJson(Object responce)
		{
			JSONObject r;
			
			/*{a:N1, b:N2, c:N3, sa:M1, sb:M2, sc:M3, h:A }*/
			try
			{
				try{
				r = new JSONObject((String)responce);
				}catch(JSONException e)
				{
					Log.v("ConnectionResponceHandler", "parceJson", e);
					return;
				}
				
				if(r.has(CMD_SLEEP))
				{
					int sleep=r.getInt(CMD_SLEEP);
					if(currentState>STATE_PAUSED)
					{
						if(sleep==0)
						{
							setCurrentState(STATE_ENABLED);
						}
						else
						{
							setCurrentState(STATE_ON);
						}
					}
				}
				if(r.has(CMD_DIR)&&r.has(CMD_OMEGA)&&r.has(CMD_VOMEGA))
				{
					driver.setNewDirection(r.getInt(CMD_DIR), r.getDouble(CMD_OMEGA),r.getDouble(CMD_VOMEGA));
					Log.v("cmd", (String)responce);
				}
				
				
				/*	s.reset();
					ds.writeByte(r.getInt("h"));
					ds.writeByte(r.getInt("a"));
					ds.writeShort(r.getInt("sa"));
					ds.writeByte(r.getInt("b"));
					ds.writeShort(r.getInt("sb"));
					ds.writeByte(r.getInt("c"));
					ds.writeShort(r.getInt("sc"));
					sendCommand(s.toByteArray());*/
				
			}
			catch(JSONException e)
			{
				Log.v("ConnectionResponceHandler", "onConnectionSuccessful", e);

			}
		/*	catch(IOException e)
			{
				Log.v("ConnectionResponceHandler", "onConnectionSuccessful", e);
				sendCommand(error);
			}*/
		}
		
		@Override
		protected void onDataPart(Object responce) {
			parceJson((String)responce);
			OnScreenLogger.setCommands(true);
		}
		
	};
	
	
	public String getSession_token() {
		return session_token;
	}

	private void writeTokenToWorkers(String session_token)
	{		
		videoReceiver.setToken("web-"+session_token);
		audioSender.setToken("ava-"+session_token);
		audioReceiver.setToken("web-"+session_token);
		videoSender.setToken("ava-"+session_token);
		
	}
	public void setSession_token(String session_token) {
		this.session_token = session_token;
		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putString(SHARED_PREFS_TOKEN, session_token).apply();
		writeTokenToWorkers(session_token);
	}
}
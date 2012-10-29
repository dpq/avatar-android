package ru.glavbot.avatarProto;



//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;



//import java.io.IOException;
//import java.util.concurrent.ScheduledThreadPoolExecutor;

//import java.io.Serializable;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;
//import java.util.SortedSet;
//import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import ru.glavbot.AVRestreamer.AudioReceiver;
import ru.glavbot.AVRestreamer.AudioSender;
import ru.glavbot.AVRestreamer.OnScreenLogger;
import ru.glavbot.AVRestreamer.VideoReceiver;
//import ru.glavbot.AVRestreamer.VideoSender;
import ru.glavbot.asyncHttpRequest.ConnectionManager;
import ru.glavbot.asyncHttpRequest.ConnectionRequest;
import ru.glavbot.asyncHttpRequest.ProcessAsyncRequestResponceProrotype;
import ru.glavbot.avatarProto.FullScreenDialog.FullScreenDialogButtonListener;
import ru.glavbot.customLogger.AVLogger;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings.Secure;
//import android.os.StrictMode;

//import android.util.Log;

import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
//import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
//import android.widget.Toast;


public class AvatarMainActivity extends AccessoryProcessor {
	
	private static final String TAG= "AvatarMainActivity";
	boolean DEBUG=true;
    /** Called when the activity is first created. */
    // private Button startButton;
     private Button pauseButton;
     private Button settingsButton;
     private Button sendLinkButton;
   //  private Button resumeButton;
     private Button stopButton;
    // private Button volumeButton;
     private SurfaceView cameraPreview;
     private SurfaceView videoView;
     private FrameLayout frameLayoutRun;
     private RelativeLayout relativeLayoutStart;
     
     
    // private VideoSender videoSender;
     private AudioSender audioSender;
     private AudioReceiver audioReceiver;
     private VideoReceiver videoReceiver;
 
	//  private SensorManager mSensorManager;
	//  private Sensor mLuxmeter;
     private String gatewayIp;
     
   //  private static final int SEND_CONTROL_LINK_DIALOG = 1001;
     private static final int CONFIGURE_SERVER_DIALOG = 1002;
     private static final int SELECT_EMAIL_DIALOG=1003;
     
     private static final int CONNECTION_LOST_DIALOG = 1004;
     private static final int LOW_CHARGE_DIALOG = 1005;
     private static final int PAUSE_DIALOG = 1006;
     private static final int REMOTE_PAUSE_DIALOG = 1007;
     private static final int TIME_OUT_DIALOG = 1008;
     
     
     
    // private static final int VOLUME_REGULATION_DIALOG = 1003;
     
     
     
     private static final String SHARED_PREFS = "RobotSharedPrefs";
     private static final String SHARED_PREFS_EMAIL = "email";
     private static final String SHARED_PREFS_VOLUME = "volume";
     private static final String SHARED_PREFS_TTL = "ttl";
     private static final String SHARED_PREFS_TOKEN = "token"; 
     private static final String SHARED_PREFS_WHEELS = "wheels"; 
     private static final String SHARED_PREFS_STATE = "state";      
     
     
     private static final String SERVER_SCHEME = "http";    

     private static final String SHARE_PATH = "share"; 
     private static final String CMD_PATH = "cmd"; 
     private static final String START_VIDEO_PATH = "start.cgi"; 
     private static final String RESET_ACCESSORY_PATH="arduino.cgi";
     private static final int CAMERAS_TIMEOUT=10000;
     
    // private static final String RESTREAMER_PATH = "restreamer"; 
     private static final String MACADDR_PARAM = "macaddr";
     private static final String EMAIL_PARAM = "email";
	 
     //email: ���� ������� ������ �� ������������� ������?
     private static final String TTL_PARAM = "ttl";
     private static final String TOKEN_PARAM = "token";
     private static final String HOST_PARAM = "host";
     private static final String PORT_PARAM = "port";
     private static final String MODE_PARAM = "mode";
     private static final String DEVICE_PARAM = "device";
     private static final String MODE_PARAM_VALUE = "read";
     
     private static final String SERVER_AUTHORITY_PARAM = "serverAuthority";
     private static final String SERVER_HTTP_PORT_PARAM = "serverHttpPort";
     private static final String VIDEO_PORT_OUT_PARAM = "videoPortOut";
     private static final String AUDIO_PORT_IN_PARAM = "audioPortIn";
     private static final String AUDIO_PORT_OUT_PARAM = "audioPortOut";
     private static final String USE_GSM_PARAM = "useGsm";
     
     
     private static final String EMAILS_LIST_PARAM = "emailsListWithTime";
     private static final int MAX_EMAILS=15;
     
    // private static final String WHEEL_ANGLE_1 = "WheelAngle1";
   //  private static final String WHEEL_ANGLE_2 = "WheelAngle2";
    // private static final String WHEEL_ANGLE_3 = "WheelAngle3";
     
    // private static final String WHEEL_DIR_1 = "WheelDir1";
    // private static final String WHEEL_DIR_2 = "WheelDir2";
   ////  private static final String WHEEL_DIR_3 = "WheelDir3";
     
     
     private String serverAuthority = "auth.glavbot.ru"; 
     private String serverHttpPort = "1018";
     private int videoPort = 5001;
     private int audioPortIn = 10002;
     private int audioPortOut = 10003;
     private boolean useGsm = false;
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
 	
 	private CustomAutoCompleteTextView autoCompleteTextViewAddress;
 	private ImageButton mailListButton;
 	private Button resumeButton;
 	private ProgressDialog progressDialog;
 	TextView statusText;
 	TextView textViewSignal;
 	TextView textViewCharge;
 	TextView textViewEmail;
 	
 	ImageView imageViewSignal;
 	ImageView imageViewCharge;
 	private String android_id;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		try {
			Thread.sleep(1000, 0);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		 mainThreadHandler = new AvatarMainActivityHandler();     
		/*try {
		    Runtime.getRuntime().exec("su"); 
		} catch( Exception e ) { // pokemon catching
			Toast.makeText(getApplicationContext(), "Cannot receive root privileges! Error is "+e.getMessage(), Toast.LENGTH_LONG);
		}*/
		android_id = Secure.getString(getContentResolver(),
                Secure.ANDROID_ID); 
		
        protocolManager= new ConnectionManager();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.main);
        
        statusText = (TextView) findViewById(R.id.StatusText);
		logger = OnScreenLogger.init(statusText);
        
        videoView= (SurfaceView)findViewById(R.id.videoView);
    	cameraPreview = (SurfaceView)findViewById(R.id.CameraPreview);
		settingsButton=(Button)findViewById(R.id.SettingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				showDialog(CONFIGURE_SERVER_DIALOG);
			}
		});
		
    	pauseButton= (Button)findViewById(R.id.PauseButton);
	    pauseButton.setOnClickListener(new OnClickListener(){
		    public void onClick(View v) {
		        setCurrentState(STATE_PAUSED);

		    }
	    });
    	
	    
	    
    	
    	relativeLayoutStart = (RelativeLayout)findViewById(R.id.relativeLayoutStart);
    	frameLayoutRun = (FrameLayout)findViewById(R.id.frameLayoutRun);
    	
    	
    	autoCompleteTextViewAddress = (CustomAutoCompleteTextView) findViewById(R.id.autoCompleteTextViewAddress);
    	mailListButton =(ImageButton) findViewById(R.id.MailListButton);
    	mailListButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				showDialog(SELECT_EMAIL_DIALOG);
			}
		});
    	
    	autoCompleteTextViewAddress.setOnFocusChangeListener(new OnFocusChangeListener(){
    	    public void onFocusChange(View v, boolean hasFocus){
    	        if (hasFocus)
    	        {
    	            autoCompleteTextViewAddress.selectAll();
    	        }
    	        else
    	        {
    	        	//autoCompleteTextViewAddress.setSelection(0,0);
    	        }
    	    }
    	});

    	
    	sendLinkButton = (Button)findViewById(R.id.SendLinkButton);
    	sendLinkButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				
				if(isNetworkAvailable)
				{
					
					setEmail(autoCompleteTextViewAddress.getText().toString());
					setTtl(24*60*60);
					emailsSet.addEmail(autoCompleteTextViewAddress.getText().toString());
					while(emailsSet.size()>MAX_EMAILS)
						emailsSet.remove(emailsSet.iterator().next());
					
					
					
					ArrayAdapter<String> adapter = 
					        new ArrayAdapter<String>(AvatarMainActivity.this, android.R.layout.simple_list_item_1, emailsSet.toEmailStringSet());
					
					autoCompleteTextViewAddress.setAdapter(adapter);
				    if(!autoCompleteTextViewAddress.isPopupShowing()){
				    	autoCompleteTextViewAddress.dismissDropDown();
				    }

					SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
					prefs.edit().putStringSet(EMAILS_LIST_PARAM, emailsSet.toStringSet()).apply();
					progressDialog = ProgressDialog.show(AvatarMainActivity.this, "",getResources().getText(R.string.sendingLinkMessage) );
					shareRobot();
						
				}
				else
				{
					toastBuilder.makeAndShowToast(R.string.toastNoWifi, ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
				}
			}
    		
    	});
    	
    	resumeButton=(Button)findViewById(R.id.ResumeButton);
    	resumeButton.setOnClickListener(new OnClickListener(){
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
    	
    	textViewSignal= (TextView)findViewById(R.id.TextViewSignal);  
    	textViewSignal.setText("");
    	textViewCharge=(TextView)findViewById(R.id.TextViewCharge);  
    	textViewEmail=(TextView)findViewById(R.id.TextViewEmail);  
    	
     	imageViewSignal=(ImageView)findViewById(R.id.ImageViewSignal);
     	imageViewCharge=(ImageView)findViewById(R.id.ImageViewCharge);
    	
    	
		volumeSelect=(SeekBar)findViewById(R.id.seekBarVolume);
		volumeSelect.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser)
				{
					setVolume(progress);
					/*AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
					audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, 0);*/
				}
			}
		});
		AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
		volumeSelect.setMax(audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
		volumeSelect.setProgress(audiomanager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
    	

		stopButton = (Button)findViewById(R.id.StopButton);
		stopButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				setCurrentState(STATE_OFF);
			}});
		
		
		
		videoReceiver = new VideoReceiver(videoView);
    //    videoSender = new VideoSender(this, cameraPreview);
        audioSender = new AudioSender();
        audioReceiver= new AudioReceiver();
        
      //  mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
       // mLuxmeter = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        
		try {
			Process process = new ProcessBuilder()
		       .command("/system/bin/su")
		       .start();
			OutputStream o =process.getOutputStream();
			o.write("/system/xbin/echo \"4096 8192 16384\" > /proc/sys/net/ipv4/tcp_wmem\n".getBytes());		
		       
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "fail!", Toast.LENGTH_LONG).show();
		} 
        
        driver= new RoboDriver(this);
    }
    
    protected void setVolume(int newVolume)
    {
		AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
		audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, newVolume, 0);
		if(volumeSelect!=null)
		{
			volumeSelect.setProgress(newVolume);
		}
		SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
		prefs.edit().putInt(SHARED_PREFS_VOLUME, newVolume).apply();
		
    }
    
    protected void restoreVolume()
    {
    	
    	
    	
		AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
		if(audiomanager.getMode()!=AudioManager.MODE_IN_COMMUNICATION)
		{
			audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		}
		
		SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
    	int volume = prefs.getInt(SHARED_PREFS_VOLUME, audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
		
    	
		
		audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
		if(volumeSelect!=null)
		{
			volumeSelect.setProgress(volume);
		}
    }
    

    
    
    protected class AvatarMainActivityHandler extends  AccessoryHandler
    {
    	@Override
    	public void handleMessage(Message msg) {
         	
    		super.handleMessage(msg);
         	switch (msg.what)
         	{
         		case RERUN_COMMANDS:
         			reRunCommands();
         			break;
         		case GET_TELEMETRICS_WIFI:
         			getTelemetricsWifi();
         			break;
         		case SEND_TELEMETRIC_REPORT:
         			reportTelemetric();
         			break;
         		case CALC_PING:
         			calcPing();
         			break;
         		//default:
         			//throw new RuntimeException("Unknown command to video writer thread");
         	};
				
         }
    }
    
    @Override
    protected void reopenAccessory()
    {
    	super.reopenAccessory();
    	if (!isAccessoryAvailable())
    	{
			Uri.Builder builder = new Uri.Builder();
			builder.path(RESET_ACCESSORY_PATH)
;
			Uri uri=builder.build();
			String realAddress = "http://"+gatewayIp+":6000/"+uri.toString();
			
			ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,realAddress);
			r.setTimeout(1000);
			r.setAnswerProcessor(emptyResponce);
			bottomCameraStreamManager.push(r);
    	}
    }
    
    protected long pingStartMsecs;
    protected int ping = 0;
    
    //protected LogCollector logCollector;
    ArrayList<String> logData= new ArrayList<String>();
    
    protected void calcPing() {
		

    	if(currentState>0)
    	{
    		Uri.Builder builder = new Uri.Builder();
    		builder.path(CMD_PATH)
    		.appendQueryParameter(TOKEN_PARAM, getSession_token())
    		.appendQueryParameter(MODE_PARAM, "rtt")
    		.appendQueryParameter(DEVICE_PARAM, android_id);
    		Uri uri=builder.build();
    		String realAddress = SERVER_SCHEME+"://"+serverAuthority+":"+serverHttpPort+"/"+uri.toString();
    		ConnectionRequest req= new ConnectionRequest(ConnectionRequest.GET, realAddress);
    		req.setTimeout(TELEMETRIC_DELAY/2);
    		req.setAnswerProcessor(pingResponce);
    		pingStartMsecs=System.currentTimeMillis();
    		pingManager.push(req);
    		/*ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,"http://"+gatewayIp+":6000/wifi.cgi");
    		r.setTimeout(TELEMETRIC_DELAY);
    		r.setAnswerProcessor(emptyResponce);
    		telemetricManager.push(r);*/
    	}
    	else
    	{
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(CALC_PING), TELEMETRIC_DELAY);
			pingManager.clearQueue();
    	}
    	
	}
    
    ConnectionManager pingManager = new ConnectionManager();
	ProcessAsyncRequestResponceProrotype pingResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			
			long pingEndMsecs=System.currentTimeMillis();
			ping = (int)(pingEndMsecs-pingStartMsecs)/2;
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(CALC_PING), TELEMETRIC_DELAY);
				
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			//textViewSignal.setText(String.format("%d", statusCode));
			//sendTelemetricMessage();
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(CALC_PING), 1000);	
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			//textViewSignal.setText(e.getMessage());
			//sendTelemetricMessage();
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(CALC_PING), 1000);}
		
	};
	ProcessAsyncRequestResponceProrotype emptyResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			

				
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			//textViewSignal.setText(String.format("%d", statusCode));
			//sendTelemetricMessage();
			//mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(CALC_PING), 1000);	
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			//textViewSignal.setText(e.getMessage());
			//sendTelemetricMessage();
		//	mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(CALC_PING), 1000);
		}
		
	};
    
/*
	SensorEventListener sensorEventListener = new SensorEventListener(){

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
			
		}

		public void onSensorChanged(SensorEvent event) {
			
			if(currentState>STATE_ON)
				driver.updateLuxmeterValue(event.values[0]);
			else
				driver.updateLuxmeterValue(STOPITSOT);
		}
    	
    };
    */
    
    
    boolean isListeningNetwork=false;

    private void processNetworkState()
    {
        if(isNetworkAvailable)
        {
        	setCurrentState(Math.abs(currentState));
        //	WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE); 
        //	DhcpInfo info = wifi.getDhcpInfo();
        	gatewayIp="192.168.100.35";//android.text.format.Formatter.formatIpAddress(info.serverAddress);
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
            AVLogger.d(BroadcastReceiver.class.getSimpleName(), "action: "
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
 	protected static final int STATE_REMOTE_PAUSED=2;
 	protected static final int STATE_REMOTE_PAUSED_NO_NETWORK=-2;	
 	protected static final int STATE_ON=3;
 	protected static final int STATE_ON_NO_NETWORK=-3;
 	protected static final int STATE_ENABLED=4;
 	protected static final int STATE_ENABLED_NO_NETWORK=-4;
 	
 	protected int currentState=0;
 	
 	protected void setWelcomeScreen()
 	{
 		frameLayoutRun.setVisibility(View.GONE);
 		relativeLayoutStart.setVisibility(View.VISIBLE);
 		statusText.setVisibility(View.GONE);
 		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
 		imm.showSoftInput(autoCompleteTextViewAddress, 0);
 		autoCompleteTextViewAddress.requestFocus();
 		autoCompleteTextViewAddress.selectAll();

 	}
 	protected void setWorkerScreen()
 	{
 		statusText.setVisibility(View.VISIBLE);
 		frameLayoutRun.setVisibility(View.VISIBLE);
 		relativeLayoutStart.setVisibility(View.GONE);
 		textViewEmail.setText(getEmail());
 		InputMethodManager imm = (InputMethodManager)getSystemService(
 			      Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(autoCompleteTextViewAddress.getWindowToken(), 0);

 	}
 	
 /*	protected void showProperButton(int state)
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
 		
 	}*/
 	
 	protected void setCurrentState(int newState)
 	{
 		int prevState=currentState;
 		currentState= newState;
 		//showProperButton(newState);
 		switch(newState)
 		{
 			case STATE_OFF:
 				disableAll();
 				break;
 			case STATE_ENABLED:
				runCommands();
 				runStreaming();
 				setWorkerScreen();
 				driver.toWork();
 				break;
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
 				runCommands();
 				if(prevState>currentState)
 				{
 					driver.reset();
 					//stopCommands();
 					stopStreaming();
 					
 				}
	 			setWorkerScreen();
	 			driver.updateLuxmeterValue(STOPITSOT);
			    showDialog(PAUSE_DIALOG);
 				break;
 			case STATE_REMOTE_PAUSED:
 				runCommands();
 				if(prevState>currentState)
 				{
 					driver.reset();
 					stopStreaming();
 				}
	 			setWorkerScreen();
	 			driver.updateLuxmeterValue(STOPITSOT);
			    showDialog(REMOTE_PAUSE_DIALOG);
 				break;
 			case STATE_ENABLED_NO_NETWORK:
 				stopStreaming();
 			case STATE_ON_NO_NETWORK:
 				stopCommands();
 				driver.updateLuxmeterValue(STOPITSOT);
 			case STATE_REMOTE_PAUSED_NO_NETWORK:
 			case STATE_PAUSED_NO_NETWORK: 				
 				break;

 		}
 		if(currentState>0)
 		{
 			reportTelemetric();
 		}
 		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putInt(SHARED_PREFS_STATE, currentState).commit();

 		
 		
 	}
 	boolean commandsRunning = false;
	protected void runCommands()
	{
		if(!commandsRunning)
		{
			Uri.Builder builder = new Uri.Builder();
			builder.path(CMD_PATH)
			.appendQueryParameter(TOKEN_PARAM, getSession_token())
			.appendQueryParameter(DEVICE_PARAM, android_id)
			.appendQueryParameter(MODE_PARAM, MODE_PARAM_VALUE);
			Uri uri=builder.build();
			String realAddress = SERVER_SCHEME+"://"+serverAuthority+":"+serverHttpPort+"/"+uri.toString();
			ConnectionRequest req= new ConnectionRequest(ConnectionRequest.GET, realAddress);
			req.setTimeout(7500);
			req.setAnswerProcessor(cmdConnectionResponse);
			req.setProgressProcessor(cmdConnectionResponse);
			req.setProcessingType(ConnectionRequest.READ_STRINGS_ONE_BY_ONE);
			protocolManager.push(req);
			//mainThreadHandler.removeMessages(RERUN_COMMANDS);
			sendTelemetricMessage();
			commandsRunning=true;
		}
	}
	
	protected void stopCommands()
	{
		if(commandsRunning)
		{
			protocolManager.stopCurrent();
			driver.reset();
			removeTelemetricMessages();
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
	//		videoSender.stopCamera();
			videoReceiver.stopReceiveVideo();
			audioSender.stopVoice();
			audioReceiver.stopVoice();
			streamsRunning=false;
			sendStopCamerasRequest();
		}
	}
    
	protected void runStreaming()
    {
		if(!streamsRunning)
		{
			//videoSender.startCamera();
			
			audioReceiver.startVoice();
			audioSender.startVoice();
			videoReceiver.startReceiveVideo();
			streamsRunning=true;
			sendStartCamerasRequest();
			
		}
    }
	
	private void sendStopCamerasRequest()
	{
		if(!streamsRunning)
		{
			ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,"http://"+gatewayIp+":6000/stop.cgi");
			r.setTimeout(CAMERAS_TIMEOUT);
			r.setAnswerProcessor(stopCameraResponce);
		//	bottomCameraStreamManager.clearQueue(); // only last one should be executed
			bottomCameraStreamManager.push(r);
		}
	}
	
	
	private void sendStartCamerasRequest()
	{
		if(streamsRunning)
		{
			Uri.Builder builder = new Uri.Builder();
			builder.path(START_VIDEO_PATH)
			.appendQueryParameter(TOKEN_PARAM, getSession_token())
			.appendQueryParameter(HOST_PARAM, serverAuthority)
			.appendQueryParameter(PORT_PARAM, Integer.toString(videoPort));
		
			Uri uri=builder.build();
			String realAddress = "http://"+gatewayIp+":6000/"+uri.toString();
		
			ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,realAddress);
			r.setTimeout(CAMERAS_TIMEOUT);
			r.setAnswerProcessor(startCameraResponce);
		//	bottomCameraStreamManager.clearQueue(); // only last one should be executed
			bottomCameraStreamManager.push(r);
		}
	}
	

	
	ConnectionManager bottomCameraStreamManager = new ConnectionManager();

	
	ProcessAsyncRequestResponceProrotype startCameraResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
		
				String answer = (String)responce;
				//answer.charAt(0);
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			sendStartCamerasRequest();
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			sendStartCamerasRequest();
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
		}
		
	};
	
	ProcessAsyncRequestResponceProrotype stopCameraResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
		
				String answer = (String)responce;
				//answer.charAt(0);
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			sendStopCamerasRequest();
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			sendStopCamerasRequest();
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
		}
		
	};
	/*
	ProcessAsyncRequestResponceProrotype emptyResponce1 = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
	
				String answer = (String)responce;
				//answer.charAt(0);
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
		
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			
		}

		@Override
		protected void onConnectionFail(Throwable e) {
		
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
		}
		
	};*/
	
	private static final int TELEMETRIC_DELAY=10000;
	//private static final int TELEMETRIC_REPORT_DELAY= TELEMETRIC_DELAY;
	int wifiLevel=100500;
	int chargeLevel=100500;
	ConnectionManager telemetricManager = new ConnectionManager();
	ProcessAsyncRequestResponceProrotype telemetricsWifiResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			
			try{
				wifiLevel =	Integer.parseInt(((String)responce));
				if(wifiLevel>=-50)
					imageViewSignal.setImageResource(R.drawable.signal_good);
				else
				{
					if(wifiLevel>=-70)
						imageViewSignal.setImageResource(R.drawable.signal_medium);
					else
					{
						imageViewSignal.setImageResource(R.drawable.signal_poor);
					}
				}	
			}
			catch(NumberFormatException e)
			{
				wifiLevel=(int)STOPITSOT;
				AVLogger.e("", "unknown router behaviour",e);
			}
			textViewSignal.setText((String)responce);
			

			/*if(wifiLevel>-70)
			{
				textViewSignal.setText("");
			}
			else
			{
				textViewSignal.setText(R.string.TextWeakSignal);
			}*/
			sendTelemetricMessage();
				
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			textViewSignal.setText(String.format("%d", statusCode));
			sendTelemetricMessage();
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			textViewSignal.setText(e.getMessage());
			sendTelemetricMessage();
		//	toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
		}
		
	};
	
	protected void getTelemetricsWifi()
	{
		ConnectionRequest r = new ConnectionRequest(ConnectionRequest.GET,"http://"+gatewayIp+":6000/wifi.cgi");
		r.setTimeout(TELEMETRIC_DELAY/2);
		r.setAnswerProcessor(telemetricsWifiResponce);
		telemetricManager.push(r);
	}
    
	protected void sendTelemetricMessage()
	{
		mainThreadHandler.removeMessages(GET_TELEMETRICS_WIFI);
		mainThreadHandler.removeMessages(SEND_TELEMETRIC_REPORT);
		
		Message m =mainThreadHandler.obtainMessage(GET_TELEMETRICS_WIFI);
		mainThreadHandler.sendMessageDelayed(m, TELEMETRIC_DELAY);
		m =mainThreadHandler.obtainMessage(SEND_TELEMETRIC_REPORT);
		mainThreadHandler.sendMessageDelayed(m, TELEMETRIC_DELAY);
	}
	
	protected void reportTelemetric()
	{
		if(currentState>0)
		{
			Uri.Builder builder = new Uri.Builder();
			builder.path(CMD_PATH)
			.appendQueryParameter(TOKEN_PARAM, getSession_token())
			.appendQueryParameter(MODE_PARAM, "telemetry")
			.appendQueryParameter(DEVICE_PARAM, android_id)
			.appendQueryParameter("battery", Integer.toString(chargeLevel))
			.appendQueryParameter("signal", Integer.toString(wifiLevel))
			.appendQueryParameter("state", Integer.toString(currentState))
			.appendQueryParameter("ping", Integer.toString(ping));
			Uri uri=builder.build();
			String realAddress = SERVER_SCHEME+"://"+serverAuthority+":"+serverHttpPort+"/"+uri.toString();
			String data="";
			ArrayList<String> log=avLogger.exportLog();
			ListIterator<String> it= log.listIterator();
			while (it.hasNext())
			{
				data+=("\r\n"+it.next());
			}
			ConnectionRequest req= new ConnectionRequest(ConnectionRequest.POST, realAddress,data);
			req.setTimeout(1000);
			req.setAnswerProcessor(emptyResponce);
			telemetricManager.push(req);
		}
		else
		{
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(GET_TELEMETRICS_WIFI), TELEMETRIC_DELAY);
			telemetricManager.clearQueue();
			
		}
		logData.clear();
	}
	
	protected void removeTelemetricMessages()
	{
		
		mainThreadHandler.removeMessages(GET_TELEMETRICS_WIFI);
		mainThreadHandler.removeMessages(SEND_TELEMETRIC_REPORT);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        
       disableAll();
       stopListeningNetwork();
     //  mSensorManager.unregisterListener(sensorEventListener);
       AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
		if(audiomanager.getMode()!=AudioManager.MODE_NORMAL)
		{
			audiomanager.setMode(AudioManager.MODE_NORMAL);
		}
		driver.stop();
	//	logCollector.cancel(true);
    }
    
    
    //LinkedHashSet<String> emailsSet=new LinkedHashSet<String>();
    
    //@SuppressWarnings("unchecked")
	@Override
    protected void onResume()
    {
    	super.onResume();
    	
    	//ArrayList<String> logcatParams= new ArrayList<String>();
    	//logcatParams.add("*:d");
    	
    	
    	//logCollector= new LogCollector(logData);
    	//logCollector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, logcatParams);
    	mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(CALC_PING));
    	SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
    	email=prefs.getString(SHARED_PREFS_EMAIL, null);
    	ttl=prefs.getInt(SHARED_PREFS_TTL, 0);
    	session_token = prefs.getString( SHARED_PREFS_TOKEN, null);
    	currentState=prefs.getInt( SHARED_PREFS_STATE, STATE_OFF);
    	/*if(currentState>STATE_ON)
    	{
    		currentState=STATE_ON;
    	}else if(currentState>STATE_OFF)
    	{
    			
    	}*/
    	
    	writeTokenToWorkers(getSession_token());
		autoCompleteTextViewAddress.setText(email);  

    	
		//prefs.edit().putStringSet(EMAILS_LIST_PARAM, null).commit();
		Set<String> ss=prefs.getStringSet(EMAILS_LIST_PARAM, null);
		if(ss!=null)
		{
			emailsSet.fromStringSet(ss);
		}
		

		
		ArrayAdapter<String> adapter = 
		        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emailsSet.toEmailStringSet());
		autoCompleteTextViewAddress.setAdapter(adapter);
    	
	    if(!autoCompleteTextViewAddress.isPopupShowing()){
	    	autoCompleteTextViewAddress.dismissDropDown();
	    }
    	
        
        serverAuthority = prefs.getString( SERVER_AUTHORITY_PARAM, "auth.glavbot.ru");
        serverHttpPort = prefs.getString( SERVER_HTTP_PORT_PARAM, "8080");//"1017";
        videoPort =prefs.getInt( VIDEO_PORT_OUT_PARAM, 5001);
        audioPortIn = prefs.getInt( AUDIO_PORT_IN_PARAM, 10002);
        audioPortOut = prefs.getInt( AUDIO_PORT_OUT_PARAM, 10003);
        useGsm = prefs.getBoolean(USE_GSM_PARAM, true);
        
        unpackWheelArray();
        
        
        
    	setPortsAndHosts();
    	startListeningNetwork();
    	

		restoreVolume();
   		doResume();
   		driver.start();
    }
	
    public void setPortsAndHosts()
    {
    	videoReceiver.setAddress(serverAuthority, videoPort);
    	audioSender.setHostAndPort(serverAuthority, audioPortOut);
    	audioSender.setUseGsm(useGsm);
    	audioReceiver.setHostAndPort(serverAuthority, audioPortIn);
    	audioReceiver.setUseGsm(useGsm);
    	//videoSender.setHostAndPort(serverAuthority, videoPort);
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
	CheckBox checkBoxUseGsm;
	
	RadioGroup radioGroupWheel;
	RadioGroup radioGroupDest;
	SeekBar seekBarAngle;
	TextView textWheelAngle;
	int desiredPositions[]= {8,9};
	int wheelId=0;
	
	
	protected void resetSeekBar()
	{
		if(seekBarAngle!=null)
		{
			seekBarAngle.setProgress(RoboDriver.WHEEL_DIRECTIONS[desiredPositions[0]][wheelId]);
		}
	}
	protected void commitSeekBar()
	{
		if(seekBarAngle!=null)
		{
			int progress = seekBarAngle.getProgress();
			RoboDriver.WHEEL_DIRECTIONS[desiredPositions[0]][wheelId]=progress;
			RoboDriver.WHEEL_DIRECTIONS[desiredPositions[1]][wheelId]=progress;
			driver.calibrate(desiredPositions[0], 0, driver.tagHeadPos);
		}
	}
	
	
	public static class EmailWithDateHashSet extends LinkedHashSet<EmailWithDate>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		
		EmailWithDateHashSet(){};
		
		public void addEmail(String string) {
		

			boolean found=false;
			for(EmailWithDate item: this)
			{
				if(item.email.compareTo(string)==0)
				{
					item.date=Calendar.getInstance();
					found=true;
					break;
				}
				
			}
			if(!found)
			{
				EmailWithDate newItem = new EmailWithDate();
				newItem.email=string;
				add(newItem);
			}			
		}

		public EmailWithDateHashSet(Set<String> source)
		{
			fromStringSet(source);
		}
		
		public LinkedHashSet<String> toStringSet()
		{
			LinkedHashSet<String> lhs= new LinkedHashSet<String>();
			
			for(EmailWithDate item: this)
			{
				lhs.add(item.toString());
			}	
			return lhs;
		}
		
		public String[] toEmailStringSet()
		{
			String[] lhs= new String[this.size()];
			int i=0;
			for(EmailWithDate item: this)
			{
				lhs[i]=item.email;
				i++;
			}	
			return lhs;
		}
		
		void fromStringSet(Set<String> source)
		{
			clear();
			for(String item: source)
			{
				add(new EmailWithDate(item));
			}
		}
		
		
		
	};
	
	
	
	
	EmailWithDateHashSet emailsSet=new EmailWithDateHashSet();
	
	
	public static class EmailWithDate {
		String email="";
		Calendar date=Calendar.getInstance();
		
		
		public EmailWithDate()
		{
			
		}
		
		public EmailWithDate(String s)
		{
			fromString(s);
		}
		
		
		@Override
		public String toString()
		{
			JSONObject j = new JSONObject();
			try {
				j.put("email", email);
				j.put("date", date.getTimeInMillis());
			} catch (JSONException e) {
			
				e.printStackTrace();
			}
			
			return j.toString();
			
		}
		public void fromString(String s)
		{
			try {
				JSONObject j=new JSONObject(s);
				email=j.getString("email");
				date.setTimeInMillis(j.getLong("date"));
				
			} catch (JSONException e) {
			
				e.printStackTrace();
			}
			
		}
		
		

		
		
	}
	
	
	public static class EmailWithDataAdapter extends ArrayAdapter<EmailWithDate>{

		
        private static SimpleDateFormat df = new SimpleDateFormat();
        static{
         df.applyPattern("dd MMM  HH:mm");
        }
	    Context context;
	    int layoutResourceId;   
	    EmailWithDate data[] = null;
	   
	    public EmailWithDataAdapter(Context context, int layoutResourceId, EmailWithDate[] data) {
	        super(context, layoutResourceId, data);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        EmailWithDateHolder holder = null;
	       
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	           
	            holder = new EmailWithDateHolder();
	            holder.email = (TextView)row.findViewById(R.id.txtMail);
	            holder.date = (TextView)row.findViewById(R.id.txtDate);
	           
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (EmailWithDateHolder)row.getTag();
	        }
	       
	        EmailWithDate emd = data[position];
	        holder.email.setText(emd.email);
	        holder.date.setText(df.format(emd.date.getTime()));
	       
	        return row;
	    }
	   
	    static class EmailWithDateHolder
	    {
	    	TextView email;
	        TextView date;
	    }
	}
	
	
	private ListView selectEmailDialogLV;
	private Dialog selectEmailDialog;
	private Button wipeButton;
	@Override
	protected Dialog  onCreateDialog(int id)
	{
		Dialog d= null;
		switch (id)
		{
			/*case SEND_CONTROL_LINK_DIALOG:
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
							AVLogger.e("AvatarMainActivity", "AlertDialog.buttonOk.onClick", e);
							setTtl(0);
						}
						shareRobot();
						alertDialog.dismiss();*/
						/*if(turnedOn)
						{
							startButton.toggle();
						}*/
						//setCurrentState(STATE_ON);
						
				/*	}});
				buttonCancel.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						alertDialog.cancel();
					}});
				d= alertDialog;
				break;
			}*/
			case SELECT_EMAIL_DIALOG:
			{
				//AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyDialogStyle));
				//AlertDialog ad;
				//builder.setTitle("Select email");
				
				//AlertDialog.Builder builder = new AlertDialog.Builder(this);
				selectEmailDialog = new Dialog(this,R.style.MyDialogStyleFS);
				
				
				LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
				selectEmailDialogLV = (ListView)inflater.inflate(R.layout.addressbook_layout,
				                               (ViewGroup) findViewById(R.id.AddressBookListId));
		        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.addressbook_header_row, null);
		        selectEmailDialogLV.addHeaderView(header);
				
		        selectEmailDialog.setContentView(selectEmailDialogLV);
				//builder.setView(selectEmailDialogLV);
				selectEmailDialogLV.setOnItemClickListener(new OnItemClickListener() {
		          
					public void onItemClick(AdapterView<?> lv, View view, int position, long id) {
						autoCompleteTextViewAddress.setText(emailsSet.toEmailStringSet()[(int)id]);
						selectEmailDialog.cancel();
						
					}
		        });
				//layout.setAdapter(adapter)
			//	layout.set
				
				/*builder.setItems(emailsSet.toArray(new String[0]), new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	autoCompleteTextViewAddress.setText(emailsSet.toEmailStringSet()[item]);
				    }
				});*/

				//selectEmailDialog=builder.create();
				
				wipeButton=(Button)header.findViewById(R.id.WipeButton);
				wipeButton.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						emailsSet.clear();
						ArrayAdapter<String> adapter =new ArrayAdapter<String>(AvatarMainActivity.this, android.R.layout.simple_list_item_1, emailsSet.toEmailStringSet());
						autoCompleteTextViewAddress.setAdapter(adapter);
					    if(!autoCompleteTextViewAddress.isPopupShowing()){
					    	autoCompleteTextViewAddress.dismissDropDown();
					    }
						selectEmailDialog.cancel();
					}
				});
				
				selectEmailDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_transparent_bg);
				d=selectEmailDialog;
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
				checkBoxUseGsm=(CheckBox)layout.findViewById(R.id.checkBoxUseGsm);
				/*
				editTextWheel1Angle=(EditText) layout.findViewById(R.id.editTextWheel1Angle);
				editTextWheel1Angle.setText(String.format("%d", angles[0]));
				editTextWheel2Angle=(EditText) layout.findViewById(R.id.editTextWheel2Angle);
				editTextWheel2Angle.setText(String.format("%d", angles[1]));
				editTextWheel3Angle=(EditText) layout.findViewById(R.id.editTextWheel3Angle);
				editTextWheel3Angle.setText(String.format("%d", angles[2]));
				*/
				radioGroupWheel=(RadioGroup)layout.findViewById(R.id.radioGroupWheel);
				radioGroupDest=(RadioGroup)layout.findViewById(R.id.radioGroupDest);
				seekBarAngle=(SeekBar)layout.findViewById(R.id.seekBarAngle);
				textWheelAngle = (TextView)layout.findViewById(R.id.textWheelAngle);
				radioGroupWheel.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						
						switch(checkedId)
						{
						case R.id.radio0: 
							wheelId=0; break;
						case R.id.radio1:
							wheelId=1; break;
						case R.id.radio2:
							wheelId=2; break;
						};
						resetSeekBar();
						
					}
				});
				
				radioGroupDest.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						
						
						switch(checkedId)
						{
					case R.id.radioF: 
						desiredPositions[0]=0;
						desiredPositions[1]=4; break;
					case R.id.radioRF:
						desiredPositions[0]=1;
						desiredPositions[1]=5;
						 break;
					case R.id.radioR:
						desiredPositions[0]=2;
						desiredPositions[1]=6;
						 break;
					case R.id.radioBR:
						desiredPositions[0]=3;
						desiredPositions[1]=7;
						break;
					case R.id.radioT:
						desiredPositions[0]=8;
						desiredPositions[1]=9; break;
					}
						resetSeekBar();
						driver.calibrate(desiredPositions[0], 0, driver.tagHeadPos);
					}
				});
				
				seekBarAngle.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						
						if(fromUser)
							commitSeekBar();
						textWheelAngle.setText(String.format("%d", progress));
							
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						
						
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						
						
					}});
				
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
						
						
						useGsm=checkBoxUseGsm.isChecked();
						/*s=editTextWheel1Angle.getText().toString();
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
						}*/
						//angles[2]=Integer.decode(s);
						//driver.setCompensationAngles(angles);
						SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
						SharedPreferences.Editor  editor = prefs.edit();
						editor.putString(SERVER_AUTHORITY_PARAM, serverAuthority);
						editor.putString(SERVER_HTTP_PORT_PARAM, serverHttpPort);
						editor.putInt(VIDEO_PORT_OUT_PARAM, videoPort);
						editor.putInt(AUDIO_PORT_IN_PARAM, audioPortIn);
						editor.putInt(AUDIO_PORT_OUT_PARAM, audioPortOut);
						editor.putBoolean(USE_GSM_PARAM, useGsm);
					  //  editor.putInt( WHEEL_ANGLE_1,  angles[0]);  
					   // editor.putInt( WHEEL_ANGLE_2,  angles[1]);   
					   // editor.putInt( WHEEL_ANGLE_3,  angles[2]);
					   // driver.setCompensationAngles(angles);
						packWheelArray();
				    	editor.apply();
				    	setPortsAndHosts();
				    	alertDialog.dismiss();
						
					}});
				buttonCancel.setOnClickListener(new OnClickListener(){

					public void onClick(View v)
					{   unpackWheelArray();
						alertDialog.cancel();
					}});
				d= alertDialog;
				break;
			}
		/*	case VOLUME_REGULATION_DIALOG:
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
			}*/
			case  CONNECTION_LOST_DIALOG:
			{
				FullScreenDialog fsd = new FullScreenDialog(this);
				fsd.setImage(R.drawable.connection_lost_icon_big);
				fsd.setDescription(R.string.connectionLostDialogDesc);
				fsd.setActionButton(R.string.connectionLostDialogButton, new FullScreenDialogButtonListener(fsd){

					@Override
					public void doAction() {
					
						setCurrentState(STATE_OFF);
					}
					
				});
				d=fsd;
				break;
			}
			case LOW_CHARGE_DIALOG:
			{
				FullScreenDialog fsd = new FullScreenDialog(this);
				fsd.setImage(R.drawable.low_charge_icon_big);
				fsd.setDescription(R.string.lowChargeDialogDesc);
				fsd.setActionButton(R.string.lowChargeDialogButton, new FullScreenDialogButtonListener(fsd){

					@Override
					public void doAction() {						
					}
					
				});
				d=fsd;
				break;
			}
			case PAUSE_DIALOG:
			{
				FullScreenDialog fsd = new FullScreenDialog(this);
				fsd.setImage(R.drawable.pause_icon_big);
				fsd.setDescription(R.string.pauseDialogDesc);
				fsd.setActionButton(R.string.pauseDialogButton, new FullScreenDialogButtonListener(fsd){

					@Override
					public void doAction() {
				
						if(isNetworkAvailable)
						{
							setCurrentState(STATE_ENABLED);
						}
						else
						{
							setCurrentState(STATE_ENABLED_NO_NETWORK);
						}
					}
					
				});
				d=fsd;
				break;
			}
			case REMOTE_PAUSE_DIALOG:
			{
				FullScreenDialog fsd = new FullScreenDialog(this);
				fsd.setImage(R.drawable.pause_icon_big);
				fsd.setDescription(R.string.remotePauseDialogDesc);
				fsd.setActionButton(R.string.remotePauseDialogButton, new FullScreenDialogButtonListener(fsd){

					@Override
					public void doAction() {
					
						setCurrentState(STATE_OFF);
					}
					
				});
				d=fsd;
				break;
			}
			case TIME_OUT_DIALOG:
			{
				FullScreenDialog fsd = new FullScreenDialog(this);
				fsd.setImage(R.drawable.pause_icon_big);
				fsd.setDescription(R.string.timeOutDialogDesc);
				fsd.setActionButton(R.string.timeOutDialogButton, new FullScreenDialogButtonListener(fsd){

					@Override
					public void doAction() {
						
						setCurrentState(STATE_OFF);
					}
					
				});
				d=fsd;
				break;
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
	
	
	protected void packWheelArray()
	{
		JSONArray a = new JSONArray();
		for(int i =0; i< 10;i++)
		{
			JSONArray b = new JSONArray();
			for(int j=0; j< 9; j++)
			{
				b.put(RoboDriver.WHEEL_DIRECTIONS[i][j] );
			}
			a.put(b);
		}
		
		SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
		prefs.edit().putString(SHARED_PREFS_WHEELS, a.toString()).commit();
			
		
	}
	protected void  unpackWheelArray()
	{
		
		SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
		try {
		JSONArray o = new JSONArray(prefs.getString(SHARED_PREFS_WHEELS, ""));
		

		for(int i =0; i< 10;i++)
		{
			JSONArray b;
			
				b = o.getJSONArray(i);

			for(int j=0; j< 9; j++)
			{
				RoboDriver.WHEEL_DIRECTIONS[i][j]=b.getInt(j);
			}
			
		}
		} catch (JSONException e) {
			
			//e.printStackTrace();
			RoboDriver.WHEEL_DIRECTIONS=RoboDriver.ETALON_WHEEL_DIRECTIONS;
		}
		
	}
	
	
	
	protected void onPrepareDialog (int id, Dialog dialog)
	{
		if(id==CONFIGURE_SERVER_DIALOG)
		{
			editTextServer.setText(serverAuthority);
			editTextServerPort.setText(serverHttpPort);
			editTextVideoOutPort.setText(String.format("%d", videoPort));
			editTextAudioOutPort.setText(String.format("%d", audioPortOut));
			editTextAudioInPort.setText(String.format("%d", audioPortIn));	
			checkBoxUseGsm.setChecked(useGsm);
			resetSeekBar();
		}/*else if(id==VOLUME_REGULATION_DIALOG)
		{
			AudioManager audiomanager = (AudioManager)getSystemService(Activity.AUDIO_SERVICE);
			volumeSelect.setMax(audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
			volumeSelect.setProgress(audiomanager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
		}*/
		else if (id==SELECT_EMAIL_DIALOG)
		{
			
			//AlertDialog d=(AlertDialog)dialog;gfghfghfghfghfghfghfg
			//d.getListView().setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emailsSet.toArray(new String[0])));
			
			selectEmailDialogLV.setAdapter(new EmailWithDataAdapter(this,R.layout.addressbook_regular_row,emailsSet.toArray(new EmailWithDate[0])));
			//selectEmailDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_transparent_bg);
		//	d.
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
			req.setTimeout(10000);
			req.setAnswerProcessor(shareConnectionResponce);
			req.setProcessingType(ConnectionRequest.READ_ALL);
			protocolManager.push(req);
			sendLinkButton.setEnabled(false);
		}
	}
	
	
	
	ProcessAsyncRequestResponceProrotype shareConnectionResponce = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			AVLogger.v("connect", (String)responce);
			progressDialog.dismiss();
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
				//AVLogger.v("connect", (String)responce);
				
				//setCurrentState(STATE_ON);
			}
			catch(JSONException e)
			{
				AVLogger.e("ConnectionResponceHandler", "onConnectionSuccessful", e);
				toastBuilder.makeAndShowToast(R.string.toastInviteHz, ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
				//setCurrentState(STATE_ON);
				
			}
			sendLinkButton.setEnabled(true);
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			progressDialog.dismiss();
			toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteServerRefuse, statusCode), ToastBuilder.ICON_WARN, ToastBuilder.LENGTH_LONG);
			//setCurrentState(STATE_ON);	
			sendLinkButton.setEnabled(true);
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			progressDialog.dismiss();
			toastBuilder.makeAndShowToast(getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()), ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
			//setCurrentState(STATE_ON);
			sendLinkButton.setEnabled(true);
		}
		
	};
	
	@Override
	protected void readChargeState(int charge)
	{
		
		//byte[] data = readCommand(2);
		//if(data!=null)
		//{
			//int tmp1=data[1];
			//int chrg=(tmp1<<8)+data[0]; 
			//if(chrg>0)
			chargeLevel=charge; 
			textViewCharge.setText(String.format("Charge: %d", chargeLevel));
		//	mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(READ_CHARGE_STATE), 1000);
		//}
		//else
		//{
		//	mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(READ_CHARGE_STATE), 1000);
		//}
		
	};
	
	

	public static final int RERUN_COMMANDS = 1;
	public static final int GET_TELEMETRICS_WIFI = 2;
	public static final int SEND_TELEMETRIC_REPORT = 3;
	public static final int CALC_PING=4;
	protected static final int RERUN_COMMANDS_DELAY = 7500;	
	
	
	
	


	ProcessAsyncRequestResponceProrotype cmdConnectionResponse = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(RERUN_COMMANDS), RERUN_COMMANDS_DELAY);//  reRunCommands();
			OnScreenLogger.setCommands(false);
			stopStreaming();
			driver.reset();
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(RERUN_COMMANDS), RERUN_COMMANDS_DELAY);
			OnScreenLogger.setCommands(false);
			stopStreaming();
			//driver.reset();
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			mainThreadHandler.sendMessageDelayed(mainThreadHandler.obtainMessage(RERUN_COMMANDS), RERUN_COMMANDS_DELAY);
			//driver.reset();
			OnScreenLogger.setCommands(false);
			stopStreaming();
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
			
			String resp = (String) responce;
			//driver.resetWatchdog();
			/* {a:N1, b:N2, c:N3, sa:M1, sb:M2, sc:M3, h:A } */
			if (resp.length() > 1) {
				//driver.resetCmdWatchDog();
				JSONObject r;
				try {
					try {
						r = new JSONObject(resp);
					} catch (JSONException e) {
						AVLogger.v("ConnectionResponceHandler", "parceJson", e);

						return;
					}

					if (r.has(CMD_SLEEP)) {
						int sleep = r.getInt(CMD_SLEEP);
						if (currentState > STATE_REMOTE_PAUSED) {
							if (sleep == 0) {
								setCurrentState(STATE_ENABLED);
							} else {
								setCurrentState(STATE_ON);
							}
						}
					}
					if (r.has(CMD_DIR) && r.has(CMD_OMEGA) && r.has(CMD_VOMEGA)) {
						if (currentState > STATE_PAUSED) {
							driver.setNewDirection(r.getInt(CMD_DIR),
									r.getDouble(CMD_OMEGA),
									r.getDouble(CMD_VOMEGA));
							AVLogger.d("cmd", (String) responce);
						}
					} else {

					}

				} catch (JSONException e) {
					AVLogger.v("ConnectionResponceHandler", "parceJson", e);

				}
			}

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
		audioReceiver.setToken("web-"+session_token);//web-
		//videoSender.setToken("ava-"+session_token);
		
	}
	public void setSession_token(String session_token) {
		this.session_token = session_token;
		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putString(SHARED_PREFS_TOKEN, session_token).apply();
		writeTokenToWorkers(session_token);
	}
}
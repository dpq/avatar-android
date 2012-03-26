package ru.glavbot.avatarProto;



//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.mjpegsample.MjpegView.MjpegView;
import ru.glavbot.asyncHttpRequest.ConnectionManager;
import ru.glavbot.asyncHttpRequest.ConnectionRequest;
import ru.glavbot.asyncHttpRequest.ProcessAsyncRequestResponceProrotype;
import ru.glavbot.avatarProto.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.StrictMode;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toast;
import android.widget.ToggleButton;


public class AvatarMainActivity extends AccessoryProcessor {
	
	
	boolean DEBUG=true;
    /** Called when the activity is first created. */
     private ToggleButton startButton;
     private Button sendLinkButton;
     private Button resumeButton;
     private Button stopButton;
     private SurfaceView cameraPreview;
     private MjpegView videoView;
     private FrameLayout frameLayoutRun;
     private RelativeLayout relativeLayoutStart;
     
     
     private VideoSender videoSender;
     private AudioSender audioSender;
     private AudioReceiver audioReceiver;
     private VideoReceiver videoReceiver;
 
     private static final int SEND_CONTROL_LINK_DIALOG = 1001;
     private static final String SHARED_PREFS = "RobotSharedPrefs";
     private static final String SHARED_PREFS_EMAIL = "email";
     private static final String SHARED_PREFS_TTL = "ttl";
     private static final String SHARED_PREFS_TOKEN = "token";    
     
     private static final String SERVER_SCHEME = "http";    
     static final String SERVER_AUTHORITY = "auth.glavbot.ru"; 
     private static final String SHARE_PATH = "share"; 
     private static final String CMD_PATH = "cmd"; 
     private static final String MACADDR_PARAM = "macaddr";
     private static final String EMAIL_PARAM = "email";
     //email: ���� ������� ������ �� ������������� ������?
     private static final String TTL_PARAM = "ttl";
     private static final String TOKEN_PARAM = "token";
     private static final String MODE_PARAM = "mode";
     private static final String MODE_PARAM_VALUE = "read";

     private static final int SERVER_AUDIO_PORT_OUT = 10002;
     private static final int SERVER_AUDIO_PORT_IN = 10003;
     ToastBuilder toastBuilder = new ToastBuilder(this);

     
     
     private  String email;
     private  String session_token;
     private  int  ttl;
     private boolean turnedOn=false;
     
 //	private WebView webView;
 	private ConnectivityManager network;
 	private ConnectionManager protocolManager;
 	private RoboDriver driver;
 	boolean isNetworkAvailable=false;

 	

 	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {

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
        .build());
        }
        protocolManager= new ConnectionManager();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		
        setContentView(R.layout.main);
        
        videoView= (MjpegView)findViewById(R.id.videoView);
    	cameraPreview = (SurfaceView)findViewById(R.id.CameraPreview);
    	startButton= (ToggleButton)findViewById(R.id.StartButton);
    	stopButton=(Button)findViewById(R.id.SendLinkButton);
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
					startButton.toggle();
				}
				else
				{
					toastBuilder.makeAndShowToast(R.string.toastNoWifi, ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
				}
			}
    		
    	});
    	
    	
		startButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked)
				{
					if(isNetworkAvailable)
					{
						setCurrentState(STATE_ON);
					}
					else
					{
						toastBuilder.makeAndShowToast(R.string.toastNoWifi, ToastBuilder.ICON_ERROR, ToastBuilder.LENGTH_LONG);
						buttonView.toggle();
					}
				}
				else
					setCurrentState(STATE_PAUSED);
			}
		});
		stopButton = (Button)findViewById(R.id.StopButton);
		stopButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				setCurrentState(STATE_OFF);
			}});
		
		
		videoReceiver = new VideoReceiver(videoView,"http://dev.glavbot.ru/restreamer?oid=%s");
        videoSender = new VideoSender(this, cameraPreview);
        audioSender = new AudioSender(this,SERVER_AUTHORITY,SERVER_AUDIO_PORT_OUT);
        audioReceiver= new AudioReceiver(this,SERVER_AUTHORITY,SERVER_AUDIO_PORT_IN);
        driver= new RoboDriver(this);
    }
    boolean isListeningNetwork=false;

    private void processNetworkState()
    {
        if(isNetworkAvailable)
        {
        	setCurrentState(Math.abs(currentState));
        }
        else
        {
        	setCurrentState(-Math.abs(currentState));
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
 	
 	
 	protected void setCurrentState(int newState)
 	{
 		int prevState=currentState;
 		currentState= newState;
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
 					stopStreaming();
 				}
 			case STATE_PAUSED:
 				if(prevState>currentState)
 				{
 					stopStreaming();
 					stopCommands();
 				}
 				setWorkerScreen();
 				break;
 			case STATE_ENABLED_NO_NETWORK:
 				stopStreaming();
 			case STATE_ON_NO_NETWORK:
 				stopCommands();
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
			builder.scheme(SERVER_SCHEME).authority(SERVER_AUTHORITY).path(CMD_PATH)
			.appendQueryParameter(TOKEN_PARAM, session_token)
			.appendQueryParameter(MODE_PARAM, MODE_PARAM_VALUE);
			Uri uri=builder.build();
			ConnectionRequest req= new ConnectionRequest(ConnectionRequest.GET, uri.toString());
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
			commandsRunning=false;
		}
	}
	protected void reRunCommands()
	{
		if(currentState>STATE_OFF)
		{
			commandsRunning=false;	
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
			streamsRunning=false;
		}
	}
    
	protected void runStreaming()
    {
		if(!streamsRunning)
		{
			videoSender.startCamera();
			audioSender.startVoice();
			audioReceiver.startVoice();
			videoReceiver.startReceiveVideo();
			streamsRunning=true;
		}
    }
    
    @Override
    protected void onPause() {
        super.onPause();
       disableAll();
       stopListeningNetwork();
    }

    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	SharedPreferences prefs = getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE );
    	email=prefs.getString(SHARED_PREFS_EMAIL, null);
    	ttl=prefs.getInt(SHARED_PREFS_TTL, 0);
    	session_token = prefs.getString( SHARED_PREFS_TOKEN, null);
    	videoReceiver.setToken(session_token);
    	audioSender.setToken(session_token);
    	audioReceiver.setToken(session_token);
    	videoSender.setToken(session_token);
    	startListeningNetwork();
   		doResume();
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
						setCurrentState(STATE_ON);
						
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
		if(isNetworkAvailable&& (ttl>0))
		{
			WifiManager wifiMan = (WifiManager) this
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInf = wifiMan.getConnectionInfo();
			String macAddr = wifiInf.getMacAddress();
			Uri.Builder builder = new Uri.Builder();
			builder.scheme(SERVER_SCHEME).authority(SERVER_AUTHORITY)
					.path(SHARE_PATH)
					.appendQueryParameter(MACADDR_PARAM, macAddr)
					.appendQueryParameter(EMAIL_PARAM, email)
					.appendQueryParameter(TTL_PARAM, String.format("%d", ttl));
			Uri uri = builder.build();
			ConnectionRequest req= new ConnectionRequest(ConnectionRequest.GET, uri.toString());
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
					startButton.toggle();
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

	
/*	public void doHangup()
	{
			stopCommands();
			stopStreaming();
	}*/


	ProcessAsyncRequestResponceProrotype cmdConnectionResponse = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			reRunCommands();
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			reRunCommands();
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			reRunCommands();
			driver.reset();

		}
		
		/*ByteArrayOutputStream s = new ByteArrayOutputStream(10);
		DataOutputStream ds = new DataOutputStream(s);
		byte[] error={90,90,0,90,0,90,0};*/
		private static final String CMD_SLEEP="sleep";
		private static final String CMD_DIR="dir";
		private static final String CMD_OMEGA="omega";
		private static final String CMD_VOMEGA="head";
		
		
		
		
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
					if(sleep==0)
					{
						setCurrentState(STATE_ENABLED);
					}
					else
					{
						setCurrentState(STATE_ON);
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
		}
		
	};
	
	
	public String getSession_token() {
		return session_token;
	}

	public void setSession_token(String session_token) {
		this.session_token = session_token;
		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putString(SHARED_PREFS_TOKEN, session_token).apply();
		videoReceiver.setToken(session_token);
		audioSender.setToken(session_token);
		audioReceiver.setToken(session_token);
		videoSender.setToken(session_token);
	}
}
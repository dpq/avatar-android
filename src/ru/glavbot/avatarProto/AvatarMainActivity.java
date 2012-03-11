package ru.glavbot.avatarProto;



import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import de.mjpegsample.MjpegView.MjpegView;
import ru.glavbot.asyncHttpRequest.ConnectionManager;
import ru.glavbot.asyncHttpRequest.ConnectionRequest;
import ru.glavbot.asyncHttpRequest.ProcessAsyncRequestResponceProrotype;
import ru.glavbot.avatarProto.R;

//import com.ryong21.R;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;


public class AvatarMainActivity extends AccessoryProcessor {
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
     private MjpegView videoView;
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
     //email: кому послать ссылку на использование робота?
     private static final String TTL_PARAM = "ttl";
     private static final String TOKEN_PARAM = "token";
     private static final String MODE_PARAM = "mode";
     private static final String MODE_PARAM_VALUE = "read";
   //  private static final int SERVER_VIDEO_PORT = 10000;
     private static final int SERVER_AUDIO_PORT_OUT = 10002;
     private static final int SERVER_AUDIO_PORT_IN = 10003;
     

     
     
     private  String email;
     private  String session_token;
     private  int  ttl;
     private boolean isRunning=false;
     
 	WebView webView;
 	ConnectivityManager network;
 	ConnectionManager protocolManager;
 	boolean isNetworkAvailable=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        protocolManager= new ConnectionManager();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
        
        leftEngineForward= (TextView)findViewById(R.id.LeftEngineForward);
        leftEngineForward.setVisibility(View.GONE);
   	 	leftEngineBackward= (TextView)findViewById(R.id.LeftEngineBackward);
   	 	leftEngineBackward.setVisibility(View.GONE);
     	rightEngineForward= (TextView)findViewById(R.id.RightEngineForward);
     	rightEngineForward.setVisibility(View.GONE);
     	rightEngineBackward= (TextView)findViewById(R.id.RightEngineBackward);
     	rightEngineBackward.setVisibility(View.GONE);
     	yawUp= (TextView)findViewById(R.id.YawUp);
     	yawUp.setVisibility(View.GONE);
     	yawDown= (TextView)findViewById(R.id.YawDown);
     	yawDown.setVisibility(View.GONE);
     	pitchLeft= (TextView)findViewById(R.id.PitchLeft);
     	pitchLeft.setVisibility(View.GONE);
     	pitchRight= (TextView)findViewById(R.id.PitchRight);
     	pitchRight.setVisibility(View.GONE);
     	wave= (TextView)findViewById(R.id.Wave);
     	wave.setVisibility(View.GONE);
    	cameraPreview = (SurfaceView)findViewById(R.id.CameraPreview);
    	startButton= (ToggleButton)findViewById(R.id.StartButton);
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
					Toast.makeText(AvatarMainActivity.this, R.string.toastNoWifi, Toast.LENGTH_LONG).show();
				}
				
			}
    		
    	});
		startButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
				if(isChecked/*&&(!isRunning)*/)
				{
					if(isNetworkAvailable)
					{
						isRunning=true;
						turnAllOn();
					}
					else
					{
						Toast.makeText(AvatarMainActivity.this, R.string.toastNoWifi, Toast.LENGTH_LONG).show();
						buttonView.toggle();
					}
					//startPlayer();
				}
				else
				if(isRunning)
				{
					isRunning=false;
					turnAllOff();
				}
			}
			
		});
		
		
		videoView= (MjpegView)findViewById(R.id.videoView);
		
		videoReceiver = new VideoReceiver(videoView,"http://dev.glavbot.ru/restreamer?oid=%s");
		
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        videoSender = new VideoSender(this, cameraPreview);
        audioSender = new AudioSender(this,SERVER_AUTHORITY,SERVER_AUDIO_PORT_OUT);
        audioReceiver= new AudioReceiver(this,SERVER_AUTHORITY,SERVER_AUDIO_PORT_IN);
        

        /*stpe = new ScheduledThreadPoolExecutor(1);  
          stpe.scheduleAtFixedRate(new Runnable() {  
            public void run() {  
                    System.gc();
            }  
        },0, 1,TimeUnit.SECONDS);  */

    }
   // ScheduledThreadPoolExecutor stpe;
    boolean isListeningNetwork=false;

    private void checkForNetwork()
    {
    	network = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo inf = network.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      // boolean oldNetworkAvailable = isNetworkAvailable;
        isNetworkAvailable=inf.isConnected();
        processNetworkState();

        
        	
    }
    
    private void processNetworkState()
    {
    	
        if(isNetworkAvailable&&isRunning)
        {
        	turnAllOn();
        }
        else
        {
        	turnAllOff();
        	
        }
    }
    
    
    BroadcastReceiver networkStateListener = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(BroadcastReceiver.class.getSimpleName(), "action: "
                    + intent.getAction());
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
            isNetworkAvailable =state == WifiManager.WIFI_STATE_ENABLED;
            //checkForNetwork();
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
    
    private void turnAllOff()
    {
    	if(turnedOn)
    	{
    		doHangup();
    		
    		//videoReceiver.stopReceiveVideo();
    		turnedOn=false;
    	}
    }
    
    boolean turnedOn=false;
    private void turnAllOn()
    {
    	if(!turnedOn)
    	{
    		runCommands();
    		videoSender.startCamera();
    		audioSender.startVoice();
    		audioReceiver.startVoice();
    		videoReceiver.startReceiveVideo();
    		turnedOn=true;
    	}
    }

    @Override
    protected void onPause() {
        super.onPause();
       turnAllOff();
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
    	//stopPlayer();
    	videoReceiver.setToken(session_token);
    	audioSender.setToken(session_token);
    	audioReceiver.setToken(session_token);
    	videoSender.setToken(session_token);
    	checkForNetwork();
    	startListeningNetwork();
    	if(isRunning&&isNetworkAvailable)
    	{
    		turnAllOn();
    	}

    }
	
    
    @Override
    public void onDestroy() {
       // this.mWakeLock.release();
    	turnAllOff();
    	stopListeningNetwork();
        super.onDestroy();
    //    releaseMediaRecorder();

    }

    
	EditText emailET;
	//DatePicker timeoutD;
	//TimePicker timeoutT;
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
				textTimeout = (TextView)layout.findViewById(R.id.text_timeout);
				//timeoutD = (DatePicker) layout.findViewById(R.id.datePickerValidToDate);
				//timeoutD.setCalendarViewShown(false);
				//timeoutD.setSpinnersShown(true);
				
			
				//timeoutT= (TimePicker) layout.findViewById(R.id.timePickerValidToTime);
				//timeoutT.setIs24HourView(true);
				timeSelect= (SeekBar) layout.findViewById(R.id.seekBarLength);
				timeSelect.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						// TODO Auto-generated method stub
						textTimeout.setText(getResources().getString(R.string.sendLinkDlgExpires,progress+1));
					}
				});
				textTimeout.setText(getResources().getString(R.string.sendLinkDlgExpires,1));
				//timeSelect.setProgress(1);
				//timeSelect.setProgress(0);
				
				Button buttonOk = (Button)layout.findViewById(R.id.buttonOk);
				Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
				buttonOk.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {

						setEmail(emailET.getText().toString());
						//long cur = Calendar.getInstance().getTimeInMillis();
						//long dateTo = timeoutD.getCalendarView().getDate();
						//long timeTo = timeoutT.getCurrentHour()*1000*60*60+timeoutT.getCurrentMinute()*1000*60;
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
						if(isRunning)
						{
							startButton.toggle();
						}
						
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

	@Override
	protected void onPrepareDialog (int id, Dialog dialog) 
	{
		switch (id)
		{
			case SEND_CONTROL_LINK_DIALOG:
			{
				final Calendar c = Calendar.getInstance();
				//timeoutD.setMinDate(c.getTimeInMillis()-10000);
				//timeoutD.setMaxDate(c.getTimeInMillis()+((long)(Integer.MAX_VALUE-60*60*24))*1000);
				c.add(Calendar.DAY_OF_YEAR, 1);				
				//timeoutD.updateDate(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
				//timeoutT.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
				//timeoutT.setCurrentMinute(c.get(Calendar.MINUTE));
				
				
			}
		};
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
					Toast.makeText(AvatarMainActivity.this, R.string.toastInviteOk, Toast.LENGTH_LONG).show();
					startButton.toggle();
				}
				else
				{
					Toast.makeText(AvatarMainActivity.this, getResources().getString(R.string.toastInviteFail, r.getString("message")), Toast.LENGTH_LONG).show();
				}
			}
			catch(JSONException e)
			{
				Log.e("ConnectionResponceHandler", "onConnectionSuccessful", e);
				Toast.makeText(AvatarMainActivity.this,R.string.toastInviteHz, Toast.LENGTH_LONG).show();
				
			}
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			// TODO Auto-generated method stub
			Toast.makeText(AvatarMainActivity.this,getResources().getString(R.string.toastInviteServerRefuse, statusCode), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onConnectionFail(Throwable e) {
			// TODO Auto-generated method stub
			Toast.makeText(AvatarMainActivity.this,getResources().getString(R.string.toastInviteFailNoConnection, e.getMessage()) , Toast.LENGTH_LONG).show();
		
		}
		
	};
	

	

	protected void runCommands()
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
		drawDirection(leftEngineForward,leftEngineBackward,value);
		
	}

	public void doRight(int value)
	{
		drawDirection(rightEngineForward,rightEngineBackward,value);
	}
	



	public void drawDirection(TextView left, TextView right,int value)
	{
		if(value>0)
		{
			drawColor(left,right,255);
			long color = 0xff000000+(value<<16)+(value<<8)+value;
			wave.setBackgroundColor((int)color);

		}
		else
		if(value<0)
		{
			drawColor(right,left,255);
			value= -value;
			long color = 0xff000000+(value<<16)+(value<<8)+value;
			wave.setBackgroundColor((int)color);
		}
		else
		if(value==0)
		{
			drawColor(right,left,0);
			wave.setBackgroundColor(0);
			
		}
		wave.invalidate();
	}
	
	public void doYaw(int value)
	{
		
		drawDirection(yawUp,yawDown,value);
		

	}
	public void doPitch(int value)
	{
		drawDirection(pitchLeft,pitchRight,value);
		

	}
	//public void doWave(boolean value)
	//{
	//	/*wave.setBackgroundColor(value?0xffffffff:0xff000000);
	//	wave.invalidate();*/
	//}
	public void hitTheLights()
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

	}
	
	public void doHangup()
	{
			hitTheLights();
			protocolManager.stopCurrent();
		     videoSender.stopCamera();
		     videoReceiver.stopReceiveVideo();
		     audioSender.stopVoice();
		     audioReceiver.stopVoice();
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
	
	ProcessAsyncRequestResponceProrotype cmdConnectionResponse = new ProcessAsyncRequestResponceProrotype()
	{

		@Override
		protected void onConnectionSuccessful(Object responce) {
			//parceJson((String)responce);
			if(turnedOn)
			{
				hitTheLights();
				runCommands();
				//startButton.toggle();
			}
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			runCommands();
		}

		@Override
		protected void onConnectionFail(Throwable e) {
		//	Toast.makeText(AvatarMainActivity.this, String.format("Connection failed with message %s!",e.getMessage()), Toast.LENGTH_LONG).show();
			if(turnedOn)
			{
				hitTheLights();
				runCommands();
			}
		}
		
		ByteArrayOutputStream s = new ByteArrayOutputStream(10);
		DataOutputStream ds = new DataOutputStream(s);
		byte[] error={90,90,0,90,0,90,0};
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
					Log.v("ConnectionResponceHandler", "onConnectionSuccessful", e);
					return;
					//sendCommand(error);
					//Toast.makeText(Test1Activity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
				}
				
					s.reset();
					ds.writeByte(r.getInt("h"));
					ds.writeByte(r.getInt("a"));
					ds.writeShort(r.getInt("sa"));
					ds.writeByte(r.getInt("b"));
					ds.writeShort(r.getInt("sb"));
					ds.writeByte(r.getInt("c"));
					ds.writeShort(r.getInt("sc"));
					
					
					
					
					
					sendCommand(s.toByteArray());
				
				/*if (r.has("pitch"))
				{
					doPitch(r.getInt("pitch"));
				}
				//doWave(r.has("wave"));
				if(r.has("hangup"))
				{	
					startButton.toggle();
				}*/
				
			}
			catch(JSONException e)
			{
				Log.v("ConnectionResponceHandler", "onConnectionSuccessful", e);
				//sendCommand(error);
				//Toast.makeText(Test1Activity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
			}
			catch(IOException e)
			{
				Log.v("ConnectionResponceHandler", "onConnectionSuccessful", e);
				sendCommand(error);
				//Toast.makeText(Test1Activity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
			}
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
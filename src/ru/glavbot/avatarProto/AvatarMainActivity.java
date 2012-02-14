package ru.glavbot.avatarProto;



import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import de.mjpegsample.MjpegView.MjpegView;
import ru.glavbot.avatarProto.R;

//import com.ryong21.R;

import edu.gvsu.masl.asynchttp.ConnectionManager;
import edu.gvsu.masl.asynchttp.ConnectionResponceHandler;
import edu.gvsu.masl.asynchttp.HttpConnection;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

public class AvatarMainActivity extends Activity {
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
     private static final int SERVER_AUDIO_PORT = 10002;

     
     
     private  String email;
     private  String session_token;
     private  int  ttl;
     private boolean isRunning=false;
     
 	WebView webView;
 	ConnectivityManager network;
 	boolean isNetworkAvailable=false;

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
				if(isNetworkAvailable)
				{
					showDialog(SEND_CONTROL_LINK_DIALOG);
				}
				else
				{
					Toast.makeText(AvatarMainActivity.this, "No wifi connection available. Please enable wifi!", Toast.LENGTH_LONG).show();
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
						Toast.makeText(AvatarMainActivity.this, "No wifi connection available. Please enable wifi!", Toast.LENGTH_LONG).show();
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
		
		/*webView = (WebView) findViewById(R.id.webview);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setAppCacheEnabled(true);
		*/
		videoView= (MjpegView)findViewById(R.id.videoView);
		
		videoReceiver = new VideoReceiver(videoView,"http://dev.glavbot.ru/restreamer?oid=%s");
		
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        videoSender = new VideoSender(this, cameraPreview);
        audioSender = new AudioSender(this,SERVER_AUTHORITY,SERVER_AUDIO_PORT);
        network = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo inf = network.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        isNetworkAvailable=inf.isConnected();
        //senderThread.start();
        /* This code together with the one in onDestroy() 
         * will make the screen be always on until this Activity gets destroyed. */
      //  final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      // this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
      //  this.mWakeLock.acquire();

    }
    
    boolean isListeningNetwork=false;

    BroadcastReceiver networkStateListener = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(BroadcastReceiver.class.getSimpleName(), "action: "
                    + intent.getAction());
            //isNetworkAvailable =!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
            NetworkInfo inf = network.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            isNetworkAvailable=inf.isConnected();

        }

    };
    
    public void startListeningNetwork() {
    	if(!isListeningNetwork)
    	{
    		IntentFilter filter = new IntentFilter();
    		filter.addAction("android.intent.action.SERVICE_STATE");
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
    	doHangup();
        ConnectionManager.getInstance().stopCurrent();
        videoReceiver.stopReceiveVideo();
    }
    
    private void turnAllOn()
    {
    	runCommands();
    	videoSender.startCamera();
    	audioSender.startVoice();
    	videoReceiver.startReceiveVideo();
    	
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
    	startListeningNetwork();
    	if(isRunning)
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
	DatePicker timeoutD;
	TimePicker timeoutT;
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
				builder.setTitle("Enter email!");
				alertDialog = builder.create();
				emailET = (EditText) layout.findViewById(R.id.editTextEmail);
				timeoutD = (DatePicker) layout.findViewById(R.id.datePickerValidToDate);
				timeoutD.setCalendarViewShown(false);
				timeoutD.setSpinnersShown(true);
				
			
				timeoutT= (TimePicker) layout.findViewById(R.id.timePickerValidToTime);
				timeoutT.setIs24HourView(true);
				Button buttonOk = (Button)layout.findViewById(R.id.buttonOk);
				Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
				buttonOk.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {

						setEmail(emailET.getText().toString());
						long cur = Calendar.getInstance().getTimeInMillis();
						long dateTo = timeoutD.getCalendarView().getDate();
						long timeTo = timeoutT.getCurrentHour()*1000*60*60+timeoutT.getCurrentMinute()*1000*60;
						long ttl = ((dateTo+timeTo) - cur)/1000;
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
				timeoutD.updateDate(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
				timeoutT.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
				timeoutT.setCurrentMinute(c.get(Calendar.MINUTE));
				
				
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
			HttpConnection connection = new HttpConnection(
					shareConnectionHandler);
			connection.get(uri.toString());
		}
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
					Toast.makeText(AvatarMainActivity.this, "Invite sent successfully, waiting for commands", Toast.LENGTH_LONG).show();
					startButton.toggle();
				}
				else
				{
					Toast.makeText(AvatarMainActivity.this, "Invite sending failed with message \r"+r.getString("message"), Toast.LENGTH_LONG).show();
				}
			}
			catch(JSONException e)
			{
				Log.e("ConnectionResponceHandler", "onConnectionSuccessful", e);
				Toast.makeText(AvatarMainActivity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
				
			}
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			// TODO Auto-generated method stub
			Toast.makeText(AvatarMainActivity.this, String.format("Server returned %d, try again later",statusCode), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onConnectionFail(Exception e) {
			// TODO Auto-generated method stub
			Toast.makeText(AvatarMainActivity.this, String.format("Connection failed with message %d!",e.getMessage()), Toast.LENGTH_LONG).show();
		
		}

		@Override
		protected void onDataPart(String responce) {
			// TODO Auto-generated method stub
			
		}
		
	};
	

	

	protected void runCommands()
	{
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SERVER_SCHEME).authority(SERVER_AUTHORITY).path(CMD_PATH)
		.appendQueryParameter(TOKEN_PARAM, session_token)
		.appendQueryParameter(MODE_PARAM, MODE_PARAM_VALUE);
		Uri uri=builder.build();
		HttpConnection connection = new HttpConnection(cmdConnectionHandler);
		connection.setPollingMode(true);
		connection.get(uri.toString());
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
			drawColor(leftEngineForward,leftEngineBackward,255);
			long color = 0xff000000+(value<<16)+(value<<8)+value;
			wave.setBackgroundColor((int)color);

		}
		else
			if(value<0)	
		{
			drawColor(leftEngineBackward,leftEngineForward,255);
			value= -value;
			long color = 0xff000000+(value<<16)+(value<<8)+value;
			wave.setBackgroundColor((int)color);
		}
		else
			if(value==0)
			{
				drawColor(leftEngineBackward,leftEngineForward,0);
				wave.setBackgroundColor(0);
				
			}
		wave.invalidate();
	}

	public void doRight(int value)
	{
		if(value>0)
		{
			drawColor(rightEngineForward,rightEngineBackward,255);
			long color = 0xff000000+(value<<16)+(value<<8)+value;
			wave.setBackgroundColor((int)color);

		}
		else
		if(value<0)
		{
			drawColor(rightEngineBackward,rightEngineForward,255);
			value= -value;
			long color = 0xff000000+(value<<16)+(value<<8)+value;
			wave.setBackgroundColor((int)color);
		}
		else
		if(value==0)
		{
			drawColor(rightEngineBackward,rightEngineForward,0);
			wave.setBackgroundColor(0);
			
		}
		wave.invalidate();
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
	public void doHangup()
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
		     videoSender.stopCamera();
		     audioSender.stopVoice();
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
			parceJson(responce);
			if(isRunning)
			{
				runCommands();
				//startButton.toggle();
			}
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			runCommands();
		}

		@Override
		protected void onConnectionFail(Exception e) {
		//	Toast.makeText(AvatarMainActivity.this, String.format("Connection failed with message %s!",e.getMessage()), Toast.LENGTH_LONG).show();
			if(isRunning)
			{
				runCommands();
			}
		}

		protected void parceJson(String responce)
		{
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
				//doWave(r.has("wave"));
				if(r.has("hangup"))
				{	
					startButton.toggle();
				}
				
			}
			catch(JSONException e)
			{
				Log.v("ConnectionResponceHandler", "onConnectionSuccessful", e);
				//Toast.makeText(Test1Activity.this, "Unknown server responce. Possibly fail", Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onDataPart(String responce) {
			parceJson(responce);
		}
		
	};
	
	
	public String getSession_token() {
		return session_token;
	}

	public void setSession_token(String session_token) {
		this.session_token = session_token;
		getSharedPreferences (SHARED_PREFS,Context.MODE_PRIVATE ).edit().putString(SHARED_PREFS_TOKEN, session_token).apply();
		videoReceiver.setToken(session_token);
	}

}
package ru.glavbot.test1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
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
     //email: ���� ������� ������ �� ������������� ������?
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

        /* This code together with the one in onDestroy() 
         * will make the screen be always on until this Activity gets destroyed. */
      //  final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      // this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
      //  this.mWakeLock.acquire();

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
    	
        super.onDestroy();
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
	
	
	
	protected void startCamera()
	{
		try {
			socket = new Socket(InetAddress.getByName(SERVER_AUTHORITY), SERVER_VIDEO_PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			return;
		}
		pfd = ParcelFileDescriptor.fromSocket(socket);
		
	}
	
	protected void stopCamera()
	{
		;
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
		}
		else
		{
		//	runCommands();
		}
	}
	/*
	 *     ������ ������ ����� ��������� � ���� JSON-������ (����� ������) � ����� ��� ����� �� ��������� �����: left, right, yaw, pitch, wave, hangup.
    left, right, yaw, pitch ��������� �������� �� -255 �� 255; ������ ������� ������������� ��� �������� �������� 1�1�� �� �������� ����������. ��������� ���� ����� ���� �� ���������� �����: ���� �������� ���� n >= 0, �������� ������ �������� � ���������� ���� ������ �������� � #nnnnnn; � ��������� ������ �������� ������ �������� � ���������� ���� ������ �������� � #nnnnnn.

	������� ������ �������� ������ ��������
	left	#1				#2
	right	#3				#4
	yaw		#5				#6
	pitch	#7				#8
	������� wave �������������� �����: � ������ �� ������� � ������ ����� ���������� ���� �������� #9 � #FFFFFF, � ������ �� ���������� - � #000000.
	� ������ ������� � ������ ������� hangup ����� ���� �������� ������������ � #000000 � ���������� �������� ����� �� ������������ (����� ������� �����).
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
			Toast.makeText(Test1Activity.this, String.format("Connection failed with message %s!",e.getMessage()), Toast.LENGTH_LONG).show();
			startButton.toggle();
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
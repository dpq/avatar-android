package ru.glavbot.avatarProto;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

public class AccessoryProcessor extends Activity {
	
	private static final String TAG = "RoboRuler"; 

	private static final String ACTION_USB_PERMISSION = "ru.glavbot.avatarProto.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private BufferedInputStream mInputStream=null;
	//protected FileOutputStream mOutputStream;
	protected FileOutputStream mOutputStream=null;
	
	protected Object sync=new Object();
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (sync) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				synchronized (sync) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (accessory != null && accessory.equals(mAccessory)) {
						closeAccessory();
						try {
							Process process = new ProcessBuilder()
						       .command("/system/bin/su")
						       .start();
								OutputStream o =process.getOutputStream();
								o.write("/system/bin/reboot -p\n".getBytes());

						} catch (Exception e) {
							Toast.makeText(getApplicationContext(), "fail!", Toast.LENGTH_LONG).show();
						} 
					}	


				}


			}
		}
	};
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	    filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	    filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);

		registerReceiver(mUsbReceiver, filter);

		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}


        
    }

 //   byte[] commandData = new byte[7];
    


@Override
public Object onRetainNonConfigurationInstance() {
	if (mAccessory != null) {
		return mAccessory;
	} else {
		return super.onRetainNonConfigurationInstance();
	}
}


protected void sendCommand(byte[] commandData) {
	// TODO Auto-generated method stub
	synchronized (sync) {
		if(mOutputStream!=null)
		{
			try {
				mOutputStream.write(commandData,0,commandData.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("","",e);
			
			}
		}
	}
}

volatile byte[] readerData = {};//new byte[commandLength];
Thread readerThread= null;
Thread watcherThread= null;

protected void readCommand(int commandLength) {
	// TODO Auto-generated method stub
	readerData=new byte[commandLength];
	synchronized (sync) {
		if(mInputStream!=null)
		{
			
			//try {
				

				
			       readerThread = new Thread() {
			            public void run() {                
			                try {
			                	int read=0;
			                	int totalRead=0;
			                	while(totalRead<readerData.length)
			                	{
			                		read=mInputStream.read(readerData,totalRead, readerData.length-totalRead);
			                		if(read>0)
			                		{
			                			totalRead+=read;
			                		}
			                		
			                	}
			                	
								int tmp1=readerData[1];
								int chrg=(tmp1<<8)+readerData[0]; 
								if(chrg>0)
									reportCharge(chrg);
								
			                } catch (Exception e) {
			                  
			                } 
			            }
			        };

			        
			        watcherThread=  new Thread() {
			            public void run() {   
			            	readerThread.start();
			            	try {
								readerThread.join(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			            }
			        };
			        
			        watcherThread.start();
			        
				
					
					
				//	return readerData;
				

		}
	//	return readerData;
	}
}

private void reportCharge(int chrg)
{
	Message msg=mainThreadHandler.obtainMessage(AvatarMainActivity.READ_CHARGE_STATE);
	msg.arg1=chrg;
	mainThreadHandler.sendMessage(msg);
}



@Override
protected void onResume() {
	super.onResume();
	synchronized (sync) {
	//Intent intent = getIntent();
		if (mInputStream != null && mOutputStream != null) {
			return;
		}

		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,mPermissionIntent);
						mPermissionRequestPending = true;
				}
			}
		}
	} else {
		Log.d(TAG, "mAccessory is null");
	}
	}
}

@Override
protected void onPause() {
	super.onPause();
	closeAccessory();
}

@Override
public void onDestroy() {
	unregisterReceiver(mUsbReceiver);
	closeAccessory();
	super.onDestroy();
}

protected Handler mainThreadHandler;



Handler getHandler()
{
	return mainThreadHandler;
}


private void openAccessory(UsbAccessory accessory) {
	mFileDescriptor = mUsbManager.openAccessory(accessory);
	if (mFileDescriptor != null) {
		mAccessory = accessory;
		FileDescriptor fd = mFileDescriptor.getFileDescriptor();
		mInputStream =new BufferedInputStream( new FileInputStream(fd));
		mOutputStream =  new FileOutputStream(fd);
		//Thread thread = new Thread(this, "DemoKit");
		//thread.start();
		/*try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Log.d(TAG, "accessory opened"); 
		//enableControls(true);
	} else {
		Log.d(TAG, "accessory open fail");
	}
}

private void closeAccessory() {
	//enableControls(false);
	try {
	if(mInputStream!=null)
	{
		mInputStream.close();
	}
	} catch (IOException e) {
	} finally {
		mInputStream=null;

	}
	
	try {
	if(mOutputStream!=null)
	{
		mOutputStream.close();
	}
	} catch (IOException e) {
	} finally {
		mOutputStream=null;

	}
	
	try {

		
		if (mFileDescriptor != null) {
			mFileDescriptor.close();
		}
	} catch (IOException e) {
	} finally {
		mFileDescriptor = null;
		mAccessory = null;

	}
}
}

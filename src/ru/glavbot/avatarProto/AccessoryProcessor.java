package ru.glavbot.avatarProto;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class AccessoryProcessor extends Activity {
	
	private static final String TAG = "RoboRuler";

	private static final String ACTION_USB_PERMISSION = "ru.glavbot.avatarProto.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	protected FileOutputStream mOutputStream;
	
	
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
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
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
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


@Override
protected void onResume() {
	super.onResume();

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
					mUsbManager.requestPermission(accessory,
							mPermissionIntent);
					mPermissionRequestPending = true;
				}
			}
		}
	} else {
		Log.d(TAG, "mAccessory is null");
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
	super.onDestroy();
}

private void openAccessory(UsbAccessory accessory) {
	mFileDescriptor = mUsbManager.openAccessory(accessory);
	if (mFileDescriptor != null) {
		mAccessory = accessory;
		FileDescriptor fd = mFileDescriptor.getFileDescriptor();
		mInputStream = new FileInputStream(fd);
		mOutputStream = new FileOutputStream(fd);
		//Thread thread = new Thread(this, "DemoKit");
		//thread.start();
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

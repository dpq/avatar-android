package ru.glavbot.avatarProto;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import ru.glavbot.customLogger.AVLogger;


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

public abstract class AccessoryProcessor extends Activity {
	
	private static final String TAG = "RoboRuler"; 

	private static final String ACTION_USB_PERMISSION = "ru.glavbot.avatarProto.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	private UsbAccessory mAccessory;
	
	protected AVLogger avLogger;
	
	protected boolean isAccessoryAvailable()
	{
		boolean avail=false;
		synchronized (sync)
		{
			avail=(mAccessory!=null);
		}
		return avail;
	}
	
	private ParcelFileDescriptor mFileDescriptor;
	private volatile BufferedInputStream mInputStream=null;
	//protected FileOutputStream mOutputStream;
	protected volatile FileOutputStream mOutputStream=null;
	
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
						AVLogger.d(TAG, "permission denied for accessory "
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
						/*	Process process = new ProcessBuilder()
						       .command("/system/bin/su")
						       .start();
								OutputStream o =process.getOutputStream();
								o.write("/system/bin/reboot -p\n".getBytes());*/

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
        avLogger= new AVLogger(Log.INFO);
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
    

/*
@Override
public Object onRetainNonConfigurationInstance() {
	if (mAccessory != null) {
		return mAccessory;
	} else {
		return super.onRetainNonConfigurationInstance();
	}
}*/
    
    protected abstract void readChargeState(int charge);
    


volatile WriterThread writer=null;
volatile ReaderThread reader= null;

protected static class WriterThread extends Thread
{
	FileOutputStream  os;
	protected LinkedBlockingQueue<byte[]> commands = new LinkedBlockingQueue<byte[]>(2);
	Handler errHandler;
	WriterThread(FileOutputStream mOS,Handler h)
	{
		this.setName("ArduinoWriter");
		this.os=mOS;
		errHandler=h;
	}
	
	public LinkedBlockingQueue<byte[]> getCommands()
	{
		return commands;
	}
	
	
	public void run()
	{
		while (!isInterrupted())
		{
			

			try{
			byte[] cmd =commands.take();
				os.write(cmd,0,cmd.length);
			
			}catch (IOException e) {
				// TODO Auto-generated catch block
			AVLogger.e("","",e);
			errHandler.obtainMessage(WRITE_DATA_ERROR).sendToTarget();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				AVLogger.e("","",e);
			}
		}
		
	}
	
	
	
};

protected static class ReaderThread extends Thread
{
	BufferedInputStream  is;
	protected Vector<byte[]> commands = new Vector<byte[]>();
	Handler handler;
	ReaderThread(BufferedInputStream mIS,Handler h)
	{
		this.setName("ArduinoReader");
		this.is=mIS;
		handler=h;
	}
	
	byte[] cmd= new byte[2];
	
	public void run()
	{
		while (!isInterrupted())
		{
			try {
            	int read=0;
            	int totalRead=0;
            	while(totalRead<cmd.length)
            	{
            		read=is.read(cmd,totalRead, cmd.length-totalRead);
            		if(read>0)
            		{
            			totalRead+=read;
            		}
            		
            	}
            	
				int tmp1=cmd[1];
				int chrg=(tmp1<<8)+cmd[0]; 
				if(chrg>0)
				{
					Message msg=handler.obtainMessage(READ_CHARGE_STATE);
					msg.arg1=chrg;
					handler.sendMessage(msg);
				}
			}
			catch (IOException e)
			{
				
			}
		}
		
	}
	
	
	
};


protected void sendCommand(byte[] commandData) {
	// TODO Auto-generated method stub
//	reopenAccessory();
	synchronized (sync) {
		if(mOutputStream!=null&&writer!=null)
		{
			writer.getCommands().offer(commandData);
		}
	/*try {
		mOutputStream.write(commandData,0,commandData.length);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	}
}





public static final int READ_CHARGE_STATE = 1004;
public static final int WRITE_DATA_ERROR = 1005;

/*
volatile boolean tryingRead=false; 

protected void readCommand(int commandLength) {
	// TODO Auto-generated method stub
	
	synchronized (sync) {
		if(mInputStream!=null)
		{
			//reopenAccessory();
			readerData=new byte[commandLength];
			if(!tryingRead)
			{
				tryingRead=true;
				Thread watcherThread=  new Thread() {
						
			        	Thread readerThread = new Thread() {
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
			            public void run() {   
			            	readerThread.start();
			            	try {
								readerThread.join(1000);
								tryingRead=false;
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
								mainThreadHandler.obtainMessage(WRITE_DATA_ERROR).sendToTarget();
							}
			            }
			        };
			        
			        watcherThread.start();
		}
		}

	}
}

*/

/*
private void reportCharge(int chrg)
{
	Message msg=mainThreadHandler.obtainMessage(READ_CHARGE_STATE);
	msg.arg1=chrg;
	mainThreadHandler.sendMessage(msg);
}

*/

@Override
protected void onResume() {
	super.onResume();
	if(mainThreadHandler == null)
	{
		throw new RuntimeException("mainThreadHandler is not initialized!!!!");
	}
	requestAccessory();
}


protected void reopenAccessory()
{
	synchronized (sync) {
		closeAccessory();
	}
	requestAccessory();
}


protected void requestAccessory()
{
		synchronized (sync) {

			// Intent intent = getIntent();
			if (mInputStream != null && mOutputStream != null) {
				return;
			}

			UsbAccessory[] accessories = mUsbManager.getAccessoryList();
			UsbAccessory accessory = (accessories == null ? null
					: accessories[0]);
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
				AVLogger.d(TAG, "mAccessory is null");
			}
		}
}


@Override
protected void onPause() {
	super.onPause();
	synchronized (sync) {
	closeAccessory();
	}
}

@Override
public void onDestroy() {
	unregisterReceiver(mUsbReceiver);
	synchronized (sync) {
		closeAccessory();
	}
	super.onDestroy();
}


protected class AccessoryHandler extends Handler
{
	 public void handleMessage(Message msg) {
      	
      	switch (msg.what)
      	{
      		case READ_CHARGE_STATE:
      			readChargeState(msg.arg1);
      			break;
      		case WRITE_DATA_ERROR:
      			reopenAccessory();
      			break;


      	};
				
      }
}


protected AccessoryHandler mainThreadHandler;



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
		//tryingRead=false;
		//Thread thread = new Thread(this, "DemoKit");
		//thread.start();
		/*try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader = new ReaderThread(mInputStream,mainThreadHandler);
		writer = new WriterThread(mOutputStream,mainThreadHandler);
		writer.start();
		reader.start();
		
		
		AVLogger.d(TAG, "accessory opened"); 
		//enableControls(true);
	} else {
		AVLogger.d(TAG, "accessory open fail");
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
	
	if(reader!=null)
	{
		reader.interrupt();
		reader=null;
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
	
	if(writer!=null)
	{
		writer.interrupt();
		writer=null;
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

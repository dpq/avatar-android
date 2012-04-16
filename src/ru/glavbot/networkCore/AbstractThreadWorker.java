package ru.glavbot.networkCore;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class AbstractThreadWorker extends Thread {

	private static final int HAS_ERROR = -1;

	private static final long DEFAULT_DELAY = 10000;
	
	private final Object sync= new Object();
	
	private AbstractWorkerHandler workerHandler=null;

	private boolean isRunning=false;
	
	public AbstractThreadWorker()
	{
		super();
		initFeedbackHandler();
		start();
		try {
			synchronized(sync)
			{
				sync.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			
		}
		
	}
	
	@Override
	public final void run()
	{
		synchronized(sync){
			 
	        Looper.prepare();
	        workerHandler=createWorker();
	        sync.notifyAll();
		 };
	        
	     Looper.loop();
	}
	
	
	
	
	
	protected abstract AbstractWorkerHandler createWorker();
	

	public static abstract class AbstractWorkerHandler  extends Handler{
		public static final int INIT = 0;
	    public static final int RUN = 1;
	    public static final int DONE = 2;

	    Handler errorHandler;
	    
	    
	    public AbstractWorkerHandler(Handler errorHandler)
	    {
	    	super();
	    	this.errorHandler=errorHandler;
	    }
	    
	    
	    
		public final void handleMessage(Message msg) {
	    	
	    	switch (msg.what)
	    	{
	    		case INIT:
	    			doInit();
	    			break;
	    		case RUN:
	    			doRun(msg);
	    			break;
	    		case DONE:
	    			doDone();
	    			break;
	    		default:
	    			throw new RuntimeException("Unknown command to AbstractWorkerHandler thread");
	    	};
			
	    }

		protected abstract void init() throws Exception;

		protected abstract void run(Message msg) throws Exception;

		protected abstract void done();	
		
		
		
		private void doInit()
		{
			try{
				init();
			}
			catch (Exception e)
			{
				errorHandler.obtainMessage(HAS_ERROR,e).sendToTarget();
			}
			
		}

		private void doRun(Message msg)
		{
			try{
				run (msg );
			}
			catch (Exception e)
			{
				errorHandler.obtainMessage(HAS_ERROR,e).sendToTarget();
			}
		}

		private void doDone()
		{
			removeMessages(INIT);
			removeMessages(RUN);
			done();
			
		}
	}
	
	
	public void startWork()
	{
		workerHandler.obtainMessage(AbstractWorkerHandler.INIT).sendToTarget();
		setRunning(true);
	}
	
	public void startWorkDelayed(long ms)
	{
		Message msg = workerHandler.obtainMessage(AbstractWorkerHandler.INIT);
		workerHandler.sendMessageDelayed(msg, ms);
		setRunning(true);
	}
	
	
	public void stopWork()
	{
		workerHandler.obtainMessage(AbstractWorkerHandler.DONE).sendToTarget();
		setRunning(false);
	}
	
	
	
	protected void restart()
	{
		stopWork();
		startWorkDelayed(DEFAULT_DELAY);
	}
	
	
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}


	protected Handler feedbackHandler;

	protected void initFeedbackHandler()
	{
		feedbackHandler=new Handler()
			{
				public  void handleMessage(Message msg) {
			    	
			    	switch (msg.what)
			    	{
			    		case HAS_ERROR:
			    			restart();
			    			break;
			    		default:
			    			throw new RuntimeException("Unknown command to errorHandler in "+AbstractThreadWorker.class.getSimpleName());
			    	};
					
			    }
			};
	}
	
}

package edu.gvsu.masl.asynchttp;

import java.util.ArrayList;

/**
 * Simple connection manager to throttle connections
 * 
 * @author Greg Zavitz
 */
public class ConnectionManager {
	
	//public static final int MAX_CONNECTIONS = 5;

	//private ArrayList<Runnable> active = new ArrayList<Runnable>();
	private ArrayList<HttpConnection> queue = new ArrayList<HttpConnection>();
	private Thread thread=null;
	private static ConnectionManager instance;

	public static ConnectionManager getInstance() {
		if (instance == null)
			instance = new ConnectionManager();
		return instance;
	}

	private boolean isThreadActive()
	{
		return (thread!=null) && thread.isAlive();
	}
	public void push(HttpConnection runnable) {
		queue.add(runnable);
		if (!isThreadActive())
			startNext();
	}

	private void startNext() {
		if (!queue.isEmpty()) {
			HttpConnection next = queue.get(0);
			//queue.remove(0);
			//active.add(next);

			thread = new Thread(next);
			thread.start();
		}
	}

	public void stopCurrent() {
		if(isThreadActive())
		{
			//active.remove(runnable);
			HttpConnection curr =queue.get(0);
			curr.setRun(false);
			thread.interrupt();
			thread=null;
			queue.remove(0);
		}
		startNext();
	}
	
	public void didComplete(HttpConnection runnable) {
		queue.remove(runnable);
		startNext();
	}

}

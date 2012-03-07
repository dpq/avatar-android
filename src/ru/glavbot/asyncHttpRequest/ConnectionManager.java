package ru.glavbot.asyncHttpRequest;

import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;

public class ConnectionManager {
	private ArrayList<ConnectionRequest> queue = new ArrayList<ConnectionRequest>();

	private ConnectionRunner runner=null;
	public void push(ConnectionRequest request) {
		queue.add(request);
		if (runner==null)
			startNext();
	}

	private DefaultHttpClient client = new DefaultHttpClient();
	
	private void startNext() {
		if(runner!=null)
			throw new RuntimeException("Makaronas i spagetti");
		
		if (!queue.isEmpty()) {
			ConnectionRequest next = queue.get(0);
			runner = new ConnectionRunner(this);
			runner.execute(next);
		}
	}

	public void stopCurrent() {
		if(runner!=null)
		{
			
			runner.cancel(true);
			
				runner=null;
				queue.remove(0);
			
			//else
			//	throw new RuntimeException("Running task considered immortal. Kill it by throwing your tab into the trash");
		}
		startNext();
	}

	public DefaultHttpClient getClient() {
		return client;
	}


}

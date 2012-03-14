package ru.glavbot.asyncHttpRequest;

import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;

public class ConnectionManager {
	private ArrayList<ConnectionRequest> queue = new ArrayList<ConnectionRequest>();

	private AbstractConnectionRunner runner=null;
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
			{
				switch(next.getProcessingType())
				{
				case ConnectionRequest.READ_ALL:
					runner = new ReadAllConnectionRunner(this);
					break;
				case ConnectionRequest.READ_STRINGS_ONE_BY_ONE:
					runner = new ReadStringsConnectionRunner(this);
					break;
				case ConnectionRequest.RETURN_REQUEST_ENTITY:
					runner = new ReturnEntityConnectionRunner(this);
					break;
				default:
					throw new RuntimeException("ConnectionManager::startNext: wrong request type!");
				}
			//runner = new AbstractConnectionRunner(this);
			
			}
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

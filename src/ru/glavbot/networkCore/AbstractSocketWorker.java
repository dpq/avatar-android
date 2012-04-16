package ru.glavbot.networkCore;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import android.util.Log;

public abstract class AbstractSocketWorker extends AbstractThreadWorker{

	private static final int MAX_BUFFER = 32768;

	protected volatile boolean isRecording = false;
    
	protected String host;
	protected int port;
	private String token;

	
    
    public AbstractSocketWorker(String host,int port)
    {
    	super();
    	this.host=host;
    	this.port=port;
    }
    
    
    
    public static abstract class  AbstractSocketHandler extends AbstractWorkerHandler
	{

    	private int inputBuffer = MAX_BUFFER;
    	private int outputBuffer = MAX_BUFFER;
    	
    	
    	AbstractSocketWorker owner;
    	public AbstractSocketHandler(AbstractSocketWorker owner)
    	{
    		super(owner.feedbackHandler);
    		this.owner=owner;

    		
    	}

		protected Socket socket = null;
		@Override
		protected void init() throws Exception {
			// TODO Auto-generated method stub
			InetAddress addr=null;
			
			try{
				addr = InetAddress.getByName(owner.host);
			
				socket = new Socket(addr, owner.port);
				socket.setKeepAlive(true);
				//socket.setTcpNoDelay(true);
				socket.setSoTimeout(10000);
				socket.setSendBufferSize(outputBuffer);
				socket.setReceiveBufferSize(inputBuffer);
				/*OutputStream s = socket.getOutputStream();
				String ident = "ava-"+owner.getToken();
				if(s!=null)
				{
					s.write(ident.getBytes());
					s.flush();
				}*/
			}catch (Exception e)
			{
				socket=null;
				throw e;
			}
			
		}


		@Override
		protected void done() {
			// TODO Auto-generated method stub
			try {
				if(socket!=null)
				socket.close();
			} catch (IOException e) {
				Log.e("","",e);
			}
			socket=null;
		}
		
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

};
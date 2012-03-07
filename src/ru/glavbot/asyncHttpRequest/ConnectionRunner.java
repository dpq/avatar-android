package ru.glavbot.asyncHttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;





import android.os.AsyncTask;
import android.util.Log;

class ConnectionRunner extends AsyncTask<ConnectionRequest,AsyncRequestResponse,AsyncRequestResponse> {

	ConnectionRequest request=null;
	
	ConnectionManager owner;
	
	ConnectionRunner(ConnectionManager owner)
	{
		this.owner=owner;
		if (owner==null)
			throw new RuntimeException("Go to hell, owner should not be null");
	}
	
	
	
	@Override
	protected AsyncRequestResponse doInBackground(ConnectionRequest... params) {
		request = params[0];
		DefaultHttpClient client = owner.getClient();
		HttpConnectionParams.setSoTimeout(client.getParams(), request.getTimeout());
		HttpResponse response = null;
		AsyncRequestResponse asyncResponce=null;
		try {
			
			switch (request.getMethod()) {
			case ConnectionRequest.GET:
				response = client.execute(new HttpGet(request.getUrl()));
				break;
			case ConnectionRequest.POST:
				HttpPost httpPost = new HttpPost(request.getUrl());
				httpPost.setEntity(new StringEntity(request.getData()));
				response = client.execute(httpPost);
				break;
			case ConnectionRequest.PUT:
				HttpPut httpPut = new HttpPut(request.getUrl());
				httpPut.setEntity(new StringEntity(request.getData()));
				response = client.execute(httpPut);
				break;
			case ConnectionRequest.DELETE:
				response = client.execute(new HttpDelete(request.getUrl()));
				break;
			}
			asyncResponce=processResponce(response,request.shouldReadAll());
		} catch (Exception e) {
			

			if(response!=null)
			{
				try {
					response.getEntity().getContent().close();
					
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					Log.e("","",e1);
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.e("","",e1);
					
				}
			}
			asyncResponce= new AsyncRequestResponse(AsyncRequestResponse.STATUS_INTERNAL_ERROR,null,e);
		}
		
		return asyncResponce;
	}

	private AsyncRequestResponse processResponce(HttpResponse responce, boolean readAll) throws IllegalStateException,
	IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(responce.getEntity()
					.getContent()));
			String line, result = "";
			AsyncRequestResponse  rr=null;
			try {
				while (((line = br.readLine()) != null)&&!isCancelled())
				{
	
					if(!readAll)
					{
						publishProgress(new AsyncRequestResponse(AsyncRequestResponse.STATUS_PROGRESS,line,null ));
					}
					else
					{
						result += line;
					}
				}
				if(readAll)
				{
					 rr = new AsyncRequestResponse(responce.getStatusLine().getStatusCode(),result ,null);
				}
			} catch (Exception e) {
					Log.e("", "", e);
					br.close();
					responce.getEntity().getContent().close();
					rr = new AsyncRequestResponse(AsyncRequestResponse.STATUS_INTERNAL_ERROR,null,e);
			}
			return rr;
	}
	
	@Override
	protected void onProgressUpdate (AsyncRequestResponse... values)
	{
		IProcessAsyncRequestResponse progressProcessor = request.getProgressProcessor();
		if(progressProcessor!=null)
		{
			progressProcessor.processAsyncRequestResponse(values[0]);
		}
	}
	@Override
	protected void onPostExecute (AsyncRequestResponse result)
	{
		owner.stopCurrent();
		IProcessAsyncRequestResponse answerProcessor = request.getAnswerProcessor();
		if(answerProcessor!=null)
		{
			answerProcessor.processAsyncRequestResponse(result);
		}
	}
}

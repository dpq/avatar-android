package ru.glavbot.asyncHttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;

import android.util.Log;

public class ReadStringsConnectionRunner extends AbstractConnectionRunner {

	ReadStringsConnectionRunner(ConnectionManager owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AsyncRequestResponse processResponce(HttpResponse responce)
			throws IllegalStateException, IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(responce.getEntity()
				.getContent()));
		String line;
		AsyncRequestResponse  rr=null;
		//int code = responce.getStatusLine().getStatusCode();
		//if(code>0)
		try {
			while (((line = br.readLine()) != null)&&!isCancelled())
			{
					publishProgress(new AsyncRequestResponse(AsyncRequestResponse.STATUS_PROGRESS,line,null ));
			}
			rr = new AsyncRequestResponse(responce.getStatusLine().getStatusCode(),null,null);
		} catch (Exception e) {
				Log.e("", "", e);
				br.close();
				responce.getEntity().consumeContent();
				rr = new AsyncRequestResponse(AsyncRequestResponse.STATUS_INTERNAL_ERROR,null,e);
		}
		return rr;
	}

}

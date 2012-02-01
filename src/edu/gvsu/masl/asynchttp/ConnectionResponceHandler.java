package edu.gvsu.masl.asynchttp;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class ConnectionResponceHandler {
	private Handler handler;

	Handler getNativeHandler() {
		return handler;
	};

	public ConnectionResponceHandler() {
		handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case HttpConnection.DID_SUCCEED: {
					ReadedResponce resp = (ReadedResponce) message.obj;
					if ((resp.getStatus() >= 200) && (resp.getStatus() < 300)) {
						onConnectionSuccessful(resp.getText());
					} else {
						onConnectionUnsuccessful(resp.getStatus());
					}

					break;
				}
				case HttpConnection.DID_ERROR: {
					Exception e = (Exception) message.obj;
					Log.e("ConnectionResponceHandler", "handleMessage", e);
					onConnectionFail(e);
					break;
				}
				case HttpConnection.GOT_PART: {
					ReadedResponce resp = (ReadedResponce) message.obj;
					onDataPart(resp.getText());
					break;
				}
				
				};
			};
		};
	};

	protected abstract void onConnectionSuccessful(String responce);
	protected abstract void onDataPart(String responce);

	protected abstract void onConnectionUnsuccessful(int statusCode);

	protected abstract void onConnectionFail(Exception e);

};

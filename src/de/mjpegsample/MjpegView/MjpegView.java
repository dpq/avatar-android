package de.mjpegsample.MjpegView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;



import edu.gvsu.masl.asynchttp.ConnectionResponceHandler;
import edu.gvsu.masl.asynchttp.HttpConnection;
import edu.gvsu.masl.asynchttp.ReadedResponce;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
    public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;
    
    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;    
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;    
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;
    private Context context;
    private int surfaceWidth;
    private int surfaceHeight;
    
    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private Bitmap ovl;
       
         
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { mSurfaceHolder = surfaceHolder; }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN) return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }
         
        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }
         
        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width()+2;
            int bheight = b.height()+2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left+1, (bheight/2)-((p.ascent()+p.descent())/2)+1, p);
            return bm;        	 
        }

        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            int width;
            int height;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            String fps = "";
            while (mRun&&(!interrupted())) {
                if(surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                                bm = mIn.readMjpegFrame();
                                destRect = destRect(bm.getWidth(),bm.getHeight());
                                c.drawColor(Color.BLACK);
                                c.drawBitmap(bm, null, destRect, p);
                                if(showFps) {
                                    p.setXfermode(mode);
                                    if(ovl != null) {
                                    	height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom-ovl.getHeight();
                                    	width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right -ovl.getWidth();
                                        c.drawBitmap(ovl, width, height, null);
                                    }
                                    p.setXfermode(null);
                                    frameCounter++;
                                    if((System.currentTimeMillis() - start) >= 1000) {
                                        fps = String.valueOf(frameCounter)+"fps";
                                        frameCounter = 0; 
                                        start = System.currentTimeMillis();
                                        ovl = makeFpsOverlay(overlayPaint, fps);
                                    }
                                }
                            } catch (IOException e) {}
                        }
                    } finally { if (c != null) mSurfaceHolder.unlockCanvasAndPost(c); }
                }
            }
        }
    }

    private void init(Context context) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        this.context=context;
        
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = MjpegView.POSITION_LOWER_RIGHT;
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }
    
    public void startPlayback() { 
        if(mIn != null) {
            mRun = true;
            thread = new MjpegViewThread(getHolder(), context);
            thread.setSurfaceSize(surfaceWidth, surfaceHeight); 
            thread.start();    		
        }
    }
    
    public void stopPlayback() { 
        mRun = false;
        initializing=false;
        boolean retry = true;
        try {
        	if(mIn!=null)
			{
        		mIn.close();
			}
		} catch (IOException e1) {			
		}
        while(retry) {
            try {
            	
            	if(thread!=null)
            	{
            		thread.interrupt();
            		thread.join();
            		Log.d("",thread.isAlive()?"alive":"dead");
            		thread=null;
            	}
            	retry = false;
            } catch (InterruptedException e) {}
        }
    }

    public MjpegView(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) { 
    	if(thread !=null)
    	{
    		thread.setSurfaceSize(w, h); 
    	}
    	surfaceWidth=w;
    	surfaceHeight=h;
    }

    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
    }
    
    public MjpegView(Context context) { super(context); init(context); }    
    public void surfaceCreated(SurfaceHolder holder) { surfaceDone = true; }
    public void showFps(boolean b) { showFps = b; }
    private void setSource(MjpegInputStream source) { mIn = source; startPlayback();}
    public void setOverlayPaint(Paint p) { overlayPaint = p; }
    public void setOverlayTextColor(int c) { overlayTextColor = c; }
    public void setOverlayBackgroundColor(int c) { overlayBackgroundColor = c; }
    public void setOverlayPosition(int p) { ovlPos = p; }
    public void setDisplayMode(int s) { displayMode = s; }
    
    
    
    private static class MJpegInputStreamConnector implements Runnable
    {

    	MJpegInputStreamConnector(String url, ConnectionResponceHandler handler)
    	{
    		this.url=url;
    		this.m_handler=handler.getNativeHandler();
    	};
    	 private String url= null;
    	 private Handler m_handler=null;
    	 private HttpResponse res= null;

		public void run() {
			// TODO Auto-generated method stub

			if (url == null || m_handler == null)
				throw new RuntimeException(
						"MJpegInputStreamConnector started without thread or handler");

			DefaultHttpClient httpclient = new DefaultHttpClient();
			Message message;
			try {
				res = httpclient.execute(new HttpGet(URI.create(url)));
				ReadedResponce rr = new ReadedResponce(res.getStatusLine()
						.getStatusCode(), res.getEntity().getContent());
				message = Message.obtain(m_handler, HttpConnection.DID_SUCCEED,
						rr);
			} catch (ClientProtocolException e) {
				Log.e("", "", e);
				message = Message
						.obtain(m_handler, HttpConnection.DID_ERROR, e);
			} catch (IOException e) {
				Log.e("", "", e);
				message = Message
						.obtain(m_handler, HttpConnection.DID_ERROR, e);
			}

			m_handler.sendMessage(message);

		}
		        
		        
		        
		        
			
    	
    };
   
    
    
    private String url;
    private boolean initializing=false;
    
    public void requestRead(String url) {
    	this.url=url;
    	initializing=true;
    	MJpegInputStreamConnector r = new MJpegInputStreamConnector(url,openStreamHandler);
    	Thread t = new Thread(r);
    	t.start();
    }
    
    
    
    ConnectionResponceHandler openStreamHandler= new ConnectionResponceHandler()
    {

		@Override
		protected void onConnectionSuccessful(Object responce) {
			// TODO Auto-generated method stub
			 setSource(new MjpegInputStream((InputStream) responce));
			 initializing=false;
			 //view.setSource();
			 setDisplayMode(MjpegView.SIZE_BEST_FIT);
			 showFps(true);
			 
		}

		@Override
		protected void onDataPart(Object responce) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onConnectionUnsuccessful(int statusCode) {
			// TODO Auto-generated method stub
			if(initializing)
				requestRead(url);
			
		}

		@Override
		protected void onConnectionFail(Exception e) {
			// TODO Auto-generated method stub
			if(initializing)
				requestRead(url);
		}
    	
    };
    
    
}
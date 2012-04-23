package de.mjpegsample.MjpegView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

//import java.util.Properties;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MjpegInputStream extends DataInputStream {
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final byte[] CONTENT_LENGTH = {'C','o','n','t','e','n','t','-','L','e','n','g','t','h'};
    private final byte[] COLON ={':'};
    private final byte[] EOL_MARKER = { (byte)0x0D,(byte) 0x0A };
    
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;
  //private static Socket socket;
	

	
    public MjpegInputStream(InputStream in) { super(new BufferedInputStream(in, FRAME_MAX_LENGTH)); }
	
    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for(int i=0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if(c == sequence[seqIndex]) {
                seqIndex++;
                if(seqIndex == sequence.length) return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }
	
    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        DataInputStream headerIn = new DataInputStream(new ByteArrayInputStream(headerBytes));
        int contentLengthEnd=getEndOfSeqeunce(headerIn,CONTENT_LENGTH);
        if(contentLengthEnd>0)
        {
        	int afterSemicolon=getEndOfSeqeunce(headerIn,COLON);
        	if(afterSemicolon>0)
        	{
        		headerIn.mark(HEADER_MAX_LENGTH);
        		int crLfPos=getEndOfSeqeunce(headerIn,EOL_MARKER);
        		headerIn.reset();
        		if(crLfPos>0)
        		{
        			
        			String length =  headerIn.readLine();
        			return Integer.parseInt(length);
        		}
        	}
        }

        return Integer.parseInt("huita"); // numberFormatException))
    }	

    public Bitmap readMjpegFrame() throws IOException {
    	Log.v("VideoReceiver", "recieving image");
    	mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        byte[] header = new byte[headerLen];
        readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) { 
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER); 
        }
        reset();
        byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
    
   /* @Override
    public void Close()
    {
    	
    }*/
    
}
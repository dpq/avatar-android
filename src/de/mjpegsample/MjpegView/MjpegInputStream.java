package de.mjpegsample.MjpegView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MjpegInputStream extends DataInputStream {
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;
  //private static Socket socket;
	

	private byte[] header = new byte[HEADER_MAX_LENGTH];
	static private byte[] emptySpace= new byte[HEADER_MAX_LENGTH];
	private byte[] frameData = new byte[FRAME_MAX_LENGTH];
	private ByteArrayInputStream dataIn = new ByteArrayInputStream(frameData);
	private ByteArrayInputStream headerIn = new ByteArrayInputStream(header);
	private Properties props = new Properties();
	static {
		for (int i=0;i<HEADER_MAX_LENGTH;i++)
		{
			emptySpace[i]=(byte)0x10;
		}
	}
    
    public MjpegInputStream(InputStream in) { super(new BufferedInputStream(in, FRAME_MAX_LENGTH)); 
    	dataIn.mark(FRAME_MAX_LENGTH);
    	headerIn.mark(HEADER_MAX_LENGTH);
    }
	
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

    private int parseContentLength(/*byte[] headerBytes*/) throws IOException, NumberFormatException {
        //ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
    	headerIn.reset();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }	

    public Bitmap readMjpegFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        if(headerLen>HEADER_MAX_LENGTH)
        {
        	
        	int bytesToSkip=getEndOfSeqeunce(this, EOF_MARKER);
        	skipBytes(bytesToSkip);
        	return null;
        }
        //reset();
       // byte[] header = new byte[headerLen];
        System.arraycopy(emptySpace, 0, header, 0, HEADER_MAX_LENGTH);
        readFully(header,0,headerLen);
        try {
            mContentLength = parseContentLength();
        } catch (NumberFormatException nfe) { 
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER)-headerLen; 
        }
        reset();
        //byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData,0,mContentLength);
        dataIn.reset();
        return BitmapFactory.decodeStream(dataIn);
    }
    
   /* @Override
    public void Close()
    {
    	
    }*/
    
}
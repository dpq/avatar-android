package ru.glavbot.avatarProto;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import android.util.Log;

public class RoboDriver {

	
	private static final double Pi = Math.acos(0)*2;
	// resoultion
	private static final int RESOLUTION = 10; // ms
	private static final double TURN_SPEED = 1 /*8*Pi/1000*RESOLUTION*/;
	private static final double ACCELERATION = 4;
	private static final int WHEEL_CRUISE_SPD = 252;
	private static final String TAG="RoboDriver";

	//volatile OutputStream commandWriter= null; 
	private volatile int[] compensationAngles={0,0,0};
	private volatile int[] wheelDirections = {1,1,1};
	//int[] servoDirections = {1,1,1};
	
	public synchronized void setCompensationAngles(int[] newAngles)
	{
		for(int i=0;i<compensationAngles.length;i++)
		compensationAngles[i]=newAngles[i];
	}
	public int[] getCompensationAngles()
	{
		return	compensationAngles;
	}
	public synchronized  void setWheelDirs(int[] newDirs)
	{
		for(int i=0;i<wheelDirections.length;i++)
			wheelDirections[i]=newDirs[i];
	}
	public int[] getWheelDirs()
	{
		return	wheelDirections;
	}
	
	
	ByteArrayOutputStream s = new ByteArrayOutputStream(10);
	DataOutputStream ds = new DataOutputStream(s);
//	byte[] error={90,90,0,90,0,90,0};

ScheduledThreadPoolExecutor timer= new ScheduledThreadPoolExecutor(1);

Runnable worker = new Runnable(){

	public void run() {
		// TODO Auto-generated method stub
		try{
		servoLoop();
		s.reset();
		try {
			if(isChanged())
			{
				Log.v("Goes to arduino",String.format("%d", curHeadPos));
				ds.writeByte(curHeadPos);
				int compensedValue;
				for(int i =0;i<3;i++)
				{
					compensedValue=normalize(curWheelDirs[i]+compensationAngles[i]);
					ds.writeByte(compensedValue);
					compensedValue=(int)curWheelSpeeds[i]*wheelDirections[i];
					ds.writeShort(compensedValue);
				}
			/*	compensedValue=normalize(radToDeg(curWheelDirs[1])+compensationAngles[1]);
				ds.writeByte(compensedValue);
				ds.writeShort((int)curWheelSpeeds[1]);
				ds.writeByte(radToDeg(curWheelDirs[2]));
				ds.writeShort((int)curWheelSpeeds[2]);*/
				sendCommand(s.toByteArray());
				copyCurPrev();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("","",e);
			
		}
		counter++;
		if(counter==200)
		{
		String debug="";
		byte[] b =s.toByteArray();	
		
		for(int i=0;i<3;i++){
			
				debug+= String.format(" %d ",curWheelDirs[i]);
			}

		
		for(int i=0;i<b.length;i++){
		int v = b[i];
		v&=0x000000ff;
			debug+= String.format("%04d ",v);
		}
	//	Log.v("Goes to arduino",debug);
		counter=0;
		}
		//sendCommand(s.toByteArray());
		}catch(Exception e)
		{
			Log.e("RoboRuler", "This should never happen!");
		}
	}};
	
	static int counter = 0;
	
	private static int radToDeg(double rad)
	{
		return (int)( (rad*180.0)/Pi);
	}
	
	
	protected void headControl(double vOmega) {
		// TODO Auto-generated method stub
		tagHeadPos=normalize(curHeadPos+radToDeg(vOmega),120);
		
	}

	/*protected double normalize(double src)
	{
		if(src > Pi) {
			src = Pi;
	        Log.v(TAG,"dir >!!!!");
	    }
	    if(src < 0) {
	    	src = 0;
	        Log.v(TAG,"dir <!!!!");
	    }
	    return src;
	}*/
	protected int normalize(int src)
	{
		return normalize(src,180);
	}
	
	
	protected int normalize(int src, int max)
	{
		if(src > max) {
			src = max;
	        Log.v(TAG,"dir >!!!!");
	    }
	    if(src < 0) {
	    	src = 0;
	        Log.v(TAG,"dir <!!!!");
	    }
	    return src;
	}
	
	

	private AccessoryProcessor owner;
	public RoboDriver(AccessoryProcessor owner)
	{
		this.owner=owner;
		timer.scheduleAtFixedRate(worker, RESOLUTION, RESOLUTION, TimeUnit.MILLISECONDS);
		
	}
	
	public void setNewDirection(int newDir, double newOmega, double newVOmega)
	{
		calculateDesiredValues (newDir, newOmega);
		headControl(newVOmega);
	}
	
	public void reset()
	{
		setNewDirection(8, 0, Pi/2);
    	/*setSpeed(0, 0);
        setSpeed(1, 0);
        setSpeed(2, 0);*/
	}

	public double dePi(double val) {
		if(val > 2*Pi) val = val - 2*Pi;
		if(val < 0) val = val + 2*Pi;
		return val;
	}
	
	public void servoLoop() {
	    
	    // wheel speeds
	    for(int i=0;i<3;i++) {
	        if(tagWheelSpeeds[i] > curWheelSpeeds[i]) curWheelSpeeds[i] += ACCELERATION;
	        if(tagWheelSpeeds[i] < curWheelSpeeds[i]) curWheelSpeeds[i] -= ACCELERATION;
	        
	    	//curWheelDirs[i]=tagWheelDirs[i]; 
	   /*     if(tagWheelDirs[i] > curWheelDirs[i]) curWheelDirs[i] += TURN_SPEED;
	        if(tagWheelDirs[i] < curWheelDirs[i]) curWheelDirs[i] -= TURN_SPEED;*/
	        
	        // if (any of wheel) declination is > calculated omega: set speed to zero!
	        // calculate mean direction
	        double meandir = 0;
	        for(int j=0; j<3;j++) {
	        	meandir = meandir + curWheelDirs[i]; 
	        }
	    }
	    // head
	    if((curHeadPos<tagHeadPos)&&(tagHeadPos-curHeadPos>1))
	    {
	    	curHeadPos+=1;
	    }
	    else
	    if((curHeadPos>tagHeadPos)&&(curHeadPos-tagHeadPos>1))
	    {
	    	curHeadPos-=1;
	    }
	    	/*if(tagHeadPos > curHeadPos) curHeadPos += TURN_SPEED;
        if(tagHeadPos < curHeadPos) curHeadPos -= TURN_SPEED;*/
	 }
	
protected void sendCommand(byte[] byteArray) {
		owner.sendCommand(byteArray);
	}



// CURRENT POSITIONS

// Desired
volatile private int[] tagWheelSpeeds = {0,0,0}; // current wheel target speed
volatile private int[] tagWheelDirs = {90,90,120};
volatile int tagHeadPos=90;

//Real
volatile private int[] curWheelSpeeds = {0,0,0}; // current wheel speed
volatile private int[] curWheelDirs = {90,90,120};
//private int[] emuWheelDirs = {90,90,90};
volatile int curHeadPos=90;
// Prev send
//Real
volatile private int[] prevWheelSpeeds = {0,0,0}; // current wheel speed
volatile private int[] prevWheelDirs = {0,0,0};
volatile private int prevHeadPos = 0;


boolean isChanged()
{
	if(prevHeadPos!=curHeadPos) return true;
	for(int i=0;i<3;i++)
		if(prevWheelSpeeds[i]!=curWheelSpeeds[i]) return true;
	for(int i=0;i<3;i++)
		if(prevWheelDirs[i]!=curWheelDirs[i]) return true;
	return false;
}

void copyCurPrev()
{
	prevHeadPos=curHeadPos;
	for(int i=0;i<3;i++)
	prevWheelSpeeds[i]=curWheelSpeeds[i];
	for(int i=0;i<3;i++)
		prevWheelDirs[i]=curWheelDirs[i];
		
}


//CONSTANTS




/*
*
*  ARDUINO CODE HERE ---------------------------------------------------
*
*/

/*

 DIRECTIONS:

     0
  7     1  
6           2
  5     3
     4

*/

/*
   WHEEL_DIRECTIONS array doc:
   0-2: each wheel direction in radians
   3-5: wheels direction of rotation
   6: out-of-order wheel
   7: out-of-order magic coefficient 1 (angular_speed/angle) [s^-1]
   8: out-of-order magic coefficient 2 (speed/angle)
*/

//private static final int WDIR = 3;
private static final int OOW = 6;
private static final int WMK1 = 7;
private static final int WMK2 = 8;


private static final int[] WHEEL_SHIFT = {0, 0, 0}; // COMPUTE!! based on DEFAULT_ROT: WHEEL_SHIFT[i] = WHEEL_DIRECTIONS[0][i] - WHEEL_DEFAULT_ROT[i]; 
/*
// TowerPro SG-5010 SERVO MATRIX!
private static final double[][] WHEEL_DIRECTIONS = {
{ Pi/2+Pi/3  ,Pi/6,  Pi/2,  1, -1,-1,   2,   20, 2}, // 0   ^
{ Pi, Pi/2-Pi/6, Pi/2+Pi/6,  1,-1,-1,   0,  -20, 2}, //1    /' 
{Pi/2-Pi/6,  Pi/2+Pi/6,   0, -1, -1, 1,   0,   20, 2}, // 2   >
{  Pi/2+Pi/6,     0,Pi/3, -1, 1, 1,   0,   20, 2}, // 3   \.
{  Pi/2+Pi/3  ,Pi/6,  Pi/2,  -1, 1, 1,   2,   20, 2}, // 4   v
{ Pi, Pi/2-Pi/6, Pi/2+Pi/6,  -1,1,1,   1,   20, 2}, //5   ./_
{Pi/2-Pi/6,  Pi/2+Pi/6,   0, 1, 1, -1,   1,   20, 2}, // 6   <
{ Pi/2+Pi/6,     0,Pi/3, 1, -1, -1,   1,  -20, 2 }, // 7  '\
{	 Pi,     0,     0,  0, 0, 0,   -1,  50, 0} // SLEEP MODE
};
*/


// SpringRC servo matrix (Robo version)
private static final int[][] WHEEL_DIRECTIONS = {
{ 120,  60,  30, 1, 1, 1,  2, 10, 2}, // 0   ^
{  75,  15,  15, 1, 1, 1,  0, 10, 2}, // 1   /'   -20
{  30, 150,  120, 1,-1,-1,  0, 10, 2}, // 2   >
{ 165, 105,  75,-1,-1,-1,  0, 10, 2}, // 3   \.
{ 120,  60,  30,-1,-1,-1,  2, 10, 2}, // 4   v
{  75,  15,  15,-1,-1,-1,  1, 10, 2}, // 5   ./_
{  30, 150,  120,-1, 1, 1,  1, 10, 2}, // 6   <
{ 165, 105,  75, 1, 1, 1,  1, 10, 2}, // 7  '\ -20
{  90,  90,  120, 0, 0, 0, -1, 50, 0}, // SLEEP MODE
{  90,  90,  120, 0, 0, 0, -1, 50, 0} // SLEEP MODE2
};



/*
// THIS IS TEST MATRIX!!
private static final double[][] WHEEL_DIRECTIONS = {
{ Pi/6  ,Pi/6,  Pi/6,  1, -1,-1,   2,   20, 2}, // 0   ^
{ Pi/3, Pi/3, Pi/3,  1,-1,-1,   0,  -20, 2}, //1    /' 
{ Pi/2,  Pi/2,   Pi/2, -1, -1, 1,   0,   20, 2}, // 2   >
{ 2*Pi/3,     2*Pi/3,2*Pi/3, -1, 1, 1,   0,   20, 2}, // 3   \.
{ Pi-Pi/6 ,Pi-Pi/6,  Pi-Pi/6,  -1, 1, 1,   2,   20, 2}, // 4   v
{ Pi, Pi, Pi,  -1,1,1,   1,   20, 2}, //5   ./_
{Pi/2-Pi/6,  Pi/2+Pi/6,   0, 1, 1, -1,   1,   20, 2}, // 6   <
{ Pi/2+Pi/6,     0,Pi/3, 1, -1, -1,   1,  -20, 2 }, // 7  '\
{	 0,     0,     0,  0, 0, 0,   -1,  50, 0} // SLEEP MODE
};
*/

/*
WHEEL_DIRECTIONS array doc:
0-2: each wheel direction in radians
3,4,5: wheels direction of rotation
6: out-of-order wheel
7: out-of-order magic coefficient 1 (angular_speed/angle) [s^-1]
8: out-of-order magic coefficient 2 (speed/angle)
*/






private void setWheelsDirection(int[] dir_array, int oow, int oow2) {
   for(int i=0;i<3;i++) {
       if(i != oow) setDirection(i, dir_array[i] - WHEEL_SHIFT[i]);
   }
}

private void setWheelsSpeed(int[] dir_array,int  spd,int oow, int oow2) {
   for(int i=0;i<3;i++) {
       if(i != oow) setSpeed(i, spd * dir_array[3+i]);
   }
}




private void calculateDesiredValues (int direction, double omega) {
//	int iOmega=radToDeg(omega);
    if(direction == 9) {
    	setSpeed(0, 0); // speed direction: absolutely magic!
        setSpeed(1, 0);
        setSpeed(2, 0);
    } else {
	if(Math.abs(omega) > 0.3) { // we got the angular speed
	    if(direction == 8) { // SLEEP (we do not move any direction)
	    	setWheelsDirection(WHEEL_DIRECTIONS[direction], -1,0);
	        int spd = (int)(omega * (WHEEL_CRUISE_SPD/Pi)); /* * omega / WHEEL_DIRECTIONS[direction][WMK1];*/
	        setSpeed(0, spd); // speed direction: absolutely magic!
	        setSpeed(1, -spd);
	        setSpeed(2, spd);
	    } else { // we have the direction of movement AND angular speed
	        
	        // Out-of-order wheel parameters calculation
	        int oo_diffang = (int)((180/Pi)*omega / WHEEL_DIRECTIONS[direction][WMK1]);
	        int oo_wheelnum = (int) WHEEL_DIRECTIONS[direction][OOW];
	        int oo_wheelangle = WHEEL_DIRECTIONS[direction][ oo_wheelnum ] + oo_diffang;
	            // OO-wheel speed uses WHEEL_CRUISE_SPD as base parameter (which is 2/3 full throttle) and magic coeffs of current direction
	        //double oo_wheelspeed =  WHEEL_DIRECTIONS[direction][3+oo_wheelnum]*WHEEL_CRUISE_SPD * (1+ ( Math.abs(oo_diffang) / WHEEL_DIRECTIONS[direction][WMK2]));
	        int oo_wheelspeed =  WHEEL_CRUISE_SPD;

	        setDirection(oo_wheelnum, oo_wheelangle - WHEEL_SHIFT[oo_wheelnum]);
	        setSpeed(oo_wheelnum, oo_wheelspeed * WHEEL_DIRECTIONS[direction][3+oo_wheelnum]);

	        // Now just set all-other wheels direction and speed TODO: for double-OOW situation, do not!
	        setWheelsDirection(WHEEL_DIRECTIONS[direction], (int)WHEEL_DIRECTIONS[direction][OOW]/* except this wheel (?) */,0);
	        //setWheelsSpeed(WHEEL_DIRECTIONS[direction], WHEEL_DIRECTIONS[direction][3+oo_wheelnum]*WHEEL_CRUISE_SPD,(int) WHEEL_DIRECTIONS[direction][OOW],0);
	        setWheelsSpeed(WHEEL_DIRECTIONS[direction], (int)WHEEL_CRUISE_SPD,(int) WHEEL_DIRECTIONS[direction][OOW],0);
	    }
	    
	} else { // we do not have angular speed, only current direction (including direction = SLEEP MODE (no move)
	    setWheelsDirection(WHEEL_DIRECTIONS[direction], -1,0);
	    setWheelsSpeed(WHEEL_DIRECTIONS[direction], (int)WHEEL_CRUISE_SPD, -1,0);
	}
    }
}


private void setDirection(int wheelNum, int dir) {
    tagWheelDirs[wheelNum] = normalize(dir);
    curWheelDirs[wheelNum] = normalize(dir);
}





private void setSpeed(int wheelNum, int spd) {
    tagWheelSpeeds[wheelNum] = spd;
}


}

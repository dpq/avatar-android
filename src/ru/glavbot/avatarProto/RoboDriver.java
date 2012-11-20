package ru.glavbot.avatarProto;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.glavbot.customLogger.AVLogger;



public class RoboDriver {

	
	private static final double Pi = Math.PI; // equals to JS pi
	// resoultion
	private static final int RESOLUTION = 10; // ms
	private static final double ACCELERATION = 4;
	private static final int WHEEL_CRUISE_SPD = 252;
	private static final String TAG="RoboDriver";

	
	private volatile long chargeWatchDog=System.currentTimeMillis(); 
	private static final int CHARGE_DELAY=60000;
	private int needReadCharge = 0;
	private int prevNeedReadCharge = 0;
	

	ByteArrayOutputStream s = new ByteArrayOutputStream(13);
	DataOutputStream ds = new DataOutputStream(s);


	
ScheduledThreadPoolExecutor timer= new ScheduledThreadPoolExecutor(1);


	Runnable worker = new Runnable() {

		public void run() {

			synchronized (synchronizer) {
				try {
					servoLoop();
					s.reset();
					try {
						if (isChanged()) {

							ds.writeByte(servosEnabled);
							ds.writeByte(curHeadPos);
							int compensedValue;
							for (int i = 0; i < 3; i++) {
								compensedValue = normalize(curWheelDirs[i]/* +compensationAngles[i]*/);
								ds.writeByte(compensedValue);
								compensedValue = (int) curWheelSpeeds[i]/*wheelDirections[i] */;
								ds.writeShort(compensedValue);
							}

							ds.writeByte(curLEDlight);
							ds.writeByte(needReadCharge);
							sendCommand(s.toByteArray());
							copyCurPrev();

							if (needReadCharge != 0) {
								needReadCharge = 0;
							}
							
							
							String debug = "";
							byte[] b = s.toByteArray();

							/*for (int i = 0; i < 3; i++) {

								debug += String.format(" %d ", curWheelDirs[i]);
							}*/

							for (int i = 0; i < b.length; i++) {
								int v = b[i];
								v &= 0x000000ff;
								debug += String.format("%03d ", v);
							}
							AVLogger.v("Goes to arduino", debug);
							
							
						}
					} catch (IOException e) {

						AVLogger.e("", "", e);

					}
					counter++;
					if (counter == 200) {
						
						counter = 0;
					}
				} catch (Exception e) {
					AVLogger.wtf("RoboRuler", "This should never happen!");
				}
			}
		}
	};
	
	

	
	
	static int counter = 0;
	Object synchronizer = new Object();
	
	
	protected void headControl(double vOmega) {
	
		tagHeadPos=normalize((int)vOmega,180);
		
	}

	public void updateLuxmeterValue(float luxmeter)
	{
	    synchronized(synchronizer)
	    {
	    	luxmeterValue=luxmeter;
	    }
	}
	
	
	
	protected int normalize(int src)
	{
		return normalize(src,180);
	}
	
	
	protected int normalize(int src, int max)
	{
		if(src > max) {
			src = max;
	        AVLogger.v(TAG,"dir >!!!!");
	    }
	    if(src < 0) {
	    	src = 0;
	        AVLogger.v(TAG,"dir <!!!!");
	    }
	    return src;
	}
	
	

	private AccessoryProcessor owner;
	public RoboDriver(AccessoryProcessor owner)
	{
		this.owner=owner;

		
	}
	
	public  void start()
	{
		timer.scheduleAtFixedRate(worker, RESOLUTION, RESOLUTION, TimeUnit.MILLISECONDS);
	}
	
	public  void stop()
	{
		timer.remove(worker);
	}
	
	
	public void calibrate(int newDir, double newOmega, double newVOmega)
	{
		calibrationFlag=true;
		setNewDirection(newDir,newOmega,newVOmega,false);
	}
	
	
	public void setNewDirection(int newDir, double newOmega, double newVOmega)
	{
		setNewDirection( newDir,  newOmega,  newVOmega, true);
	}
	
	private void setNewDirection(int newDir, double newOmega, double newVOmega, boolean speed)
	{
	    synchronized(synchronizer)
	    {
	    	resetWatchdog();
	    	calculateDesiredValues (newDir, newOmega,speed);
	    	headControl(newVOmega);
	    }
	}
	
	public void reset()
	{
	    synchronized(synchronizer)
	    {
	    	resetWatchdog();
	    	calculateDesiredValues (8, 0,true);
	    	tagHeadPos=120;
	    }
	}
	
	public void toWork()
	{
	    synchronized(synchronizer)
	    {
	    	resetWatchdog();
	    	calculateDesiredValues (8, 0,true);
	    	tagHeadPos=70;
	    }
	}

	public double dePi(double val) {
		if(val > 2*Pi) val = val - 2*Pi;
		if(val < 0) val = val + 2*Pi;
		return val;
	}
	
	public void servoLoop() {
	    

		if(!inertion.inertionWorking())
		{
		    for(int i=0;i<3;i++) {
		        if(tagWheelSpeeds[i] > curWheelSpeeds[i]) curWheelSpeeds[i] += ACCELERATION;
		        if(tagWheelSpeeds[i] < curWheelSpeeds[i]) curWheelSpeeds[i] -= ACCELERATION;
		        
		        if(tagWheelSpeeds[i]==0) curWheelSpeeds[i]=0;
		        
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
	    //LED

	    if((luxmeterValue<=121)&&(curLEDlight<155))
	    {
	    	curLEDlight+=5;
	    }else if((luxmeterValue>1199)&&(curLEDlight>0))
	    {
	    	curLEDlight-=5;
	    }
	    // charge
	    if(System.currentTimeMillis()-chargeWatchDog>CHARGE_DELAY)
	    {
	    	chargeWatchDog=System.currentTimeMillis();
	    	needReadCharge=1;
	    }
	   
	    
	    
	    
	    synchronized(synchronizer)
	    {
	    	servosEnabled =(byte) ((/*((System.currentTimeMillis()-watchDog)>30000)||*/((System.currentTimeMillis()-cmdWatchDog)>2000))?0:1);
	    }
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
volatile int curLEDlight=0;
volatile float luxmeterValue=0;
// Prev send
//Real
volatile private int[] prevWheelSpeeds = {0,0,0}; // current wheel speed
volatile private int[] prevWheelDirs = {0,0,0};
volatile private int prevHeadPos = 0;
private int prevLedLight=0;

// prev dir for inertion

private int prevDir=8;
private double prevOmega=0;


private volatile long cmdWatchDog=System.currentTimeMillis(); 


byte servosEnabled=1;

byte prevServosEnabled = servosEnabled;

private volatile boolean calibrationFlag = false;

boolean isChanged()
{
	//return true;
	if(prevHeadPos!=curHeadPos) return resetWatchdog();
	for(int i=0;i<3;i++)
		if(prevWheelSpeeds[i]!=curWheelSpeeds[i]) return resetWatchdog();
	for(int i=0;i<3;i++)
		if(prevWheelDirs[i]!=curWheelDirs[i]) return resetWatchdog();
	if(prevLedLight!=curLEDlight) return resetWatchdog();
	if(prevServosEnabled != servosEnabled) 
		return true;
	if(needReadCharge!=prevNeedReadCharge)
		return true;
	if(calibrationFlag)
	{
		calibrationFlag = false;
		return true;
	}
	return false;
}

boolean resetWatchdog()
{
	
	cmdWatchDog=System.currentTimeMillis();
	servosEnabled=1;
	return true;
}


void copyCurPrev()
{
	prevHeadPos=curHeadPos;
	for(int i=0;i<3;i++)
	prevWheelSpeeds[i]=curWheelSpeeds[i];
	for(int i=0;i<3;i++)
		prevWheelDirs[i]=curWheelDirs[i];
	prevLedLight=curLEDlight;
	prevServosEnabled = servosEnabled;
	prevNeedReadCharge = needReadCharge;
}


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
//private static final int WMK2 = 8;



//SpringRC  servo matrix (Robo version)
public static final int[][] ETALON_WHEEL_DIRECTIONS = {
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



// SpringRC  servo matrix (Robo version)
public volatile static int[][] WHEEL_DIRECTIONS = {
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


private void setWheelsDirection(int[] dir_array, int oow, int oow2) {
   for(int i=0;i<3;i++) {
       if(i != oow) setDirection(i, dir_array[i]/* - WHEEL_SHIFT[i]*/);
   }
}

private void setWheelsSpeed(int[] dir_array,int  spd,int oow, int oow2) {
   for(int i=0;i<3;i++) {
       if(i != oow) setTagSpeed(i, spd * dir_array[3+i]);
   }
}
private static class Inertion {
	private static final int INERTION_TIME=90;
	private long inertionStart=System.currentTimeMillis();
	private boolean inertionEnabled=false;

	public void enableInertion()
	{
		inertionEnabled=true;
		inertionStart=System.currentTimeMillis();
	}

	public void disableInertion()
	{
		inertionEnabled=false;
	}

	
	public boolean inertionWorking()
	{
		return inertionEnabled && ((System.currentTimeMillis()-inertionStart)<INERTION_TIME);
	}

};

Inertion inertion = new Inertion();

	private void calculateDesiredValues(int direction, double omega,
			boolean speed) {

		int spd = (int) (omega * (((double) WHEEL_CRUISE_SPD) / Pi)); /*
																	 *  * omega /
																	 * WHEEL_DIRECTIONS
																	 * [
																	 * direction
																	 * ][WMK1];
																	 */
		if (direction == 9) {
			setTagSpeed(0, 0); // speed direction: absolutely magic!
			setTagSpeed(1, 0);
			setTagSpeed(2, 0);
		} else {
			if (Math.abs(omega) > 0.1) { // we got the angular speed
				if (direction == 8) { // SLEEP (we do not move any direction)
					setWheelsDirection(WHEEL_DIRECTIONS[direction], -1, 0);

					if (speed) {

						setTagSpeed(0, spd);
						setTagSpeed(1, -spd);
						setTagSpeed(2, spd);

					} else {
						setTagSpeed(0, 0);
						setTagSpeed(1, 0);
						setTagSpeed(2, 0);
					}
				} else { // we have the direction of movement AND angular speed
					inertion.disableInertion();
					// Out-of-order wheel parameters calculation
					int oo_diffang = (int) ((180 / Pi) * omega / WHEEL_DIRECTIONS[direction][WMK1]);
					int oo_wheelnum = (int) WHEEL_DIRECTIONS[direction][OOW];
					int oo_wheelangle = WHEEL_DIRECTIONS[direction][oo_wheelnum]+ oo_diffang;
					// OO-wheel speed uses WHEEL_CRUISE_SPD as base parameter
					// (which is 2/3 full throttle) and magic coeffs of current
					// direction
					// double oo_wheelspeed =
					// WHEEL_DIRECTIONS[direction][3+oo_wheelnum]*WHEEL_CRUISE_SPD
					// * (1+ ( Math.abs(oo_diffang) /
					// WHEEL_DIRECTIONS[direction][WMK2]));
					int oo_wheelspeed = WHEEL_CRUISE_SPD;

					setDirection(oo_wheelnum, oo_wheelangle);
					if (speed)
						setTagSpeed(oo_wheelnum, oo_wheelspeed
								* WHEEL_DIRECTIONS[direction][3 + oo_wheelnum]);

					// Now just set all-other wheels direction and speed TODO:
					// for double-OOW situation, do not!
					setWheelsDirection(WHEEL_DIRECTIONS[direction],
							(int) WHEEL_DIRECTIONS[direction][OOW]/*
																 * except this
																 * wheel (?)
																 */, 0);
					// setWheelsSpeed(WHEEL_DIRECTIONS[direction],
					// WHEEL_DIRECTIONS[direction][3+oo_wheelnum]*WHEEL_CRUISE_SPD,(int)
					// WHEEL_DIRECTIONS[direction][OOW],0);
					if (speed) {
						setWheelsSpeed(WHEEL_DIRECTIONS[direction],
								(int) WHEEL_CRUISE_SPD,
								(int) WHEEL_DIRECTIONS[direction][OOW], 0);
					} else {
						setTagSpeed(0, 0); // speed direction: absolutely magic!
						setTagSpeed(1, 0);
						setTagSpeed(2, 0);

					}

				}

			} else { // we do not have angular speed, only current direction
						// (including direction = SLEEP MODE (no move)

				if (speed) {
					if ((Math.abs(prevOmega) == Pi) && (prevDir == 8)
							&& (direction == 8)) {
						inertion.enableInertion();
					}

					if (direction != 8)
						inertion.disableInertion();
					setWheelsSpeed(WHEEL_DIRECTIONS[direction],
							speed ? WHEEL_CRUISE_SPD : 0, -1, 0);
				}

				setWheelsDirection(WHEEL_DIRECTIONS[direction], -1, 0);
			}
		}
		prevDir = direction;
		prevOmega = omega;
	}


private void setDirection(int wheelNum, int dir) {
    tagWheelDirs[wheelNum] = normalize(dir);
    curWheelDirs[wheelNum] = normalize(dir);
}

private void setTagSpeed(int wheelNum, int spd) {
    tagWheelSpeeds[wheelNum] = spd;
}

}

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <Servo.h>
#include <Wire.h>
#include <HPRGB2.h>

const int aPin=2;
const int bPin=5;
const int cPin=8;
const int headPin=49;

const int cmdLength=11;

AndroidAccessory acc("Cyberdyne Systems",
		     "RoboRuler",
		     "RoboRuler Arduino Board",
		     "1.0",
		     "http://www.glavbot.ru",
		     "0000000012345678");

HPRGB ledShield; // default mcp4728 id(0) and default PCA9685 id(0)

class WheelCaret{
  Servo srv;
  int basePin;
  public:
  short prevSpd;
  void init(int basePin)
  {
    this->basePin=basePin;
    pinMode(basePin, OUTPUT);
    pinMode(basePin+1, OUTPUT);
    srv.attach(basePin+2);
    prevSpd=0;
    
  }

  void process(unsigned char* data)
  {
    unsigned char angle=data[0];
    short spd=data[1];
    spd=(spd<<8)+data[2];
    if(srv.read()!=angle)
      srv.write(angle);
    if(spd!=prevSpd)
    {
        prevSpd=spd;
        if(spd>0)
        {
            analogWrite(basePin,0);
            analogWrite(basePin+1,(byte)spd);
        }
        else
        if(spd<0)
        {
            analogWrite(basePin+1,0);
            analogWrite(basePin,(byte)(-spd));
        }
        else
        {
            analogWrite(basePin+1,0);
            analogWrite(basePin,0);
        }
    }
  /* Serial.print("\r\nSpd: ");
   Serial.print(spd,DEC);
   Serial.print(" Angle: ");
   Serial.print(angle,DEC);
   Serial.print(" Pin: ");
   Serial.print(basePin,DEC);*/

  }
  
};


WheelCaret a,b,c;
Servo head;

void setup()
{
	Serial.begin(115200);
	Serial.print("\r\nStart");

	  // set the digital pin as output:
        //int current;
        //for(current = ledPinStart;current<ledPinStart+ledPinLen;current++)
        a.init(aPin);
        b.init(bPin);
        c.init(cPin);
        //pinMode(headPin, OUTPUT);   
	head.attach(headPin);
        acc.powerOn();
        ledShield.begin();
        ledShield.setCurrent(350,350,350); // set maximum current for channel 1-3 (mA)
        ledShield.setFreq(600);// operation frequency of the LED driver (KHz)
        ledShield.eepromWrite();// write current settings to EEPROM
        delay(100); // wait for EEPROM writing
}

        const byte defCommand[] = {90,90,0,0,90,0,0,120,0,0,0};


long timer = millis(); 
static unsigned char my_msg[cmdLength];

void setLed(short val)
{
  static short prevVal=255;
  if(val!=prevVal)
  {
    if(val>0)
    {
      ledShield.goToRGB(val, 255, 0);
    }
    else
    {
      ledShield.goToRGB(val, 0, 0);
    }
  }
}

void loop()
{
	//byte err;
	//byte idle;
	//static byte count = 0;
	
        //static int my_already_read=0;
        if(millis()-timer>10) { // sending 100 times per second
	    if (acc.isConnected()) {
                int curRead=acc.read(my_msg,cmdLength,1);
                  //my_msg[my_already_read]=curRead;
                 // my_already_read++;

                if(curRead==cmdLength)
                {
                    if(head.read()!=my_msg[0])
                       head.write(my_msg[0]);
                    //analogWrite(headPin, czzz);
                    a.process(my_msg+1);
                    b.process(my_msg+4);
                    c.process(my_msg+7);
                    //my_already_read=0;
                    setLed(my_msg[10]);
                    Serial.print("\r\ncmd: ");
                    for(int i =0; i< cmdLength;i++)
                        Serial.print(my_msg[i],DEC);
                }
	    }
            else
            {
              memcpy(my_msg,defCommand,cmdLength);
              if(head.read()!=my_msg[0])
                 head.write(my_msg[0]);
              //analogWrite(headPin, czzz);
              a.process(my_msg+1);
              b.process(my_msg+4);
              c.process(my_msg+7);
              setLed(my_msg[10]);
            }
        timer = millis();
        }
}


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
const int headControlPin=48;


const int cmdLength=13;

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
  byte isActive;
  public:
  short prevSpd;
  void init(int basePin/*,unsigned char* data*/)
  {
    this->basePin=basePin;
    pinMode(basePin, OUTPUT);
    pinMode(basePin+1, OUTPUT);
    enable();
    //process(data);
    prevSpd=0;

  }


  void disable()
  {
    if(isActive)
    {
      srv.detach();
      analogWrite(basePin,0);
      analogWrite(basePin+1,0);
      digitalWrite(basePin+2,LOW);
      isActive=false;
      
    }
  }
  void enable()
  {
    if(!isActive)
    {
      srv.attach(basePin+2);
      isActive=true;
    }
  }


  void process(unsigned char* data)
  {
    if(isActive)
    {
      unsigned char angle=data[0];
      short spd=data[1];
      spd=(spd<<8)+data[2];
      //if(srv.read()!=angle)
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

long timer = millis(); 
long chargeTimer=millis();

const byte defCommand[] = {1,120,90,0,0,90,0,0,120,0,0,0,0};
static unsigned char my_msg[cmdLength];

void setup()
{
	Serial.begin(115200);
	Serial.print("\r\nStart");
        Serial1.begin(24000);
	  // set the digital pin as output:
        //int current;
        //for(current = ledPinStart;current<ledPinStart+ledPinLen;current++)
        a.init(aPin);
        b.init(bPin);
        c.init(cPin);
        //pinMode(headPin, OUTPUT);   
	head.attach(headPin);
        pinMode(headControlPin, OUTPUT);
        digitalWrite(headControlPin,HIGH);
        acc.powerOn();
        ledShield.begin();
        ledShield.setCurrent(350,350,350); // set maximum current for channel 1-3 (mA)
        ledShield.setFreq(600);// operation frequency of the LED driver (KHz)
        ledShield.eepromWrite();// write current settings to EEPROM
        
        memcpy(my_msg,defCommand,cmdLength);
        
        delay(100); // wait for EEPROM writing
}


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




void setAll()
{
  if(my_msg[0]!=0)
  {
        //if(!head.attached())
         //   head.attach(headPin);
        digitalWrite(headControlPin,HIGH);
        delay(2);
        //if(head.read()!=my_msg[1])
        head.write(my_msg[1]);
        a.enable();
        b.enable();
        c.enable();
        a.process(my_msg+2);
        b.process(my_msg+5);
        c.process(my_msg+8);
        setLed(my_msg[11]);
       // Serial.print("\r\ncmd: ");
       // for(int i =0; i< cmdLength;i++)
       //     Serial.print(my_msg[i],DEC);
  }
  else
  {
    delay(2);
    digitalWrite(headControlPin,LOW);
   /* if(head.attached())
    {
        head.detach();
        digitalWrite(headPin,LOW);
    }
    a.disable();
    b.disable();
    c.disable();*/
  }
  if(my_msg[12]!=0)
  {
      if (acc.isConnected()) {
         int sensorValue = analogRead(A0);
         //byte data[2];
         //data[0]=(byte)sensorValue;
         //data[1]=(byte)(sensorValue>>8);
         
         
         Serial1.print(sensorValue,DEC);
         Serial1.print("\0");
         
         //acc.write(data,2, USB_NAK_NOWAIT);
         //Serial.print("\r\nData: ");
          //Serial.print(data[0],DEC);
          //Serial.print(data[1],DEC);
         //Serial.print("\r\nCharge: ");
         //Serial.print(sensorValue,DEC);
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
                int curRead=acc.read(my_msg,cmdLength,1 );
                  //my_msg[my_already_read]=curRead;
                 // my_already_read++;

                if(curRead==cmdLength)
                {
                  setAll();
                }
	    }
            else
            {
              memcpy(my_msg,defCommand,cmdLength);
              setAll();
              //delay(10);
            }
        timer = millis();
        }
        
      /*  if(millis()- chargeTimer>1000)
        {
           int sensorValue = analogRead(A0);
           byte data[2];
             data[0]=(byte)sensorValue;
             data[1]=(byte)(sensorValue>>8);
           if (acc.isConnected()) {


             acc.write(data,2);
             
             
           }
          Serial.print("\r\nData: ");
          Serial.print(data[0],DEC);
          Serial.print(data[1],DEC);
           
          Serial.print("\r\nCharge: ");
          Serial.print(sensorValue,DEC);
          chargeTimer=millis();
        }
        */
        
        
}


#include <Servo.h>

#define CMDLENGTH 5 

unsigned char cmd[CMDLENGTH]={0};


const int leftForwardPin =4;
const int leftBackwardPin= 5;
const int leftShimPin= 6;
const int rightForwardPin =8;
const int rightBackwardPin = 9;
const int rightShimPin = 10;
const int headPin= 11;


Servo head;


class Track{
    int fp;
  int bp;
  int sp;
  public:
  void init(int basePin)
  {
    fp = basePin;
    bp = basePin+1;
    sp = basePin+2;
    pinMode(fp, OUTPUT);
    pinMode(bp, OUTPUT);
    pinMode(sp, OUTPUT);
  }

  
  void beginSetValues(int dir, int val)
  {
     analogWrite(sp,val);
    if(dir >0)
    {
      digitalWrite(bp,LOW);

    }
    else
    {
      digitalWrite(fp,LOW);

    }
    if(val==0)
    {
      digitalWrite(bp,LOW);
      digitalWrite(fp,LOW);
    }
    
 

  }
  
  void endSetValues(int dir, int val)
  {
    if(dir >0)
    {

      digitalWrite(fp,HIGH);
    }
    else
    {

      digitalWrite(bp,HIGH);
    }

    


  }
  
};

Track left;
Track right;

long timer = millis(); 

void setup()
{
	Serial.begin(115200);
	
        left.init(leftForwardPin);
        right.init(rightForwardPin);

	head.attach(headPin);
}



void loop()
{

        if(millis()-timer>20) { // sending 50 times per second
	    if (Serial.available()>=CMDLENGTH) {
                for(int i=0; i<CMDLENGTH;i++)            
                {
                  cmd[i]=Serial.read();
                }
                left.beginSetValues(cmd[0],cmd[1]);
                right.beginSetValues(cmd[2],cmd[3]);
                head.write(cmd[4]);
                
                delay(10);
                
                left.endSetValues(cmd[0],cmd[1]);
                right.endSetValues(cmd[2],cmd[3]);


	    }

        timer = millis();
        }   
        
}


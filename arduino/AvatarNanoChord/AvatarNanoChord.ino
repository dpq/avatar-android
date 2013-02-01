#include <Servo.h>

#define CMDLENGTH 5 

unsigned char cmd[CMDLENGTH]={0};


const int leftForwardPin =4;
const int leftBackwardPin= 5;
const int leftShimPin= 6;
const int rightForwardPin =7;
const int rightBackwardPin = 8;
const int rightShimPin = 9;
const int headPin= 10;


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

  
  void setValues(int dir, int val)
  {
    if(dir >0)
    {
      digitalWrite(bp,LOW);
      digitalWrite(fp,HIGH);
    }
    else
    {
      digitalWrite(fp,LOW);
      digitalWrite(bp,HIGH);
    }
    if(val==0)
    {
      digitalWrite(bp,LOW);
      digitalWrite(fp,LOW);
    }
    
    analogWrite(sp,val);

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

        if(millis()-timer>10) { // sending 100 times per second
	    if (Serial.available()>=CMDLENGTH) {
                for(int i=0; i<CMDLENGTH;i++)            
                {
                  cmd[i]=Serial.read();
                }
                left.setValues(cmd[0],cmd[1]);
                right.setValues(cmd[2],cmd[3]);
                head.write(cmd[4]);

	    }

        timer = millis();
        }   
        
}


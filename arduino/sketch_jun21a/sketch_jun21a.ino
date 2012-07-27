

#include <Servo.h> 
 
Servo myservo;  // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
 
int pos = 0;    // variable to store the servo position 
 
void setup() 
{ 
  myservo.attach(9);  // attaches the servo on pin 9 to the servo object 
} 

long timer = millis();
 
void loop() 
{ 
  if(millis()-timer>500) { // sending 50 times per second
    myservo.write(random(0, 180)); 
    timer = millis();
  }
} 

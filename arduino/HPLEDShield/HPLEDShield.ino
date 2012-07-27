#include <Wire.h>
#include "HPRGB2.h"


HPRGB ledShield; // default mcp4728 id(0) and default PCA9685 id(0)

void setup()
{
  ledShield.begin();
  ledShield.setCurrent(350,350,350); // set maximum current for channel 1-3 (mA)
  ledShield.setFreq(600);// operation frequency of the LED driver (KHz)
  ledShield.eepromWrite();// write current settings to EEPROM
  delay(100); // wait for EEPROM writing
}


void loop()
{
  //ledShield.goToRGB(255,255,255); delay (3000); //go to RGB color (white) now
  //ledShield.goToRGB12(4095,4095,4095); delay (3000); //go to 12bit RGB (white) now
  //ledShield.goToRGB(255,0,0); delay (1000); //go to RGB color (red)
  //ledShield.goToRGB(0,0,0); delay (3000); //go to RGB color (red)


  for(int i = 15; i <= 255; i+=1) {
   ledShield.goToRGB(i, 0, 0);
   if (i > 150) {
   delay(4);
   }
   if ((i > 125) && (i < 151)) {
   delay(5);
   }
   if (( i > 100) && (i < 126)) {
   delay(7);
   }
   if (( i > 75) && (i < 101)) {
   delay(10);
   }
   if (( i > 50) && (i < 76)) {
   delay(14);
   }
   if (( i > 25) && (i < 51)) {
   delay(18);
   }
   if (( i > 1) && (i < 26)) {
   delay(19);
   }
   }
   for(int i = 255; i >=15; i-=1)
   {
   ledShield.goToRGB(i,0,0);
   analogWrite(11, i);
   if (i > 150) {
   delay(4);
   }
   if ((i > 125) && (i < 151)) {
   delay(5);
   }
   if (( i > 100) && (i < 126)) {
   delay(7);
   }
   if (( i > 75) && (i < 101)) {
   delay(10);
   }
   if (( i > 50) && (i < 76)) {
   delay(14);
   }
   if (( i > 25) && (i < 51)) {
   delay(18);
   }
   if (( i > 1) && (i < 26)) {
   delay(19);
   }
   }
   delay(970);
}

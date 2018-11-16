//  Project: TINHIS.
//  BTUltraSonic.ino  --  Arduino source file.
//  Revisions:
//    2018-11-11  --  F.R. van der Meulen --  Created.

//  Librabies
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

//  Set the LCD address to 0x3F for a 16 chars and 2 line display
LiquidCrystal_I2C lcd(0x3F, 16, 2);

//  Definitions.
#define trigPinOne 3
#define echoPinOne 4
#define pingPin 6

//  Variables.
long durationOne;
int distanceOne;
long durationTwo;
int distanceTwo;
String timestamp = "";
int customers = 0;

//  Booleans for the logics
bool ultrasonicOne = false;
bool ultrasonicTwo = false;

long timer = 0;

//  Setup code.
void setup() {
  pinMode(echoPinOne, INPUT);
  pinMode(trigPinOne, OUTPUT);
  digitalWrite(trigPinOne, LOW);
  Serial.begin(38400);

  lcd.begin();
  lcd.backlight();

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Customers: ");
  lcd.setCursor(11, 0);
  lcd.print(customers);

  //timestamp = "enter";
}

//  Loop code.
void loop() {
  //  Scan for objects.
  digitalWrite(trigPinOne, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPinOne, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPinOne, LOW);

  //  Retrieve pulse duration.
  durationOne = pulseIn(echoPinOne, HIGH);

  pinMode(pingPin, OUTPUT);
  digitalWrite(pingPin, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(pingPin, LOW);

  pinMode(pingPin, INPUT);
  durationTwo = pulseIn(pingPin, HIGH);

  //  Calculate distance to object.
  distanceOne = durationOne*0.034/2;
  distanceTwo = durationTwo*0.034/2;

  //  Act on results.
  if (distanceOne < 100) {
    ultrasonicOne = true;
    if (timestamp.equals("")) {
      timestamp = "enter";
    }
    
    //Serial.println(timestamp);
    //Serial.println("distanceOne");
    //Serial.println(distanceOne);
    delay(50);
  } else if (distanceTwo < 100) {
    ultrasonicTwo = true;
    if (timestamp.equals("")) {
      timestamp = "leave";
    }
    
    //Serial.println("distanceTwo");
    //Serial.println(distanceTwo);
    delay(50);
  } else {
    if (ultrasonicOne || ultrasonicTwo) {
      timer++;
    }
    if (ultrasonicOne && ultrasonicTwo) {
      if (timestamp != "") {
        Serial.println(timestamp);
        if (timestamp.equals("enter")) {
          customers++;
        }
        else if (timestamp.equals("leave")) {
          if (customers > 0) {
            customers--;
          }
        }
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Customers: ");
        lcd.setCursor(11, 0);
        lcd.print(customers);
      }
      timer = 0;
      ultrasonicOne = false;
      ultrasonicTwo = false;
      timestamp = "";
    }
    if (timer > 100) {
      timer = 0;
      ultrasonicOne = false;
      ultrasonicTwo = false;
      timestamp = "";
      //Serial.println("trigger ended");
    }
    delay(10);
  }
}

//  Librabies
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

//  Set the LCD address to 0x3F for a 16 chars and 2 line display
LiquidCrystal_I2C lcd(0x3F, 16, 2);

//  Definitions.
#define trigPinOne 3
#define echoPinOne 4
#define pingPin 6
#define ledPin 2

//  Variables.
long durationOne;
int distanceOne;
long durationTwo;
int distanceTwo;
String timestamp;
int customers = 0;

//  Setup code.
void setup() {
  pinMode(ledPin, OUTPUT);
  pinMode(echoPinOne, INPUT);
  pinMode(trigPinOne, OUTPUT);
  digitalWrite(ledPin, LOW);
  digitalWrite(trigPinOne, LOW);
  Serial.begin(38400);

  lcd.begin();
  lcd.backlight();

  timestamp = "enter";
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
    //  Turn on LED.
    digitalWrite(ledPin, HIGH);
    customers++;
    
    Serial.println(timestamp);
    Serial.println("distanceOne");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Customers: ");
    lcd.setCursor(11, 0);
    lcd.print(customers);
    delay(500);
  } else if (distanceTwo < 100) {
    //  Turn on LED.
    digitalWrite(ledPin, HIGH);
    customers++;
    
    Serial.println(timestamp);
    Serial.println("distanceTwo");
    Serial.println(distanceTwo);
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Customers: ");
    lcd.setCursor(11, 0);
    lcd.print(customers);
    delay(500);
  } else {
    //  If no object detected, turn off LED and wait.
    digitalWrite(ledPin, LOW);
  }
}

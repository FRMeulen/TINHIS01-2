//  Librabies
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

//  Set the LCD address to 0x3F for a 16 chars and 2 line display
LiquidCrystal_I2C lcd(0x3F, 16, 2);

//  Definitions.
#define trigPin 3
#define echoPin 4
#define ledPin 2

//  Variables.
long duration;
int distance;
String timestamp;
int customers = 0;

//  Setup code.
void setup() {
  pinMode(ledPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(trigPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  digitalWrite(trigPin, LOW);
  Serial.begin(38400);

  lcd.begin();
  lcd.backlight();

  timestamp = "enter";
}

//  Loop code.
void loop() {
  //  Scan for objects.
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  //  Retrieve pulse duration.
  duration = pulseIn(echoPin, HIGH);

  //  Calculate distance to object.
  distance = duration*0.034/2;

  //  Act on results.
  if (distance < 100) {
    //  Turn on LED.
    digitalWrite(ledPin, HIGH);
    customers++;
    
    Serial.println(timestamp);
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

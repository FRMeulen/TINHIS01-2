//  Project: TINHIS.
//  BTUltraSonic.ino  --  Arduino source file.
//  Revisions:
//    2018-11-11  --  F.R. van der Meulen --  Created.

//  Definitions.
#define trigPin 3
#define echoPin 4
#define ledPin 2

//  Variables.
long duration;
int distance;
String timestamp;

//  Setup code.
void setup() {
  pinMode(ledPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(trigPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  digitalWrite(trigPin, LOW);
  Serial.begin(38400);

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
    
    Serial.println(timestamp);
    delay(500);
  } else {
    //  If no object detected, turn off LED and wait.
    digitalWrite(ledPin, LOW);
  }
}

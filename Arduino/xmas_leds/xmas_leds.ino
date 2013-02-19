int minLed = 2;
int maxLed = 7;

int currentMode = 0;
int maxMode = 3;
int baseDelay = 250;

void setup() {   
  Serial.begin(115200);  
  for (int i = minLed; i <= maxLed; i++) {
    pinMode(i, OUTPUT);
  }
}

void set_all_leds(int value) {
  for (int i = minLed; i <= maxLed; i++) {
    digitalWrite(i, value);
  }
}

void mode_blink() {
  set_all_leds(HIGH);
  delay(baseDelay);
  set_all_leds(LOW);
  delay(baseDelay);
}

void mode_in_out() {
  set_all_leds(LOW);
  digitalWrite(2, HIGH);
  digitalWrite(7, HIGH);
  delay(baseDelay);

  set_all_leds(LOW);
  digitalWrite(3, HIGH);
  digitalWrite(6, HIGH);
  delay(baseDelay);

  set_all_leds(LOW);
  digitalWrite(4, HIGH);
  digitalWrite(5, HIGH);
  delay(baseDelay);

  set_all_leds(LOW);
  digitalWrite(3, HIGH);
  digitalWrite(6, HIGH);
  delay(baseDelay);
}

void mode_bounce() {
  for (int led = minLed; led <= maxLed; led++) {
    set_all_leds(LOW);
    digitalWrite(led, HIGH);
    delay(baseDelay);
  }
  for (int led = maxLed - 1; led > minLed; led--) {
    set_all_leds(LOW);
    digitalWrite(led, HIGH);
    delay(baseDelay);
  }
}

unsigned char fadeLevel = 0;
void mode_fade() {
  set_all_leds(LOW);
  int pwmValue = (fadeLevel > 127) ? (255-fadeLevel) : fadeLevel;
  for (int led = minLed; led <= maxLed; led++) {
    analogWrite(led, pwmValue);
  }
  fadeLevel = (fadeLevel + 5) % 256;
  delay(baseDelay / 10);
}

void joystickControl() {
  int xVal = analogRead(0);
  int yVal = analogRead(1);

  if (xVal > 600) {
    baseDelay -= 50;
    if (baseDelay < 50) {
      baseDelay = 50;
    }
  }
  if (xVal < 300) {
    baseDelay += 50;
    if (baseDelay > 1000) {
      baseDelay = 1000;
    }
  }

  if (yVal < 300) {
    currentMode--;
    if (currentMode < 0) {
      currentMode =0;
    }
  }
  if (yVal > 600) {
    currentMode++;
    if (currentMode > maxMode) {
      currentMode = maxMode;
    }
  }
}

void loop() {
  switch(currentMode) {
  case 0: 
    mode_blink(); 
    break;
  case 1: 
    mode_in_out();
    break;
  case 2: 
    mode_bounce(); 
    break;
  case 3:
    mode_fade();
  }

  joystickControl();
  Serial.println("Loop");
}






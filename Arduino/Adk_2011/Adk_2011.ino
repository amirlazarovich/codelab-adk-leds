#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

//////////////////////////////////////////
////// Constants
//////////////////////////////////////////
#define BUFFER_SIZE            16

#define COMMAND_LEDS           1

#define ACTION_LED_RED         1
#define ACTION_LED_GREEN       2
#define ACTION_LED_YELLOW      3

#define PIN_LED_RED            5
#define PIN_LED_GREEN          6
#define PIN_LED_YELLOW         7

#define TIME_STEP_BETWEEN_USB_RECONNECTIONS 1000 // in milliseconds

const char *USB_MANUFACTURER = "Reversim Summit 2013";
const char *USB_MODEL        = "leds-dashboard";
const char *USB_DESCRIPTION  = "Code lab - android-adk basics";
const char *USB_VERSION      = "1.0";
const char *USB_SITE         = "http://summit2013.reversim.com";
const char *USB_SERIAL       = "0000000012345678";
                    
//////////////////////////////////////////
////// Members
//////////////////////////////////////////
// command (1 byte), action (1 byte), data-length (1 byte), data (X bytes) 
AndroidAccessory *_acc;

// members-states
long _lastTimeReconnectedToUsb;

//////////////////////////////////////////
////// Initialization
//////////////////////////////////////////

/**
 * Called once when the Arduino first loads or resets 
 */
void onCreate() {
  // initialize leds
  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_LED_YELLOW, OUTPUT); 
}


//////////////////////////////////////////
////// Main loop
//////////////////////////////////////////

/**
 * Main loop. 
 * This method is called very frequently in an infinite loop
 */
void onLoop() {
  // do nothing...
}

//////////////////////////////////////////
////// Events
//////////////////////////////////////////

/**
 * Handle messages sent from the Android device
 *
 * @param command The command sent by the Android device
 * @param action The action sent by the Android device
 * @param dataLength The length of "data"
 * @param data Pointer to the extra data sent by the device
 */
void onMessageReceived(byte command, byte action, byte dataLength, byte* data) {
  switch(command) {
    case COMMAND_LEDS:
      int ledState;
      if (data[0] == 1) {
        ledState = HIGH;
      } else {
        ledState = LOW;
      }
      
      onChangeLedState(action, ledState);      
      break;
      
    default:
      Serial.print("Unknown command received: ");
      Serial.println(command, DEC);
  }
}


/**
 * Change led on-state according to given "ledState"
 * 
 * @param action
 * @param ledState
 */ 
void onChangeLedState(byte action, int ledState) {
  switch (action) {
    case ACTION_LED_RED:
      digitalWrite(PIN_LED_RED, ledState);
      break;
     
    case ACTION_LED_GREEN:
      digitalWrite(PIN_LED_GREEN, ledState);          
      break;
   
    case ACTION_LED_YELLOW:
      digitalWrite(PIN_LED_YELLOW, ledState);
      break;   
   
    default:
     Serial.print("Unknown led-action received: ");
     Serial.println(action, DEC);
  }  
}






////
/////// Read any futher only if curious...
////























//////////////////////////////////////////
////// Android communication boilerplate 
//////////////////////////////////////////

/**
 * Called once when the arduino first loads (or resets)
 */
void setup(){
  Serial.begin(9600);
  _acc = new AndroidAccessory(USB_MANUFACTURER,
                              USB_MODEL,
                              USB_DESCRIPTION,
                              USB_VERSION,
                              USB_SITE,
                              USB_SERIAL);
  _acc->powerOn();
  _lastTimeReconnectedToUsb = 0;
  
  onCreate();
}

/**
 * loop forever. 
 * Arduino calls this method in an infinite loop after returning from method "setup"
 */
void loop() {  
  if (_acc->isConnected()) {
    Serial.println("reading...");
   
    byte msg[BUFFER_SIZE];
    int len = _acc->read(msg, BUFFER_SIZE);
    if (len > 0) {
      Serial.print("read: ");
      Serial.print(len, DEC);
      Serial.println(" bytes");

      handleMsgFromDevice(msg);
      sendAck();
    }
  } else if (_lastTimeReconnectedToUsb + TIME_STEP_BETWEEN_USB_RECONNECTIONS < millis()) {
    Serial.println("USB is not connected. Trying to reconnect...");
    reconnectUsb();
    _lastTimeReconnectedToUsb = millis();
  }
  
  onLoop();
}

/**
 * Handle messages coming from the Android device
 *
 * @param msg The raw payload 
 */
void handleMsgFromDevice(byte* msg) {
  byte command = msg[0];
  byte action = msg[1];
  byte dataLength = msg[2];
  onMessageReceived(command, action, dataLength, msg + 3); 
}

/**
 * Try to reconnect to the Android device
 */
void reconnectUsb() {
  delete _acc;
  _acc = new AndroidAccessory(USB_MANUFACTURER,
                              USB_MODEL,
                              USB_DESCRIPTION,
                              USB_VERSION,
                              USB_SITE,
                              USB_SERIAL);
  _acc->powerOn();
}    

/**
 * Send acknowledge to connected Android device
 */ 
void sendAck() {
  if (_acc->isConnected()) 
  {
    byte msg[1];
    msg[0] = 1;
    _acc->write(msg, 1);
  }  
}

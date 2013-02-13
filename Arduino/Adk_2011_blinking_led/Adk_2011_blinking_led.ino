#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

//////////////////////////////////////////
////// Constants
//////////////////////////////////////////
#define COMMAND_LEDS           1
#define ACTION_LED_13          1
#define PIN_LED_13             13

//////////////////////////////////////////
////// Initialization
//////////////////////////////////////////

/**
 * Called once when the Arduino first loads or resets 
 */
void onCreate() {
  // initialize leds
  pinMode(PIN_LED_13, OUTPUT);
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
  digitalWrite(PIN_LED_13, data[0]); 
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










////
/////// Read any futher only if curious...
////























//////////////////////////////////////////
////// Android communication boilerplate 
//////////////////////////////////////////
#define BUFFER_SIZE            16
#define TIME_STEP_BETWEEN_USB_RECONNECTIONS 1000 // in milliseconds

const char *USB_MANUFACTURER = "Reversim Summit 2013";
const char *USB_MODEL        = "leds-dashboard";
const char *USB_DESCRIPTION  = "Code lab - android-adk basics";
const char *USB_VERSION      = "1.0";
const char *USB_SITE         = "http://summit2013.reversim.com";
const char *USB_SERIAL       = "0000000012345678";

                    
// command (1 byte), action (1 byte), data-length (1 byte), data (X bytes) 
AndroidAccessory *_acc;

// members-states
long _lastTimeReconnectedToUsb;



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
  printValues(command, action, dataLength);
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

/**
 * Print the command, action and data length to serial port 
 *
 * @param command The command sent by the Android device
 * @param action The action sent by the Android device
 * @param dataLength The length of the appended data field
 */
void printValues(byte command, byte action, byte dataLength) {
  Serial.print("Command: ");
  Serial.print(command, DEC);
  Serial.print(". Action: ");
  Serial.print(action, DEC);
  Serial.print(" Data Length: ");
  Serial.println(dataLength, DEC);
}

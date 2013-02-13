#include "Arduino.h"
#include <ADK.h>

//////////////////////////////////////////
////// Constants
//////////////////////////////////////////
#define COMMAND_LEDS           1

#define ACTION_LED_RED         1
#define ACTION_LED_GREEN       2
#define ACTION_LED_YELLOW      3

#define PIN_LED_RED            5
#define PIN_LED_GREEN          6
#define PIN_LED_YELLOW         7

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
      onChangeLedState(action, data[0]);      
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

const char *USB_MANUFACTURER = "Reversim Summit 2013";
const char *USB_MODEL        = "leds-dashboard";
const char *USB_DESCRIPTION  = "Code lab - android-adk basics";
const char *USB_VERSION      = "1.0";
const char *USB_SITE         = "http://summit2013.reversim.com";
const char *USB_SERIAL       = "0000000012345678";
                    
// command (1 byte), action (1 byte), data-length (1 byte), data (X bytes) 
ADK L;


void adkPutchar(char c){Serial.write(c);}
extern "C" void dbgPrintf(const char *, ... );



/**
 * Called once when the arduino first loads (or resets)
 */
void setup(){
  Serial.begin(9600);
  L.adkSetPutchar(adkPutchar);
  L.adkInit();
  
  L.usbSetAccessoryStringVendor(USB_MANUFACTURER);
  L.usbSetAccessoryStringName(USB_MODEL);
  L.usbSetAccessoryStringLongname(USB_DESCRIPTION);
  L.usbSetAccessoryStringVersion(USB_VERSION);
  L.usbSetAccessoryStringUrl(USB_SITE);
  L.usbSetAccessoryStringSerial(USB_SERIAL);

  L.usbStart();
  onCreate();
}

/**
 * loop forever. 
 * Arduino calls this method in an infinite loop after returning from method "setup"
 */
void loop() {  
  if (L.accessoryConnected()) {
    Serial.println("reading...");
   
    byte msg[BUFFER_SIZE];
    int len = L.accessoryReceive(msg, BUFFER_SIZE);
    if (len > 0) {
      Serial.print("read: ");
      Serial.print(len, DEC);
      Serial.println(" bytes");

      handleMsgFromDevice(msg);
      sendAck();
    }
  } 
  
  onLoop();
  L.adkEventProcess(); //let the adk framework do its thing
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
 * Send acknowledge to connected Android device
 */ 
void sendAck() {
  if (L.accessoryConnected()) {
    byte msg[1];
    msg[0] = 1;
    L.accessorySend(msg, 1);
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

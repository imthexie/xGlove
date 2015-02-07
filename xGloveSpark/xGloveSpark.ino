// This #include statement was automatically added by the Spark IDE.
#include "Adafruit_L3GD20_U.h"

/* xGloveBTArduino
 * ---------------
 * Description: ...
 *
 */

/* Included Libraries */
#include "Adafruit_Sensor.h"
#include "Adafruit_LSM303_U.h"
#include "Adafruit_9DOF.h"

/* Assign a unique ID to the sensors */
static Adafruit_9DOF                 dof    =   Adafruit_9DOF();
static Adafruit_LSM303_Accel_Unified accel  =   Adafruit_LSM303_Accel_Unified(30301);
static Adafruit_LSM303_Mag_Unified   mag    =   Adafruit_LSM303_Mag_Unified(30302);

/* Global Variables for Accelerometer, Gyroscope, Magnetometer */ 
static sensors_event_t accel_event;
static sensors_event_t mag_event;
static sensors_vec_t   orientation;

 /* Analog Input Pins corresponding to each finger */
static const int indexFingerPin             =   A2; 
static const int middleFingerPin            =   A1;
static const int ringFingerPin              =   A0;

static const int pinkyPin                   =   A5;//unused
static const int thumbPin                   =   A1;//unused


/* These global variables indicate the current analogRead values
 * of each fingers, four fingers and all fingers.
 */
static int thumbCurrentValue                =   0; //unused
static int indexFingerCurrentValue          =   0; 
static int middleFingerCurrentValue         =   0;
static int ringFingerCurrentValue           =   0;
static int pinkyCurrentValue                =   0; //unused

//Mouse X, Y calibration values
int minima[]                                =   {0, -40};          // actual analogRead minima for {x, y}
int maxima[]                                =   {0,  40};          // actual analogRead maxima for {x, y}

//Version tag
const String VERSION_TAG = "v1";
const String RESET_CMD = "RESET";

//Has been reset or not
boolean needReset;

//Turn off WiFi
//WiFiClass WiFi;
//WiFi.off();

void setup() 
{
    Serial1.begin(115200);
    /* Initialise the sensors */ 
    initSensors(); 
    
    /* Read the accelerometer and magnetometer */
    accel.getEvent(&accel_event); 
    mag.getEvent(&mag_event);
  
    /* calibrate x-axis minima and maxima based on the current orientation  */
    /* of the glove                                                         */  
    if (dof.fusionGetOrientation(&accel_event, &mag_event, &orientation))
    {
         minima[0] = orientation.heading - 50;
         maxima[0] = orientation.heading + 50;
    }
    pinMode(D7, OUTPUT); //Pin indicates reset
    while(!Serial1);
    needReset = true;
    digitalWrite(D7, HIGH);
}                

 
void loop() 
{ 
    readIncomingData();
    if(needReset) 
    {
      sendResetInfo();
    } 
    else 
    {      
      readLocationSensors(false);
      readFlexSensors();
      Serial1.println(VERSION_TAG + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)orientation.heading + ',' +
                      thumbCurrentValue + ',' + indexFingerCurrentValue + ',' + middleFingerCurrentValue + ',' +
                      ringFingerCurrentValue + ',' + pinkyCurrentValue);
    }
    delay(1);
} 

void readIncomingData() 
{
  Serial1.flush();
  while(Serial1.available() > 0) 
  {
      if(Serial1.peek() == 'Y') 
      {
          digitalWrite(D7, LOW);
          needReset = false;
          Serial1.read();
      }
      else if(Serial1.peek() == 'N') 
      {
          needReset = true;
          Serial1.read();
          digitalWrite(D7, HIGH);
      }
      else 
      {
            //discard bad received data
            Serial1.read();
      }
  }
}

void sendResetInfo() {
    readLocationSensors(true);
    // Tell the computer/connected device that we are recalibrating
    Serial1.println(RESET_CMD + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)orientation.heading + ',' +
                    minima[0] + ',' + minima[1] + ',' + maxima[0] + ',' + maxima[1]);
}



void readLocationSensors(boolean setMaxima) 
{
  /* Read the accelerometer and magnetometer */
    accel.getEvent(&accel_event); 
    mag.getEvent(&mag_event);
  
    /* calibrate x-axis minima and maxima based on the current orientation  */
    /* of the glove                                                         */  
    if (dof.fusionGetOrientation(&accel_event, &mag_event, &orientation) && setMaxima)
    {
         minima[0] = orientation.heading - 50;
         maxima[0] = orientation.heading + 50;
    }
}

void readFlexSensors() 
{
    //Update finger flex sensor values
    thumbCurrentValue           =   -1; //updated 9/23
    indexFingerCurrentValue     =   analogRead(indexFingerPin);
    middleFingerCurrentValue    =   analogRead(middleFingerPin);
    ringFingerCurrentValue      =   analogRead(ringFingerPin);
    pinkyCurrentValue           =   -1; //updated 9/23
}

/* Function: initSensors
 * ---------------------
 * This function initializes the accelerometer, magnetometer,
 * and gyroscope. See https://github.com/adafruit/Adafruit_Sensor 
 * and https://github.com/adafruit/Adafruit_9DOF for more information. 
 */
void initSensors()
{
    if(!accel.begin())
    {
        /* There was a problem detecting the LSM303 ... check your connections */
        Serial1.println(F("Ooops, no LSM303 detected ... Check your wiring!"));
        while(1);
    }
    if(!mag.begin())
    {
        /* There was a problem detecting the LSM303 ... check your connections */
        Serial1.println("Ooops, no LSM303 detected ... Check your wiring!");
        while(1);
    } 
}

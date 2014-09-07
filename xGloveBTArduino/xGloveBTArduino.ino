/* xGloveBTArduino
 * ---------------
 * Description: ...
 *
 */

/* Included Libraries */
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_9DOF.h>

/* Assign a unique ID to the sensors */
static Adafruit_9DOF                 dof    =   Adafruit_9DOF();
static Adafruit_LSM303_Accel_Unified accel  =   Adafruit_LSM303_Accel_Unified(30301);
static Adafruit_LSM303_Mag_Unified   mag    =   Adafruit_LSM303_Mag_Unified(30302);

/* Global Variables for Accelerometer, Gyroscope, Magnetometer */ 
static sensors_event_t accel_event;
static sensors_event_t mag_event;
static sensors_vec_t   orientation;

 /* Analog Input Pins corresponding to each finger */
static const int thumbPin                   =   A2;
static const int indexFingerPin             =   A3; 
static const int middleFingerPin            =   A4;
static const int ringFingerPin              =   A1;
static const int pinkyPin                   =   A5;

/* These global variables indicate the current analogRead values
 * of each fingers, four fingers and all fingers.
 */
static int thumbCurrentValue                =   0;
static int indexFingerCurrentValue          =   0;
static int middleFingerCurrentValue         =   0;
static int ringFingerCurrentValue           =   0;
static int pinkyCurrentValue                =   0;

//Mouse X, Y calibration values
int minima[]                                =   {0, -40};          // actual analogRead minima for {x, y}
int maxima[]                                =   {0,  40};          // actual analogRead maxima for {x, y}


//Version tag
const String VERSION_TAG = "v1";
const String RESET = "RESET";

//Has been reset or not
boolean needReset;

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
        if(minima[0] > orientation.heading) minima[0] -= 360;
        else if(maxima[0] < orientation.heading) maxima[0] += 360;
    }
    pinMode(13, OUTPUT); //Pin indicates reset
    while(!Serial1);
    needReset = true;
    digitalWrite(13, HIGH);
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
        if(maxima[0] != 0 || minima[0] != 0)
        {
            if(maxima[0] > 120 && orientation.heading < -120)
            {
                Serial1.println(VERSION_TAG + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)(orientation.heading + 360) + ',' +
                                thumbCurrentValue + ',' + indexFingerCurrentValue + ',' + middleFingerCurrentValue + ',' +
                                ringFingerCurrentValue + ',' + pinkyCurrentValue);
            }
            else if(minima[0] < -120 && orientation.heading > 120)
            {
                Serial1.println(VERSION_TAG + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)(orientation.heading - 360) + ',' +
                                thumbCurrentValue + ',' + indexFingerCurrentValue + ',' + middleFingerCurrentValue + ',' +
                                ringFingerCurrentValue + ',' + pinkyCurrentValue);  
            }
            else{
                Serial1.println(VERSION_TAG + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)(orientation.heading) + ',' +
                                thumbCurrentValue + ',' + indexFingerCurrentValue + ',' + middleFingerCurrentValue + ',' +
                                ringFingerCurrentValue + ',' + pinkyCurrentValue);         
            } 
        }      
    }
    delay(2);
} 

void readIncomingData() 
{
  Serial1.flush();
  while(Serial1.available() > 0) 
  {
      if(Serial1.peek() == 'Y') 
      {
          digitalWrite(13, LOW);
          needReset = false;
          Serial1.read();
      }
      else if(Serial1.peek() == 'N') 
      {
          needReset = true;
          Serial1.read();
          digitalWrite(13, HIGH);
      }
      else 
      {
            //discard bad received data
            Serial1.read();
      }
  }
}

// Tell the computer/connected device that we are recalibrating
void sendResetInfo() 
{
    readLocationSensors(true);
    if(maxima[0] != 0 || minima[0] != 0)
    {
        if(maxima[0] > 120 && orientation.heading < -120)
        {
            Serial1.println(RESET + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)(orientation.heading + 360) + ',' +
                        minima[0] + ',' + minima[1] + ',' + maxima[0] + ',' + maxima[1]);
        }
        else if(minima[0] < -120 && orientation.heading > 120)
        {
            Serial1.println(RESET + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)(orientation.heading - 360) + ',' +
                            minima[0] + ',' + minima[1] + ',' + maxima[0] + ',' + maxima[1]);   
        }
        else
        {
            Serial1.println(RESET + ',' + ((int)orientation.roll - 10)  + ',' + ((int)orientation.pitch + 12) + ',' + (int)orientation.heading + ',' +
                            minima[0] + ',' + minima[1] + ',' + maxima[0] + ',' + maxima[1]); 
        } 
    }
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
        if(minima[0] > orientation.heading) minima[0] -= 360;
        else if(maxima[0] < orientation.heading) maxima[0] += 360;
    }
}

void readFlexSensors() 
{
    //Update finger flex sensor values
    thumbCurrentValue           =   analogRead(thumbPin);
    indexFingerCurrentValue     =   analogRead(indexFingerPin);
    middleFingerCurrentValue    =   analogRead(middleFingerPin);
    ringFingerCurrentValue      =   analogRead(ringFingerPin);
    pinkyCurrentValue           =   analogRead(pinkyPin);
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

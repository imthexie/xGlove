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
static const int thumbPin                   =   A5;
static const int indexFingerPin             =   A4; 
static const int ringFingerPin              =   A3;
static const int middleFingerPin            =   A2;
static const int pinkyPin                   =   A1;

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
    //Wait until serial port is open
    while(!Serial1);
     Serial1.println(RESET + ',' + (-(int)orientation.roll-10)  + ',' + (-(int)orientation.pitch - 11) + ',' + -(int)orientation.heading + ',' +
                     minima[0] + ',' + minima[1] + ',' + maxima[0] + ',' + maxima[1]);
}                

 
void loop() 
{ 
    
      readLocationSensors(false);
      readFlexSensors();
      Serial1.println(VERSION_TAG + ',' + (-(int)orientation.roll-10)  + ',' + (-(int)orientation.pitch - 11) + ',' + -(int)orientation.heading + ',' +
                      thumbCurrentValue + ',' + indexFingerCurrentValue + ',' + middleFingerCurrentValue + ',' +
                      ringFingerCurrentValue + ',' + pinkyCurrentValue);
    delay(1);
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

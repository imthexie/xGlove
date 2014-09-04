#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_9DOF.h>

/* Assign a unique ID to the sensors */
Adafruit_9DOF                 dof   = Adafruit_9DOF();
Adafruit_LSM303_Accel_Unified accel = Adafruit_LSM303_Accel_Unified(30301);
Adafruit_LSM303_Mag_Unified   mag   = Adafruit_LSM303_Mag_Unified(30302);

 /* Analog Input Pins corresponding to each finger */
static const int thumbPin                   =   A1;
static const int indexFingerPin             =   A2; 
static const int middleFingerPin            =   A3;
static const int ringFingerPin              =   A4;
static const int pinkyPin                   =   A5;


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

void setup(void)
{
  Serial1.begin(115200);
  Serial1.println(F("Calibrating xGlove...")); 
  Serial1.println();
  Serial1.println();
  
  /* Initialise the sensors */
  initSensors();
}

/**************************************************************************/
/*!
    @brief  Constantly check the roll/pitch/heading/altitude/temperature
*/
/**************************************************************************/
void loop(void)
{
  sensors_event_t accel_event;
  sensors_event_t mag_event;
  sensors_vec_t   orientation;

  /* Read the accelerometer and magnetometer */
  accel.getEvent(&accel_event);
  mag.getEvent(&mag_event);

  /* Use the new fusionGetOrientation function to merge accel/mag data */  
  if (dof.fusionGetOrientation(&accel_event, &mag_event, &orientation))
  {
    /* 'orientation' should have valid .roll and .pitch fields */
    Serial1.println(F("Orientation "));
    Serial1.println("roll    pitch    heading");
    Serial1.print(orientation.roll);
    Serial1.print(F("    "));
    Serial1.print(orientation.pitch);
    Serial1.print(F("     "));
    Serial1.print(orientation.heading);
    Serial1.println(F(" "));
    Serial1.println();
    
    Serial1.println(F("Fingers     ")); 
    Serial1.println("thumb     index    middle    ring    pinky");
    Serial1.print(analogRead(thumbPin));
    Serial1.print(F("       "));
    Serial1.print(analogRead(indexFingerPin));
    Serial1.print(F("       "));
    Serial1.print(analogRead(middleFingerPin));
    Serial1.print(F("       "));
    Serial1.print(analogRead(ringFingerPin));
    Serial1.print(F("       "));
    Serial1.print(analogRead(pinkyPin));
    Serial1.println(F(" "));  
    
    Serial1.println();
    Serial1.println();
    Serial1.println();
  }
  
  delay(500);
}

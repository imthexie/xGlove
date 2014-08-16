/* xGlove_Gesture.cpp
 * ------------------
 * still under construction
 * 
 */

#include "Arduino.h"
#include "xGlove_Sensor.h"
#include "xGlove_Gesture.h"

#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_9DOF.h>

/* Call Sensor class constructor */
Sensor sensor = Sensor();

/* Assign a unique ID to the sensors */
static Adafruit_9DOF                 dof   = Adafruit_9DOF();
static Adafruit_LSM303_Accel_Unified accel = Adafruit_LSM303_Accel_Unified(30301);
static Adafruit_LSM303_Mag_Unified   mag   = Adafruit_LSM303_Mag_Unified(30302);

/* Global Variables for Accelerometer, Gyroscope, Magnetometer */ 
static sensors_event_t accel_event;
static sensors_event_t mag_event;
static sensors_vec_t   orientation;

boolean Gesture::fist()
{
    return (sensor.all_fingers_bent(65));
}

boolean Gesture::fingers_spread(){
    return !(sensor.all_fingers_bent(50));
}

boolean Gesture::upside_down(){
    if(abs(orientation.roll) > 170)
    {
        /* This for loop is used to ensure that the glove is upside down */
        /* for at least .5 second (10*delay(50)).                        */
        /* This is to avoid measurement errors.                          */         
        for(int i = 0; i < 10; i+=1){
            
            delay(50);
            
            accel.getEvent(&accel_event);
            mag.getEvent(&mag_event);
            dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);
            
            if(abs(orientation.roll) < 115) 
            { //break if hand is back in original position
                break;
            }
            else if(i == 9) return true;
        }      
    }
    else return false;    
}

boolean Gesture::right_side_up(){
    accel.getEvent(&accel_event);
    mag.getEvent(&mag_event);
    dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);
    return (abs(orientation.roll) < 50);
}  
  
  
  
  
  


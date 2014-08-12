/* Included Libraries */
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_9DOF.h>
#include <xGlove_Computer.h>
#include <xGlove_Gesture.h>
#include <xGlove_Sensor.h> 

/* Assign a unique ID to the sensors */
static Adafruit_9DOF                 dof   = Adafruit_9DOF();
static Adafruit_LSM303_Accel_Unified accel = Adafruit_LSM303_Accel_Unified(30301);
static Adafruit_LSM303_Mag_Unified   mag   = Adafruit_LSM303_Mag_Unified(30302);


/* Global Variables for Accelerometer, Gyroscope, Magnetometer */ 
static sensors_event_t accel_event;
static sensors_event_t mag_event;
static sensors_vec_t   orientation;

/* Global Variables for Mouse functionality */
boolean currently_clicked = false;
int range                 = 12;              // output range of X or Y movement
int responseDelay         = 2;               // response delay of the mouse, in ms
int threshold             = range/18;        // resting threshold  originally -> /10
int center                = range/2;         // resting position value
int minima[]              = {0, -40};        // actual analogRead minima for {x, y}
int maxima[]              = {0, 40};         // actual analogRead maxima for {x, y}


void setup() 
{
    Serial.begin(115200);
    
    /* start mouse and keyboard control */
    Mouse.begin();
    Keyboard.begin();
    
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
}                
 
 
void loop() 
{ 
    
    /* Read the accelerometer and magnetometer */
    accel.getEvent(&accel_event);
    mag.getEvent(&mag_event);
    dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);  //merge accel/mag data 
    
    mouse_left_click(accel_event, mag_event, orientation); 
    move_mouse(accel_event, mag_event, orientation); 
    mouse_scroll();
    
    spacebar();
    mac_launchpad(accel_event, mag_event, orientation);
    load_next_previous(accel_event, mag_event, orientation);
} 

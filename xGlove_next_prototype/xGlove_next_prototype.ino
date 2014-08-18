/* Included Libraries */
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_9DOF.h>

/* Assign a unique ID to the sensors */
static Adafruit_9DOF                 dof   = Adafruit_9DOF();
static Adafruit_LSM303_Accel_Unified accel = Adafruit_LSM303_Accel_Unified(30301);
static Adafruit_LSM303_Mag_Unified   mag   = Adafruit_LSM303_Mag_Unified(30302);

/* Global Variables for Accelerometer, Gyroscope, Magnetometer */ 
static sensors_event_t accel_event;
static sensors_event_t mag_event;
static sensors_vec_t   orientation;

/* Analog Input Pins corresponding to each finger */
const int ring         = A0;
const int middle       = A1;
const int index_top    = A2;
const int index_side1  = A3;
const int index_side2  = A4; 

/*Finger flex sensor values*/
int middle_val, ring_val, index_top_val, index_side1_val, index_side2_val; 

void setup() 
{
    Serial.begin(115200);
    
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
    
    //Tell the computer/connected device that we are recalibrating
    Serial.println("RESET" + "," + orientation.heading + "," + orientation.pitch + "," + orientation.roll);    
}                
 
 
void loop() 
{ 
    read_location_sensors();
    read_flex_sensors();
    Serial.println(orientation.heading + "," + orientation.pitch + "," + orientation.roll + "," + index_top_val + "," + index_side1_val + "," + index_side2_val + "," + middle_val + "," + ring_val;
    
    //Maybe put in delay
    
} 

void read_location_sensors() {
  /* Read the accelerometer and magnetometer */
    accel.getEvent(&accel_event);
    mag.getEvent(&mag_event);
    dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);  //merge accel/mag data
 
     
}

void read_flex_sensors() {
    //Check finger flex values
    middle_val = analogRead(middle);
    ring_val = analogRead(ring);
    index_top_val = analogRead(index_top);
    index_side1_val = analogRead(index_side1);
    index_side2_val = analogRead(index_side2);   
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
        Serial.println(F("Ooops, no LSM303 detected ... Check your wiring!"));
        while(1);
    }
    if(!mag.begin())
    {
        /* There was a problem detecting the LSM303 ... check your connections */
        Serial.println("Ooops, no LSM303 detected ... Check your wiring!");
        while(1);
    } 
}


/*
*******DESCRIPTION of the structs/information, a subset of which we are sending over Bluetooth:************
/*
 Sensor event (36 bytes) 
 struct sensor_event_t is used to provide a single sensor event in a common format.
typedef struct
{
    int32_t version;
    int32_t sensor_id;
    int32_t type;
    int32_t reserved0;
    int32_t timestamp;
    union
    {
        float           data[4];
        sensors_vec_t   acceleration;
        sensors_vec_t   magnetic;
        sensors_vec_t   orientation;
        sensors_vec_t   gyro;
        float           temperature;
        float           distance;
        float           light;
        float           pressure;
        float           relative_humidity;
        float           current;
        float           voltage;
        sensors_color_t color;
    };
} sensors_event_t;
*/

//struct sensors_vec_t is used to return a vector in a common format. (My calculation is that this is 14 bytes)
/*typedef struct {
    union {
        float v[3];
        struct {
            float x;
            float y;
            float z;
        };
         Orientation sensors 
        struct {
            float roll;    < Rotation around the longitudinal axis (the plane body, 'X axis'). Roll is positive and increasing when moving downward. -90°<=roll<=90° 
            float pitch;   < Rotation around the lateral axis (the wing span, 'Y axis'). Pitch is positive and increasing when moving upwards. -180°<=pitch<=180°) 
            float heading; < Angle between the longitudinal axis (the plane body) and magnetic north, measured clockwise when viewing from the top of the device. 0-359° 
        };
    };
    int8_t status;
    uint8_t reserved[3];
} sensors_vec_t;*/



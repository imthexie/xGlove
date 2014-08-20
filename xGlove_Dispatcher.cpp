
#include "xGlove_dispatcher.h"

using namespace std;

void Dispatcher::Dispatcher() {
    /*Initialize sensors*/
    init_sensors();
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

void Dispatcher::check_sensor_values() {
    /* Read the accelerometer and magnetometer */
    accel.getEvent(&accel_event);
    mag.getEvent(&mag_event);
    dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);  //merge accel/mag data 
    
    //Update Mouse values
    xMouseReading = get_cursor_position(orientation.heading, 0); // x-axis movement
    yMouseReading = get_cursor_position(orientation.roll, 1);    // y-axis movement
    
    //Check finger flex values
    middle_val = analogRead(middle);
    ring_val = analogRead(ring);
    index_top_val = analogRead(index_top);
    index_side1_val = analogRead(index_side1);
    index_side2_val = analogRead(index_side2);  
}

void Dispatcher::dispatch_jobs() {
    
  
}

int Dispatcher::get_whole_hand_reading() {
    return middle_val + ring_val + index_top_val + index_side1_val + index_side2_val;
}

pair<int, int> get_mouse_position() {
    return make_pair(x_mouse_pos, y_mouse_pos);
}


/* Function: initSensors
 * ---------------------
 * This function initializes the accelerometer, magnetometer,
 * and gyroscope. See https://github.com/adafruit/Adafruit_Sensor 
 * and https://github.com/adafruit/Adafruit_9DOF for more
 * information. 
 */
void Dispatcher::init_sensors()
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


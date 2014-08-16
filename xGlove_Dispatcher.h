#ifndef xGlove_Dispatcher_h
#define xGlove_Dispatcher_h

/* Included Libraries */
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_9DOF.h>
#include <atomic>
#include <utility>

/**
* Event Dispatcher that is meant to be run inside a firmware 
* loop. In the loop, call checkSensorValues() and then
* dispatchJobs(). Blocking and non-blocking jobs can be 
* called with the argument to the jobs themselves in the private portion of the class. Sensor readings
* can be received from getter methods in this class. Non-blocking jobs can be 
* done simultaneously.
**/

class Dispatcher {
  public:
    void Dispatcher();
    void check_sensor_values();
    void dispatch_jobs();
    int get_whole_hand_reading();
    std::pair<int, int> get_mouse_position();
    
  private:
    //Finger values. Atomic for concurrency lock
    atomic<int> middle_val, ring_val, index_top_val, index_side1_val, index_side2_val; 
    atomic<int> x_mouse_pos, y_mouse_pos;
    atomic<bool> is_scrolling;
    private init_sensors();
    
    /* Analog Input Pins corresponding to each finger */
    const int ring         = A0;
    const int middle       = A1;
    const int index_top    = A2;
    const int index_side1  = A3;
    const int index_side2  = A4; 

    /*Accelerometer, Gyroscope, Magnetometer*/ 
    sensors_event_t accel_event; 
    sensors_event_t mag_event;
    sensors_vec_t orientation;
    
    /* Assign a unique ID to the sensors */
    Adafruit_9DOF                 dof   = Adafruit_9DOF();
    Adafruit_LSM303_Accel_Unified accel = Adafruit_LSM303_Accel_Unified(30301);
    Adafruit_LSM303_Mag_Unified   mag   = Adafruit_LSM303_Mag_Unified(30302);
    
}






#endif

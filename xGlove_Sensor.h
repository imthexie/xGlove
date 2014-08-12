/* xGlove_Sensor.h 
 * --------------------
 * The functions in xGlove_calibration.h make it easier to switch 
 * to a new glove prototype, since only the sensor values in the 
 * Sensor constructor will have to be updated. The bent functions
 * can be used to find out the rate of bending of each finger measured
 * by the flexsensors. 
 * 
 */
 
#ifndef xGlove_Sensor_h
#define xGlove_Sensor_h

#include "Arduino.h"

class Sensor
{
  
  public:
    
    Sensor();
    
    void pin_setup(int index_finger_pin, int middle_finger_pin, int ring_finger_pin, int pinky_finger_pin);
    
    boolean index_finger_bent(double percentage_bent);
    boolean middle_finger_bent(double percentage_bent);
    boolean ring_finger_bent(double percentage_bent);
    boolean pinky_bent(double percentage_bent);
    boolean all_fingers_bent(double percentage_bent);
    
    int measure_inclination_fingers();
    
    void initSensors();
    
    
  private:
    
    int _index_finger_bent;
    int _middle_finger_bent;
    int _ring_finger_bent;
    int _pinky_bent;
    int _all_fingers_bent;
  
    int _index_finger_pin;
    int _middle_finger_pin;
    int _ring_finger_pin;  
    int _pinky_pin;
};

#endif

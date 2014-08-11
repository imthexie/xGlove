/* xGlove_Sensor.cpp
 * -----------------
 * Implementation of xGlove_Sensor.h. 
 * 
 */

#include "Arduino.h"
#include "xGlove_Sensor.h"


Sensor::Sensor()
{
    _index_finger_bent  =  250;
    _middle_finger_bent =  250;
    _ring_finger_bent   =  250;
    _pinky_bent         =  250;
    _all_fingers_bent   =  _index_finger_bent + _middle_finger_bent + _ring_finger_bent + _pinky_bent; 
}

void Sensor::pin_setup(int index_finger_pin, int middle_finger_pin, int ring_finger_pin, int pinky_pin)
{
    pinMode(index_finger_pin,  OUTPUT);
    pinMode(middle_finger_pin, OUTPUT);
    pinMode(ring_finger_pin,   OUTPUT);
    pinMode(pinky_pin,         OUTPUT);
  
    _index_finger_pin   =  index_finger_pin;
    _middle_finger_pin  =  middle_finger_pin;
    _ring_finger_pin    =  ring_finger_pin;
    _pinky_pin          =  pinky_pin;
}


boolean Sensor::index_finger_bent(double percentage_bent)
{
    return (analogRead(_index_finger_pin)  <  (percentage_bent/100) * _index_finger_bent);
}


boolean Sensor::middle_finger_bent(double percentage_bent)
{
    return (analogRead(_middle_finger_pin) <  (percentage_bent/100) * _middle_finger_bent);
}


boolean Sensor::ring_finger_bent(double percentage_bent)
{
    return (analogRead(_ring_finger_pin)   <  (percentage_bent/100) * _ring_finger_bent);
}


boolean Sensor::pinky_bent(double percentage_bent)
{
    return (analogRead(_pinky_pin)         <  (percentage_bent/100) * _pinky_bent);
}


boolean Sensor::all_fingers_bent(double percentage_bent)
{
    return (analogRead(_index_finger_pin) + analogRead(_middle_finger_pin) + analogRead(_ring_finger_pin) +
            analogRead(_pinky_pin) < (percentage_bent/100) * _all_fingers_bent);
}




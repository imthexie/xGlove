/* xGlove_Gesture.cpp
 * ------------------
 * still under construction
 * 
 */

#include "Arduino.h"
#include "xGlove_Sensor.h"
#include "xGlove_Gesture.h"

/* Call Sensor class constructor */
Sensor                 sensor   = Sensor();

Gesture::Gesture()
{
}

boolean Gesture::phist()
{
    return (sensor.all_fingers_bent(65));
}

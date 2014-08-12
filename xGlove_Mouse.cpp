/* xGlove_Mouse.cpp
 * -----------------
 * Implementation of xGlove_Mouse.h. 
 * 
 */

#include "Arduino.h"
#include "xGlove_Sensor.h"
#include "xGlove_Gesture.h"


/* Function: mouse_left_click
 * --------------------------
 * The mouse_left_click function simulates a left click of the mouse
 * when the all fingers are bent slightly. The function uses the variable
 * currently_clicked avoid another click immediately after the previous click
 * because the hand was still in the clicking position. In order to click again
 * the glove first has to be returned to the non-click position (currently_clicked
 * then becomes zero). 
 */
void mouse_left_click(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation)
{        
    if(sensor.fingers_bent() && !currently_clicked && abs(orientation.pitch) < 35){
        Mouse.click(MOUSE_LEFT);  // left click 
        currently_clicked = true;    // there is a left click
    } 
    else if(sensor.fingers_spread){ 
        Mouse.release();         //release click
        currently_clicked = false;   // there is no left click 
    }  
}  


/* Function: move_mouse
 * --------------------
 * This function uses the data from the 9-DOF chip to move the mouse cursor. 
 * The function calls the readAxis function to turn the position of the 
 * glove into a corresponding position of the mouse cursor on the screen. 
 */
void move_mouse(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation){
    int xReading = get_cursor_position(orientation.heading, 0); // x-axis movement
    int yReading = get_cursor_position(orientation.roll, 1);    // y-axis movement
    Mouse.move(5 * xReading, 4 * yReading, 0);       // move the mouse
    delay(responseDelay);                            // this delay makes the movements smoother
}


/* Function: get_cursor_position
 * -----------------------------------------------------------------
 * This function is used when moving the mouse. The function turns the
 * position of the glove into a corresponding position of the mouse cursor
 * on the screen. 
 */
int get_cursor_position(int reading, int axisNumber) 
{
    int distance = 0;    // distance from center of the output range
        
    // map the reading from the analog input range to the output range:
    reading = map(reading, minima[axisNumber], maxima[axisNumber], 0, range);
    
    // if the output reading is outside from the
    // rest position threshold,  use it:
    if (abs(reading - center) > threshold) distance = (reading - center);

    // the reading needs to be inverted in order to 
    // map the movemment correctly:
    distance = -distance;

    // return the distance for this axis:
    return distance;
}


/* Function: mouse_scroll
 * ----------------------
 * This function simulates scrolling. The function enters and exits
 * scrolling mode when the ring finger is bent down while the middle finger
 * is not bent. After entering scrolling mode the scroll speed and direction
 * are determined by the bending direction and the inclination of all fingers. 
 * When the fingers are bent down, the page scrolls down. When the fingers are
 * bent up, the page scrolls up. The more the finger are bent the higher the 
 * scrolling speed. The scrolling speed is controlled by the for loop. 
 */
void mouse_scroll(){
  
    /* enter scroll mode by bending ring finger much more than middle finger */
    if(hand.ring_finger_bent(90) && !(hand.middle_finger_bent(50)) && !(hand.index_finger_bent(50)))
    {
        /* delay until user stretches ring finger again */
        while(hand.ring_finger_bent(70) && !(hand.middle_finger_bent(50))) delay(20);
        
        /* while loop will exit when ring finger is bent much more than middle finger */
        while(! (hand.ring_finger_bent(90) && !(hand.middle_finger_bent(50))) )
        {
            int inclination_percentage = hand.get_inclination();
            
            if(inclination_percentage < 20)         // scroll up
            {
               for(int i = 0; i < scroll; i = i + 3)  // for loop to make scroll speed dependent 
               {                                       // on the bending angle of the fingers
                   Mouse.move(0, 0, 1);
                   /* exit scroll mode when ring finger is bent again */
                   if(hand.ring_finger_bent(90) && !(hand.middle_finger_bent(50))) break;   
                   delay(100);
                }
            }
            else if(inclination_percentage > 80)   // scroll down
            { 
                for(int i = inclination_level - 80; i > 0; i = i - 3)  // for loop to make scroll speed dependent 
                {                                                      // on the bending angle of the fingers
                   Mouse.move(0, 0, -1);
                   /* exit scroll mode when ring finger is bent again */
                   if(hand.ring_finger_bent(90) && !(hand.middle_finger_bent(50))) break;
                   delay(100);
                }
            }
            
        }
        
        /* delay until user stretches ring finger again */
        while(!hand.ring_finger_bent(30) && !hand.middle_finger_bent(30)) delay(20);
    }
}

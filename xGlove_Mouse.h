/* xGlove_Mouse.h 
 * --------------
 * 
 */
 
#ifndef xGlove_Gesture_h
#define xGlove_Gesture_h
 

/* Function: mouse_left_click
 * --------------------------
 * The mouse_left_click function simulates a left click of the mouse
 * when the all fingers are bent slightly. The function uses the variable
 * currently_clicked avoid another click immediately after the previous click
 * because the hand was still in the clicking position. In order to click again
 * the glove first has to be returned to the non-click position (currently_clicked
 * then becomes zero). 
 */
void mouse_left_click(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation);
    
    
/* Function: move_mouse
 * --------------------
 * This function uses the data from the 9-DOF chip to move the mouse cursor. 
 * The function calls the readAxis function to turn the position of the 
 * glove into a corresponding position of the mouse cursor on the screen. 
 */
void move_mouse(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation);
    
    
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
void mouse_scroll();
  
    
/* Function: get_cursor_position
 * -----------------------------------------------------------------
 * This function is used when moving the mouse. The function turns the
 * position of the glove into a corresponding position of the mouse cursor
 * on the screen. 
 */
int Mouse::get_cursor_position(int reading, int axisNumber)   

#endif
    


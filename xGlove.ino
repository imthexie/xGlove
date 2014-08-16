/* Included Libraries */
#include <Wire.h>

/* Global Variables for Mouse functionality */
int currently_clicked = 0;
int range = 12;                  // output range of X or Y movement
int responseDelay = 2;           // response delay of the mouse, in ms
int threshold = range/18;        // resting threshold  originally -> /10
int center = range/2;            // resting position value
int minima[] = {0, -40};         // actual analogRead minima for {x, y}
int maxima[] = {0, 40};          // actual analogRead maxima for {x, y}

/* Function prototypes */
void mouse_left_click(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation);
void move_mouse(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation);
void mouse_scroll(); 
void spacebar();
void mac_launchpad(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation);
void load_next_previous(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation);


void setup() 
{
    Serial.begin(115200);
    
    /* start mouse and keyboard control */
    Mouse.begin();
    Keyboard.begin();     
}                
 
 
void loop() 
{     
    mouse_left_click(accel_event, mag_event, orientation); 
    move_mouse(accel_event, mag_event, orientation); 
    mouse_scroll();
    
    spacebar();
    mac_launchpad(accel_event, mag_event, orientation);
    load_next_previous(accel_event, mag_event, orientation);
} 

/* Function: mouse_left_click
 * --------------------------
 * The mouse_left_click function simulates a left click of the mouse
 * when the all fingers are bent slightly. The function uses the variable
 * currently_clicked avoid another click immediately after the previous click
 * because the hand was still in the clicking position. In order to click again
 * the glove first has to be returned to the non-click position (currently_clicked
 * then becomes zero). 
 */
void mouse_left_click(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation){
    
//    int mouse_left_click = getWholeHandReading();
                     
    if(mouse_left_click < 840 && mouse_left_click > 720 && currently_clicked == 0 && abs(orientation.pitch) < 35){
        Mouse.click(MOUSE_LEFT);  // left click 
        currently_clicked = 1;    // there is a left click
    } 
    else if(mouse_left_click > 870){ 
        Mouse.release(); //release click
        currently_clicked = 0;   // there is no left click 
    }  
}  

/* Function: move_mouse
 * --------------------
 * This function uses the data from the 9-DOF chip to move the mouse cursor. 
 * The function calls the readAxis function to turn the position of the 
 * glove into a corresponding position of the mouse cursor on the screen. 
 */
void move_mouse(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation){
    
    
    Mouse.move(5 * xMouseReading, 4 * yMouseReading, 0);       // move the mouse
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

    if(axisNumber == 1) reading = reading + 3; // calibration value
        
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
  
    /* enter scroll mode by bending ring finger more than middle finger */
    if(analogRead(ring) + 60 < analogRead(middle) && analogRead(index_top) > 175 && analogRead(middle) > 200)
    {
        
        /* delay until user stretches ring finger again */
        while(analogRead(ring) + 40 < analogRead(middle)) delay(20);
        
        /* start scrolling */
        while(analogRead(ring) + 60 >= analogRead(middle))
        {
            int scroll = analogRead(ring) + analogRead(middle) + analogRead(index_top) + 
                        analogRead(index_side1) + analogRead(index_side2) - 1000;
            
            if(scroll > 70)
            {
               for(int i = 0; i < scroll; i = i + 30)  // for loop to make scroll speed dependent 
               {                                       // on the bending angle of the fingers
                   Mouse.move(0, 0, 1);
                   /* exit scroll mode when ring finger is bent again */
                   if(analogRead(ring) + 65 < analogRead(middle)) break;   
                   delay(100);
                }
            }
            else if(scroll < -70){   
                for(int i = 0; i > scroll; i = i - 30)  // for loop to make scroll speed dependent 
                {                                       // on the bending angle of the fingers
                   Mouse.move(0, 0, -1);
                   /* exit scroll mode when ring finger is bent again */
                   if(analogRead(ring) + 65 < analogRead(middle)) break;
                   delay(100);
                }
            }
            
        }
        
        /* delay until user stretches ring finger again */
        while(analogRead(ring) + 40 < analogRead(middle)) delay(20);
    }
}

/* Function: spacebar
 * ------------------
 * This function simulates the spacebar key. When making a fist (all fingers
 * together) the spacebar key is pressed. The function enters a while loop after
 * simulating the spacebar key that is exited after spreading the fingers again. 
 * This is to ensure that the spacebar is not being pressed continuously when making
 * a fist, but just once. 
 */
void spacebar(){
//    int spacebar = analogRead(ring) + analogRead(middle) + analogRead(index_top) + 
//                   analogRead(index_side1) + analogRead(index_side2);
    if(spacebar < 600) //fist
    {
         Keyboard.write(32); 
         //I disagree with the spacebar waiting - if you hold down the spacebar on your computer, it will continually fire spacebar events.
    }  
}

/* Function: mac_launchpad
 * -----------------------
 * This function enters/exits the launchpad on Mac computers when the Glove is turned 
 * upside down. The function uses the short key combination CMD-SHIFT-L to enter the
 * launchpad. This shortkey combination can be set to enter launchpad in system 
 * preferences. Usually the F4 key is used to enter the launchpad, but this did not work
 * during initial tests of the glove. So I (Teun) created the shortkey combination 
 * CMD-SHIFT-L instead. 
 */
void mac_launchpad(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation){
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
            else if(i == 9) 
            {
                /* enter Launchpad */
                Keyboard.press(KEY_LEFT_GUI);
                Keyboard.press(KEY_LEFT_SHIFT); 
                Keyboard.press('L');
                Keyboard.releaseAll();
                Keyboard.release(215); 
                        
                /* delay until glove is back in original position  */
                while(abs(orientation.roll) > 50)
                {    
                    accel.getEvent(&accel_event);
                    mag.getEvent(&mag_event);
                    dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);
                    delay(100);
                 }
            }
        }      
    }     
} 

/* Function: load_next_previous
 * --------------------
 * This function can be used when working with Powerpoint or when browsing the internet. 
 * When the glove is turned sideways to the left and the fingers are bent the previous page 
 * is loaded (when browsing), or the previous slide is loaded (in MS Powerpoint). When the
 * glove is turned sideways to the right and the fingers are bent the next page is loaded 
 * (when browsing), or the next slide is loaded (in MS Powerpoint). 
 */
void load_next_previous(sensors_event_t accel_event, sensors_event_t mag_event, sensors_vec_t orientation){
    if(orientation.pitch > 70  && analogRead(ring) + analogRead(middle) + analogRead(index_top) < 600)
    {
        // right arrow key
        while(orientation.pitch > 60) // exit when hand is back in original position
        {
            
            if(analogRead(ring) + analogRead(middle) + analogRead(index_top) < 600) //finger should be bend
            {
                Keyboard.press(215);            // right arrow key
                Keyboard.release(215);          // 215 is right arrow key
                delay(5);
                    
                Keyboard.press(KEY_LEFT_GUI);   // This is a shortkey combination
                Keyboard.press(']');            // to move to the next page in the 
                Keyboard.releaseAll();          // browser
                    
                delay(1500);                    // delay between iterations of this function
             }
             
             // get current sensor data
             accel.getEvent(&accel_event);
             mag.getEvent(&mag_event);
             dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);
             
         }        
    }
    else if(orientation.pitch < -70 &&  analogRead(ring) + analogRead(middle) + analogRead(index_top) < 600)
    {
         // left arrow key
         while(orientation.pitch < -60) // exit when hand is back in original position
         {
             
             if(analogRead(ring) + analogRead(middle) + analogRead(index_top) < 600) //finger should be bend
             {
                 Keyboard.press(216);            // left arrow key
                 Keyboard.release(216);          // 216 is left arrow key
                 delay(50);
                    
                 Keyboard.press(KEY_LEFT_GUI);   // This is a shortkey combination
                 Keyboard.press('[');            // to move to the previous page in the 
                 Keyboard.releaseAll();          // browser
                    
                 delay(1500);                    // delay between iterations of this function
             }
             
             // get current sensor data 
             accel.getEvent(&accel_event);
             mag.getEvent(&mag_event);
             dof.fusionGetOrientation(&accel_event, &mag_event, &orientation);
        }        
    }
}

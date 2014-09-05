import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.InputEvent;

import processing.core.*;

class xGloveMouse 
{
	//Used in debug logs
	private final String TAG = "xGloveMouse";
	private boolean debugMouse = false;
	Robot mouseRobot;                   // create object from Robot class;
	private static final int xRate = 5; // multiplier to adjust movement rate
	private static final int yRate = 4; // multiplier to adjust movement rate

	xGloveGesture gesture;

	Dimension screen; //Computer screen data
  
	int range       =  12;              // output range of X or Y movement
	int threshold   =  range / 18;      // resting threshold  originally -> /10
	int center      =  range / 2 ;      // resting position value
	int minima[]    =  {0,  -40 };      // actual analogRead minima for {x, y}
	int maxima[]    =  {0,   40 };      // actual analogRead maxima for {x, y}

	//TODO: Test this, and evaluate need to use AtomicInteger
	int x, y; //coordinates of mouse

	private boolean currentlyClicked;

	public xGloveMouse() 
	{
		try 
		{
			mouseRobot = new Robot();
		}
		catch (AWTException e) 
		{
			e.printStackTrace();
		}

		//Automatic delay after every event fired. TODO: May need adjustment
		mouseRobot.setAutoDelay(10);

		screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		//TODO: check if this is needed vs the call in resetMouse(). 
		//Put mouse in middle of screen
		centerMouse();
		
		currentlyClicked = false;

		this.gesture = new xGloveGesture();
	}
    
	// method to move mouse from current position by given offset
	private void move(int offsetX, int offsetY) 
	{
		if(debugMouse)
		{
			System.out.print("Current Location x = " + x);
			System.out.println("\t\t      Current Location y = " + y);
		}
		
		if((x + (xRate * offsetX)) > screen.getWidth())
		{
			x = (int)screen.getWidth();
		}
		else if((x + (xRate * offsetX)) < 0)
		{
			x = 0;
		}
		else x += (xRate * offsetX);
		
		if((y + (yRate * offsetY)) > screen.getHeight())
		{
			y = (int)screen.getHeight();
		}
		else if((y + (yRate * offsetY)) < 0)
		{
			y = 0;
		}
		else y += (yRate * offsetY);
		
		if(debugMouse)
		{
			System.out.print(TAG + ": move(): " + "New Location x = " + x);
			System.out.println("\t\t      New Location y = " + y);
			System.out.println();
			System.out.println();
		}
		
		mouseRobot.mouseMove(x, y); //mouseMove moves to a given position
	}
    
	//Move mouse to a specified location
	private void moveTo(int posX, int posY) 
	{
		x = posX;
		y = posY;
		mouseRobot.mouseMove(posX, posY);
	}
    
	public void centerMouse() 
	{
		//mouseMove moves to a given position
		moveTo((int)screen.getWidth() / 2, (int)screen.getHeight() / 2); 
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
	
	public boolean isCurrentlyClicked() 
	{ 
		return currentlyClicked; 
	}
	
	public void doMouseLeftClick() 
	{        
		mouseRobot.mousePress(InputEvent.BUTTON1_MASK);  // left click 
		mouseRobot.mouseRelease(InputEvent.BUTTON1_MASK); //release click
		currentlyClicked = true;    // there is a left click
		System.out.println("There should be a click.");
	}  
	
	public void doMouseLeftClickRelease() 
	{
		mouseRobot.mouseRelease(InputEvent.BUTTON1_MASK); //release click
		System.out.println("false"); 
		currentlyClicked = false;   // there is no left click 
		try {
		    Thread.sleep(250);                
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
   /* Function: moveMouse
   * --------------------
   * This function uses the data from the 9-DOF chip to move the mouse cursor. 
   * The function calls the readAxis function to turn the position of the 
   * glove into a corresponding position of the mouse cursor on the screen. 
   */
  public void moveMouse()
  {
	  int xReading = getCursorPosition(gesture.getOrientation().heading, 0); // x-axis movement
	  int yReading = getCursorPosition(gesture.getOrientation().roll, 1);    // y-axis movement
	  
	  
	  if(debugMouse)
	  {
		  System.out.println(TAG + ": moveMouse() : " + "Result   ->   xReading: " + xReading + "\t        yReading " + yReading);
	  }
	  
	  
	  //TODO: Make this pixel density independent 
	  move(xReading, yReading);       // move the mouse
  }

    /* Function: get_cursor_position
   * -----------------------------------------------------------------
   * This function is used when moving the mouse. The function turns the
   * position of the glove into a corresponding position of the mouse cursor
   * on the screen. 
   */
  private int getCursorPosition(float heading, int axisNumber) 
  {
      int distance = 0;    // distance from center of the output range
      if(xGloveController.DEBUG && axisNumber == 0) System.out.println(TAG + ": getCursorPostion(): " + "x - axis ->  " + " Input heading: " + heading);
      if(xGloveController.DEBUG && axisNumber == 1) System.out.println(TAG + ": getCursorPostion(): " + "y - axis ->  " + " Input heading:   " + heading);

      // map the reading from the analog input range to the output range:
      heading = map((int)heading, minima[axisNumber], maxima[axisNumber], 0, range);
      
      // if the output reading is outside from the
      // rest position threshold,  use it:
      if (Math.abs(heading - center) > threshold) distance = (center - (int)heading);
      
      // the reading needs to be inverted in order to 
      // map the movement correctly:


      if(debugMouse)
      {
    	  if(axisNumber == 0)
    	  {
    		  System.out.println(TAG + ": getCursorPostion(): " + "   Mapped heading: " + heading + "  Distance: " + distance  + "\n\t      Minima " + 
			  			 		 minima[axisNumber] + "\t              Maxima " + maxima[axisNumber]);
    	  }
    	  else if(axisNumber == 1)
    	  {
    		  System.out.println(TAG + ": getCursorPostion(): " + "   Mapped heading: " + heading + "  Distance: " + distance  + "\n\t      Minima " + 
    				  			 minima[axisNumber] + "\t      Maxima " + maxima[axisNumber]);
    	  }
      }
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
  
  public void mouseScroll()
  {
	System.out.println("SUCCESS");
	/* delay until user stretches ring finger again */
    while(!gesture.isReleaseScrollModeGesture())
    {
    	System.out.println("Wait");
    	xGloveDispatcher.threadSleep(50);
    }
          
    /* while loop will exit when ring finger is bent much more than middle finger */
    
	while(!gesture.isScrollModeGesture())
    { 
	  int inclinationPercentage = gesture.getInclinationFourFingers();
	  System.out.println("Scroll Mode: " + inclinationPercentage);  
      if(inclinationPercentage < 38)         // scroll up if fingers bent up
      {
    	  
    	  inclinationPercentage = gesture.getInclinationFourFingers();
    	  System.out.println("Inclination Percentage: " + inclinationPercentage + "  Scroll rate: " + (int)((38 - inclinationPercentage)/3));  
    	  mouseRobot.mouseWheel((int)((40 - inclinationPercentage) / 3));			     // scroll up
    	  mouseRobot.setAutoDelay(40);

      }   
      else if(inclinationPercentage > 53)   // scroll down if fingers bent down
      { 
    	  inclinationPercentage = gesture.getInclinationFourFingers();
    	  System.out.println("Inclination Percentage: " + inclinationPercentage + "  Scroll rate: " + (int)((inclinationPercentage - 52) / 3));
    	  mouseRobot.mouseWheel((int)((52 - inclinationPercentage) / 3));			     // scroll down
		  mouseRobot.setAutoDelay(40);
      }
    }
	
	/* delay until user stretches ring finger again */
	while(!gesture.isReleaseScrollModeGesture()) 
    {
		  System.out.println("ZZZZZZ");
		  xGloveDispatcher.threadSleep(20);
    }     
  }
  
  public void resetMouse(int[] minima, int[] maxima) 
  {
	  this.minima[0] = minima[0];
	  this.minima[1] = minima[1];
	  this.maxima[0] = maxima[0];
	  this.maxima[1] = maxima[1];
	  centerMouse();
	  if(xGloveController.DEBUG) System.out.println(TAG + ": resetMouse() : " + "Minima[0] : " + this.minima[0] + 
			  									"Minima[1] : " + this.minima[1] + "\nMaxima[0] : " + this.maxima[0] + "Maxima[1] : " + this.maxima[1]);
  }
  
  private long map(long x, long inMin, long inMax, long outMin, long outMax)
  {
	  if(inMax == inMin) return 0;
	  return (long)((x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin);
  }
}

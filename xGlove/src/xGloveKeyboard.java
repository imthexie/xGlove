import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

class xGloveKeyboard 
{
	//Used in debug logs
	private final String TAG = "xGloveKeyboard";

	Robot keyboardRobot;

  	xGloveGesture gesture;

	public xGloveKeyboard() 
	{
		try 
		{
			keyboardRobot = new Robot();
		} catch(AWTException e) 
		{
			e.printStackTrace();
		}

		gesture = new xGloveGesture();
	}

	public void doSpacebar() 
	{
		keyboardRobot.keyPress(KeyEvent.VK_SPACE);
		keyboardRobot.keyRelease(KeyEvent.VK_SPACE);
		
		while(!gesture.isSpacebarReleaseGesture())
		{
			xGloveDispatcher.threadSleep(20);
		}
	}

	/* Function: mac_launchpad
	 * -----------------------
	 * This function enters/exits the launchpad on Mac computers when the Glove is turned 
	 * upside down. The function uses the short key combination CMD-SHIFT-L to enter the
	 * launchpad. This shortkey combination can be set to enter launchpad in system 
	 * preferences. Usually the F4 key is used to enter the launchpad, but this did not work
	 * during initial tests of the glove. (TEST IF F4 WORKS)
	 */
	public void doMacLaunchpad()
	{
	    /* enter Launchpad */
		keyboardRobot.keyPress(KeyEvent.VK_SHIFT); 
		keyboardRobot.keyPress(KeyEvent.VK_META);   
		keyboardRobot.keyPress(KeyEvent.VK_L);
		
		keyboardRobot.keyRelease(KeyEvent.VK_SHIFT);
		keyboardRobot.keyRelease(KeyEvent.VK_META);
		keyboardRobot.keyRelease(KeyEvent.VK_L);
        
        /* Thread.sleep until glove is back in original position  */
        while(!gesture.rightSideUp()) 
        {    
            xGloveDispatcher.threadSleep(50);
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
	public void doLoadNext()
	{
		// exit when hand is back in original position
		while(gesture.isLoadNextGesture()) 
        {
			//fingers should be bend
			if(gesture.fist()) 
            {
            	// right arrow key for presentations
            	keyboardRobot.keyPress(KeyEvent.VK_RIGHT); 
                keyboardRobot.keyRelease(KeyEvent.VK_RIGHT);  
                
                // Load next page in browser
                // Web shortcuts not consistent throughout browsers and OS's
                keyboardRobot.keyPress(KeyEvent.VK_META);
                keyboardRobot.keyPress(KeyEvent.VK_CLOSE_BRACKET);
                keyboardRobot.keyRelease(KeyEvent.VK_META);
                keyboardRobot.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
                
                // Thread.sleep between iterations of this function
                xGloveDispatcher.threadSleep(1500);  
                
             }             
         }        
    }
	
    public void doLoadPrevious() 
    {
    	// exit when hand is back in original position
        while(gesture.isLoadPreviousGesture()) 
        {
        	//fingers should be bend
        	if(gesture.fist()) 
            {
        		//left arrow key
        		keyboardRobot.keyPress(KeyEvent.VK_LEFT); 
                keyboardRobot.keyRelease(KeyEvent.VK_LEFT);
                
                // Load previous page in browsers
                //Web shortcuts not consistent throughout browsers and OS's
                keyboardRobot.keyPress(KeyEvent.VK_META);
                keyboardRobot.keyPress(KeyEvent.VK_OPEN_BRACKET);
                keyboardRobot.keyRelease(KeyEvent.VK_META);
                keyboardRobot.keyRelease(KeyEvent.VK_OPEN_BRACKET);  
                
                // Thread.sleep between iterations of this function
                xGloveDispatcher.threadSleep(1500);   
            }             
        }  
    }
    
}
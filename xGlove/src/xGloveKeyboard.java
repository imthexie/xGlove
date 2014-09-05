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
			//System.out.println("wait for release spacebar");
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
	
	public void test()
	{
		System.out.println("test");
		xGloveDispatcher.threadSleep(500);
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
        // right arrow key
        while(gesture.isLoadNextGesture()) // exit when hand is back in original position
        {
            if(gesture.fingersBent()) //finger should be bend
            {
                keyboardRobot.keyPress(KeyEvent.VK_RIGHT); //right arrow key
                keyboardRobot.keyRelease(KeyEvent.VK_RIGHT);  
                
                //Web shortcuts not consistent throughout browsers and OS's
                keyboardRobot.keyPress(KeyEvent.VK_META);
                keyboardRobot.keyPress(KeyEvent.VK_CLOSE_BRACKET);
                keyboardRobot.keyRelease(KeyEvent.VK_META);
                keyboardRobot.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
                
                xGloveDispatcher.threadSleep(1500);  // Thread.sleep between iterations of this function
             }             
         }        
    }
	
    public void doLoadPrevious() 
    {
        while(gesture.isLoadPreviousGesture()) // exit when hand is back in original position
        {
            if(gesture.fingersBent()) //finger should be bend
            {
                keyboardRobot.keyPress(KeyEvent.VK_LEFT); //left arrow key
                keyboardRobot.keyRelease(KeyEvent.VK_LEFT);
                
                //Web shortcuts not consistent throughout browsers and OS's
                keyboardRobot.keyPress(KeyEvent.VK_META);
                keyboardRobot.keyPress(KeyEvent.VK_OPEN_BRACKET);
                keyboardRobot.keyRelease(KeyEvent.VK_META);
                keyboardRobot.keyRelease(KeyEvent.VK_OPEN_BRACKET);
                    
                xGloveDispatcher.threadSleep(1500);   // Thread.sleep between iterations of this function
            }             
        }  
    }
}
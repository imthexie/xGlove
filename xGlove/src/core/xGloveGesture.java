package core;
class xGloveGesture 
{
	//Used in debug logs
	private final String TAG = "xGloveGesture";
	
    private xGloveSensor sensor; //reference to the sensor
    
    public xGloveGesture() 
    {
        this.sensor = xGloveDispatcher.getSensor();
    }
    
    public Orientation getOrientation() 
    { 
        return sensor.getOrientation(); 
    }
    
    public boolean fist()  
    {
    	return sensor.allFingersBent(84);
    }
    
    public boolean fistLight()  
    {
    	return sensor.allFingersBent(76);
    }
    
    public boolean fingersBent()  
    {
        return sensor.allFingersBent(50);
    } 
    
    public boolean fingersSpread()      
    {
        return !sensor.allFingersBent(50);    
    } 
    
    public boolean isStable() 
    {
    	if((Math.abs(sensor.getOrientation().roll) < 35) && (Math.abs(sensor.getOrientation().pitch) < 35)) return true;
    	return false;
    }
    
    //Consider implementing this with a Timer instead. Or just make this event a non-delayed event
    public boolean upsideDown()
    {
    	if(Math.abs(sensor.getOrientation().roll) > 160)
        {
            /* This for loop is used to ensure that the glove is upside down */
            /* for at least .5 second -> 10 * 0.05 second                    */
            /* This is to avoid measurement errors.                          */         
            for(int i = 0; i < 10; i+=1)
            {
            	//break if sensor is back in original position
                if(Math.abs(sensor.getOrientation().roll) < 115) return false;

                //Delay
                xGloveDispatcher.threadSleep(50);                             
            }    
            return true;  
        }
        return false;    
    }
    
    public boolean rightSideUp()
    {
        return Math.abs(sensor.getOrientation().roll) < 40;
    } 

    // TODO: gesture for spacebar
    public boolean isSpacebarGesture() 
    {
        if(fist() && Math.abs(sensor.getOrientation().pitch) < 30) return true;
        return false;   
    } 
    
    public boolean isSpacebarReleaseGesture()
    {
    	if(!sensor.allFingersBent(50)) return true;
    	return false;
    	//return true;
    }
    
    public boolean isMouseClickGesture(boolean currentlyClicked) 
    {
    	// Don't fire if currently clicked
    	if(currentlyClicked) return false;
    	else 
    	{
    		//System.out.println("bent "+ sensor.allFingersBent(40));
    		//System.out.println("pitch "+ (Math.abs(sensor.getOrientation().pitch) < 35));
    		
    		if((sensor.allFingersBent(40)) && (Math.abs(sensor.getOrientation().pitch) < 35)) return true;
    		return false;
    	}
    }
    
    public boolean isDragMouseGesture(boolean currentlyClicked)
    {
    	if(currentlyClicked)
    	{
    		return false;
    	}
    	else 
    	{
    		currentlyClicked = (sensor.isFingerBent(Finger.RING, 72) && sensor.isFingerBent(Finger.MIDDLE, 75) && (!sensor.isFingerBent(Finger.INDEX, 50)));
    	}
		

    	if(Debug.DEBUG_MOUSE)
    	{
    		System.out.println("ring bent "+ (sensor.isFingerBent(Finger.RING, 72)));
    		System.out.println("middle bent "+ (sensor.isFingerBent(Finger.MIDDLE, 75)));
    		System.out.println("index not bent "+ (!sensor.isFingerBent(Finger.INDEX, 50)));
    	}
    	return currentlyClicked;
    }

    public boolean isMouseReleaseGesture(boolean currentlyClicked) 
    {
    	return currentlyClicked && sensor.allFingersSpread();
    }
    
    public boolean isScrollModeGesture() 
    {
        return sensor.isFingerBent(Finger.RING, 83) && !(sensor.isFingerBent(Finger.MIDDLE, 56)) && !(sensor.isFingerBent(Finger.INDEX, 56));
    }
    
    public boolean isReleaseScrollModeGesture() 
    {
    	//System.out.println("middle not bent "+ (sensor.isFingerBent(Finger.MIDDLE, 20)));
    	//System.out.println("ring bent "+ (!sensor.isFingerBent(Finger.RING, 67)));
    	return !sensor.isFingerBent(Finger.RING, 67) && (sensor.isFingerBent(Finger.MIDDLE, 20));
    }
    
    public boolean isMouseExitGesture()
    {
    	if(sensor.getOrientation().roll > 50 && sensor.getOrientation().roll < 90 &&
    	   Math.abs(sensor.getOrientation().pitch) < 30)
    	{
    		for(int i = 0; i < 800; i++)
    		{
    			if(sensor.getOrientation().roll < 30 && fist())
    			{
    				while(sensor.allFingersBent(65));
    				return true;
    			}
    			else if(Math.abs(sensor.getOrientation().pitch) > 50) break;
    			xGloveDispatcher.threadSleep(2);    
    		}
    	}
    	return false;
    }
    
    public boolean isLoadNextGesture() 
    {
    	//System.out.println("Next");
    	//System.out.println("pitch "+ (sensor.getOrientation().pitch < -50));
    	//System.out.println("roll "+ (Math.abs(sensor.getOrientation().roll) < 100));
        return ((sensor.getOrientation().pitch < -50) && (Math.abs(sensor.getOrientation().roll) < 100));
    }
    
    public boolean isLoadPreviousGesture() 
    {
    	//System.out.println("Prev");
    	//System.out.println("pitch "+ (sensor.getOrientation().pitch >  70));
    	//System.out.println("roll "+ (Math.abs(sensor.getOrientation().roll) < 100));
        return ((sensor.getOrientation().pitch >  70) && (Math.abs(sensor.getOrientation().roll) < 100));
    }
    
    //Bend only index finger
    public boolean isToggleDongleGesture() {
    	return sensor.isFingerBent(Finger.INDEX, 60) && 
    			!sensor.isFingerBent(Finger.MIDDLE, 50) && !sensor.isFingerBent(Finger.RING, 50);
    }
    
    
    public int getInclinationFourFingers() 
    {
        return sensor.getInclinationFourFingers();
    }
}
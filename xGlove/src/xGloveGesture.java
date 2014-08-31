class xGloveGesture 
{
    private xGloveSensor sensor; //reference to the sensor

    int minima[] = {0, -40};         // actual analogRead minima for {x, y}
    int maxima[] = {0, 40};          // actual analogRead maxima for {x, y}

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
        return sensor.allFingersBent(65);
    }

    public boolean fingersBent() 
    {
        return sensor.allFingersBent(50);
    }

    public boolean fingersSpread() 
    {
        return !sensor.allFingersBent(50);
    }


    //TODO: implement this with a Timer instead. Or just make this event a non-delayed event
    public boolean upsideDown()
    {
        if(Math.abs(sensor.getOrientation().roll) > 170)
        {
            /* This for loop is used to ensure that the glove is upside down */
            /* for at least .5 second (10*delay(50)).                        */
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

    //TODO: gesture for spacebar
    public boolean isSpacebarGesture() 
    {
        //??
        return false;   
    }

    public boolean rightSideUp()
    {
        return Math.abs(sensor.getOrientation().roll) < 50;
    }  

    public boolean isMouseClickGesture(boolean currentlyClicked) 
    {
        return (fist() && !currentlyClicked && Math.abs(sensor.getOrientation().pitch) < 35);
    }

    public boolean isMouseReleaseGesture(boolean currentlyClicked) 
    {
    	return currentlyClicked && sensor.allFingersSpread();
    }

    public boolean isScrollModeGesture() 
    {
        return sensor.isFingerBent(Finger.RING, 90) && !(sensor.isFingerBent(Finger.MIDDLE, 50)) && !(sensor.isFingerBent(Finger.INDEX, 50));
    }

    public boolean isReleaseScrollModeGesture() 
    {
        return sensor.isFingerBent(Finger.RING, 70) && !(sensor.isFingerBent(Finger.MIDDLE, 50));
    }

    public boolean isLoadNextGesture() 
    {
        return sensor.getOrientation().pitch > 70;
    }

    public boolean isLoadPreviousGesture() 
    {
        return sensor.getOrientation().pitch < -70;
    }

    public int getInclinationPercentage() 
    {
        return sensor.getInclinationPercentage();
    }
}
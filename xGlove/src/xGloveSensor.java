class xGloveSensor 
{
	//Used in debug logs
	private final String TAG = "xGloveSensor";
	
	private int thumbBent;
	private int indexFingerBent;
    private int middleFingerBent;
    private int ringFingerBent;
    private int pinkyBent;
    private int allFingersBent;
  
    private int thumbVal;
    private int indexVal;
    private int middleVal;
    private int ringVal;  
    private int pinkyVal;

    private Orientation orientation;

	public xGloveSensor() 
	{
		thumbBent = 250;
	    indexFingerBent  =  250;
	    middleFingerBent =  250;
	    ringFingerBent   =  250;
	    pinkyBent         =  250;
	    allFingersBent   =  indexFingerBent + middleFingerBent + ringFingerBent + pinkyBent; 

	    orientation = new Orientation();
	}

	public void updateFlexValues(int thumbVal, int indexVal, int middleVal, int ringVal, int pinkyVal) 
	{
		this.thumbVal = thumbVal;
		this.indexVal = indexVal;
		this.middleVal = middleVal;
		this.ringVal = ringVal;
		this.pinkyVal = pinkyVal;
		if(xGloveController.DEBUG) System.out.println(TAG + " updateFlexSensors(): " + "Thumb: " + this.thumbVal 
				+ " Index: " + this.indexVal + " Middle: " + this.middleVal + " Ring: " + this.ringVal + " Pinky: " + this.pinkyVal);
	}

	public void updateOrientation(float roll, float pitch, float heading) 
	{
	    orientation.roll = roll;
	    orientation.pitch = pitch;
	    orientation.heading = heading;
	    
	    if(xGloveController.DEBUG) System.out.println(TAG + " updateOrientation(): " + "Roll: " + orientation.roll 
	    											+ " Pitch: " + orientation.pitch + " Heading: " + orientation.heading);
  	}

	public boolean isFingerBent(Finger finger, double percentageBent) 
	{
		boolean bent = (getFlexValue(finger) < (percentageBent / 100) * getBentValue(finger));
		if(xGloveController.DEBUG) System.out.println(TAG + " isFingerBent(): " + "Finger : " + finger.toString() + " Is Bent: " + bent); 
		return bent;
		
	}
	
	public boolean allFingersBent(double percentageBent)
	{
		boolean allBent = indexVal + middleVal + ringVal +
	            pinkyVal < ((percentageBent/100) * allFingersBent);
		if(xGloveController.DEBUG) System.out.println(TAG + " allFingersBent(): Is Bent: " + allBent); 
	    return allBent;
	}
	
	public boolean allFingersSpread() 
	{
		boolean allSpread = !allFingersBent(25);
		if(xGloveController.DEBUG) System.out.println(TAG + " allFingersSpread(): Is Bent: " + allSpread); 
		return allSpread;
	}

	public int getInclinationPercentage()
	{
		int inclinationPercentage = (indexVal + middleVal + ringVal + pinkyVal); 
		if(xGloveController.DEBUG) System.out.println(TAG + " getInclinationPercentage(): " + inclinationPercentage); 
	    return inclinationPercentage; 
	}

    public int getFlexValue(Finger finger) 
    {
        switch(finger) 
        {
    	case THUMB : return thumbVal;
        case INDEX : return indexVal;
        case MIDDLE : return middleVal;
        case RING : return ringVal;
        case PINKY : return pinkyVal;
        default : return -1;
        }
    }
    
    public int getBentValue(Finger finger) 
    {
        switch(finger) 
        {
        case THUMB : return thumbBent;
        case INDEX : return indexFingerBent;
        case MIDDLE : return middleFingerBent;
        case RING : return ringFingerBent;
        case PINKY : return pinkyBent;
        default : return -1;
        }
    }

    public Orientation getOrientation() 
    {
    	return orientation;
    }
}
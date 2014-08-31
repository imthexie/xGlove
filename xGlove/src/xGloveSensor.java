class xGloveSensor 
{
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
	}

	public void updateOrientation(float pitch, float heading, float roll) 
	{
	    orientation.pitch = pitch;
	    orientation.heading = heading;
	    orientation.roll = roll;
  	}

	public boolean isFingerBent(Finger finger, double percentageBent) 
	{
		return (getFlexValue(finger) < (percentageBent / 100) * getBentValue(finger));
	}
	
	public boolean allFingersBent(double percentageBent)
	{
	    return indexVal + middleVal + ringVal +
	            pinkyVal < ((percentageBent/100) * allFingersBent);
	}
	
	public boolean allFingersSpread() 
	{
		return !allFingersBent(25);
	}

	public int getInclinationPercentage()
	{
	    return (indexVal + middleVal + ringVal + pinkyVal); 
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
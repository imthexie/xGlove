class xGloveSensor 
{
	//Used in debug logs
	private final String TAG = "xGloveSensor";
	
	private int thumbBent;
    private int indexBent;
    private int middleBent;
    private int ringBent;
    private int pinkyBent;
    private int fourFingersBent; 
    private int allFingersBent;


    /* These variables indicate the analogRead values when the
     * corresponding fingers are fully stretched.
     */
    private int thumbStretch;
    private int indexStretch;
    private int middleStretch;
	private int ringStretch;
	private int pinkyStretch;
	private int fourFingersStretch;
	private int allFingersStretch;

	private int thumbVal;
	private int indexVal;
	private int middleVal;
	private int ringVal;  
	private int pinkyVal;

	private int thumbRange;
	private int indexRange;
	private int middleRange;
	private int ringRange;  
	private int pinkyRange;
	private int fourFingersRange;
	private int allFingersRange;

	private Orientation orientation;

	public xGloveSensor() 
	{
		thumbBent 		   =  85;
		middleBent         =  80;
	    ringBent           =  120;
	    pinkyBent          =  50;
	    fourFingersBent    =  indexBent + middleBent + ringBent + pinkyBent;
	    allFingersBent     =  thumbBent + indexBent + middleBent + ringBent + pinkyBent; 

	    thumbStretch	   =  185;
	    indexStretch 	   =  230;
	    middleStretch	   =  280;
	    ringStretch	 	   =  250;
	    pinkyStretch 	   =  165;
	    fourFingersStretch =  indexStretch + middleStretch + ringStretch + pinkyStretch;
	    allFingersStretch  =  fourFingersStretch + thumbStretch;
	    
	    thumbRange		   =  thumbStretch 		 -  thumbBent;
	    indexRange		   =  indexStretch 		 -  indexBent;
	    middleRange		   =  middleStretch 	 -  middleBent;
	    ringRange	  	   =  ringStretch 		 -  ringBent;  
	    pinkyRange		   =  pinkyStretch 		 -  pinkyBent;
	    fourFingersRange   =  fourFingersStretch -  fourFingersBent;
	    allFingersRange    =  allFingersStretch  -  allFingersBent;
	    
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
		boolean test = true;
		if(test)
		{
			System.out.println("bent value  " + (thumbVal + indexVal + middleVal + ringVal + pinkyVal));
			System.out.println("set value " + (allFingersStretch  - ((double)percentageBent/100.0) * 
								(double)allFingersRange));
		}
		return ((thumbVal + indexVal + middleVal + ringVal + pinkyVal)  <  allFingersStretch  - ((double)percentageBent/100.0) * 
				 (double)allFingersRange);
	}
	
	public boolean allFingersSpread() 
	{
		boolean allSpread = !allFingersBent(40);
		System.out.println(TAG + " allFingersSpread(): " + allSpread); 
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
        	case INDEX : return indexBent;
        	case MIDDLE : return middleBent;
        	case RING : return ringBent;
        	case PINKY : return pinkyBent;
        	default : return -1;
        }
    }
    
    public Orientation getOrientation() 
    {
    	return orientation;
    }
}
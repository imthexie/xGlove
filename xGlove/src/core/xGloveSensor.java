package core;
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
		thumbBent 		   =  0; //updated 9/23
		indexBent 		   =  680; //updated 9/23
		middleBent         =  370; //updated 9/23
	    ringBent           =  840; //updated 9/23
	    pinkyBent          =  0;
	    fourFingersBent    =  indexBent + middleBent + ringBent + pinkyBent;
	    allFingersBent     =  thumbBent + indexBent + middleBent + ringBent + pinkyBent; 

	    thumbStretch	   =  0; //updated 9/23
	    indexStretch 	   =  860; //updated 9/23
	    middleStretch	   =  580; //updated 9/23
	    ringStretch	 	   =  1000; //updated 9/23
	    pinkyStretch 	   =  0; //updated 9/23
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
		if(Debug.MAIN_DEBUG) System.out.println(TAG + " updateFlexSensors(): " + "Thumb: " + this.thumbVal 
				+ " Index: " + this.indexVal + " Middle: " + this.middleVal + " Ring: " + this.ringVal + " Pinky: " + this.pinkyVal);
	}
	
	public void updateOrientation(float roll, float pitch, float heading) 
	{
	    orientation.roll = roll;
	    orientation.pitch = pitch;
	    orientation.heading = heading;
	    
	    if(Debug.MAIN_DEBUG) System.out.println(TAG + " updateOrientation(): " + "Roll: " + orientation.roll 
	    											+ " Pitch: " + orientation.pitch + " Heading: " + orientation.heading);
  	}
	
	public boolean isFingerBent(Finger finger, double percentageBent) 
	{
		switch(finger)
		{
			case THUMB   	: return (thumbVal  <  thumbStretch  - (percentageBent/100.0)  *  thumbRange  );
			case INDEX   	: return (indexVal  <  indexStretch  - (percentageBent/100.0)  *  indexRange  );
			case MIDDLE  	: return (middleVal <  middleStretch - (percentageBent/100.0)  *  middleRange );
			case RING    	: return (ringVal   <  ringStretch   - (percentageBent/100.0)  *  ringRange   );
			case PINKY   	: return (pinkyVal  <  pinkyStretch  - (percentageBent/100.0)  *  pinkyRange  );
			default      	: return false;
		}
	}
	
	public boolean allFingersBent(double percentageBent)
	{
		return isFingerBent(Finger.INDEX, percentageBent) && isFingerBent(Finger.MIDDLE, percentageBent)
					&& isFingerBent(Finger.RING, percentageBent);
	}
	
	public boolean allFingersSpread() 
	{
		boolean allSpread = !allFingersBent(62);
		if(Debug.MAIN_DEBUG) System.out.println(TAG + " allFingersSpread(): Is Bent: " + allSpread); 
		return allSpread;
	}
	
	public int getInclinationPercentage(Finger finger)
	{
		switch(finger)
		{
			case THUMB   	: return  (100 *  (thumbStretch  - thumbVal ))  /  thumbRange ;
			case INDEX   	: return  (100 *  (indexStretch  - indexVal ))  /  indexRange ;
			case MIDDLE  	: return  (100 *  (middleStretch - middleVal))  /  middleRange;
			case RING    	: return  (100 *  (ringStretch   - ringVal  ))  /  ringRange  ;
			case PINKY   	: return  (100 *  (pinkyStretch  - pinkyVal ))  /  pinkyRange ;
			default 		: return  -1;
		}
	}
	
	public int getInclinationFourFingers()
	{
		return (100 *  (fourFingersStretch  - (indexVal + middleVal + ringVal + pinkyVal)))  /  fourFingersRange;
	}
	
    public int getFlexValue(Finger finger) 
    {
        switch(finger) 
        {
    		case THUMB  : return thumbVal;
    		case INDEX  : return indexVal;
    		case MIDDLE : return middleVal;
    		case RING   : return ringVal;
    		case PINKY  : return pinkyVal;
    		default     : return -1;
        }
    }
    
    public int getBentValue(Finger finger) 
    {
        switch(finger) 
        {
        	case THUMB   : return thumbBent;
        	case INDEX   : return indexBent;
        	case MIDDLE  : return middleBent;
        	case RING    : return ringBent;
        	case PINKY   : return pinkyBent;
        	default      : return -1;
        }
    }
    
    public Orientation getOrientation() 
    {
    	return orientation;
    }
}
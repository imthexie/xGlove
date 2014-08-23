class xGloveSensor {
	private int indexFingerBent;
    private int middleFingerBent;
    private int ringFingerBent;
    private int pinkyBent;
    private int allFingersBent;
  
    private int indexVal;
    private int middleVal;
    private int ringVal;  
    private int pinkyVal;

    private Orientation orientation;

	public xGloveSensor() {
	    indexFingerBent  =  250;
	    middleFingerBent =  250;
	    ringFingerBent   =  250;
	    pinkyBent         =  250;
	    allFingersBent   =  indexFingerBent + middleFingerBent + ringFingerBent + pinkyBent; 

	    orientation = new Orientation();
	}

	public void updateFlexValues(int indexVal, int middleVal, int ringVal, int pinkyVal) {
		this.indexVal = indexVal;
		this.middleVal = middleVal;
		this.ringVal = ringVal;
		this.pinkyVal = pinkyVal;
	}

	public void updateOrientation(float pitch, float heading, float roll) {
	    orientation.pitch = pitch;
	    orientation.heading = heading;
	    orientation.roll = roll;
  	}


	public boolean indexFingerBent(double percentage_bent) 
	{
	    return (indexVal < (percentage_bent/100) * indexFingerBent);
	}


	public boolean middleFingerBent(double percentage_bent)
	{
	    return (middleVal < (percentage_bent/100) * middleFingerBent);
	}


	public boolean ringFingerBent(double percentage_bent)
	{
	    return (ringVal < (percentage_bent/100) * ringFingerBent);
	}


	public boolean pinkyBent(double percentage_bent)
	{
	    return (pinkyVal < (percentage_bent/100) * pinkyBent);
	}


	public boolean allFingersBent(double percentage_bent)
	{
	    return indexVal + middleVal + ringVal +
	            pinkyVal < ((percentage_bent/100) * allFingersBent);
	}
	
	public boolean allFingersSpread() {
		return !allFingersBent(25);
	}

	public int getInclinationPercentage(){
	    return (indexVal + middleVal + ringVal + pinkyVal); 
	}

	public enum Finger {
        INDEX_TOP, MIDDLE, RING, PINKY;
    }

    int getFlexValue(Finger finger) {
        switch(finger) {
            case INDEX_TOP : return indexVal;
            case MIDDLE : return middleVal;
            case RING : return ringVal;
            case PINKY : return pinkyVal;
            default : return -1;
        }
    }

    public Orientation getOrientation() {return orientation;}
}
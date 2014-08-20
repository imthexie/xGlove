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
	    fourFingersBent   =  indexFingerBent + middleFingerBent + ringFingerBent + pinkyBent; 

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
	    return (indexVal + middleVal + ringVal +
	            pinkyVal < (percentage_bent/100) * _all_fingers_bent);
	}

	public int getInclinationPercentage(){
	    return (indexVal + middleVal + ringVal + pinkyVal); 
	}

	public enum Finger {
        INDEX_TOP, MIDDLE, RING, PINKY;
    }

    int getFlexValue(Finger finger) {
        switch(finger) {
            case Finger.INDEX_TOP : return indexTopVal;
            case Finger.MIDDLE : return middleVal;
            case Finger.RING : return ringVal;
            case Finger.PINKY : return pinkyVal;
        }
    }

    public Orientation getOrientation() {return orientation;}
}
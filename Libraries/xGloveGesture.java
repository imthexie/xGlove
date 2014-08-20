class Gesture {
    private xGloveSensor sensor; //reference to the sensor

    public Gesture() {
        this.sensor = xGloveDispatcher.getSensor();
    }

    public Orientation getOrientation() {
        return sensor.getOrientation();
    }

    public boolean fist() {
        return sensor.allFingersBent(65);
    }

    public boolean fingersBent() {
        return sensor.allFingersBent(50);
    }

    public boolean fingersSpread() {
        return !sensor.allFingersBent(50);
    }


    //TODO: implement this with a Timer instead. Or just make this event a non-delayed event
    public boolean upsideDown(){
        if(Math.abs(sensor.getOrientation().roll) > 170)
        {
            /* This for loop is used to ensure that the glove is upside down */
            /* for at least .5 second (10*delay(50)).                        */
            /* This is to avoid measurement errors.                          */         
            for(int i = 0; i < 10; i+=1){
                
                Thread.sleep(50);

                //sensor.getOrientation() probably updated in background thread.
                
                if(Math.abs(sensor.getOrientation().roll) < 115) 
                { //break if sensor is back in original position
                    return false;
                }
            }    
            return true;  
        }
        return false;    
    }

    public boolean rightSideUp(){
        return Math.abs(sensor.getOrientation().roll) < 50;
    }  

    public boolean isMouseClickGesture(boolean currentlyClicked) {
        return (fist() && !currentlyClicked && Math.abs(orientation.pitch) < 35);
    }

    public boolean isMouseReleaseGesture(boolean currentlyClicked) {
    return currentlyClicked && sensor.fingers_spread());
  }

    public boolean scrollModeGesture() {
        return sensor.ringFingerBent(90) && !(sensor.middleFingerBent(50)) && !(sensor.indexFingerBent(50));
    }

    public boolean releaseScrollModeGesture() {
        return sensor.ringFingerBent(70) && !(sensor.middleFingerBent(50));
    }

    public boolean isLoadNextGesture() {
        return sensor.getOrientation().pitch > 70;
    }

    public boolean isLoadPreviousGesture() {
        return sensor.getOrientation().pitch < -70;
    }

    public int getInclinationPercentage() {
        return sensor.getInclinationPercentage();
    }
}
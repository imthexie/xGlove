import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* Event Dispatcher that is meant to be run inside a firmware 
* loop. In the loop, call checkSensorValues() and then
* dispatchJobs(). Blocking and non-blocking jobs can be 
* called with the argument to the jobs themselves in the private portion of the class. Sensor readings
* can be received from getter methods in this class. Non-blocking jobs can be 
* done simultaneously.
**/

class xGloveDispatcher {

    //Finger and sensor values. TODO: Investigate need to use Atomic for concurrency
    public static xGloveSensor sensor;
    public xGloveMouse mouse;
    public xGloveKeyboard keyboard;

    //Thread Pool that takes care of the events 
    private ExecutorService threadPool;


    //Public methods
    public xGloveDispatcher() {
        //Tweak the number of threads here
        threadPool = Executors.newFixedThreadPool(2);
        sensor = new xGloveSensor();
        mouse = new xGloveMouse(); //Mouse must be constructed after sensor
        keyboard - new xGloveKeyboard();
    };

    public void updateSensorValues(float orientationPitch, float orientationRoll, float orientationHeading, 
                                    int indexVal, int middleVal, int ringVal, int pinkyVal) {
        sensor.updateOrientation(orientationPitch, orientationRoll, orientationHeading);
        sensor.updateFlexValues(indexVal, middleVal, ringVal, pinkyVal);
    }

    public void dispatchEvents() {
        //TODO
        threadPool.execute()

    };

    public static xGloveSensor getSensor() {return sensor;}
}
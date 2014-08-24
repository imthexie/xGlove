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
    private xGloveMouse mouse;
    private xGloveKeyboard keyboard;
    private xGloveGesture gesture;
    
    //Assorted jobs and events for the threads to do
    private mouseMoveEvent mouseMoveEvent;
    private mouseClickEvent mouseClickEvent;
    private mouseClickReleaseEvent mouseClickReleaseEvent;

    //Thread Pool that takes care of the events 
    private ExecutorService threadPool;


    //Public methods
    public xGloveDispatcher() {
        //Tweak the number of threads here
        threadPool = Executors.newFixedThreadPool(3);
        sensor = new xGloveSensor();
        gesture = new xGloveGesture();
        mouse = new xGloveMouse(); //Mouse must be constructed after sensor
        keyboard = new xGloveKeyboard();

        mouseMoveEvent = new mouseMoveEvent();
        mouseClickEvent = new mouseClickEvent();
        mouseClickReleaseEvent = new mouseClickReleaseEvent();
    };

    public void updateSensorValues(float orientationPitch, float orientationHeading, float orientationRoll, 
                                    int indexVal, int middleVal, int ringVal, int pinkyVal) {
        sensor.updateOrientation(orientationPitch, orientationHeading, orientationRoll);
        sensor.updateFlexValues(indexVal, middleVal, ringVal, pinkyVal);
    }

    public void dispatchEvents() {
        //Always try to move the mouse for now
        threadPool.execute(mouseMoveEvent);

        //Mouse click and release
        if(gesture.isMouseClickGesture(mouse.isCurrentlyClicked())) {
            threadPool.execute(mouseClickEvent); 
        }
        else if(gesture.isMouseReleaseGesture(mouse.isCurrentlyClicked())) { 
            threadPool.execute(mouseClickReleaseEvent);
        }

        //Scrolling is a blocking function. No Thread pool
        if(gesture.isScrollModeGesture()) {
            mouse.mouseScroll();
        }

        //Mac launchpad is a blocking function
        if(gesture.upsideDown()) {
            keyboard.doMacLaunchpad();
        }

        //Load next and previous slide is also blocking
        if(gesture.isLoadNextGesture()) {
            keyboard.doLoadNext();
        } 
        else if (gesture.isLoadPreviousGesture()) {
            keyboard.doLoadPrevious();
        }
    };

    public static xGloveSensor getSensor() {return sensor;}

    /**Thread jobs**/

    //A Thread job for moving the mouse
    private class mouseMoveEvent implements Runnable {
        
        public mouseMoveEvent() {}

        @Override
        public void run() {
            mouse.moveMouse();
        }
    }

    //A Thread job for moving the mouse
    private class mouseClickEvent implements Runnable {
        
        public mouseClickEvent() {}

        @Override
        public void run() {
            mouse.doMouseLeftClick();
        }
    }

    private class mouseClickReleaseEvent implements Runnable {

        public mouseClickReleaseEvent() {}

        @Override
        public void run() {
            mouse.doMouseLeftClickRelease();
        }
    }

    public void reset(float orientationPitch, float orientationHeading, float orientationRoll, 
    					int range, int threshold, int center, 
    					int[] minima, int[] maxima) {
    	sensor.updateOrientation(orientationPitch, orientationHeading, orientationRoll);
    	mouse.resetMouse(range, threshold, center, minima, maxima);
    }

    public void killExecutor() {
        threadPool.shutdown();
        //Wait for thread pool to shut down
        while (!threadPool.isTerminated()) {}
    }

    public static void threadSleep(int millis) {
    	try {
    		Thread.sleep(millis);
    	} catch(InterruptedException e) {
    		System.out.println("Thread interrupted.");
    	}
    }
}
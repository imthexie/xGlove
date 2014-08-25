import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
* Event Dispatcher that is meant to be run inside a firmware 
* loop. In the loop, call checkSensorValues() and then
* dispatchJobs(). Blocking and non-blocking jobs can be 
* called with the argument to the jobs themselves in the private portion of the class. Sensor readings
* can be received from getter methods in this class. Non-blocking jobs can be 
* done simultaneously.
**/

class xGloveDispatcher {
	
	//Dispatcher Event to dispatch events upon

    //Finger and sensor values. TODO: Investigate need to use Atomic for concurrency
    public static xGloveSensor sensor;
    private xGloveMouse mouse;
    private xGloveKeyboard keyboard;
    private xGloveGesture gesture;
    
    //Assorted jobs and events for the threads to do
    private mouseMoveEvent mouseMoveEvent;
    private mouseClickEvent mouseClickEvent;
    private mouseClickReleaseEvent mouseClickReleaseEvent;
    private dispatcherEvent dispatcherEvent;
    
    //Thread Pool that takes care of the events 
    private ExecutorService threadPool;
    private ExecutorService dispatcherThread;
    
    //Boolean for if the dispatcher is on a blocking action, don't fire events.
    private volatile boolean dispatcherBlocked;

    //Public methods
    public xGloveDispatcher() {
    	//Dispatcher thread that waits for events
    	dispatcherThread = Executors.newFixedThreadPool(1);
    	dispatcherBlocked = false;
    	
        //Tweak the number of threads here
        threadPool = Executors.newFixedThreadPool(3);
        sensor = new xGloveSensor();
        gesture = new xGloveGesture();
        mouse = new xGloveMouse(); //Mouse must be constructed after sensor
        keyboard = new xGloveKeyboard();

        mouseMoveEvent = new mouseMoveEvent();
        mouseClickEvent = new mouseClickEvent();
        mouseClickReleaseEvent = new mouseClickReleaseEvent();
        dispatcherEvent = new dispatcherEvent();
    };

    public void updateSensorValues(float orientationPitch, float orientationHeading, float orientationRoll, 
                                    int indexVal, int middleVal, int ringVal, int pinkyVal) {
    	if(xGloveController.DEBUG) System.out.println("Updating Sensors");

        sensor.updateOrientation(orientationPitch, orientationHeading, orientationRoll);
        sensor.updateFlexValues(indexVal, middleVal, ringVal, pinkyVal);
        if(!dispatcherBlocked) dispatcherThread.execute(dispatcherEvent);
    }

    public void dispatchEvents() {
    	
    	if(xGloveController.DEBUG) System.out.println("Dispatching Events");
    	
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
        	dispatcherBlocked = true;
            mouse.mouseScroll();
        }

        //Mac launchpad is a blocking function
        if(gesture.upsideDown()) {
        	dispatcherBlocked = true;
            keyboard.doMacLaunchpad();
        }

        //Load next and previous slide is also blocking
        if(gesture.isLoadNextGesture()) {
        	dispatcherBlocked = true;
            keyboard.doLoadNext();
        } 
        else if (gesture.isLoadPreviousGesture()) {
        	dispatcherBlocked = true;
            keyboard.doLoadPrevious();
        }
    };

    public static xGloveSensor getSensor() {return sensor;}

    /**Thread jobs**/
    
    private class dispatcherEvent implements Runnable {
    	public dispatcherEvent() {}
    	@Override 
    	public void run() {
    		dispatchEvents();
    	}
    }

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

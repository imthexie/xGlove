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
	//Used in debug logs
	private final String TAG = "xGloveDispatcher";
	
	private static boolean moveMouse = true;
	
    //Finger and sensor values. TODO: Investigate need to use Atomic for concurrency
    public static xGloveSensor sensor;
    private xGloveMouse        mouse;
    private xGloveKeyboard     keyboard;
    private xGloveGesture      gesture;
    
    //Assorted jobs and events for the threads to do
    private mouseMoveEvent         mouseMoveEvent;
    private mouseClickEvent        mouseClickEvent;
    private mouseClickReleaseEvent mouseClickReleaseEvent;
    private dispatcherEvent        dispatcherEvent;
    
    //Thread Pool that takes care of the events 
    private ExecutorService threadPool;
    private ExecutorService dispatcherThread;
    private ExecutorService mouseThread;
    
    //Boolean for if the dispatcher is on a blocking action, don't fire events.
    private volatile boolean dispatcherBlocked;
    
    //Public methods
    public xGloveDispatcher() 
    {
    	//Dispatcher thread that waits for events
    	dispatcherThread = Executors.newFixedThreadPool(1);
    	dispatcherBlocked = false;
    	
    	//Mouse thread
    	mouseThread = Executors.newFixedThreadPool(1);
    	
        //Tweak the number of threads here
        threadPool             =      Executors.newFixedThreadPool(2);
        sensor                 =      new xGloveSensor();
        gesture                =      new xGloveGesture();
        mouse                  =      new xGloveMouse(); //Mouse must be constructed after sensor
        keyboard               =      new xGloveKeyboard();
        
        mouseMoveEvent         =      new mouseMoveEvent();
        mouseClickEvent        =      new mouseClickEvent();
        mouseClickReleaseEvent =      new mouseClickReleaseEvent();
        dispatcherEvent        =      new dispatcherEvent();
    };

    //Called continuously to update sensor values
    public void updateSensorValues(float orientationRoll, float orientationPitch, float orientationHeading, 
                                    int thumbVal, int indexVal, int middleVal, int ringVal, int pinkyVal) 
    {
    	if(xGloveController.DEBUG) System.out.println(TAG + ": Updating Sensors");

        sensor.updateOrientation(orientationRoll, orientationPitch, orientationHeading);
        sensor.updateFlexValues(thumbVal, indexVal, middleVal, ringVal, pinkyVal);
        if(!dispatcherBlocked) dispatcherThread.execute(dispatcherEvent);
    }
    
    //Based on sensor values, do events
    public void dispatchEvents() 
    {
    	if(xGloveController.DEBUG) System.out.println("Dispatching Events");
    	
    	/* Move mouse */
    	if(moveMouse)
    	{
    		mouseThread.execute(mouseMoveEvent);
    	}
    	
    	/* Gestures */
    	if(gesture.isMouseClickGesture(mouse.isCurrentlyClicked())) 
      	{
	    	threadPool.execute(mouseClickEvent); 
	    }
    	else if(gesture.isDragMouseGesture()) // drag when thumb and index finger are stretched, and middle finger,										
        {								      // ring finger and pinky are bent 
            mouse.doDragMouse();
        }
	    else if(gesture.isMouseReleaseGesture(mouse.isCurrentlyClicked())) 
	    { 
	    	threadPool.execute(mouseClickReleaseEvent);
	    }
	    else if(gesture.isMouseExitGesture())
    	{
    		moveMouse = !moveMouse;
    		threadSleep(50);
    	}
	    else if(gesture.isScrollModeGesture()) //Blocking functions must block and unblock the dispatcher
	    {
	    	dispatcherBlocked = true;
	        mouse.mouseScroll();
	        dispatcherBlocked = false;
	    }
        else if(gesture.isSpacebarGesture())
    	{
        	dispatcherBlocked = true;
    		keyboard.doSpacebar();
	        dispatcherBlocked = false;
    	}
    	else if(gesture.upsideDown()) 
	    {
    		dispatcherBlocked = true;
	        keyboard.doMacLaunchpad(); 
	        dispatcherBlocked = false;
	    }  
    	else if(gesture.isLoadNextGesture()) 
	    {
	     	dispatcherBlocked = true;
	        keyboard.doLoadNext();
	        dispatcherBlocked = false;
	    } 
    	else if (gesture.isLoadPreviousGesture()) 
	    {
	        dispatcherBlocked = true;
	        keyboard.doLoadPrevious();
	        dispatcherBlocked = false;
	    }
	    else  
	    {
	        dispatcherBlocked = false;
	    }	
    	
    	threadSleep(4);
    	
    }
    
    public static xGloveSensor getSensor() 
    { 
        return sensor; 
    }
    
    
    /**Thread jobs**/
    
    private class dispatcherEvent implements Runnable 
    {
    	public dispatcherEvent() {}
    	@Override 
    	public void run() 
        {
    		dispatchEvents();
    	}
    }
    
    //A Thread job for moving the mouse
    private class mouseMoveEvent implements Runnable 
    {  
        public mouseMoveEvent() {}

        @Override
        public void run() 
        {
            mouse.moveMouse();
        }
    }
    
    //A Thread job for clicking the mouse
    private class mouseClickEvent implements Runnable 
    {
        public mouseClickEvent() {}

        @Override
        public void run() 
        {
            mouse.doMouseLeftClick();
        }
    }

    //Thread job for releasing left mouse click
    private class mouseClickReleaseEvent implements Runnable 
    {
        public mouseClickReleaseEvent() {}

        @Override
        public void run() 
        {
            mouse.doMouseLeftClickRelease();
        }
    }

    //Reset maxima minima values for dispatcher
    public void reset(float orientationRoll, float orientationPitch, float orientationHeading, 
    					int[] minima, int[] maxima) 
    {
    	sensor.updateOrientation(orientationRoll, orientationPitch, orientationHeading);
    	mouse.resetMouse(minima, maxima);
    }
    
    public void killExecutor() 
    {
        threadPool.shutdown();
        //Wait for thread pool to shut down
        while (!threadPool.isTerminated()) {}
    }
    
    //Delay method
    public static void threadSleep(int millis) 
    {
    	try 
        {
    		Thread.sleep(millis);
    	} 
        catch(InterruptedException e) 
        {
    		System.out.println("Thread interrupted.");
    	}
    }
}

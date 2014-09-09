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
	//Used in debug logs
	private final String TAG = "xGloveDispatcher";
	
	private volatile boolean moveMouse = true;
	
    //Finger and sensor values. TODO: Investigate need to use Atomic for concurrency
    public static xGloveSensor sensor;
    private xGloveMouse        mouse;
    private xGloveKeyboard     keyboard;
    private xGloveGesture      gesture;
    
    //Assorted jobs and events for the threads to do
    private mouseMoveEvent         mouseMoveEvent;
    private dispatcherEvent        dispatcherEvent;
    
    //Thread Pool that takes care of the events 
    private ExecutorService threadPool;
    private ExecutorService dispatcherThread;
    private ExecutorService mouseThread;
    
    //Boolean for if the dispatcher is on a blocking action, don't fire events.
    private volatile boolean dispatcherBlocked;
    
    //Counts number of executions queued up
    private volatile int numExecutes;
    private volatile int numMouseExecutes;
    
    //Public methods
    public xGloveDispatcher() 
    {
    	//Dispatcher thread that waits for events
    	dispatcherThread = Executors.newFixedThreadPool(1);
    	dispatcherBlocked = false;
    	
    	numExecutes = 0;
    	numMouseExecutes = 0;
    	
    	//Mouse thread
    	mouseThread = Executors.newFixedThreadPool(1);
    	
        //Tweak the number of threads here
        threadPool             =      Executors.newFixedThreadPool(2);
        sensor                 =      new xGloveSensor();
        gesture                =      new xGloveGesture();
        mouse                  =      new xGloveMouse(); //Mouse must be constructed after sensor
        keyboard               =      new xGloveKeyboard();
        
        mouseMoveEvent         =      new mouseMoveEvent();
        dispatcherEvent        =      new dispatcherEvent();
    };

    //Called continuously to update sensor values
    public void updateSensorValues(float orientationRoll, float orientationPitch, float orientationHeading, 
                                    int thumbVal, int indexVal, int middleVal, int ringVal, int pinkyVal) 
    {
    	if(Debug.MAIN_DEBUG) System.out.println(TAG + ": Updating Sensors");

        sensor.updateOrientation(orientationRoll, orientationPitch, orientationHeading);
        sensor.updateFlexValues(thumbVal, indexVal, middleVal, ringVal, pinkyVal);
        
        //Allow up to 3 executes to queue up
        if(numExecutes < 3 && !dispatcherBlocked) 
        {
        	//Increments number of executions queued for dispatcher
        	numExecutes++;
        	dispatcherThread.execute(dispatcherEvent);
        }
        
        if(moveMouse && !dispatcherBlocked && numMouseExecutes < 3 && gesture.isStable()) 
        {
        	numMouseExecutes++;
        	/* Move mouse continuously when dispatcher is not blocked*/
        	mouseThread.execute(mouseMoveEvent);
        }
    }
    
    //Based on sensor values, do events
    public void dispatchEvents() 
    {
    	if(Debug.MAIN_DEBUG) System.out.println("Dispatching Events");
    	
    	/* Gestures */
    	if(gesture.isDragMouseGesture(mouse.isCurrentlyClicked()))      // drag when thumb and index finger are stretched, and middle finger,										
        {								      // ring finger and pinky are bent 
    		if(Debug.DEBUG_MOUSE) System.out.println("Drag");
    		mouse.doDragMouse();
        }
    	else if(gesture.isMouseClickGesture(mouse.isCurrentlyClicked()))
      	{
    		if(Debug.DEBUG_MOUSE) System.out.println("Click");
    		mouse.doMouseLeftClick();
      	}
    	else if(gesture.isMouseReleaseGesture(mouse.isCurrentlyClicked()))
	{ 
    		if(Debug.DEBUG_MOUSE) System.out.println("Release");
    		mouse.doMouseLeftClickRelease();
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
        	//Decrement number of executions queued for dispatcher
    		numExecutes--;
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
        	//Decrement number of executions queued for mouse
        	numMouseExecutes--;
            mouse.moveMouse();
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

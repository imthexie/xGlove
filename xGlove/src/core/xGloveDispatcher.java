package core;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dongle.DongleController;

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
    private DongleController   dongleController;
    //Assorted jobs and events for the threads to do
    private MouseMoveEvent         mouseMoveEvent;
    private DispatcherEvent        dispatcherEvent;
    
    //Thread Pool that takes care of the events 
    private ExecutorService threadPool;
    private ExecutorService dispatcherThread;
    private ExecutorService mouseThread;
    
    //Boolean for if the dispatcher is on a blocking action, don't fire events.
    private volatile boolean dispatcherBlocked;
    
    //Counts number of executions queued up
    private volatile int numExecutes;
    private volatile int numMouseExecutes;
    
    //Array of jobs to test and execute on each turn
    private ArrayList<Job> jobArray;
    
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
        dongleController	   =      new DongleController();
        
        mouseMoveEvent         =      new MouseMoveEvent();
        dispatcherEvent        =      new DispatcherEvent();
        
        jobArray = new ArrayList<Job>();
        initJobs();
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
    	
    	for(Job job : jobArray) 
    	{
    		if(job.checkGesture())
    		{
    			job.execute();
    			if(job.isConcurrent) Thread.yield();
    			break;
    		}
    	}
    	Job job = jobArray.get(0);
    	if(job.checkGesture())
    		{
    			job.execute();
    			if(job.isConcurrent) Thread.yield();
    		}
    }
    
    public static xGloveSensor getSensor() 
    { 
        return sensor; 
    }
    
    private void initJobs() 
    {
    	//Mouse drag
    	jobArray.add(new Job(false, false, false, 
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
						mouse.doDragMouse();
					}
    			}, 
    			new Callable<Boolean>() 
    			{
					@Override
					public Boolean call() throws Exception {
						return gesture.isDragMouseGesture(mouse.isCurrentlyClicked());
					}
    			}));
    	
    	//Mouse click
    	jobArray.add(new Job(false, false, false,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
    					if(Debug.DEBUG_SENSORS) System.out.println("============Mouse Click=========== currentlyClicked: " + mouse.isCurrentlyClicked());
						mouse.doMouseLeftClick();
					}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isMouseClickGesture(mouse.isCurrentlyClicked());
					}
    			}));
    	
    	//Mouse click release
    	jobArray.add(new Job(false, false, false,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
    					if(Debug.DEBUG_SENSORS) System.out.println("============Mouse Release=========== currentlyClicked: " + mouse.isCurrentlyClicked());
    					
						mouse.doMouseLeftClickRelease();
					}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isMouseReleaseGesture(mouse.isCurrentlyClicked());
					}
    			}));
    	
    	//Mouse exit
    	jobArray.add(new Job(false, false, false,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
						moveMouse = !moveMouse;
					}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isMouseExitGesture();
					}
    			}));
    	
    	//Toggle Dongle
    	jobArray.add(new Job(true, true, false,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
						dongleController.toggle();
					}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isToggleDongleGesture();
					}
    			}));
    	
    	/*Blocking jobs*/
    	
    	//Scroll Mode
    	jobArray.add(new Job(false, false, true,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
						mouse.mouseScroll();
					}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isScrollModeGesture();
					}
    			}));
    	
    	//Spacebar
    	jobArray.add(new Job(false, false, true,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
						keyboard.doSpacebar();
					}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isSpacebarGesture();
					}
    			}));
    	
    	//Upside down
    	jobArray.add(new Job(false, false, true,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
    					keyboard.doMacLaunchpad();
    				}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.upsideDown();
					}
    			}));
    	
    	//Load next
    	jobArray.add(new Job(false, false, true,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
    					keyboard.doLoadNext();
    				}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isLoadNextGesture();
					}
    			}));
    	
    	//Load Previous
    	jobArray.add(new Job(false, false, true,
    			new Runnable() {
    				@Override 
    				public void run() 
    				{
    					keyboard.doLoadPrevious();
    				}
    			}, 
    			new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception 
					{
						return gesture.isLoadPreviousGesture();
					}
    			}));
    }
    
    //When adding to the job array, make sure each specification in the constructor is correct for the correct behavior.
    private class Job 
    {
    	private boolean isConcurrent, isLinear, isBlocking;
    	private Runnable event;
    	private LinearRunnableWrapper linearEventWrapper;
    	private Callable<Boolean> gestureCheck;
    	private volatile boolean isExecuting;
    	
    	public Job(boolean isConcurrent, boolean isLinear, boolean isBlocking, Runnable event, Callable<Boolean> gestureCheck) 
    	{
    		this.isConcurrent = isConcurrent;
    		this.isLinear = isLinear;
    		this.isBlocking = isBlocking;
    		
    		//The event to fire upon checking for the right gesture
    		this.event = event;
    		//If a concurrent event should be fired one at a time, wrap it in this class
    		if(isLinear) 
    		{
    			linearEventWrapper = new LinearRunnableWrapper(event);
    		}
    		
    		//Function that checks for gesture
    		this.gestureCheck  = gestureCheck;
    		isExecuting = false;
    	}
    	
    	public boolean checkGesture() 
    	{
    		if(isLinear && isExecuting) return false;
    		try {
    			boolean returnValue = gestureCheck.call().booleanValue();
    			if(returnValue && isLinear) isExecuting = true;
    			if(returnValue && isBlocking) dispatcherBlocked = true;
    			return returnValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
    	}
    	
    	public void execute() 
    	{
    		if(isLinear && isConcurrent) 
    		{
    			threadPool.execute(linearEventWrapper); //wrapper flips on/off isExecuting when executed
    		}
    		else if(isConcurrent) 
			{
    			threadPool.execute(event);
			} 
    		else 
    		{
    			event.run();
    			if(isLinear) isExecuting = false; //this makes it ok for you to specify that non-concurrent functions are linear, though not necessary
    			if(isBlocking) dispatcherBlocked = false;
    		}
    	}
    	
    	private class LinearRunnableWrapper implements Runnable
    	{
    		Runnable runnable;
    		
    		public LinearRunnableWrapper(Runnable runnable) 
    		{
    			this.runnable = runnable;
    		}

			@Override
			public void run() {
				runnable.run();
				isExecuting = false;
			}
    	}
    }
    
    /**Thread jobs**/
    
    private class DispatcherEvent implements Runnable 
    {
    	public DispatcherEvent() {}
    	@Override 
    	public void run() 
        {
        	//Decrement number of executions queued for dispatcher
    		numExecutes--;
    		if(!dispatcherBlocked) dispatchEvents();
    	}
    }
    
    //A Thread job for moving the mouse
    private class MouseMoveEvent implements Runnable 
    {  
        public MouseMoveEvent() {}

        @Override
        public void run() 
        {
        	//Decrement number of executions queued for mouse
        	numMouseExecutes--;
            if(moveMouse && !dispatcherBlocked) mouse.moveMouse();
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

import java.awt.AWTException;
import java.awt.Dimension;
import processing.core.*;
import processing.serial.*;
	
public class xGloveController extends PApplet{
	/* WARNING: This sketch takes over the mouse
	 Press escape to close running sketch */
	
	public static void main(String args[]) {
	    PApplet.main(new String[] {"xGloveController" });
	}
	
	Serial myPort;   // Create object from Serial class
	                             
	public static final short LF = 10;        // ASCII linefeed
	public static final short portIndex = 0;  // select the com port, 
	                                          // 0 is the first port
	xGloveDispatcher dispatcher;
	DispatcherThread dispatcherThread;

	//Set this to false to not log message receipts
	boolean DEBUG = true;

	public void setup() {
	  println(Serial.list());
	  println(" Connecting to -> " + Serial.list()[portIndex]);
	  myPort = new Serial(this,Serial.list()[portIndex], 115200);
	  dispatcher = new xGloveDispatcher(); 
	  dispatcherThread = new DispatcherThread(dispatcher, 0);
	  dispatcherThread.start();
	}

	public void draw() {}

	public void serialEvent(Serial p) {
	  String message = myPort.readStringUntil(LF); // read serial data
	  if(message != null)
	  {
	    if(DEBUG) println("Raw: " + message);
	    String[] data  = message.split(","); // Split the comma-separated message

	    try {
	      if("RESET".equals(data[0])) {
	    	  float orientationPitch = Float.parseFloat(data[1].trim());  
	    	  float orientationHeading = Float.parseFloat(data[2].trim()); 
	    	  float orientationRoll = Float.parseFloat(data[3].trim());
	    	  
	    	  int range = Integer.parseInt(data[4].trim());               // output range of X or Y movement
	    	  int threshold = Integer.parseInt(data[5].trim());         // resting threshold  originally -> /10
	    	  int center = Integer.parseInt(data[6].trim());          // resting position value
	    	  int minima[] = {0, -40};         // actual analogRead minima for {x, y}
	    	  int maxima[] = {0, 40};          // actual analogRead maxima for {x, y}
	    	  minima[0] = Integer.parseInt(data[7].trim());
	    	  minima[1] = Integer.parseInt(data[8].trim());
	    	  maxima[0] = Integer.parseInt(data[9].trim());
	    	  maxima[1] = Integer.parseInt(data[10].trim());
	    	  
	    	  dispatcher.reset(orientationPitch, orientationHeading, orientationRoll, 
	    			  			range, threshold, center, 
	    			  			minima, maxima);	
	    	  return;
	      } else if(!"v1".equals(data[0])) {
	        throw new Exception("Data header was not recognized");
	      }

	      float orientationPitch = Float.parseFloat(data[1].trim());  
    	  float orientationHeading = Float.parseFloat(data[2].trim()); 
    	  float orientationRoll = Float.parseFloat(data[3].trim());
	      
	      int index = Integer.parseInt(data[4].trim());
	      int middle = Integer.parseInt(data[5].trim());
	      int ring = Integer.parseInt(data[6].trim());
	      int pinky = Integer.parseInt(data[7].trim());
	            
	      dispatcher.updateSensorValues(orientationPitch, orientationHeading, orientationRoll, index, middle, ring, pinky);
	      
	      if(DEBUG) {
	        println("Received : Pitch:" + orientationPitch + ", Heading: " + orientationHeading + ", Roll: " + orientationRoll);
	        println("FLex sensors: " + index + ", " + middle + ", " + ring + ", " + pinky);
	      }
	    }
	    catch (Throwable t) {
	      println("Parse Error :" + message); // parse error
	    }      
	  }
	}

	class DispatcherThread extends Thread {
	 
	  volatile boolean running;           // Is the thread running?  Yes or no?
	  int wait;                  // How many milliseconds should we wait in between executions?
	  xGloveDispatcher dispatcher;
	  // Constructor, create the thread
	  // It is not running by default
	  DispatcherThread(xGloveDispatcher dispatcher, int waitTime) {
	    wait = waitTime;
	    running = false;
	    this.dispatcher = dispatcher;
	  }
	 
	  // Overriding "start()"
	  public void start() {
	    // Set running equal to true
	    running = true;
	    super.start();
	  }
	 
	 
	  // We must implement run, this gets triggered by start()
	  public void run() {
	    while (running) {
	      dispatcher.dispatchEvents();
	      try {
	    	  Thread.sleep(wait);
	      } catch(InterruptedException e) {
	    	  e.printStackTrace();
	      }
	    }
	  }
	 
	 
	  //Quits the thread
	  public void quit() {
	    System.out.println("Quitting."); 
	    running = false;  // Setting running to false ends the loop in run()
	    dispatcher.killExecutor();
	    interrupt();
	  }
	}
}
/* WARNING: This sketch takes over the mouse
 Press escape to close running sketch */

import java.awt.AWTException;
import java.awt.Dimension;
import processing.serial.*;

Serial    myPort;   // Create object from Serial class
                             
public static final short LF = 10;        // ASCII linefeed
public static final short portIndex = 0;  // select the com port, 
                                          // 0 is the first port
xGloveDispatcher dispatcher;
DispatcherThread dispatcherThread;

//Set this to false to not log message receipts
boolean DEBUG = true;

void setup() {
  println(Serial.list());
  println(" Connecting to -> " + Serial.list()[portIndex]);
  myPort = new Serial(this,Serial.list()[portIndex], 115200);
  dispatcher = new xGloveDispatcher(); 
  dispatcherThread = new dispatcherThread(dispatcher, 0);
  dispatcherThread.start();
}

void draw() {}

void serialEvent(Serial p) {
  String message = myPort.readStringUntil(LF); // read serial data
  if(message != null)
  {
    if(DEBUG) println("Raw: " + message);
    String[] data  = message.split(","); // Split the comma-separated message

    try {
      if("RESET".equals(data[0])) {
        dispatcher.reset();
      } else if(!"v1".equals(data[0])) {
        throw new Exception("Data header was not recognized");
      }

      float orientationPitch = Float.parseFloat(data[1].trim());  
      float orientationHeading = Float.parseFloat(data[2].trim()); 
      float orientationRoll = Float.parseFloat(data[3].trim());
      
      int thumb  =  Integer.parseInt(data[4].trim());
      int index  =  Integer.parseInt(data[5].trim());
      int middle =  Integer.parseInt(data[6].trim());
      int ring   =  Integer.parseInt(data[7].trim());
      int pinky  =  Integer.parseInt(data[8].trim());
            
      dispatcher.updateSensorValues(orientationPitch, orientationHeading, orientationRoll, thumb, index, middle, ring, pinky);
      
      if(DEBUG) {
        println("Received : Pitch:" + orientationPitch + ", Heading: " + orientationHeading + ", Roll: " + orientationRoll);
        println("FLex sensors: " + "thumb" + "," + "index" + ", " + middle + ", " + ring + ", " + pinky);
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
  void start () {
    // Set running equal to true
    running = true;
    super.start();
  }
 
 
  // We must implement run, this gets triggered by start()
  void run () {
    while (running) {
      dispatcher.dispatchEvents();
      Thread.sleep(wait);
    }
  }
 
 
  //Quits the thread
  void quit() {
    System.out.println("Quitting."); 
    running = false;  // Setting running to false ends the loop in run()
    dispatcher.killExecutor();
    interrupt();
  }
}

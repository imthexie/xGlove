import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;

import processing.core.*;
import processing.serial.*;

	
public class xGloveController extends PApplet implements KeyListener
{
	//Used in debug logs
	private final String TAG = "xGloveController";
	
	//Used for tracking versions of this class. Eclipse complains about not declaring this for some reason.
	private static final long serialVersionUID = 1L;
	
	public static void main(String args[]) 
	{
	    PApplet.main(new String[] {"xGloveController" });
	}
	
	Serial myPort = null;   // Create object from Serial class
	                             
	public static final short LF = 10;        // ASCII linefeed
	public static int portIndex = 0;  		  // select the com port, 
	                                          // 0 is the first port
	xGloveDispatcher dispatcher;

	//Set this to false to not log message receipts
	public static boolean DEBUG = false;
	
	//Is connected to the right port
	private volatile boolean isConnected;
	
	//Latest time data sent
	private long timeOfLatestData;
	
	//Thread to check for timeout
	PortTimeoutThread portTimeoutThread;
	
	public void setup() 
	{	
		String[] serialPorts = Serial.list();
		//Close port when shutting down 
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
				System.out.println("Clean up Resources."); 
		    	if(myPort != null) {
		    		myPort.clear();
		    		myPort.write('N'); //Reset bluetooth to send resets
					myPort.stop(); 	
		    	}
		    	if(portTimeoutThread != null) portTimeoutThread.quit();
		    }

		});
		
		//Query user for COM port. This will be set using a UI setup process later using name of port.
		println(serialPorts);
		
		Scanner reader = new Scanner(System.in);
		System.out.print("Enter the index of the COM port : ");
		portIndex = reader.nextInt();
		reader.close();
		
		isConnected = false;
		dispatcher = new xGloveDispatcher(); 
		
		portTimeoutThread = new PortTimeoutThread(2000);
		portTimeoutThread.start();
	}
	
	public void connectToPort() {
		//Keep trying to connect to a port
		for(int i = 0; i < 150; i++) 
		{
			String[] serialPorts = Serial.list();
			if(DEBUG)
			{
				println(serialPorts);
				println(" Connecting to -> " + serialPorts[portIndex]);
			}
			try 
			{
				//Close port if opened before
				if(myPort != null) 
				{
					myPort.clear();
					myPort.stop(); 	
					myPort.dispose();
				}
				myPort = new Serial(this, Serial.list()[portIndex], 115200);
				myPort.clear();
				myPort.write('N'); // request reset
				break;
		 	} 
		 	catch(Exception e) 
		 	{
		 		myPort = null;
		 		if(i == 149) {
		 			System.out.println("Could not connect to to the port. Please reset the bluetooth connection.");
		 			System.exit(1);
		 		}
		 		//Wait and try again
		 		delay(100);
		 	}
		}
	}

	public void draw() 
	{
		if(DEBUG)
		{
			println("is Connected: " + isConnected);
		}
		if(!isConnected) 
		{
			try 
			{
				if(myPort != null) myPort.write('N'); //request reset
			} 
			catch (Exception e) {
				myPort = null;
			}
		}
	}

	public void serialEvent(Serial p) 
	{
	  String message = myPort.readStringUntil(LF); // read serial data
	  if(message != null)
	  {
	    if(DEBUG) println(TAG + "Raw: " + message);
	    String[] data  = message.split(","); // Split the comma-separated message

	    try 
	    {
	      if("RESET".equals(data[0])) 
	      {
	    	  float orientationRoll = Float.parseFloat(data[1].trim());  
	    	  float orientationPitch = Float.parseFloat(data[2].trim()); 
	    	  float orientationHeading = Float.parseFloat(data[3].trim());
	    	  
	    	  int minima[] = {0, -40};         // actual analogRead minima for {x, y}
	    	  int maxima[] = {0, 40};          // actual analogRead maxima for {x, y}
	    	  minima[0] = Integer.parseInt(data[4].trim());
	    	  minima[1] = Integer.parseInt(data[5].trim());
	    	  maxima[0] = Integer.parseInt(data[6].trim());
	    	  maxima[1] = Integer.parseInt(data[7].trim());
	    	  
	    	  dispatcher.reset(orientationRoll, orientationPitch, orientationHeading, 
	    			  			minima, maxima);
	    	  myPort.clear();
	    	  myPort.write('Y');
	    	  isConnected = true;
	    	  timeOfLatestData = System.currentTimeMillis();
	    	  return;
	      } 
	      else if(!"v1".equals(data[0])) 
	      {
	    	  myPort.clear();
	    	  myPort.write('N'); //request reset
	    	  timeOfLatestData = System.currentTimeMillis();
	    	  if(DEBUG)
	    	  {
	    	  throw new Exception(TAG + "Data header was not recognized");
	    	  }
	      }
	      
	      float orientationRoll = Float.parseFloat(data[1].trim());  
	      float orientationPitch = Float.parseFloat(data[2].trim());
	      float orientationHeading = Float.parseFloat(data[3].trim());
	      
    	  int thumb = Integer.parseInt(data[4].trim());
	      int index = Integer.parseInt(data[5].trim());
	      int middle = Integer.parseInt(data[6].trim());
	      int ring = Integer.parseInt(data[7].trim());
	      int pinky = Integer.parseInt(data[8].trim());
	            
	      dispatcher.updateSensorValues(orientationRoll, orientationPitch, orientationHeading, thumb, index, middle, ring, pinky);
    	  timeOfLatestData = System.currentTimeMillis();
	      if(DEBUG) 
	      {
	        println(TAG + " serialEvent() : Received : Pitch:" + orientationPitch + ", Heading: " + orientationHeading + ", Roll: " + orientationRoll);
	        println(TAG + " serialEvent() : Flex sensors: " + index + ", " + middle + ", " + ring + ", " + pinky);
	      }
	    }
	    catch (Throwable t) 
	    {
	      if(DEBUG)
	      {
	    	  println("Parse Error : " + message);  // parse error
	    	  println(t.getMessage()); 				// Print error message
	      }
	    }      
	  }
	}
	
	class PortTimeoutThread extends Thread {	
		volatile boolean running;           // Is the thread running?  Yes or no?
		int wait;                  // How many milliseconds should we wait in between executions?
		// Constructor, create the thread
		PortTimeoutThread(int waitTime) {
			wait = waitTime;
			running = false;
		}
		// Overriding "start()"
		public void start() {
		// Set running equal to true
			running = true;
		    super.start();
		}
	 
		public void run() 
		{
			while (running) 
			{				
				if(!isConnected) 
				{
					connectToPort();
				} 
				else {
				
					//If 5000 milliseconds have passed since last data has been sent, reset in same port.
					long currTime = System.currentTimeMillis();
					if(DEBUG)
					{
						println(TAG + ": PortTimeoutThread : CurrTime: " + currTime + " timeOfLatestData: "+ timeOfLatestData + " diff: " + (currTime - timeOfLatestData));
					}
					if(currTime - timeOfLatestData > 3000) 
					{
						setup();
					}
				}
				try 
				{
					Thread.sleep(wait);
				} 
				catch(InterruptedException e) {}
			}
		}
			 
			 
		//Exits the thread
		public void quit() 
		{
			System.out.println("Exit - The program has been terminated."); 
			running = false;  // Setting running to false ends the loop in run()
			interrupt();
		}
	}

	//Must override these to implement KeyListener
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) 
	{ 
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0); 
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
}

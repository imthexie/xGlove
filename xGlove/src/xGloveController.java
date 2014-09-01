import processing.core.*;
import processing.serial.*;
	
public class xGloveController extends PApplet
{
	//Used in debug logs
	private final String TAG = "xGloveController";
	
	//Used for tracking versions of this class. Eclipse complains about not declaring this for some reason.
	private static final long serialVersionUID = 1L;
	
	public static void main(String args[]) 
	{
	    PApplet.main(new String[] {"xGloveController" });
	}
	
	Serial myPort;   // Create object from Serial class
	                             
	public static final short LF = 10;        // ASCII linefeed
	public static final short portIndex = 0;  // select the com port, 
	                                          // 0 is the first port
	xGloveDispatcher dispatcher;

	//Set this to false to not log message receipts
	public static boolean DEBUG = true;

	public void setup() 
	{
	  println(Serial.list());
	  println(" Connecting to -> " + Serial.list()[portIndex]);
	  
	  while(true) 
	  {
		try 
		{
			myPort = new Serial(this,Serial.list()[portIndex], 115200);
			break;
	 	} 
	 	catch(Exception e) 
	 	{
	 		//Wait and try again
	 		delay(1000);
	 	}
	  }
	  dispatcher = new xGloveDispatcher(); 
	}

	public void draw() {}
	
	public void keyPressed() 
	{
		if(key == ESC) 
		{
			exit();
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
	    	  return;
	      } 
	      else if(!"v1".equals(data[0])) 
	      {
	        throw new Exception(TAG + "Data header was not recognized");
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
	      
	      if(DEBUG) 
	      {
	        println(TAG + " serialEvent() : Received : Pitch:" + orientationPitch + ", Heading: " + orientationHeading + ", Roll: " + orientationRoll);
	        println(TAG + " serialEvent() : Flex sensors: " + index + ", " + middle + ", " + ring + ", " + pinky);
	      }
	    }
	    catch (Throwable t) 
	    {
	      println("Parse Error : " + message); // parse error
	      println(t.getMessage()); //Print error message
	    }      
	  }
	}
}

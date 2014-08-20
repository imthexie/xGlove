/* WARNING: This sketch takes over your mouse
 Press escape to close running sketch */

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import processing.serial.*;

Serial    myPort;   // Create object from Serial class
arduMouse myMouse;  // create arduino controlled mouse  
                             
public static final short LF = 10;        // ASCII linefeed
public static final short portIndex = 0;  // select the com port, 
                                          // 0 is the first port

int posX, posY, btn; // data from msg fields will be stored here   
void setup() {
  println(Serial.list());
  println(" Connecting to -> " + Serial.list()[portIndex]);
  myPort = new Serial(this,Serial.list()[portIndex], 115200);
  myMouse = new arduMouse(); 
  myMouse.move(0,0);
  btn = 0; // turn mouse off until requested by Arduino message
}

void draw() {
   if ( btn != 0)    {    
      myMouse.move(posX, posY); // move mouse by received x and y offsets
      btn = 0;
   }
   
   delay(50);
      
}
void serialEvent(Serial p) {
  String message = myPort.readStringUntil(LF); // read serial data
  if(message != null)
  {
    println(message);
    String [] data  = message.split(","); // Split the comma-separated message
    
    if (data[0].equals("Data"))// check for data header    
    {
      if(data.length > 3)
      {
        try {
          posX = Integer.parseInt(data[1].trim());  
          posY = Integer.parseInt(data[2].trim()); 
          btn  = Integer.parseInt(data[3].trim());
          
          println("Received: X: " + posX + " Y: " + posY + " btn: " + btn);
        }
        catch (Throwable t) {
          println("Parse Error :" + message); // parse error
        }      
      }
    }
  }
}

class arduMouse {
  Robot mouseRobot;     // create object from Robot class;
  static final short rate = 4; // multiplier to adjust movement rate
  int x, y;
  arduMouse() {
    try {
      mouseRobot = new Robot();
    }
    catch (AWTException e) {
      e.printStackTrace();
    }
    Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    
    //Put mouse in middle of screen
    y =  (int)screen.getHeight() / 2 ;
    x =  (int)screen.getWidth() / 2;
  }
  // method to move mouse from center of screen by given offset
  void move(int offsetX, int offsetY) {
    x += (rate * offsetX);
    y += (rate * offsetY);
    mouseRobot.mouseMove(x, y);
  }
  
  //Move mouse to a specified location
  void moveTo(int xPos, int yPos) {
    mouseRobot.mouseMove(xPos, yPos);
  }
}

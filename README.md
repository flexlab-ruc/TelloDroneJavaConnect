# TelloDroneJavaConnect

## :warning: queue system is not fully tested yet!
At the moment the queue is working but things like

**August 2019: Added support for front camera on drone**

You must have [FFMPEG](https://ffmpeg.org) installed on your computer and added it your path
test your installation with this command in the terminal: "ffmpeg -version" 

images are saved as jpg-files in a folder named grabbedImages  

**September 2019: minor changes Queue is working 

A brief example of how to program a Tello drone with Java.
Tello drones accept commands through UDP and responds with a short text message

You can find further information about the drone here: [https://www.ryzerobotics.com/tello/downloads] including the full SDK describing all commands accepted by the drone

## Code Example
This example makes it very easy to commmunicate with the drone - see the code below:
```java
    // Create Drone instance
    TelloDrone drone = new TelloDrone();

    // write commands to console
    drone.setLogToConsole(true);

    // Sending commands to drone
    drone.connect();

    drone.streamOn();

    drone.takeoff();
    drone.rotateClockwise(360);
    drone.land();

```
Look in the [documentation](#documentation) for more commands

## Using the command queue
You can add a queue of commands you want executed in a separate thread. This might be handy if you need to fly the drone to a specific place while you do something else in your program
```java
    // create a queue of commands and execute them

    drone.addToCommandQueue("sdk?");
    drone.addToCommandQueue("sn?");
    drone.addToCommandQueue("takeoff");
    drone.addToCommandQueue("ccw 180");
    drone.addToCommandQueue("cw 180");
    drone.addToCommandQueue("land");


    //disable logging drone and use the eventlistener below instead
    drone.setLogToConsole(false);

    // add eventlistener to the "drone command queue"
    drone.addCommandQueueEventListener(new TelloDrone.DroneCommandEventListener() {
        @Override
        public void commandExecuted(TelloDrone.Command command)
        {
            System.out.println("Command Executed:");
            System.out.println(command);

        }

        @Override
        public void commandFinished(TelloDrone.Command command)
        {
            System.out.println("Command Finished:");
            System.out.println(command);
        }

        @Override
        public void commandAdded(TelloDrone.Command command)
        {
            System.out.println(command.getCommand() + " added to queue");

        }

        @Override
        public void commandQueueFinished() {
            System.out.println("Done. No more commands in queue");
        }
    });

    drone.startCommandQueue();
```


## How to run this project
This project has no other requirements than an actual Tello Drone. Just folow these steps and you should be up and running in a matter of minutes
* Download this project and open it in intelliJ or another java IDE
* Turn on the Tello drone
* Connect your computer to the Tello Wifi
* Run the code.... 

## getting video stream from the Tello drone
You can grab the stream if you have ffmpeg installed and has send the 'streamon' command to the drone
```
ffplay -probesize 32 -i udp://@:11111 -framerate 30
```

# Documentation <a name="documentation"></a>

## `public class TelloDrone`

 * **Author:** Ebbe Vang
 * **Version:** 0.1

     This class connects to a Tello drone using a UDP connection and sends commands to the drone

     Supports all commands in sdk1.

## `public boolean connect()`

connects to the drone using the 'command' comamnd and allows you to send further commands

 * **Returns:** whether the drone is connected or not

## `private boolean sendMessage(String command)`

Send any command to the drone - see the sdk for more details about the commands you can send to the drone

 * **Parameters:** `command` — the command you want to send to the drone
 * **Returns:** whether the command is accepted or not

## `public boolean takeoff()`

The drone takes off - The drone will rise about 1 meter

 * **Returns:** whether the command is accepted or not

## `public boolean land()`

The drone lands

 * **Returns:** whether the command is accepted or not

## `public boolean goDown(int cm)`

move drone down

 * **Parameters:** `cm` — distance in cm
 * **Returns:** whether the command is accepted or not

## `public boolean goUp(int cm)`

move drone up

 * **Parameters:** `cm` — distance in cm
 * **Returns:** whether the command is accepted or not

## `public boolean goLeft(int cm)`

move drone left

 * **Parameters:** `cm` — distance in cm
 * **Returns:** whether the command is accepted or not

## `public boolean goRight(int cm)`

move drone right

 * **Parameters:** `cm` — distance in cm
 * **Returns:** whether the command is accepted or not

## `public boolean goForward(int cm)`

move drone forward

 * **Parameters:** `cm` — distance in cm
 * **Returns:** whether the command is accepted or not

## `public boolean goBackwards(int cm)`

move drone backwards

 * **Parameters:** `cm` — distance in cm
 * **Returns:** whether the command is accepted or not

## `public boolean streamOn()`

start video stream from front camera

 * **Returns:** whether the command is accepted or not

## `public boolean streamOff()`

Stop streaming from front camera

 * **Returns:** 

## `public boolean rotateClockwise(int degrees)`

The Drone will turn clockwise

 * **Parameters:** `degrees` — you want the drone to turn - must be between 0 and 360
 * **Returns:** whether the command is accepted or not

## `public boolean rotateCounterClockwise(int degrees)`

The Drone will turn counter clockwise

 * **Parameters:** `degrees` — you want the drone to turn - must be between 0 and 360
 * **Returns:** whether the command is accepted or not

## `public boolean isLogToConsole()`

Log status.

 * **Returns:** If the drone is writing to the console or not

## `public boolean emergency()`

Stops motors immediately!*

 * **Returns:** 

## `public void setLogToConsole(boolean logToConsole)`

activate or deactivate log to console

 * **Parameters:** `logToConsole` — Log to console

## `public int grabImage()`

grab image from front camera. image will be saved in grabbedImages folder

 * **Returns:** image number - which is used as a filename

## `public String getGrabbedImageURL()`

get URL for latest image taken by grabbedImage method

 * **Returns:** image url as string

## `public boolean setSpeed(int speed)`

Set drone speed

 * **Parameters:** `speed` — in cm/s (1-100)
 * **Returns:** whether the speed is set or not

## `public double getSpeed()`

g get the speed of the drone i cm/s

 * **Returns:** speed in cm/s (1-100)

## `public int getBatteryPercentage()`

get the battery charge level in percentage

 * **Returns:** battery charge in percentage (1-100)

## `public String getTime()`

get time droen has been in the air

 * **Returns:** time flying

## `public void addToCommandQueue(String command)`

Add to queue of commands

 * **Parameters:** `command` — 

## `public void startCommandQueue()`

start executing commands from command queue in separate thread

## `public void suspendCommandQueue()`

suspend command queue

## `public void resumeCommandQueue()`

resume command queue

## `public void addCommandQueueEventListener(DroneCommandEventListener listener)`

add eventlistener to command queue

 * **Parameters:** `listener` — 

## `public void removeCommandQueueEventListener(DroneCommandEventListener listener)`

remove eventlistener from command queue

 * **Parameters:** `listener` — 

## `public void clearCommandQueue()`

remove all commands from queue

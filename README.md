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

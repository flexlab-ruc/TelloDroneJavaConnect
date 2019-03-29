# TelloDroneJavaConnect

A brief example of how to program a Tello drone with Java.
Tello drones accept commands through UDP and responds with a short text message

You can find further information about the drone here: [https://www.ryzerobotics.com/tello/downloads] including the full SDK describing all commands accepted by the drone

## Code Example
This example makes it very easy to commmunicate with the drone - see the code below:
```
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
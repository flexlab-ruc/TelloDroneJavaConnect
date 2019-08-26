package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws UnknownHostException {

        // Create Drone instance
        TelloDrone drone = new TelloDrone();

        //drone.setLogToConsole(true);

        drone.connect();

        drone.getBatteryPercentage();

        /*
        drone.setSpeed(90);
        drone.getSpeed();

        // Sending commands to drone

        drone.streamOn();

        drone.takeoff();

        drone.rotateClockwise(180);

        drone.grabImage();
        System.out.println(drone.getGrabbedImageURL());

        drone.rotateCounterClockwise(180);

        drone.grabImage();
        System.out.println(drone.getGrabbedImageURL());

        drone.goForward(50);
        drone.goLeft(50);
        drone.goBackwards(50);
        drone.goRight(50);
        drone.goDown(50);
        drone.goUp(50);

        drone.land();
        drone.streamOff();
        */



        drone.addCommandQueueEventListener(new TelloDrone.DroneCommandEventListener() {
            @Override
            public void commandExecuted(TelloDrone.Command command) {
                System.out.println(command.getCommand());

            }

            @Override
            public void commandFinished(TelloDrone.Command command) {
                System.out.println(command.getReply());
            }

            @Override
            public void commandAdded(TelloDrone.Command command) {
                System.out.println(command.getCommand() + " added to queue");

            }

            @Override
            public void commandQueueFinished() {
                System.out.println("done");
            }
        });
    }
}
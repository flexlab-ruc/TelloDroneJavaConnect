package dk.ruc.flexlab.TelloDroneJavaInterface;

public class Main {

    public static void main(String[] args)  {

        // Create Drone instance
        TelloDrone drone = new TelloDrone();

        drone.setLogToConsole(true);

        drone.connect();

        // sending single commands to the drone:

        drone.getBatteryPercentage();

        drone.takeoff();

        drone.rotateClockwise(360);

        drone.land();

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
    }
}
package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws UnknownHostException {

        // Create Drone instance
        TelloDrone drone = new TelloDrone();

        // write commands to console
        drone.setLogToConsole(true);

        // Sending commands to drone
        drone.connect();
        drone.takeoff();
        drone.rotateClockwise(360);
        drone.land();
    }
}

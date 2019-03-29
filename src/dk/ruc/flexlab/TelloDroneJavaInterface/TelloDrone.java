package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Ebbe Vang
 * @version 0.1
 * This class connects to a Tello drone using a UDP connection and sends commands to the drone
 */
public class TelloDrone {

    private final int udpPort = 8889;
    private DatagramSocket socket;
    private InetAddress IPAddress;
    private boolean isConnected = false;
    private boolean logToConsole = true;
    private Date date = new Date();

    /**
     * connects to the drone and allows you to send further commands
     * @return whether the drone is connected or not
     */
    public boolean connect()
    {
        try {
            IPAddress = InetAddress.getByName("192.168.10.1");
            socket = new DatagramSocket(udpPort);
            sendMessage("command");
            //System.out.println(receiveMessage());
            if (receiveMessage().equals("ok\u0000\u0000\u0000"))
            {
                isConnected = true;
                log("Succesfully connected to the drone");
                return true;

            }
            log("Cannot connect to the drone");
            return false;
        } catch (Exception e) {

            return false;
        }
    }

    private boolean sendMessage(String command)
    {
        try{
            byte[] sendData = command.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udpPort);
            socket.send(sendPacket);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private String receiveMessage()
    {
        byte[] receiveData = new byte[5];
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            return "communication error";
        }
        return new String(packet.getData());
    }

    /**
     * The drone takes off - The drone will rise about 1 meter
     * @return whether the command is accepted or not
     */
    public boolean takeoff()
    {
        sendMessage("takeoff");
        if (receiveMessage().equals("ok\u0000\u0000\u0000"))
        {
            log ("Drone takeoff");
            return true;
        }
        log("Take off command failed");
        return false;
    }

    /**
     * The drone lands
     * @return whether the command is accepted or not
     */
    public boolean land()
    {
        sendMessage("land");
        if (receiveMessage().equals("ok\u0000\u0000\u0000"))
        {
            log("Drone landed");
            return true;
        }
        log("Landing command failed");
        return false;
    }

    public boolean streamOn()
    {
        sendMessage("streamon");
        if(receiveMessage().equals("ok\u0000\u0000\u0000"))
        {
            log("stream is turned on");
            return true;
        }
        log("failed to turn on stream");
        return false;
    }

    /**
     * The Drone will turn clockwise
     * @param degrees you want the drone to turn - must be between 0 and 3600
     * @return whether the command is accepted or not
     */
    public boolean rotateClockwise (int degrees)
    {
        if (degrees >= 0 && degrees <= 3600)
        {
            sendMessage("cw " + degrees);
            if(receiveMessage().equals("ok\u0000\u0000\u0000"))
            {
                log("Drone turned " + degrees + " degrees clockwise");
                return true;
            }
        }
        log("Rotation clockwise command failed");
        return false;
    }

    /**
     * Log status.
     * @return If the drone is writing to the console or not
     */
    public boolean isLogToConsole() {
        return logToConsole;
    }

    /**
     * activate or deactivate log to console
     * @param logToConsole Log to console
     */
    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }

    private void log(String message)
    {
        if (logToConsole)
        {
            System.out.print(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            System.out.print("\t");
            System.out.println(message);
        }
    }
}

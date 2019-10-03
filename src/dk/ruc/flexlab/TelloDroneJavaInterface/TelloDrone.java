/*
https://github.com/SovGVD/nodetello/blob/master/lib/nodetello.js
 https://github.com/f41ardu/TelloProcessing/blob/master/TelloSDKVideo/TelloSDKVideo.pde
 https://tellopilots.com/threads/tello-video-web-streaming.455/
 https://tellopilots.com/threads/tello-whats-possible.88/page-3
 https://github.com/SMerrony/tello
 http://gsvideo.sourceforge.net/
 https://gstreamer.freedesktop.org/download/
 https://github.com/f41ardu/TelloProcessing
 udp://0.0.0.0:6038 - 6037
 http://www.magicandlove.com/blog/2018/04/09/saving-video-from-processing-with-the-jcodec-2-3/
 */

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Ebbe Vang
 * @version 0.1
 * This class connects to a Tello drone using a UDP connection and sends commands to the drone
 * Supports all commands in sdk1.
 */

public enum Flip {
    LEFT, RIGHT, FORWARD, BACK, BACKLEFT, BACKRIGHT, FRONTLEFT, FRONTRIGHT
}

public class TelloDrone {

    // hobye hack
    private DatagramSocket socketState;
    private final int udpPortState = 8890;

    private final int udpPort = 8889;
    private DatagramSocket socket;
    private InetAddress IPAddress;
    //private boolean isConnected = false;
    private boolean logToConsole = true;
    private boolean streamOn = false;
    private int imageCounter = 0;
    private CommanderThread commander = new CommanderThread(this);
    public stateReceiveThread stateReceiver = new stateReceiveThread(this); // hobye hack


    public TelloDrone() { //mads hacket her
        //  String path = System.getProperty("user.dir");
        //  System.out.println("Working Directory = " + path);
        log("Initializing Drone");
        File folder = new File(dataPath("") + "/"+ "grabbedImages");

        File[] images = folder.listFiles();
        print(images.length);
        if (images.length >1)
        {
            Arrays.sort(images);
            imageCounter = Integer.parseInt(images[images.length-1].getName().substring(0, 4));
        }
        //System.out.println("imagecounter" + imageCounter);
        setupStateReceive(); // hobye hack
    }

    /**
     * connects to the drone using the 'command' comamnd and allows you to send further commands
     *
     * @return whether the drone is connected or not
     */
    public boolean connect() {
        try {
            log("Connecting to drone");
            IPAddress = InetAddress.getByName("192.168.10.1");
            socket = new DatagramSocket(udpPort);
            sendMessage("command");
            //System.out.println(receiveMessage());
            if (ok()) {
                //isConnected = true;
                log("Succesfully connected to the drone");
                return true;
            }
            log("Cannot connect to the drone");
            return false;
        }
        catch (Exception e) {

            return false;
        }
    }

    private boolean setupStateReceive() // hobye hack
    {
        try {

            IPAddress = InetAddress.getByName("192.168.10.1");
            socketState = new DatagramSocket(udpPortState);
            return true;
        }
        catch (Exception e) {

            return false;
        }
    }

    public String receiveState() { // hobye hack
        byte[] receiveData = new byte[500];
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
        String response;
        try {
            socketState.receive(packet);
            response =  new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        }
        catch (IOException e) {
            return "communication error";
        }
        return response.trim();
    }

    private boolean ok() {
        return receiveMessage().equals("ok");
    }

    /**
     * Send any command to the drone - see the sdk for more details about the commands you can send to the drone
     *
     * @param command the command you want to send to the drone
     * @return whether the command is accepted or not
     */
    private boolean sendMessage(String command) {
        try {
            byte[] sendData = command.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udpPort);
            socket.send(sendPacket);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendCommand(Command command) {
        sendMessage(command.getCommand());
        if (!command.isDirect())
        {
            if (ok()) {
                log("command \"" +  command + "\" accepted");
                return true;
            } else {
                log("command \"" + command + "\" failed");
                return false;
            }
        }
        return true;
    }

    private String receiveMessage() {
        byte[] receiveData = new byte[5];
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
        String response;
        try {
            socket.receive(packet);
            response =  new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        }
        catch (IOException e) {
            return "communication error";
        }
        return response.trim();
    }

    /**
     * The drone takes off - The drone will rise about 1 meter
     *
     * @return whether the command is accepted or not
     */
    public boolean takeoff() {
        sendMessage("takeoff");
        if (ok()) {
            log("Drone takeoff");
            return true;
        }
        log("Take off command failed");
        return false;
    }

    /**
     * The drone lands
     *
     * @return whether the command is accepted or not
     */
    public boolean land() {
        sendMessage("land");
        if (ok()) {
            log("Drone landed");
            return true;
        }
        log("Landing command failed");
        return false;
    }

    /**
     * move drone down
     *
     * @param cm distance in cm
     * @return whether the command is accepted or not
     */
    public boolean goDown(int cm) {
        return move("down", cm);
    }

    /**
     * move drone up
     *
     * @param cm distance in cm
     * @return whether the command is accepted or not
     */
    public boolean goUp(int cm) {
        return move("up", cm);
    }

    /**
     * move drone left
     *
     * @param cm distance in cm
     * @return whether the command is accepted or not
     */
    public boolean goLeft(int cm) {
        return move("left", cm);
    }

    /**
     * move drone right
     *
     * @param cm distance in cm
     * @return whether the command is accepted or not
     */
    public boolean goRight(int cm) {
        return move("right", cm);
    }

    /**
     * move drone forward
     *
     * @param cm distance in cm
     * @return whether the command is accepted or not
     */
    public boolean goForward(int cm) {
        return move("forward", cm);
    }

    /**
     * move drone backwards
     *
     * @param cm distance in cm
     * @return whether the command is accepted or not
     */
    public boolean goBackwards(int cm) {
        return move("back", cm);
    }

    public boolean move(String direction, int cm) {
        if (cm >= 20 && cm <= 500) {
            sendMessage(direction + " " + cm);
            if (ok()) {
                log("Drone moved " + direction + ": " + cm + "cm. ");
                return true;
            }
            log(direction + " command failed");
            return false;
        }
        log(direction + " command failed (only cm beteen 20 and 500 allowed)");
        return false;
    }

    /**
     * start video stream from front camera
     * @return whether the command is accepted or not
     */
    public boolean streamOn() {
        if (streamOn) {
            log("failed to set stream ON - stream is already ON!");
            return false;
        }

        sendMessage("streamon");
        if (ok()) {
            log("stream is turned on");
            streamOn = true;
            return true;
        }
        log("failed to turn on stream");
        return false;
    }

    /**
     * Stop streaming from front camera
     * @return
     */
    public boolean streamOff() {
        if (!streamOn) {
            log("failed to set stream OFF - stream is already OFF!");
            return false;
        }

        sendMessage("streamoff");
        if (ok()) {
            log("stream is turned off");
            return true;
        }
        log("failed to turn off stream");
        return false;
    }

    /**
     * The Drone will turn clockwise
     *
     * @param degrees you want the drone to turn - must be between 0 and 360
     * @return whether the command is accepted or not
     */
    public boolean rotateClockwise(int degrees) {
        if (degrees >= 1 && degrees <= 360) {
            sendMessage("ccw " + degrees);
            if (ok()) {
                log("Drone turned " + degrees + " degrees clockwise");
                return true;
            }
        }
        log("Rotation clockwise command failed");
        return false;
    }

    /**
     * The Drone will turn counter clockwise
     *
     * @param degrees you want the drone to turn - must be between 0 and 360
     * @return whether the command is accepted or not
     */
    public boolean rotateCounterClockwise(int degrees) {
        if (degrees >= 1 && degrees <= 360) {
            sendMessage("ccw " + degrees);
            if (ok()) {
                log("Drone turned " + degrees + " degrees counterclockwise");
                return true;
            }
        }
        log("Rotation clockwise command failed");
        return false;
    }

    /**
     * Log status.
     *
     * @return If the drone is writing to the console or not
     */
    public boolean isLogToConsole() {
        return logToConsole;
    }

    /**
     * Stops motors immediately!*
     *
     * @return
     */
    public boolean emergency() {
        sendMessage("streamoff");
        if (ok()) {
            log("stream is turned off");
            return true;
        }
        log("failed to turn off stream");
        return emergency();
    }

    /**
     * activate or deactivate log to console
     *
     * @param logToConsole Log to console
     */
    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }

    private void log(String message) {
        if (logToConsole) {
            System.out.print(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            System.out.print("\t");
            System.out.println(message);
        }
    }

    /**
     * grab image from front camera. image will be saved in grabbedImages folder
     *
     * @return image number - which is used as a filename
     */
    public int grabImage() {
        try {
            imageCounter++;
            String command = "ffmpeg -i udp://192.168.10.1:11111 -vframes 1 -q:v 2 -nostats -loglevel 0 grabbedImages/" + String.format("%04d", imageCounter) + ".jpg";
            log("grabbing image");
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            log("done grabbing image");
            if (new File("grabbedImages/" + String.format("%04d", imageCounter) + ".jpg").exists()) return imageCounter;
            else return -1;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            imageCounter--;
            return -1;
        }
    }

    /**
     * get URL for latest image taken by grabbedImage method
     *
     * @return image url as string
     */
    public String getGrabbedImageURL() {
        if (imageCounter == 0) return null;
        else return "grabbedImages/" + String.format("%04d", imageCounter) + ".jpg";
    }

    /**
     * Set drone speed
     *
     * @param speed in cm/s (1-100)
     * @return whether the speed is set or not
     */
    public boolean setSpeed(int speed) {
        if (speed >= 1 && speed <= 100) {
            sendMessage("speed " + speed);
            if (ok()) {
                log("Speed is set to " + speed);
                return true;
            }
            log("speed command failed");
            return false;
        }
        log(" speed command failed (only speed beteen 1 and 100 is allowed)");
        return false;
    }

    /**
     * g get the speed of the drone i cm/s
     *
     * @return speed in cm/s (1-100)
     */
    public double getSpeed() {
        sendMessage("speed?");
        String speed = receiveMessage();
        try {
            double doubleSpeed = Double.parseDouble(speed);
            return doubleSpeed;
        }
        catch (NumberFormatException e) {
            log("getSpeed command failed");
            return -1;
        }
    }

    /**
     * get the battery charge level in percentage
     *
     * @return battery charge in percentage (1-100)
     */
    public int getBatteryPercentage() {
        sendMessage("battery?");
        String battery = receiveMessage();
        try {
            int intBat = Integer.parseInt(battery);
            return intBat;
        }
        catch (NumberFormatException e) {
            log("getBatteryPercentage command failed");
            return -1;
        }
    }

    /**
     * get time droen has been in the air
     *
     * @return time flying
     */
    public String getTime() {
        sendMessage("speed?");
        return receiveMessage();
    }

    public void addToCommandQueue(String command, boolean direct) { // hobye hack
        Command c = new Command(command, direct);
        commander.addToCommandQueue(c);
    }

    /**
     * Add to queue of commands
     * the Queue is automatically started
     * @param command
     */
    public void addToCommandQueue(String command) { // hobye hack

        addToCommandQueue(command, false);
    }

    /**
     * start executing commands from command queue in separate thread
     */
    public void startCommandQueue() {
        commander.start();
    }

    //hobye hack
    public void startStateReicever() {
        stateReceiver.start();
    }

    // hobye hack
    public String getStateInfo(String state)
    {
        String[] states = stateReceiver.state.split(";");
        for (int i = 0; i < states.length; i++)
        {
            if(states[i].trim().startsWith(state + ":"))
            {
                return states[i].split(":")[1].trim();
            }
        }
        return "";
    }
    /**
     * suspend command queue
     */
    public void suspendCommandQueue() {
        commander.suspendQueue();
    }

    /**
     * resume command queue
     */
    public void resumeCommandQueue() {
        commander.resumeQueue();
    }

    /**
     * add eventlistener to command queue
     * @param listener
     */
    public void addCommandQueueEventListener(DroneCommandEventListener listener) {
        commander.addEventListener(listener);
    }

    /**
     * remove eventlistener from command queue
     * @param listener
     */
    public void removeCommandQueueEventListener(DroneCommandEventListener listener) {
        commander.removeEventListener(listener);
    }

    /**
     * remove all commands from queue
     */
    public void clearCommandQueue() {
        commander.clearQueue();
    }

    public Command[] getQueuedCommands() {
        return (Command[]) commander.commandsToExecute.toArray();
    }

    public Command[] getExecutedCommands() {
        return (Command[]) commander.executedCommands.toArray();
    }

    private class stateReceiveThread extends Thread
    {
        private final TelloDrone drone;
        public String state ="";
        public stateReceiveThread(TelloDrone drone) {
            this.drone = drone;
        }
        @Override
        public void run() {
            while (true)
            {
                state = drone.receiveState();
            }
        }
    }
    private class CommanderThread extends Thread
    {
        private final TelloDrone drone;
        ArrayList<Command> commandsToExecute = new ArrayList<Command>();
        ArrayList<Command> executedCommands = new ArrayList<Command>();
        ArrayList<DroneCommandEventListener> eventListeners = new ArrayList<DroneCommandEventListener>();
        private boolean clearQueue;
        private boolean suspend;

        public CommanderThread(TelloDrone drone) {
            this.drone = drone;
        }

        @Override
        public void run() {
            while (true)
            {
                if (commandsToExecute.size() > 0)
                {
                    for (DroneCommandEventListener listener : eventListeners)
                    {
                        listener.commandExecuted(commandsToExecute.get(0));
                    }

                    drone.sendMessage(commandsToExecute.get(0).getCommand());
                    if (!commandsToExecute.get(0).isDirect()) // hobye hack
                    {
                        commandsToExecute.get(0).setReply(receiveMessage());
                    }

                    for (DroneCommandEventListener listener : eventListeners)
                    {
                        listener.commandFinished(commandsToExecute.get(0));
                    }

                    executedCommands.add(commandsToExecute.get(0));
                    commandsToExecute.remove(0);
                }
                if (suspend) {
                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        }
                        catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                if (clearQueue) {
                    commandsToExecute.clear();
                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        }
                        catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                if (commandsToExecute.isEmpty())
                {
                    for (DroneCommandEventListener listener : eventListeners)
                    {
                        listener.commandQueueFinished();
                    }
                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        }
                        catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void addToCommandQueue(Command command)
        {
            this.commandsToExecute.add(command);

            for (DroneCommandEventListener listener : eventListeners)
            {
                listener.commandAdded(command);
            }
            // if thread is waiting the notify  :-)
            synchronized(this)
            {
                this.notify();
            }
        }


        public void resumeQueue()
        {
            this.notify();
        }

        public void suspendQueue()
        {
            suspend = true;
        }

        public void clearQueue()
        {
            clearQueue = true;
        }

        public void addEventListener(DroneCommandEventListener listener)
        {
            this.eventListeners.add(listener);
        }

        public void removeEventListener(DroneCommandEventListener listener)
        {
            this.eventListeners.remove(listener);
        }
    }
}
public class Command
{
    private String command;
    private String reply;
    private boolean direct = false;


    public Command(String command) {
        this.command = command; // hobye hack
    }

    public Command(String command, boolean direct) { // hobye hack
        this.command = command;
        this.direct = direct;
    }

    public String getCommand() {
        return command;
    }


    public boolean isDirect()
    {
        return direct;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("command: \t");
        builder.append(command);
        builder.append(System.getProperty("line.separator"));
        builder.append("reply: \t");
        builder.append(reply);
        builder.append(System.getProperty("line.separator"));
        return builder.toString();
    }
}
public static interface DroneCommandEventListener {
    void commandExecuted(Command command);

    void commandFinished(Command command);

    void commandAdded(Command command);

    void commandQueueFinished();
}

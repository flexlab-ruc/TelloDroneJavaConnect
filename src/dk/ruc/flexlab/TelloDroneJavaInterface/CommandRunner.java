package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.util.List;

/**
 * Executes a list of commands sequentially on the drone. In simulation mode the
 * commands are only displayed, which lets the menu be used without a live drone.
 */
public class CommandRunner {

    private final TelloDrone drone;
    private final boolean simulation;

    public CommandRunner(TelloDrone drone, boolean simulation) {
        this.drone = drone;
        this.simulation = simulation;
    }

    public void run(List<DroneCommand> commands) {
        System.out.println();
        System.out.println("=== Début de l'exécution (" + commands.size() + " commande(s))"
                + (simulation ? " - MODE SIMULATION" : "") + " ===");
        long start = System.currentTimeMillis();
        int failed = 0;
        for (int i = 0; i < commands.size(); i++) {
            DroneCommand command = commands.get(i);
            System.out.println("[" + (i + 1) + "/" + commands.size() + "] " + command.describe());
            boolean ok = execute(command);
            if (!ok) {
                failed++;
            }
            if (i < commands.size() - 1) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        long elapsed = (System.currentTimeMillis() - start) / 1000;
        System.out.println("=== Fin de l'exécution en " + elapsed + " s"
                + (failed > 0 ? " (" + failed + " commande(s) en échec)" : " (toutes les commandes ont réussi)") + " ===");
    }

    public boolean execute(DroneCommand command) {
        if (simulation) {
            System.out.println("   [simulation] " + command.getAction() + " " + command.getValue());
            return true;
        }
        switch (command.getAction()) {
            case "takeoff":
                return drone.takeoff();
            case "land":
                return drone.land();
            case "forward":
                return drone.goForward(command.getValue());
            case "back":
                return drone.goBackwards(command.getValue());
            case "left":
                return drone.goLeft(command.getValue());
            case "right":
                return drone.goRight(command.getValue());
            case "up":
                return drone.goUp(command.getValue());
            case "down":
                return drone.goDown(command.getValue());
            case "cw":
                return drone.rotateClockwise(command.getValue());
            case "ccw":
                return drone.rotateCounterClockwise(command.getValue());
            case "speed":
                return drone.setSpeed(command.getValue());
            case "wait":
                try {
                    Thread.sleep(command.getValue() * 1000L);
                    return true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            default:
                return false;
        }
    }
}

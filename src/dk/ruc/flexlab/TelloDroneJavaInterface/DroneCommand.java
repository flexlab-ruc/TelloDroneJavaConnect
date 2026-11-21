package dk.ruc.flexlab.TelloDroneJavaInterface;

/**
 * Representation of a single command accepted by the interactive terminal menu.
 * The format matches the Tello SDK1 commands ("forward 100", "cw 90", ...)
 * so a saved path can also be replayed with addToCommandQueue().
 */
public class DroneCommand {

    private final String action;
    private final int value;

    public DroneCommand(String action, int value) {
        this.action = action;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public int getValue() {
        return value;
    }

    public String toToken() {
        if ("takeoff".equals(action) || "land".equals(action)) {
            return action;
        }
        return action + " " + value;
    }

    public static DroneCommand parse(String line) {
        if (line == null) {
            return null;
        }
        String[] parts = line.trim().toLowerCase().split("\\s+");
        if (parts.length == 0 || !isAction(parts[0])) {
            return null;
        }
        String action = parts[0];
        if (!needsValue(action)) {
            return new DroneCommand(action, 0);
        }
        if (parts.length < 2) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        DroneCommand command = new DroneCommand(action, value);
        return command.isValid() ? command : null;
    }

    public boolean isValid() {
        switch (action) {
            case "forward":
            case "back":
            case "left":
            case "right":
            case "up":
            case "down":
                return value >= 20 && value <= 500;
            case "cw":
            case "ccw":
                return value >= 1 && value <= 360;
            case "speed":
                return value >= 1 && value <= 100;
            case "wait":
                return value >= 0;
            case "takeoff":
            case "land":
                return true;
            default:
                return false;
        }
    }

    public static boolean isAction(String name) {
        return "forward".equals(name) || "back".equals(name) || "left".equals(name)
                || "right".equals(name) || "up".equals(name) || "down".equals(name)
                || "cw".equals(name) || "ccw".equals(name) || "speed".equals(name)
                || "wait".equals(name) || "takeoff".equals(name) || "land".equals(name);
    }

    public String describe() {
        switch (action) {
            case "takeoff":
                return "Décollage";
            case "land":
                return "Atterrissage";
            case "forward":
                return "Avancer de " + value + " cm";
            case "back":
                return "Reculer de " + value + " cm";
            case "left":
                return "Aller à gauche de " + value + " cm";
            case "right":
                return "Aller à droite de " + value + " cm";
            case "up":
                return "Monter de " + value + " cm";
            case "down":
                return "Descendre de " + value + " cm";
            case "cw":
                return "Pivoter de " + value + " degrés (horaire)";
            case "ccw":
                return "Pivoter de " + value + " degrés (anti-horaire)";
            case "speed":
                return "Régler la vitesse sur " + value + " cm/s";
            case "wait":
                return "Attendre " + value + " s";
            default:
                return toToken();
        }
    }

    private static boolean needsValue(String action) {
        return !("takeoff".equals(action) || "land".equals(action));
    }
}

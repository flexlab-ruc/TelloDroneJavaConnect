package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive terminal application for the Tello drone.
 * Offers direct commands, named saved paths, path loading and execution,
 * with the live stream window open and a capture every few seconds.
 */
public class Main {

    private static final String PATHS_FOLDER = "paths";
    private static final String CAPTURES_FOLDER = "grabbedImages";
    private static final int CAPTURE_INTERVAL_SECONDS = 5;

    private static final Scanner scanner = new Scanner(System.in);

    private final TelloDrone drone;
    private final boolean simulation;
    private final CommandRunner runner;
    private final PathStore pathStore;
    private final StreamManager streamManager;

    private Main(TelloDrone drone, boolean simulation) {
        this.drone = drone;
        this.simulation = simulation;
        this.runner = new CommandRunner(drone, simulation);
        this.pathStore = new PathStore(PATHS_FOLDER);
        this.streamManager = new StreamManager(CAPTURES_FOLDER, CAPTURE_INTERVAL_SECONDS);
    }

    public static void main(String[] args) {
        System.out.println("=== Tello Drone - Pilotage en terminal ===");
        System.out.println("Assurez-vous d'être connecté au Wi-Fi du drone (192.168.10.1).");
        TelloDrone drone = new TelloDrone();

        boolean connected = false;
        boolean simulation = false;
        System.out.print("Ne passez en mode réel que si vous avez accepté le contrôle. Lancer la connexion au drone ? (o/n) : ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.startsWith("o")) {
            connected = drone.connect();
            if (connected) {
                drone.streamOn();
            } else {
                System.out.print("Connexion impossible. Continuer en mode simulation (les commandes ne seront pas envoyées) ? (o/n) : ");
                answer = scanner.nextLine().trim().toLowerCase();
                simulation = answer.startsWith("o");
            }
        } else {
            simulation = true;
            System.out.println("Mode simulation : les commandes seront affichées sans être envoyées.");
        }
        if (!connected && !simulation) {
            System.out.println("Non connecté au drone, le programme s'arrête.");
            return;
        }

        Main app = new Main(drone, simulation);
        app.streamManager.start();
        try {
            app.mainMenu();
        } finally {
            if (!simulation) {
                drone.streamOff();
            }
            app.streamManager.stop();
        }
        System.out.println("Au revoir !");
    }

    private void mainMenu() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("========== MENU PRINCIPAL ==========");
            System.out.println("1 - Entrer des commandes");
            System.out.println("2 - Créer un nouveau chemin");
            System.out.println("3 - Charger un chemin");
            System.out.println("0 - Quitter");
            if (simulation) {
                System.out.println("(mode simulation : les commandes ne seront pas envoyées au drone)");
            }
            int choice = askNumber("Choix : ");
            switch (choice) {
                case 1:
                    List<DroneCommand> list = buildCommandList();
                    executeOrSaveInline(list);
                    break;
                case 2:
                    createPath();
                    break;
                case 3:
                    loadPath();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Choix inconnu, recommencez.");
            }
        }
    }

    private void executeOrSaveInline(List<DroneCommand> list) {
        if (list.isEmpty()) {
            System.out.println("Aucune commande ajoutée.");
            return;
        }
        System.out.println();
        System.out.println("1 - Exécuter immédiatement");
        System.out.println("2 - Sauvegarder comme chemin");
        System.out.println("3 - Retour au menu principal");
        int choice = askNumber("Choix : ");
        switch (choice) {
            case 1:
                runner.run(list);
                break;
            case 2:
                savePathAsFile(list);
                break;
            case 3:
            default:
                break;
        }
    }

    private void createPath() {
        List<DroneCommand> list = buildCommandList();
        if (list.isEmpty()) {
            System.out.println("Aucune commande ajoutée, chemin non créé.");
            return;
        }
        savePathAsFile(list);
    }

    private void savePathAsFile(List<DroneCommand> list) {
        String name = askText("Nom du chemin : ");
        while (pathStore.exists(name)) {
            System.out.println("1 - Écraser le chemin existant");
            System.out.println("2 - Choisir un autre nom");
            int choice = askNumber("Choix : ");
            if (choice == 2) {
                name = askText("Nom du chemin : ");
            } else {
                break;
            }
        }
        try {
            pathStore.save(name, list);
            System.out.println("Chemin \"" + PathStore.sanitize(name) + "\" sauvegardé (" + list.size() + " commande(s), fichier " + PATHS_FOLDER + "/" + PathStore.sanitize(name) + ".txt).");
        } catch (IOException e) {
            System.out.println("Impossible de sauvegarder le chemin : " + e.getMessage());
        }
    }

    private void loadPath() {
        List<String> names = pathStore.listNames();
        if (names.isEmpty()) {
            System.out.println("Aucun chemin sauvegardé. Créez-en un avec le menu 2.");
            return;
        }
        System.out.println();
        System.out.println("=== Chemins disponibles ===");
        for (int i = 0; i < names.size(); i++) {
            System.out.println((i + 1) + " - " + names.get(i));
        }
        System.out.println("0 - Retour");
        int choice = askNumber("Choix : ");
        if (choice < 1 || choice > names.size()) {
            return;
        }
        String name = names.get(choice - 1);
        List<DroneCommand> list;
        try {
            list = pathStore.load(name);
        } catch (IOException e) {
            System.out.println("Impossible de charger le chemin \"" + name + "\" : " + e.getMessage());
            return;
        }
        System.out.println();
        System.out.println("=== Chemin \"" + name + "\" (" + list.size() + " commande(s)) ===");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + list.get(i).describe());
        }
        System.out.println();
        System.out.println("1 - Lancer ce chemin");
        System.out.println("2 - Retour au menu principal");
        int launch = askNumber("Choix : ");
        if (launch == 1) {
            runner.run(list);
        }
    }

    private List<DroneCommand> buildCommandList() {
        List<DroneCommand> list = new ArrayList<DroneCommand>();
        System.out.println();
        System.out.println("=== Ajout de commandes (0 pour terminer) ===");
        while (true) {
            printCommandHelp();
            int choice = askNumber("Choix : ");
            if (choice == 0) {
                return list;
            }
            DroneCommand command = readCommand(choice);
            if (command == null) {
                System.out.println("Choix invalide, remplissez les champs demandés.");
                continue;
            }
            list.add(command);
            System.out.println("   -> ajouté : " + command.describe());
        }
    }

    private void printCommandHelp() {
        System.out.println();
        System.out.println("1 - Avancer de X m         7 - Pivoter de X degrés (horaire)");
        System.out.println("2 - Reculer de X m         8 - Pivoter de X degrés (anti-horaire)");
        System.out.println("3 - Aller à gauche de X m  9 - Décollage");
        System.out.println("4 - Aller à droite de X m  10 - Atterrissage");
        System.out.println("5 - Monter de X m          11 - Régler la vitesse (1-100 cm/s)");
        System.out.println("6 - Descendre de X m       12 - Attendre X secondes");
        System.out.println("0 - Terminer la liste de commandes");
    }

    private DroneCommand readCommand(int choice) {
        switch (choice) {
            case 1:
                return addMove("forward");
            case 2:
                return addMove("back");
            case 3:
                return addMove("left");
            case 4:
                return addMove("right");
            case 5:
                return addMove("up");
            case 6:
                return addMove("down");
            case 7:
                return new DroneCommand("cw", askNumberInRange("Pivoter horaire de combien de degrés (1-360) ? : ", 1, 360));
            case 8:
                return new DroneCommand("ccw", askNumberInRange("Pivoter anti-horaire de combien de degrés (1-360) ? : ", 1, 360));
            case 9:
                return new DroneCommand("takeoff", 0);
            case 10:
                return new DroneCommand("land", 0);
            case 11:
                return new DroneCommand("speed", askNumberInRange("Vitesse en cm/s (1-100) : ", 1, 100));
            case 12:
                return new DroneCommand("wait", askNumberInRange("Attendre combien de secondes (0-60) ? : ", 0, 60));
            default:
                return null;
        }
    }

    private DroneCommand addMove(String action) {
        double meters = askDouble("Distance en mètres (0.2 à 5, ex : 1 ou 0.5) : ");
        int cm = (int) Math.round(meters * 100);
        while (cm < 20 || cm > 500) {
            System.out.println("Distance invalide : le drone accepte entre 0.2 m et 5 m.");
            meters = askDouble("Distance en mètres (0.2 à 5, ex : 1 ou 0.5) : ");
            cm = (int) Math.round(meters * 100);
        }
        return new DroneCommand(action, cm);
    }

    private int askNumberInRange(String prompt, int min, int max) {
        int value = askNumber(prompt);
        while (value < min || value > max) {
            System.out.println("Valeur invalide : entrez un nombre entre " + min + " et " + max + ".");
            value = askNumber(prompt);
        }
        return value;
    }

    private int askNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide : tapez un nombre.");
            }
        }
    }

    private double askDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide : tapez un nombre.");
            }
        }
    }

    private String askText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Entrez un texte non vide.");
        }
    }
}

package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persists named paths ("chemins") as plain text files, one SDK1 command per line,
 * inside a dedicated folder (default: "paths").
 */
public class PathStore {

    private final File folder;

    public PathStore(String folderPath) {
        folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public List<String> listNames() {
        String[] names = folder.list();
        List<String> result = new ArrayList<>();
        if (names != null) {
            for (String name : names) {
                if (name.endsWith(".txt")) {
                    result.add(name.substring(0, name.length() - 4));
                }
            }
        }
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    public boolean exists(String name) {
        return new File(folder, saveFileName(name)).exists();
    }

    public void save(String name, List<DroneCommand> commands) throws IOException {
        File file = new File(folder, saveFileName(name));
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            for (DroneCommand command : commands) {
                writer.println(command.toToken());
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public List<DroneCommand> load(String name) throws IOException {
        List<DroneCommand> commands = new ArrayList<>();
        File file = new File(folder, saveFileName(name));
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                DroneCommand command = DroneCommand.parse(line);
                if (command != null) {
                    commands.add(command);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return commands;
    }

    private String saveFileName(String name) {
        return sanitize(name) + ".txt";
    }

    public static String sanitize(String name) {
        String cleaned = name.trim().replaceAll("[^A-Za-z0-9-_. ]", "_").replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? "chemin" : cleaned;
    }
}

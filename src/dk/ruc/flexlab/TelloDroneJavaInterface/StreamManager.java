package dk.ruc.flexlab.TelloDroneJavaInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Opens the live video view of the drone (UDP port 11111) and saves a capture
 * every intervalSeconds. One single ffmpeg process reads the stream and produces
 * both the SDL window and the jpg files, because a UDP video stream can only be
 * read once. Falls back to ffplay (view only) when the SDL output device is
 * missing, or to captures only when ffplay is missing.
 */
public class StreamManager {

    private static final String STREAM_URL = "udp://@:11111";
    private static final String CAPTURE_PREFIX = "capture_";

    private final String outputFolder;
    private final int intervalSeconds;
    private Process process;

    public StreamManager(String outputFolder, int intervalSeconds) {
        this.outputFolder = outputFolder;
        this.intervalSeconds = intervalSeconds;
    }

    public void start() {
        File captureFolder = new File(outputFolder);
        if (!captureFolder.exists()) {
            captureFolder.mkdirs();
        }
        if (toolAvailable("ffmpeg")) {
            if (hasDevice("sdl")) {
                spawnViewAndCaptures();
                return;
            }
            if (toolAvailable("ffplay")) {
                System.out.println("[Vue drone] sortie SDL indisponible avec ce ffmpeg : affichage via ffplay, captures automatiques indisponibles.");
                spawnViewOnly();
                return;
            }
            spawnCapturesOnly();
            return;
        }
        if (toolAvailable("ffplay")) {
            System.out.println("[Vue drone] ffmpeg introuvable dans le PATH : affichage via ffplay, captures impossibles (installez ffmpeg pour les captures).");
            spawnViewOnly();
            return;
        }
        System.out.println("[Vue drone] ffmpeg/ffplay introuvables dans le PATH : pas de fenêtre live ni de captures. Installez ffmpeg (https://ffmpeg.org) et rendez-le accessible dans le PATH.");
    }

    public void stop() {
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(3, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            process = null;
        }
    }

    private void spawnViewAndCaptures() {
        try {
            process = new ProcessBuilder(
                    "ffmpeg", "-hide_banner", "-loglevel", "warning", "-i", STREAM_URL,
                    "-map", "0:v", "-f", "sdl", "Vue drone (q pour fermer)",
                    "-map", "0:v", "-vf", "fps=1/" + intervalSeconds, "-q:v", "2",
                    outputFolder + File.separator + CAPTURE_PREFIX + "%04d.jpg")
                    .inheritIO().start();
            System.out.println("[Vue drone] fenêtre live ouverte ; captures toutes les " + intervalSeconds + " s dans " + outputFolder + "/");
        } catch (Exception e) {
            process = null;
            System.out.println("[Vue drone] impossible de démarrer l'affichage ffmpeg : " + e.getMessage());
        }
    }

    private void spawnCapturesOnly() {
        try {
            process = new ProcessBuilder(
                    "ffmpeg", "-hide_banner", "-loglevel", "warning", "-i", STREAM_URL,
                    "-vf", "fps=1/" + intervalSeconds, "-q:v", "2",
                    outputFolder + File.separator + CAPTURE_PREFIX + "%04d.jpg")
                    .inheritIO().start();
            System.out.println("[Vue drone] ffplay introuvable : captures toutes les " + intervalSeconds + " s dans " + outputFolder + "/ (pas d'affichage live).");
        } catch (Exception e) {
            process = null;
            System.out.println("[Vue drone] impossible de démarrer l'affichage ffmpeg : " + e.getMessage());
        }
    }

    private void spawnViewOnly() {
        try {
            process = new ProcessBuilder(
                    "ffplay", "-probesize", "32", "-i", STREAM_URL, "-framerate", "30",
                    "-window_title", "Vue drone")
                    .inheritIO().start();
        } catch (Exception e) {
            process = null;
            System.out.println("[Vue drone] impossible de démarrer ffplay : " + e.getMessage());
        }
    }

    private boolean toolAvailable(String tool) {
        try {
            Process p = new ProcessBuilder(tool, "-version").start();
            consumeQuietly(p);
            return p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasDevice(String device) {
        try {
            Process p = new ProcessBuilder("ffmpeg", "-hide_banner", "-devices").start();
            boolean found = false;
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2 && device.equals(parts[1])) {
                        found = true;
                    }
                }
            } finally {
                reader.close();
            }
            p.waitFor();
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    private void consumeQuietly(Process p) {
        InputStream in = null;
        try {
            in = p.getInputStream();
            byte[] buffer = new byte[2048];
            while (in.read(buffer) != -1) {
            }
        } catch (Exception ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored2) {
                }
            }
        }
    }
}

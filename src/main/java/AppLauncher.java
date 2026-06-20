/**
 * Launcher für Anwendung.
 * Separat um Boot-Fehler zu vermeiden.
 * Startprozess wird an MainApp umgeleitet
 */
public class AppLauncher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}

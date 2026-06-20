import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Initialisierung des Hauptfensters
 * Laden der Hauptstruktur
 * Stylesheet binden
 */
public class MainApp extends Application {

    /**
     * Hauptbenutzeroberfläche aufbauen
     * @param stage Hauptfenster für die Anwendung
     */
    @Override
    public void start(Stage stage) {
        MainStructure mainView = new MainStructure(); // Layout-Koordinator
        Scene scene = new Scene(mainView, (900), 600);

        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // CSS-Datei zuweisen
        stage.getIcons().add(new Image("file:src/main/resources/icon.png"));
        stage.setTitle("MediaVault");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Startpunkt der Anwendung
     */
    public static void main(String[] args) {
        launch();
    }
}
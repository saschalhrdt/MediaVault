import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;

/**
 * Dialogfenster zur Verwaltung von Genres.
 * Ermöglicht das Hinzufügen und Löschen von Genres je nach Medientyp.
 * Änderungen werden in Datenbank geschrieben.
 *
 */

public class ManageGenresDialog {
    private final Stage stage;
    private final MediaRepository repository;
    private final ComboBox<String> cbType;
    private final ListView<String> lvGenres;
    private final TextField txtNewGenre;

    /**
     * Konstruktor zur Erstellung des Dialogs für Genreverwaltung
     * @param owner Übergeordnetes Fesnter für modale Sperrung
     * @param repository Repository für Datenbankzugriff
     */
    public ManageGenresDialog(Stage owner, MediaRepository repository) {
        this.repository = repository != null ? repository : new MediaRepository(); // Repository überprüfen

        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Genres verwalten");
        stage.getIcons().add(new Image("file:src/main/resources/icon.png"));

        VBox root = new VBox(10);
        root.getStyleClass().add("genre-view");
        root.setPadding(new Insets(15));

        cbType = new ComboBox<>();
        cbType.getItems().addAll("Album", "Buch", "Film", "Serie", "Videospiel");
        cbType.setValue("Film");
        cbType.setMaxWidth(Double.MAX_VALUE);

        lvGenres = new ListView<>();
        VBox.setVgrow(lvGenres, Priority.ALWAYS); // Liste dehnt sich bei Skalierung vertikal aus

        txtNewGenre = new TextField();
        txtNewGenre.setPromptText("Neues Genre eingeben...");

        Button btnAdd = new Button("Hinzufügen [+]");
        Button btnDelete = new Button("Löschen [\uD83D\uDDD1]");

        // Footer für neue Genres und Buttons
        HBox inputActionBox = new HBox(10, txtNewGenre);
        HBox.setHgrow(txtNewGenre, Priority.ALWAYS); // Maximale Breite

        HBox buttonBox = new HBox(10, btnAdd, btnDelete);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Komponenten dem Layout hinzufügen
        root.getChildren().addAll(
                new Label("Medientyp wählen:"), cbType,
                new Label("Existierende Genres:"), lvGenres,
                inputActionBox, buttonBox
        );

        loadGenres();

        // Genreliste je nach Medientyp aktualisieren
        cbType.valueProperty().addListener((obs, oldVal, newVal) -> loadGenres());

        // Genre hinzufügen
        btnAdd.setOnAction(event -> {
            String genreName = txtNewGenre.getText().trim();
            if (genreName.isEmpty()) return; // Leere Eingaben ignorieren

            // Genre in Datenbank speichern
            boolean success = this.repository.addGenre(genreName, cbType.getValue());
            if (success) {
                txtNewGenre.clear();
                loadGenres();
            } else { // Warnt, wenn Kombination aus Typ und Genre bereits existiert
                Alert alert = new Alert(Alert.AlertType.WARNING, "Dieses Genre existiert bereits für diesen Medientyp!", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Genre löschen
        btnDelete.setOnAction(event -> {
            String selectedGenre = lvGenres.getSelectionModel().getSelectedItem();
            if (selectedGenre == null) { // Prüfen, ob Genre überhaupt gewählt wurde
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Bitte wählen Sie zuerst ein Genre aus der Liste aus!", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Warnung vor endgültigem Löschen
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Möchten Sie das Genre '" + selectedGenre + "' wirklich löschen? Dadurch wird es auch von allen Medien entfernt.", ButtonType.YES, ButtonType.NO);
            if (confirmAlert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                this.repository.deleteGenre(selectedGenre, cbType.getValue());
                loadGenres();
            }
        });

        Scene scene = new Scene(root, 380, 420);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Existierenden Genres für ausgewählten Medientypen laden
     */
    private void loadGenres() {
        lvGenres.getItems().clear();
        if (this.repository != null) {
            List<String> genres = this.repository.getGenresForType(cbType.getValue());
            lvGenres.getItems().addAll(genres);
        }
    }

    /**
     * Dialog modal anzeigen
     */
    public void show() {
        stage.showAndWait();
    }
}
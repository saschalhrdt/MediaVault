import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialogfenster zum Hinzufügen von neuen Medienobjekten.
 * Eingabemasken für Daten wie Titel, Erscheinungsjahr, Medientyp,
 * Genres, Status und Notizen.
 */
public class AddMediaDialog {
    private final MediaRepository repository;
    private final Runnable onSaveCallback;

    /**
     * Konstruktor zur Erstellung des Dialogs für neue Medien.
     * @param repository Repository für Datenbankzugriff
     * @param onSaveCallback Callback zur Aktualisierung des Überblicks nach Speichern
     */
    public AddMediaDialog(MediaRepository repository, Runnable onSaveCallback) {
        this.repository = repository;
        this.onSaveCallback = onSaveCallback;
    }

    /**
     * Erstellt und zeigt Dialogfenster,
     * konfiguriert Komponenten
     */
    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL); // Blockiert das Hauptfenster im Hintergrund
        dialog.setTitle("Medium hinzufügen");
        dialog.getIcons().add(new Image("file:src/main/resources/icon.png"));

        VBox dialogVBox = new VBox(15);
        dialogVBox.getStyleClass().add("add-popup");

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Titel ");

        // Default-Wert für Jahr wird automatisch auf derzeitiges Jahr festgelegt
        int currentYear = LocalDate.now().getYear();
        Spinner<Integer> spinnerYear = new Spinner<>(1700, currentYear + 10, currentYear);
        spinnerYear.setEditable(true);
        spinnerYear.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Album", "Buch", "Film", "Serie", "Videospiel");
        cbType.setPromptText("Mediumtyp auswählen");

        TextField txtCreator = new TextField();
        txtCreator.setPromptText("Ersteller ");

        MenuButton mbGenres = new MenuButton("Typ auswählen");
        mbGenres.setDisable(true);
        mbGenres.setMaxWidth(Double.MAX_VALUE);

        List<CheckMenuItem> genreItems = new ArrayList<>();

        // Genres werden dynamisch je nach ausgewähltem Medientyp geladen
        cbType.setOnAction(event -> {
            String selectedType = cbType.getValue();
            if (selectedType != null) {
                mbGenres.getItems().clear();
                genreItems.clear();
                mbGenres.setDisable(false);
                mbGenres.setText("Genres auswählen");

                // Genres passend zu Medientyp aus Datenbank laden
                List<String> dynamicGenres = repository.getGenresForType(selectedType);

                // Checkbox-Einträge für die Genres erstellen
                for (String genreName : dynamicGenres) {
                    CheckMenuItem item = new CheckMenuItem(genreName);
                    genreItems.add(item);
                    mbGenres.getItems().add(item);
                }
            }
        });

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Abgeschlossen", "Offen");
        cbStatus.setPromptText("Status auswählen");

        Spinner<Integer> spinnerRating = new Spinner<>(1, 5, 5);
        spinnerRating.setMaxWidth(120);
        spinnerRating.setDisable(true);

        // Bewertung nur möglich, wenn Status "Abgeschlossen" ausgewählt wurde
        cbStatus.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if ("Abgeschlossen".equals(newValue)) {
                spinnerRating.setDisable(false);
            } else {
                spinnerRating.setDisable(true);
                spinnerRating.getValueFactory().setValue(5);
            }
        }));

        TextField txtImagePath = new TextField();
        txtImagePath.setPromptText("Bildname");

        TextArea txtNotes = new TextArea();
        txtNotes.setPromptText("Notizen");
        txtNotes.setWrapText(true);
        txtNotes.setPrefRowCount(8);

        Button btnSave = new Button("Speichern");
        btnSave.getStyleClass().add("add-save-button");

        HBox buttonContainer = new HBox(btnSave);
        buttonContainer.setAlignment(Pos.CENTER);

        btnSave.setOnAction(event -> {
            // Pflichtfelder
            if (txtTitle.getText().trim().isEmpty() || cbType.getValue() == null || cbStatus.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Titel, Typ und Status müssen ausgefüllt sein.");
                alert.showAndWait();
                return;
            }

            // Mehrere Genres aus Menü sammeln
            List<String> selectedGenres = new ArrayList<>();
            for (CheckMenuItem item : genreItems) {
                if (item.isSelected()) {
                    selectedGenres.add(item.getText());
                }
            }

            // Bewertung wird nur für Medien mit Status "Abgeschlossen" gespeichert
            int ratingValue;
            if ("Abgeschlossen".equals(cbStatus.getValue())) {
                ratingValue = spinnerRating.getValue();
            } else {
                ratingValue = 0;
            }

            // Daten an Repository übergeben
            repository.saveMedia(
                txtTitle.getText(),
                spinnerYear.getValue(),
                cbType.getValue(),
                txtCreator.getText(),
                cbStatus.getValue(),
                ratingValue,
                txtImagePath.getText(),
                txtNotes.getText(),
                selectedGenres
            );

            dialog.close();
            onSaveCallback.run();
        });

        // Elemente dem Container hinzufügen
        dialogVBox.getChildren().addAll(
                new Label("Titel:"), txtTitle,
                new Label("Jahr:"), spinnerYear,
                new Label("Typ:"), cbType,
                new Label("Ersteller:"), txtCreator,
                new Label("Genres: "), mbGenres,
                new Label("Status:"), cbStatus,
                new Label("Bewertung:"), spinnerRating,
                new Label("Cover-Dateiname:"), txtImagePath,
                new Label("Notizen:"), txtNotes,
                buttonContainer
        );

        Scene dialogScene = new Scene(dialogVBox, 500, 800);
        dialogScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.setResizable(false);
        dialog.show();
    }
}

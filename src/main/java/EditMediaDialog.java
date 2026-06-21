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
import java.util.ArrayList;
import java.util.List;

/**
 * Dialogfenster zur Bearbeitung existierender Medienobjekte.
 * Ermöglicht Veränderungen an Titel, Jahr, Status, Typ, Genres und Notizen.
 * Ändernungen werden danach im Repository gespeichert.
 */
public class EditMediaDialog {
    private final Stage stage;
    private final MediaRepository repository;
    private final Medium medium;
    private final Runnable onSaveCallback;
    private final TextField txtTitle = new TextField();
    private final TextField txtYear = new TextField();
    private final TextField txtCreator = new TextField();
    private final ComboBox<String> cbType = new ComboBox<>();
    private final ComboBox<String> cbStatus = new ComboBox<>();
    private final ComboBox<String> cbRating = new ComboBox<>();
    private final TextArea txtNotes = new TextArea();
    private final ListView<CheckBox> lvGenres = new ListView<>();

    /**
     * Erstellt Dialog zur Bearbeitung eines Mediums
     *
     * @param owner          Stage für Dialog
     * @param repository     Repository für Datenbankzugriff
     * @param medium         Zu bearbeitendes Medium
     * @param onSaveCallback Callback zur Aktualisierung des Überblicks nach Speichern
     */
    public EditMediaDialog(Stage owner, MediaRepository repository, Medium medium, Runnable onSaveCallback) {
        this.repository = repository;
        this.medium = medium;
        this.onSaveCallback = onSaveCallback;

        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);
        stage.setTitle("Medium bearbeiten");
        stage.getIcons().add(new Image("https://cdn-icons-png.flaticon.com/512/2237/2237920.png"));

        VBox root = new VBox(10);
        root.getStyleClass().add("edit-view");
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        // Felder automatisch ausfüllen mit aktuellen Daten
        txtTitle.setText(medium.getTitle());
        txtYear.setText(String.valueOf(medium.getYear()));
        txtCreator.setText(medium.getCreator());

        cbType.getItems().addAll("Album", "Buch", "Film", "Serie", "Videospiel");
        cbType.setValue(medium.getType());

        cbStatus.getItems().addAll("Abgeschlossen", "Offen");
        cbStatus.setValue(medium.getStatus());

        cbRating.getItems().addAll("N/A", "1", "2", "3", "4", "5");
        if (medium.getRating() > 0) {
            cbRating.setValue(String.valueOf(medium.getRating()));
        } else {
            cbRating.setValue("N/A");
        }

        txtNotes.setText(medium.getNotes() != null ? medium.getNotes() : "");
        txtNotes.setWrapText(true);
        txtNotes.setMaxWidth(Double.MAX_VALUE);
        txtNotes.setPrefRowCount(8);
        txtNotes.setPrefHeight(120);

        root.getChildren().addAll(
                new Label("Titel:"), txtTitle,
                new Label("Jahr:"), txtYear,
                new Label("Ersteller:"), txtCreator,
                new Label("Typ:"), cbType,
                new Label("Status:"), cbStatus,
                new Label("Bewertung:"), cbRating,
                new Label("Notizen:"), txtNotes,
                new Label("Genres:"), lvGenres
        );

        loadGenres();
        // Listener für dynamisches Nachladen der Genres bei Typänderung
        cbType.valueProperty().addListener((obs, oldVal, newVal) -> loadGenres());

        Button btnSave = new Button("Speichern");

        btnSave.setOnAction(event -> saveChanges());

        HBox buttonBox = new HBox(btnSave);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(10, 0, 10, 0));

        VBox.setVgrow(lvGenres, Priority.NEVER);
        lvGenres.setPrefHeight(100);
        lvGenres.setMaxHeight(120);
        lvGenres.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(buttonBox);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 360, 750);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Genres werden basierend auf dem Medientyp geladen
     * Markiert zugewiesene Genres für den Eintrag
     */
    private void loadGenres() {
        lvGenres.getItems().clear();
        List<String> availableGenres = repository.getGenresForType(cbType.getValue());
        List<String> currentGenres = repository.getGenresForMedium(medium.getTitle());

        for (String genre : availableGenres) {
            CheckBox cb = new CheckBox(genre);
            if (currentGenres.contains(genre)) {
                cb.setSelected(true);
            }
            lvGenres.getItems().add(cb);
        }
    }

    /**
     * Eingaben validieren und in Datenbank speichern
     */
    private void saveChanges() {
        if (txtTitle.getText().trim().isEmpty() || cbType.getValue() == null || cbStatus.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Titel, Typ und Status müssen ausgefüllt sein.");
            alert.showAndWait();
            return;
        }

        int ratingValue = 0;
        if (cbRating.getValue() != null && !cbRating.getValue().equals("N/A")) {
            ratingValue = Integer.parseInt(cbRating.getValue());
        }

        try {
            List<String> selectedGenres = new ArrayList<>();
            for (CheckBox cb : lvGenres.getItems()) {
                if (cb.isSelected()) {
                    selectedGenres.add(cb.getText());
                }
            }

            repository.updateMedia(
                    medium.getTitle(),
                    txtTitle.getText(),
                    Integer.parseInt(txtYear.getText().trim()),
                    cbType.getValue(),
                    txtCreator.getText(),
                    cbStatus.getValue(),
                    ratingValue,
                    medium.getImagePath(),
                    txtNotes.getText(),
                    selectedGenres
            );

            stage.close();
            onSaveCallback.run();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ungültiges Erscheinungsjahr.");
            alert.showAndWait();
        }
    }

    /**
     * Dialog modal anzeigen
     */
    public void show () {
        stage.showAndWait();
    }
}

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;

/**
 * Dialogfenster zur Filterung der Haupttabelle
 * Bietet Möglichkeiten nach Typ, Status, Bewertung, Jahr und Genres zu filtern
 * Verfügbaren Genres passen sich dynamisch an
 * Gefilterten Werte werden über Interface zurückgegeben
 */
public class FilterMediaDialog {
    private final Stage stage;
    private final ComboBox<String> cbType;
    private final ComboBox<String> cbStatus;
    private final ComboBox<String> cbRating;
    private final ComboBox<String> cbGenre;
    private final TextField txtJahr;
    private final MediaRepository repository;

    /**
     * Callback, um ausgewählten Filterkriterien an Struktur zu übergeben
     */
    public interface FilterResultListener {
        /**
         * Wird aufgerufen, wenn der "Anwenden" Button geklickt wird.
         * @param type Ausgewählter Typ
         * @param status Ausgewählter Status
         * @param rating Ausgewählte Bewertung
         * @param genre Ausgewähltes Genre
         * @param year Ausgewähltes Jahr
         */
        void onFilterApplied(String type, String status, String rating, String genre, String year);
    }

    /**
     * Konstruktor zur Erstellung und Initialisierung des Filter-Dialogs
     * @param owner Übergeordnetes Fesnter für modale Sperrung
     * @param repository Repository für Datenbankzugriff
     * @param currentType Aktuell aktive Typ-Filter
     * @param currentStatus Aktuell aktiver Status-Filter
     * @param currentRating Aktuell aktiver Bewertungs-Filter
     * @param currentGenre Aktuell aktiver Genre-Filter
     * @param currentYear Aktuell aktiver Jahr-Filter
     * @param listener Listener, der bei Anwendung der Filter benachrichtigt wird
     */
    public FilterMediaDialog(Stage owner, MediaRepository repository, String currentType, String currentStatus, String currentRating, String currentGenre, String currentYear, FilterResultListener listener) {
        this.repository = repository;

        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL); // Hauptfenster im Hintergrund blockieren
        stage.initOwner(owner);
        stage.setResizable(false);
        stage.setTitle("Filter");
        stage.getIcons().add(new Image("file:src/main/resources/icon.png"));

        VBox root = new VBox(10);
        root.getStyleClass().add("filter-view");
        root.setPadding(new Insets(15));

        // Initialisierung der einzelnen Komponenten
        root.getChildren().add(new Label("Medientyp:"));
        cbType = new ComboBox<>();
        cbType.getItems().addAll("Alle", "Album", "Buch", "Film", "Serie", "Videospiel");
        cbType.setValue(currentType);
        cbType.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(cbType);

        root.getChildren().add(new Label("Status:"));
        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Alle", "Abgeschlossen", "Offen");
        cbStatus.setValue(currentStatus);
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(cbStatus);

        root.getChildren().add(new Label("Bewertung:"));
        cbRating = new ComboBox<>();
        cbRating.getItems().addAll("Alle", "★☆☆☆☆ (1)", "★★☆☆☆ (2)", "★★★☆☆ (3)", "★★★★☆ (4)", "★★★★★ (5)");
        cbRating.setValue(currentRating);
        cbRating.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(cbRating);

        root.getChildren().add(new Label("Genre:"));
        cbGenre = new ComboBox<>();
        cbGenre.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(cbGenre);

        root.getChildren().add(new Label("Jahr:"));
        txtJahr = new TextField();
        txtJahr.setText(currentYear);
        txtJahr.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(txtJahr);

        // Genres je nach ausgewählten Zustand laden
        updateGenreComboBox(cbType.getValue(), currentGenre);

        // Genres werden bei anderem Medientypen neu geladen
        cbType.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateGenreComboBox(newValue, "Alle");
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        VBox.setMargin(buttonBox, new Insets(25, 0, 0, 0));
        Button btnApply = new Button("Anwenden");

        btnApply.setOnAction(event -> {
            String yearValue = txtJahr.getText().trim();
            if (yearValue.isEmpty()) { // Bei unausgefüllten Jahr werden alle Jahre betrachtet
                yearValue = "Alle";
            }
            // Filterdaten zurückmelden
            listener.onFilterApplied(cbType.getValue(), cbStatus.getValue(), cbRating.getValue(), cbGenre.getValue(), yearValue);
            stage.close();
        });

        buttonBox.getChildren().addAll(btnApply);
        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root, 320, 420);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Aktualisiert Genreliste basierend auf dem gewählten Medientypen
     * Auswahl nur möglich, wenn ein Typ gewählt wurde
     * @param selectedType Aktuell gewählte Medientyp
     * @param valueToSet Wert, auf den die Auswahlbox nach dem Laden gesetzt werden soll
     */
    private void updateGenreComboBox(String selectedType, String valueToSet) {
        cbGenre.getItems().clear();
        cbGenre.getItems().add("Alle");

        // Deaktiviert Genrefiltern für alle Medientypen
        if (selectedType.equals("Alle")) {
            cbGenre.setValue("Alle");
            cbGenre.setDisable(true);
            return;
        }

        cbGenre.setDisable(false);

        // Passende Genres für Medientypen aus Datenbank abrufen
        List<String> allGenres = repository.getGenresForType(selectedType);
        cbGenre.getItems().addAll(allGenres);

        // Prüfen, ob Filterwert in neuer Liste existiert
        if (cbGenre.getItems().contains(valueToSet)) {
            cbGenre.setValue(valueToSet);
        } else {
            cbGenre.setValue("Alle");
        }
    }

    /**
     * Dialog modal anzeigen
     */
    public void show() {
        stage.showAndWait();
    }
}
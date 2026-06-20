import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.List;

/**
 * Detailansicht (seitlich) für einen ausgewählten Eintrag.
 * Wird über SplitPane eingeblendet und zeigt Titel, Status, Bewertung
 * Genres, Cover-Bild sowie die Möglichkeit den Eintrag zu löschen und in eine
 * Vollbildansicht zu wechseln.
 */
public class DetailView extends VBox {
    private final Label lblTitle = new Label();
    private final Label lblStatus = new Label();
    private final Label lblRating = new Label();
    private final Label lblGenre = new Label();
    private final ImageView imgCover = new ImageView();
    private final Button btnCloseDetails = new Button();
    private final Button btnOpenDetails = new Button();
    private final Button btnDeleteEntry = new Button();
    private MediaRepository repository;
    private Runnable onDataChanged;
    private Medium currentMedium;
    private final MainStructure mainStructure;

    /**
     * Konstruktor zur Initialisierung und für Aufbau der Detailansicht.
     * @param splitPane Übergeordnete SplitPane für Detailansicht
     * @param repository Repository für Datenbankzugriff
     * @param onDataChanged Callback zur Aktualisierung des Überblicks nach Löschen
     * @param mainStructure Steuerungsstruktur für den Wechsel in vollständige Eintragsansicht
     */
    public DetailView(SplitPane splitPane, MediaRepository repository, Runnable onDataChanged, MainStructure mainStructure) {
        super(15); // Vertikaler Abstand von 15 Pixeln zwischen den Elementen
        this.repository = repository;
        this.onDataChanged = onDataChanged;
        this.mainStructure = mainStructure;
        this.getStyleClass().add("detail-view");
        this.setAlignment(Pos.TOP_CENTER);

        lblTitle.getStyleClass().add("detail-title");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        lblTitle.setMinWidth(0);
        lblTitle.setWrapText(true); // Zeilenumbruch bei längeren Titel

        // Cover-Bild in HBox kapseln
        imgCover.getStyleClass().add("detail-image");
        HBox imageBox = new HBox(imgCover);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setMaxWidth(Double.MAX_VALUE);

        // Schließen-Button entfernt diese Detailansicht aus der SplitPane
        btnCloseDetails.getStyleClass().add("detail-button-close");
        btnCloseDetails.setOnAction(event -> {
            splitPane.getItems().remove(this);
        });

        Region leftSpacer = new Region();
        leftSpacer.prefWidthProperty().bind(btnCloseDetails.widthProperty());

        // Aufbau von Header
        BorderPane headerPane = new BorderPane();
        headerPane.setMaxWidth(Double.MAX_VALUE);
        headerPane.setLeft(leftSpacer);
        headerPane.setCenter(lblTitle);

        BorderPane.setAlignment(btnCloseDetails, Pos.TOP_CENTER);
        headerPane.setRight(btnCloseDetails);

        btnOpenDetails.getStyleClass().add("detail-button-view");
        btnDeleteEntry.getStyleClass().add("detail-button-delete");

        // Löschvorgang mit Sicherheitsabfrage
        btnDeleteEntry.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warnung");
            alert.setHeaderText("Löschen bestätigen");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                repository.deleteMedia(currentMedium.getTitle());
                splitPane.getItems().remove(this); // Detailansicht schließen
                onDataChanged.run(); // Überblicktabelle aktualisieren
            }
        });

        // Aufbau von Footer
        HBox footerBox = new HBox(20);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.getChildren().add(btnOpenDetails);
        footerBox.getChildren().add(btnDeleteEntry);

        this.getChildren().addAll(headerPane, imageBox, lblStatus, lblRating, lblGenre, footerBox);
    }

    /**
     * Befüllt Komponenten dynamisch mit Daten
     * @param clicked Ausgewähltes Medium
     */
    public void showDetails(Medium clicked) {
        if (clicked == null) return;

        this.currentMedium = clicked;

        // Komponenten befüllen
        lblTitle.setText(clicked.getTitle());
        btnCloseDetails.setText("X");
        imgCover.setImage(clicked.getCoverImage(175, 250));
        lblStatus.setText("Status: " + clicked.getStatusLabel());
        lblRating.setText("Bewertung: " + Medium.convertToStars(clicked.getRating()));

        // Öffnet erweiterte Detailansicht/Eintragsansicht
        btnOpenDetails.setText("Detailansicht");
        btnOpenDetails.setOnAction(event -> {
            if (currentMedium != null) {
                mainStructure.showEntryView(currentMedium);
            }
        });
        btnDeleteEntry.setText("Löschen [\uD83D\uDDD1]");

        // Genres basierend auf Titel aus Datenbank laden
        List<String> genres = repository.getGenresForMedium(clicked.getTitle());
        if (!genres.isEmpty()) {
            lblGenre.setText("Genres: " + String.join(", ", genres));
        } else {
            lblGenre.setText("Genres: -");
        }
    }
}

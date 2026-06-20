import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

/**
 * Vollständige Detailansicht eines Eintrags.
 * Zeigt vergrößertes Cover-Bild, Titel, Status, Jahr, Ersteller,
 * Bewertung und Notizen an.
 * Ermöglicht außerdem die Bearbeitung und das Löschen des aktuellen Eintrags.
 */
public class EntryView extends BorderPane {
    private final Medium medium;
    private final MediaRepository repository;
    private final MainStructure mainStructure;

    /**
     * Konstruktor zur Erstellung der detaillierten Eintragsansicht.
     * @param medium Anzuzeigendes Medium mit Daten
     * @param mainStructure Steuerungsstruktur für Wechsel in andere Ansichten
     */
    public EntryView(Medium medium, MainStructure mainStructure) {
        this.medium = medium;
        this.repository = new MediaRepository();
        this.mainStructure = mainStructure;
        setupView();
    }

    /**
     * Konfiguriert Komponenten und ordnet sie im Layout an.
     */
    private void setupView() {
        this.getStyleClass().add("entry-view");

        // Header Bereich für Bearbeiten und Löschen Buttons
        HBox headerBar = new HBox();
        Button btnEdit = new Button("Bearbeiten ✎");
        btnEdit.getStyleClass().add("entry-button-edit");
        btnEdit.setOnAction(event -> {
            Stage owner = (Stage) this.getScene().getWindow();

            new EditMediaDialog(owner, repository, medium, () -> {

                // Überblick nach Bearbeitung der Daten aktualisieren
                mainStructure.getOverview().loadMediaFromDatabase("Alle", "Alle");

                // Editierten Datensatz aus Datenbank laden
                List<Medium> updatedList = repository.fetchMedia("Alle", "Alle", "Alle", "Alle", "Alle", medium.getTitle());
                if (!updatedList.isEmpty()) {

                    // Eintragsansicht mit aktualisierten Daten laden
                    mainStructure.showEntryView(updatedList.get(0));
                }
            }).show();
        });

        Button btnDeleteEntry = new Button("Löschen \uD83D\uDDD1");
        btnDeleteEntry.getStyleClass().add("entry-button-delete");
        btnDeleteEntry.setOnAction(event -> {
            // Sicherheitsabfrage vor Löschen
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warnung");
            alert.setHeaderText("Löschen bestätigen");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                repository.deleteMedia(medium.getTitle()); // Löscht Eintrag aus Datenbank
                mainStructure.getOverview().closeDetailView();
                mainStructure.showOverview();
                mainStructure.getOverview().loadMediaFromDatabase("Alle", "Alle");
            }
        });

        headerBar.getChildren().addAll(btnEdit, btnDeleteEntry);
        this.setTop(headerBar);

        HBox mainContent = new HBox(40);
        mainContent.setPadding(new Insets(20));

        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);

        StackPane coverContainer = new StackPane();
        ImageView coverView = new ImageView();
        coverView.setImage(medium.getCoverImage(350, 600));

        coverContainer.getChildren().add(coverView);
        leftColumn.getChildren().addAll(coverContainer);

        VBox rightColumn = new VBox(15);
        Label lblMainTitle = new Label(medium.getTitle());
        lblMainTitle.getStyleClass().add("entry-label-title");
        Label lblSubtitle = new Label(medium.getType() + " - " + medium.getYear() + " - " + medium.getCreator());
        List<String> genres = medium.getGenres();
        Label lblGenres = new Label();
        if (genres != null && !genres.isEmpty()) {
            lblGenres.setText("Genres: " + String.join(", ", genres));
        } else {
            lblGenres.setText("Genre: -");
        }
        Label lblStatusBadge = new Label("Status: " + medium.getStatusLabel());
        Label lblRating = new Label("Bewertung: " + Medium.convertToStars(medium.getRating()));
        Label lblNotes = new Label("Notizen: " + (medium.getNotes() != null ? medium.getNotes() : "-"));
        lblNotes.setWrapText(true);
        lblNotes.setMaxWidth(400);

        rightColumn.getChildren().addAll(lblMainTitle, lblSubtitle, lblGenres, lblStatusBadge, lblRating, lblNotes);

        mainContent.getChildren().addAll(leftColumn, rightColumn);
        this.setCenter(mainContent);
    }
}
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

/**
 * Hauptübersicht der Anwendung.
 * Kombiniert Tabellenansicht mit seitlicher Detailansicht.
 * Ermöglicht außerdem Suchen nach Ersteller oder Titel, Hinzufügen und Filtern von Einträgen.
 */
public class Overview extends VBox {
    private final TableView<Medium> mediaTable;
    private final SplitPane splitPane;
    private final DetailView detailView;
    private final TextField searchField;
    private final MediaRepository repository;
    private final MainStructure mainStructure;

    // Aktive Filterzustände
    private String activeCategoryFilter = "Alle";
    private String activeStatusFilter = "Alle";
    private String activeRatingFilter = "Alle";
    private String activeGenreFilter = "Alle";
    private String activeYearFilter = "Alle";

    /**
     * Konstruktor zur Erstellung der Hauptübersicht
     * Initialisierung der Tabelle, Filter- und Hinzufügen-Button und Suchfeld
     * @param mainStructure Steuerungsstruktur für den Ansichtenwechsel
     */
    public Overview(MainStructure mainStructure) {
        this.mainStructure = mainStructure;
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("overview");
        this.repository = new MediaRepository();

        // Header für Suchfeld und Buttons
        HBox overviewHeader = new HBox(10);
        overviewHeader.getStyleClass().add("overview-header");

        searchField = new TextField();
        searchField.setPromptText("Suche nach Ersteller oder Titel");
        searchField.getStyleClass().add("overview-search-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAddMedia = new Button("Hinzufügen [+]");
        btnAddMedia.getStyleClass().add("overview-add-button");
        Button btnFilterMedia = new Button("Filter [≡]");
        btnFilterMedia.getStyleClass().add("overview-filter-button");

        // Filterdialog
        btnFilterMedia.setOnAction(event -> {
            Stage owner = (Stage) this.getScene().getWindow();

            // Aktive Filterzustände übergeben
            new FilterMediaDialog(owner, repository, activeCategoryFilter, activeStatusFilter, activeRatingFilter, activeGenreFilter, activeYearFilter,
                    (type, status, rating, genre, year) -> {
                        // Callback: Filterkriterien aktualisieren und Tabelle neu laden
                        this.activeCategoryFilter = type;
                        this.activeStatusFilter = status;
                        this.activeRatingFilter = rating;
                        this.activeGenreFilter = genre;
                        this.activeYearFilter = year;

                        loadMediaFromDatabase(activeCategoryFilter, activeStatusFilter, activeRatingFilter, activeGenreFilter, activeYearFilter);
                    }
            ).show();
        });

        overviewHeader.getChildren().addAll(searchField, spacer, btnAddMedia, btnFilterMedia);

        // Struktur für Tabelle und Detailansicht
        splitPane = new SplitPane();
        mediaTable = new TableView<>();
        mediaTable.getStyleClass().add("media-table");

        // Detailansicht initialisieren und Callback für Datenänderungen mitgeben
        this.detailView = new DetailView(splitPane, repository, () -> loadMediaFromDatabase(activeCategoryFilter, activeStatusFilter, activeRatingFilter, activeGenreFilter, activeYearFilter), mainStructure);

        // Livefilterung bei Texteingabe
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadMediaFromDatabase(activeCategoryFilter, activeStatusFilter, activeRatingFilter, activeGenreFilter, activeYearFilter);
        });

        // Dialog für Hinzufügen
        btnAddMedia.setOnAction(event -> new AddMediaDialog(
                repository, () -> loadMediaFromDatabase(activeCategoryFilter, activeStatusFilter, activeRatingFilter, activeGenreFilter, activeYearFilter)).show());

        splitPane.getItems().addAll(mediaTable);
        this.getChildren().addAll(overviewHeader, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Konfiguration der Tabellenspalten
        TableColumn<Medium, String> colTitle = new TableColumn<>("Titel");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(150);
        colTitle.setResizable(false);

        TableColumn<Medium, String> colYear = new TableColumn<>("Jahr");
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colYear.setPrefWidth(50);
        colYear.setResizable(false);

        // Dynamisches Statuslabel je nach Medientyp
        TableColumn<Medium, String> colStatus= new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusLabel()));
        colStatus.setPrefWidth(100);
        colStatus.setResizable(false);

        TableColumn<Medium, String> colType= new TableColumn<>("Typ");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(100);
        colType.setResizable(false);

        TableColumn<Medium, String> colCreator = new TableColumn<>("Ersteller");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        colCreator.setPrefWidth(100);
        colCreator.setResizable(false);

        TableColumn<Medium, String> colGenre = new TableColumn<>("Genre");
        colGenre.setCellValueFactory(data -> {
            Medium medium = data.getValue();
            List<String> genres = medium.getGenres();
            return new SimpleStringProperty(genres != null ? String.join(", ", genres) : "-");
        });
        colGenre.setPrefWidth(150);
        colGenre.setResizable(false);

        mediaTable.getColumns().addAll(colTitle, colYear, colStatus, colType, colCreator, colGenre);

        // Zeilenauswahl für Detailansicht
        mediaTable.setOnMouseClicked(event -> {
            Medium clicked = mediaTable.getSelectionModel().getSelectedItem();
            if (clicked != null) {
                detailView.showDetails(clicked);
                if (!splitPane.getItems().contains(detailView)) {
                    splitPane.getItems().add(detailView);
                    splitPane.setDividerPositions(0.6);
                }
            }
        });

        loadMediaFromDatabase(activeCategoryFilter, activeStatusFilter, activeRatingFilter, activeGenreFilter, activeYearFilter);
    }

    /**
     * Hilfsmethode für vereinfachte Filterung nach Kategorie und Status
     * @param categoryFilter Anzuzeigender Medientyp
     * @param statusFilter Anzuzeigender Status
     */
    public void loadMediaFromDatabase(String categoryFilter, String statusFilter) {
        loadMediaFromDatabase(categoryFilter, statusFilter, this.activeRatingFilter, this.activeGenreFilter, this.activeYearFilter);
    }

    /**
     * Gefilterte Daten aus Datenbank holen und Tabelle füllen
     * @param categoryFilter Medientyp-Filter
     * @param statusFilter Status-Filter
     * @param ratingFilter Bewertungs-Filter
     * @param genreFilter Genre-Filter
     * @param yearFilter Jahr-Filter
     */
    public void loadMediaFromDatabase(String categoryFilter, String statusFilter, String ratingFilter, String genreFilter, String yearFilter) {
        mediaTable.getItems().clear();
        String searchText = (searchField != null) ? searchField.getText() : "";

        // Lokale Filterzustände aktualisieren
        this.activeCategoryFilter = categoryFilter;
        this.activeStatusFilter = statusFilter;
        this.activeRatingFilter = ratingFilter;
        this.activeGenreFilter = genreFilter;
        this.activeYearFilter = yearFilter;

        // Ergebnisse in Tabelle hinzufügen
        mediaTable.getItems().addAll(repository.fetchMedia(categoryFilter, statusFilter, ratingFilter, genreFilter, yearFilter, searchText));
    }

    /**
     * Schließen der Detailansicht
     */
    public void closeDetailView() {
        if (splitPane.getItems().contains(detailView)) {
            splitPane.getItems().remove(detailView);
        }
    }
}
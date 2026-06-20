import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Sidebar der Anwendung.
 * Stellt permanente Steuerungselemente zur Verfügung wie nach Medientyp oder Status schnell filtern,
 * Haupttabelle und Statistiken aufzurufen und Genres zu verwalten.
 */
public class Sidebar extends VBox {
    private MainStructure main;
    private Overview overview;

    /**
     * Konstruktor zur Erstellung der Sidebar
     * Initialisierung und Verknüpfung der Navigations-Buttons
     * @param main Koordinator für Ansichtenwechsel
     * @param overview Tabellenübersicht
     */
    public Sidebar(MainStructure main, Overview overview) {
        this.main = main;
        this.overview = overview;
        this.getStyleClass().add("sidebar");

        // Überschrift der Anwendung
        Label lblHeader = new Label("MediaVault");
        lblHeader.getStyleClass().add("sidebar-label-header");

        // Überblick-Button zeigt komplette Tabelle
        Button btnOverview = new Button("Überblick");
        btnOverview.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Alle", "Alle");
        });

        // Backlog-Button zeigt alle Einträge mit Status "Offen"
        Button btnBacklog = new Button("Backlog");
        btnBacklog.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Alle", "Offen");
        });

        // Statistik-Button zeigt Statistikansicht
        Button btnStatistic = new Button("Statistik");
        btnStatistic.setOnAction(event -> main.showStatistic());

        // Verschiedene Buttons für schnelle Typfilterung
        Label lblCategories = new Label("Kategorien");
        lblCategories.getStyleClass().add("sidebar-label-categories");

        Button btnFilms = new Button("Filme");
        Button btnShows = new Button("Serien");
        Button btnBooks = new Button("Bücher");
        Button btnGames = new Button("Videospiele");
        Button btnAlbums= new Button("Alben");

        btnAlbums.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Album", "Alle");
        });

        btnBooks.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Buch", "Alle");
        });

        btnFilms.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Film", "Alle");
        });

        btnShows.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Serie", "Alle");
        });

        btnGames.setOnAction(event -> {
            main.showOverview();
            overview.loadMediaFromDatabase("Videospiel", "Alle");
        });

        // Spacer, um Genre verwalten Button unten anzuheften
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnManageGenres = new Button("Genres verwalten");
        btnManageGenres.setOnAction(event -> {
            Stage owner = (Stage) this.getScene().getWindow();
            new ManageGenresDialog(owner, new MediaRepository()).show();
        });

        this.getChildren().addAll(
                lblHeader, btnOverview, btnBacklog,
                btnStatistic, lblCategories, btnAlbums,
                btnBooks, btnFilms, btnShows, btnGames,
                spacer, btnManageGenres);
    }
}

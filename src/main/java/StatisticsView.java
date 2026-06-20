import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Map;

/**
 * Visualisierung von Statistiken.
 * Zeigt Anzahl der Medientypen in der DB sowie die Anzahl,
 * welche den Status "Abgeschlossen" haben.
 * Zeigt außerdem die Anzahl von vergebenen Sterne-Bewertungen
 */
public class StatisticsView extends VBox {
    // Anzahl Medientypen
    private Label lblFilmCount = new Label();
    private Label lblWatchedFilmCount = new Label();
    private Label lblShowCount = new Label();
    private Label lblWatchedShowCount = new Label();
    private Label lblBookCount = new Label();
    private Label lblReadBookCount = new Label();
    private Label lblGameCount = new Label();
    private Label lblPlayedGameCount = new Label();
    private Label lblAlbumCount = new Label();
    private Label lblHeardAlbumCount = new Label();

    // Anzahl Bewertungen
    private Label lbl5StarCount = new Label();
    private Label lbl4StarCount = new Label();
    private Label lbl3StarCount = new Label();
    private Label lbl2StarCount = new Label();
    private Label lbl1StarCount = new Label();

    /**
     * Konstruktor zur Erstellung der Statistik-Ansicht
     */
    public StatisticsView() {
        this.getStyleClass().add("statistics-view");
        this.setAlignment(Pos.TOP_LEFT);

        // Header für Titel
        HBox titleHBox = new HBox();
        titleHBox.getStyleClass().add("statistics-header");
        titleHBox.setAlignment(Pos.CENTER);
        titleHBox.setMaxWidth(Double.MAX_VALUE);

        Label statisticTitle = new Label("Statistik");
        statisticTitle.setAlignment(Pos.CENTER);
        statisticTitle.getStyleClass().add("statistics-title");
        titleHBox.getChildren().add(statisticTitle);

        Label mediaTitle = new Label("Medientypen");
        mediaTitle.getStyleClass().add("statistics-media-title");
        Label ratingTitle = new Label("Bewertungen");
        ratingTitle.getStyleClass().add("statistics-rating-title");
        VBox.setMargin(ratingTitle, new Insets(20, 0, 0, 0)); // Abstand nach oben

        this.getChildren().addAll(titleHBox, mediaTitle, lblFilmCount, lblWatchedFilmCount, lblShowCount, lblWatchedShowCount,
                lblBookCount, lblReadBookCount, lblGameCount, lblPlayedGameCount, lblAlbumCount,
                lblHeardAlbumCount, ratingTitle, lbl5StarCount, lbl4StarCount, lbl3StarCount, lbl2StarCount, lbl1StarCount);
    }

    /**
     * Aktualisiert Textinhalte basierend auf übergebenen Daten
     * Fallback 0, wenn Key fehlt
     * @param stats Map mit Statistikzahlen aus Datenbank
     */
    public void refreshStatistics(Map<String, Integer> stats) {
        // Werte für Filme
        lblFilmCount.setText("Anzahl Filme \uD83C\uDFAC: " + stats.getOrDefault("Film", 0));
        lblWatchedFilmCount.setText("Davon gesehen: " + stats.getOrDefault("Film_Abgeschlossen", 0));
        lblWatchedFilmCount.getStyleClass().add("statistics-sub-label");

        // Werte für Serien
        lblShowCount.setText("Anzahl Serien \uD83D\uDCFA: " + stats.getOrDefault("Serie", 0));
        lblWatchedShowCount.setText("Davon gesehen: " + stats.getOrDefault("Serie_Abgeschlossen", 0));
        lblWatchedShowCount.getStyleClass().add("statistics-sub-label");

        // Werte für Bücher
        lblBookCount.setText("Anzahl Bücher \uD83D\uDCDA: " + stats.getOrDefault("Buch", 0));
        lblReadBookCount.setText("Davon gelesen: " + stats.getOrDefault("Buch_Abgeschlossen", 0));
        lblReadBookCount.getStyleClass().add("statistics-sub-label");

        // Werte für Spiele
        lblGameCount.setText("Anzahl Videospiele \uD83C\uDFAE: " + stats.getOrDefault("Videospiel", 0));
        lblPlayedGameCount.setText("Davon gespielt: " + stats.getOrDefault("Videospiel_Abgeschlossen", 0));
        lblPlayedGameCount.getStyleClass().add("statistics-sub-label");

        // Werte für Alben
        lblAlbumCount.setText("Anzahl Alben \uD83D\uDCBF: " + stats.getOrDefault("Album", 0));
        lblHeardAlbumCount.setText("Davon gehört: " + stats.getOrDefault("Album_Abgeschlossen", 0));
        lblHeardAlbumCount.getStyleClass().add("statistics-sub-label");

        // Werte für Sterne-Bewertungen
        lbl5StarCount.setText("Anzahl von ★★★★★ Sterne Bewertungen: " + stats.getOrDefault("5", 0));
        lbl4StarCount.setText("Anzahl von ★★★★ Sterne Bewertungen: " + stats.getOrDefault("4", 0));
        lbl3StarCount.setText("Anzahl von ★★★ Sterne Bewertungen: " + stats.getOrDefault("3", 0));
        lbl2StarCount.setText("Anzahl von ★★ Sterne Bewertungen: " + stats.getOrDefault("2", 0));
        lbl1StarCount.setText("Anzahl von ★ Sterne Bewertungen: " + stats.getOrDefault("1", 0));
    }
}
import javafx.scene.layout.BorderPane;
import java.util.Map;

/**
 * Zentrale Layout-Struktur
 * Verwaltet Wechsel zwischen verschiedenen Ansichten
 * Sidebar bleibt dabei dauerhaft links
 */
public class MainStructure extends BorderPane {
    private Overview overview;
    private StatisticsView statsView;
    private MediaRepository repository;

    /**
     * Gibt Hauptübersicht zurück
     * Ermöglicht anderen Dialogen darauf zuzugreifen
     * @return Aktuelle Overview Instanz
     */
    public Overview getOverview() {
        return this.overview;
    }

    /**
     * Konstruktor zur Initialisierung der Hauptoberfläche
     * Erstellung der Hauptkomponenten
     * Aufbauen des grundlegenden Layouts mit Sidebar links und Tabelle mittig
     */
    public MainStructure() {
        this.overview = new Overview(this);
        this.statsView = new StatisticsView();
        this.repository = new MediaRepository();

        Sidebar sidebar = new Sidebar(this, overview);
        this.setLeft(sidebar); // Sidebar links

        this.setCenter(overview); // Überblick (Tabelle) mittig
    }

    /**
     * Zentrum der Anwendung auf Überblick umschalten
     */
    public void showOverview() {
        this.setCenter(overview);
    }

    /**
     * Zentrum der Anwendung auf Eintragsansicht umschalten
     * Bei Aufruf wird eine neue EntryView mit den aktuellen Daten erstellt
     * @param medium Anzuzeigendes Medium-Objekt mit Daten
     */
    public void showEntryView(Medium medium) {
        EntryView entryView = new EntryView(medium, this);
        this.setCenter(entryView);
    }

    /**
     * Zentrum der Anwendung auf Statistikansicht umschalten
     * Daten aus Repository laden und Statistik-Komponente aktualisieren
     */
    public void showStatistic() {
        Map<String, Integer> stats = repository.fetchStatistics(); // Aktuelle Daten aus Datenbank abrufen
        statsView.refreshStatistics(stats); // Neue Werte aktualisieren
        this.setCenter(statsView);
    }
}

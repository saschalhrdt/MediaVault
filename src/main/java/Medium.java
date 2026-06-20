import javafx.scene.image.Image;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Zentrales Datenmodell für ein Medium
 * Klasse kapselt Eigenschaften eines Eintrags,
 * sowie Methoden zur Formatierung der Statuslabels,
 * Sterne-Bewertungen und Laden von Cover-Bildern
 */
public class Medium {

    private String title;
    private int year;
    private String type;
    private String creator;
    private String status;
    private int rating;
    private String imagePath;
    private String notes;
    private List<String> genres;

    /**
     * Konstruktor zur Erstellung eines Medium-Objektes
     * @param title Titel des Mediums
     * @param year Erscheinungsjahr
     * @param type Typ ("Film", "Serie", "Buch", "Videospiel" oder "Album")
     * @param creator Ersteller ("Regisseur", "Autor", "Entwickler", "Band" etc.)
     * @param status Status ("Offen" oder "Abgeschlossen")
     * @param rating Bewertung (1-5)
     * @param imagePath Dateiname des Cover-Bildes im lokalen Dateisystem
     * @param notes Notizen
     * @param genres Liste der zugeordneten Genres
     */
    public Medium(String title, int year, String type, String creator, String status, int rating,
                  String imagePath, String notes, List<String> genres) {
        this.title = title;
        this.year = year;
        this.type = type;
        this.creator = creator;
        this.status = status;
        this.rating = rating;
        this.imagePath = imagePath;
        this.notes = notes;
        this.genres = genres != null ? genres : new ArrayList<>();
    }

    // Getter und Setter
    public String getTitle() { return title; }

    public int getYear() { return year; }

    public String getType() { return type; }

    public String getCreator() { return creator; }

    public String getStatus() { return status; }

    public int getRating() { return rating; }

    public String getImagePath() { return imagePath; }

    public String getNotes() { return notes; }

    public List<String> getGenres() { return genres; }

    public void setStatus(String status) { this.status = status; }

    public void setGenres(List<String> genres) {
        this.genres = genres != null ? genres : new ArrayList<>();
    }

    /**
     * Dynamische Bezeichnung für Status "Abgeschlossen" je nach Medientyp
     * @return Passender Begriff für den jeweiligen Medientypen
     */
    public String getStatusLabel() {
        if("Abgeschlossen".equals(status)) {
            switch (type) {
                case "Film": return "Gesehen";
                case "Serie": return "Gesehen";
                case "Buch": return "Gelesen";
                case "Videospiel": return "Gespielt";
                case "Album": return "Gehört";
            }
        }
        return status;
    }

    /**
     * Konvertiert Bewertung in visuelle Sterne um
     * @param rating Numerische Bewertung als Zahl
     * @return String aus ausgefüllten und leeren Sternen
     */
    public static String convertToStars(int rating) {
        if (rating < 1) return "N/A";

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) stars.append("★");
        for (int i = rating; i < 5; i++) stars.append("☆");
        return stars.toString();
    }


    /**
     * Lädt Cover-Bild aus lokalem "covers"-Ordner.
     * Wenn der Pfad leer ist oder die Datei nicht gefunden wird, wird ein
     * Platzhalter genutzt.
     * @param width Breite des Bildes
     * @param height Höhe des Bildes
     * @return Platzhalter oder null, wenn der Platzhalter nicht existiert
     */
    public Image getCoverImage(double width, double height) {
        // Pfad zu Ordner für Bilder definieren
        String projectPath = System.getProperty("user.dir");
        File coversFolder = new File(projectPath + File.separator + "covers");

        // Prüfen, ob Cover eingetragen wurde und ob Datei existiert
        if (this.imagePath != null && !this.imagePath.trim().isEmpty()) {
            File imageFile = new File(coversFolder, this.imagePath.trim());
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), width, height, true, true);
            }
        }

        InputStream placeholderStream = getClass().getResourceAsStream("/placeholder.jpg");
        if (placeholderStream != null) {
            return new Image(placeholderStream, width, height, true, true);
        }
        return null;
    }
}


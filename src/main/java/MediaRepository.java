import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datenzugriffsschicht der Anwendung.
 * Verwaltet die Datenbankoperationen, regelt n:m-Beziehung zwischen Medien und Genres
 * und sichert Schreibvorgänge
 */
public class MediaRepository {

    /**
     * Stellt Verbindung zu lokaler SQLite-Datenbank her
     * und aktiviert die Unterstützung für Foreign-Key-Constraints
     * (ON DELETE CASCADE)
     * @return Verbindung zur Datenbankdatei
     * @throws SQLException Fehlermeldung bei fehlerhaftem Verbindungsaufbau
     */
    private Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/media_vault.db");
        Statement stmt = conn.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");

        return conn;
    }

    /**
     * Holt Genres, die einem bestimmten Medientypen zugeordnet sind.
     * Rückgabeliste ist alphabetisch aufsteigend sortiert.
     * @param mediaType Filtertyp (z.B. "Film")
     * @return Sortierte Liste der Genres
     */
    public List<String> getGenresForType(String mediaType) {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT name FROM genres WHERE medien_typ = ? ORDER BY name ASC";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Parameter setzen und Query ausführen
            pstmt.setString(1, mediaType);
            try (ResultSet rs = pstmt.executeQuery()) {
                // Ergebnisse pro Zeile auslesen und in Liste übertragen
                while (rs.next()) {
                    genres.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    /**
     * Holt Genres, die einem einzelnen Medium zugeordnet sind über dessen Titel.
     * Löst n:m-Beziehung über Hilfstabelle "medien_genres"
     * @param title Titel des gesuchten Mediums
     * @return Liste der zugeordneten Genre-Namen
     */
    public List<String> getGenresForMedium(String title) {
        List<String> genres = new ArrayList<>();
        /* Verknüpft Genres über die Hilfstabelle mit den Medien,
           um die zugehörigen Genres für den jeweiligen Titel zu finden */
        String sql = "SELECT g.name FROM genres g " +
                     "JOIN medien_genres mg on g.id = mg.genre_id " +
                     "JOIN medien m ON m.id = mg.medium_id " +
                     "WHERE m.titel = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    genres.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    /**
     * Ruft Medienliste dynamisch nach Filtern und Suchfeld ab
     * SQL-Abfrage wird je nach gewählten Parametern zusammengebaut
     * @param categoryFilter Filter für Medientyp
     * @param statusFilter Filter für Status
     * @param ratingFilter Filter für Bewertung
     * @param genreFilter Filter für Genre
     * @param yearFilter Filter für Jahr
     * @param searchQuery Suchbegriff für Freitextsuche (Titel oder Ersteller)
     * @return Gefilterte Liste
     */
    public List<Medium> fetchMedia(String categoryFilter, String statusFilter, String ratingFilter, String genreFilter, String yearFilter, String searchQuery) {
        List<Medium> mediaList = new ArrayList<>();

        // Basisquery mit Joins auf Genre-Tabellen für Genrefilter
        StringBuilder query = new StringBuilder(
                "SELECT DISTINCT m.titel, m.jahr, m.typ, m.ersteller, m.status, m.bewertung, m.bild_pfad, m.notizen " +
                        "FROM medien m " +
                        "LEFT JOIN medien_genres mg ON m.id = mg.medium_id " +
                        "LEFT JOIN genres g ON mg.genre_id = g.id " +
                        "WHERE 1=1" // Vereinfacht Anhängen der Filter
        );

        // SQl-Erweiterung je nach Filtern
        if (!categoryFilter.equals("Alle")) {
            query.append(" AND m.typ = '").append(categoryFilter).append("'");
        }
        if (!statusFilter.equals("Alle")) {
            query.append(" AND m.status = '").append(statusFilter).append("'");
        }
        if (!genreFilter.equals("Alle")) {
            query.append(" AND g.name = '").append(genreFilter).append("'");
        }
        if (!yearFilter.equals("Alle")) {
            query.append(" AND m.jahr = '").append(yearFilter).append("'");
        }
        if (!ratingFilter.equals("Alle")) {
            // Wandelt ausgewählten Bewertungsstring in Nummer um für die Query
            String numericRating = ratingFilter.replaceAll("[^1-5]", "");
            if (!numericRating.isEmpty()) {
                query.append(" AND m.bewertung = ").append(numericRating);
            }
        }

        // Query für Freitextsuche
        boolean hasSearch = searchQuery != null && !searchQuery.trim().isEmpty();
        if (hasSearch) {
            query.append(" AND (m.titel LIKE ? OR m.ersteller LIKE ?)");
        }

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            // Wildcards für Freitextsuche
            if (hasSearch) {
                String wildCardQuery = "%" + searchQuery.trim() + "%";
                pstmt.setString(1, wildCardQuery);
                pstmt.setString(2, wildCardQuery);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mediaList.add(extractMediumFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mediaList;
    }

    /**
     * Speicherung eines neuen Eintrags mit zugeordneten Genres.
     * Bei fehlschlagender Genrespeicherung wird Eintrag auch nicht gespeichert
     * @param title Titel des Mediums
     * @param year Erscheinungsjahr
     * @param type Medientyp
     * @param creator Ersteller
     * @param status Status
     * @param rating Bewertung
     * @param imagePath Dateiname des Cover-Bildes
     * @param notes Notizen
     * @param selectedGenres Liste der ausgewählten Genres
     */
    public void saveMedia(String title, int year, String type, String creator, String status, int rating, String imagePath, String notes, List<String> selectedGenres) {
        String insertMediaSql = "INSERT INTO medien (titel, jahr, typ, ersteller, status, bewertung, bild_pfad, notizen) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertRelationSql = "INSERT INTO medien_genres (medium_id, genre_id) VALUES(?, (SELECT id FROM genres WHERE name = ?))";

        Connection conn = null;
        try {
            conn = this.connect();
            conn.setAutoCommit(false); // Transaktion starten, damit alles oder nichts gespeichert wird

            // Medium einfügen und generierte ID abfangen
            try (PreparedStatement pstmt = conn.prepareStatement(insertMediaSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, title);
                pstmt.setInt(2, year);
                pstmt.setString(3, type);
                pstmt.setString(4, creator);
                pstmt.setString(5, status);
                pstmt.setInt(6, rating);
                pstmt.setString(7, imagePath);
                pstmt.setString(8, notes);
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long mediumId = generatedKeys.getLong(1);

                        // n:m-Beziehungen einfügen
                        try (PreparedStatement pstmtRelation = conn.prepareStatement(insertRelationSql)) {
                            for (String genre : selectedGenres) {
                                pstmtRelation.setLong(1, mediumId);
                                pstmtRelation.setString(2, genre);
                                pstmtRelation.executeUpdate();
                            }
                        }
                    }
                }
            }
            conn.commit(); // Transaktion erfolgreich abschließen
            System.out.println("Medium und Genres erfolgreich hinzugefügt.");
        } catch (SQLException e) {
            if (conn != null) { // Rollback im Fehlerfall
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    /**
     * Löscht einen Eintrag dauerhaft aus der Datenbank anhand seines Titels
     * @param title Titel des zu löschenden Mediums
     */
    public void deleteMedia(String title) {
        String sql = "DELETE FROM medien WHERE titel = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hilfsmethode zum Extrahieren der Daten aus einer Zeile des ResultSets.
     * Transformiert diese dann in ein Medium-Objekt
     * @param rs ResultSet an Cursor-Position
     * @return Medium-Objekt
     * @throws SQLException Fehlermeldung
     */
    private Medium extractMediumFromResultSet(ResultSet rs) throws SQLException {
        String title = rs.getString("titel");
        List<String> genres = getGenresForMedium(title);

        return new Medium(
                rs.getString("titel"),
                rs.getInt("jahr"),
                rs.getString("typ"),
                rs.getString("ersteller"),
                rs.getString("status"),
                rs.getInt("bewertung"),
                rs.getString("bild_pfad"),
                rs.getString("notizen"),
                genres
        );
    }

    /**
     * Generierung von Statistiken.
     * Zählt Gesamtzahl pro Medientyp,
     * Elemente mit Status "Abgeschlossen" sowie
     * Verteilung der Bewertungen
     * @return Map mit Kategorien und Summen
     */
    public Map<String, Integer> fetchStatistics() {
        Map<String, Integer> statistics = new HashMap<>();
        String typeSql = "SELECT typ, COUNT(*) AS anzahl FROM medien GROUP BY typ";
        String typeCompleted = "SELECT typ, COUNT(*) AS anzahl FROM medien WHERE status = 'Abgeschlossen' GROUP BY typ";
        String ratingSql = "SELECT bewertung, COUNT(*) AS anzahl FROM medien WHERE bewertung IS NOT NULL AND bewertung > 0 GROUP BY bewertung";

        try (Connection conn = this.connect()) {

            // Gesamtzahl pro Typ
            try (PreparedStatement pstmtType = conn.prepareStatement(typeSql);
                 ResultSet rsType = pstmtType.executeQuery()) {
                while (rsType.next()) {
                    String type = rsType.getString("typ");
                    int count = rsType.getInt("anzahl");
                    statistics.put(type, count);
                }
            }

            // Zahl pro Typ mit Status = "Abgeschlossen"
            try (PreparedStatement pstmtTypeCompleted = conn.prepareStatement(typeCompleted);
                 ResultSet rsType = pstmtTypeCompleted.executeQuery()) {
                while (rsType.next()) {
                    String type = rsType.getString("typ");
                    int count = rsType.getInt("anzahl");
                    statistics.put(type + "_Abgeschlossen", count);
                }
            }

            // Zählen der Bewertungen
            try (PreparedStatement pstmtRating = conn.prepareStatement(ratingSql);
                 ResultSet rsRating = pstmtRating.executeQuery()) {
                while (rsRating.next()) {
                    int rating = rsRating.getInt("bewertung");
                    int count = rsRating.getInt("anzahl");
                    statistics.put(String.valueOf(rating), count);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statistics;
    }

    /**
     * Aktualisierung der Daten eines Eintrags sowie die Genre-Zuweisung per
     * Hilfstabelle.
     * @param oldTitle Ursprünglicher Titel bei Änderung
     * @param title Neuer oder unveränderter Titel
     * @param year Erscheinungsjahr
     * @param type Medientyp
     * @param creator Ersteller
     * @param status Status
     * @param rating Bewertung
     * @param imagePath Dateiname des Cover-Bildes
     * @param notes Notizen
     * @param selectedGenres Liste der ausgewählten Genres
     */
    public void updateMedia(String oldTitle, String title, int year, String type, String creator, String status, int rating, String imagePath, String notes, List<String> selectedGenres) {
        String updateMediaSql = "UPDATE medien SET titel = ?, jahr = ?, typ = ?, ersteller = ?, status = ?, bewertung = ?, bild_pfad = ?, notizen = ? WHERE titel = ?";
        String deleteRelationsSql = "DELETE FROM medien_genres WHERE medium_id = (SELECT id FROM medien WHERE titel = ?)";
        String insertRelationSql = "INSERT INTO medien_genres (medium_id, genre_id) VALUES((SELECT id FROM medien WHERE titel = ?), (SELECT id FROM genres WHERE name = ?))";

        Connection conn = null;
        try {
            conn = this.connect();
            conn.setAutoCommit(false); // Transaktionssicherung

            // Daten des Mediums aktualisieren
            try (PreparedStatement pstmt = conn.prepareStatement(updateMediaSql)) {
                pstmt.setString(1, title);
                pstmt.setInt(2, year);
                pstmt.setString(3, type);
                pstmt.setString(4, creator);
                pstmt.setString(5, status);
                pstmt.setInt(6, rating);
                pstmt.setString(7, imagePath);
                pstmt.setString(8, notes);
                pstmt.setString(9, oldTitle);
                pstmt.executeUpdate();
            }

            // Alte Genre Verknüpfungen löschen
            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteRelationsSql)) {
                pstmtDelete.setString(1, title);
                pstmtDelete.executeUpdate();
            }

            // Neue Genre Verknüpfungen einfügen
            try (PreparedStatement pstmtRelation = conn.prepareStatement(insertRelationSql)) {
                for (String genre : selectedGenres) {
                    pstmtRelation.setString(1, title);
                    pstmtRelation.setString(2, genre);
                    pstmtRelation.executeUpdate();
                }
            }

            conn.commit(); // Transaktion abschließen
        } catch (SQLException e) {
            if (conn != null) { // Bei Fehlerfall Rollback einleiten
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    /**
     * Ermöglicht es, Genres nach einer Duplikatprüfung für einen
     * Medientypen hinzuzufügen
     * @param name Name des Genres
     * @param mediaType Zugeordneter Medientyp
     * @return true, wenn das Genre erfolgreich angelegt werden konnte
     */
    public boolean addGenre(String name, String mediaType) {
        String checkSql = "SELECT COUNT(*) FROM genres WHERE name = ? AND medien_typ = ?";
        String insertSql = "INSERT INTO genres (name, medien_typ) VALUES (?, ?)";

        try (Connection conn = this.connect()) {
            // Prüfung, ob Kombination aus Name und Typ bereits existiert
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, name);
                checkStmt.setString(2, mediaType);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Abbruch, wenn Genre bereits existiert
                    }
                }
            }

            // Einfügevorgang
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, name);
                insertStmt.setString(2, mediaType);
                insertStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Löscht ein Genre aus der Liste.
     * Zuerst aus der Hilfstabelle, danach aus der Genretabelle
     * @param name Name des Genres
     * @param mediaType Zugeordneter Medientyp
     */
    public void deleteGenre(String name, String mediaType) {
        String deleteRelationSql = "DELETE FROM medien_genres WHERE genre_id = (SELECT id FROM genres WHERE name = ? AND medien_typ = ?)";
        String deleteGenreSql = "DELETE FROM genres WHERE name = ? AND medien_typ = ?";

        try (Connection conn = this.connect()) {
            conn.setAutoCommit(false);
            try {
                // Relation auflösen
                try (PreparedStatement pstmtRel = conn.prepareStatement(deleteRelationSql)) {
                    pstmtRel.setString(1, name);
                    pstmtRel.setString(2, mediaType);
                    pstmtRel.executeUpdate();
                }
                // Genre aus Genretabelle löschen
                try (PreparedStatement pstmtGenre = conn.prepareStatement(deleteGenreSql)) {
                    pstmtGenre.setString(1, name);
                    pstmtGenre.setString(2, mediaType);
                    pstmtGenre.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback(); // Bei Fehler zurückdrehen
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


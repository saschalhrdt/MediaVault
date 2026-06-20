Media Vault ist eine Anwendung zur Verwaltung persönlicher Medienbestände  
(Filme, Serien, Bücher, Videospiele und Musikalben).  
Zur Umsetzung wurden JavaFX und SQLite genutzt.  

Anleitung:    


Datenbank:    
Die von dem Nutzer hinzugefügten Einträge mit ihren Daten werden in der lokalen media_vault-Datenbank  
im Data-Ordner gespeichert. Die Datenbank selbst kommt mit bereits voreingefügten Genres  
für jeden Typen, die auch gelöscht werden können.  

Anwendung:  
Um die Anwendung selbst zu Starten muss die Klasse AppLauncher ausgeführt werden.  

Cover:  
Um Cover-Bilder zu einem Eintrag hinzuzufügen, müssen  
diese im covers-Ordner gespeichert werden und beim Hinzufügen/Bearbeiten  
muss der gesamte Dateiname inklusive Bildformat eingegeben werden (Bspw: "film_cover.jpg").  


Technische Anforderungen:  

- JavaFX   
- Maven  
- SQLite  
- CSS (im ressources-Ordner)  

Folgende funktionale Anforderungen wurden umgesetzt:  

Verwaltung:   

- Medien hinzufügen, bearbeiten und löschen  
- Genres verwalten (hinzufügen und löschen)  
- Detailansicht eines Mediums anzeigen (zwei Ansichten)  

Anzeige:  

- Tabellarische Übersicht  
- Sortieren anhand der Tabelle, Filtern nach Typ, Status, Bewertung, Genre (je nach Typ) und Jahr  
- Ebenso wurde die optionale Freitextsuche implementiert  

Bewertung:  

- Bewertungssystem; Anzeige per Sterne  
- Anzeige des Status, dynamisch anpassend an Medientypen  

Andere Erweiterungen:  

- Laden von lokalen Coverbildern möglich  
- Statistiken (Anzahl pro Typ sowie Anzahl von 1-5 Sterne Bewertungen)  

package com.nicohoffmann.auftragserfassung.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entitätsklasse für einen Arbeitseintrag in der Datenbank.
 * Repräsentiert einen Eintrag in der Tabelle "eintraege" mit
 * einer optionalen Verknüpfung zu einer Baustelle.
 * @author Nico Hoffmann
 * @version 1.0
 */
@Entity(
        tableName = "eintraege",
        foreignKeys = @ForeignKey(
                entity = Baustelle.class,
                parentColumns = "id",
                childColumns = "baustelleId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("baustelleId")}
)
public class Eintrag {

    // ====================================
    // Instance Variables
    // ====================================

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String datum;
    private String zeitVon;
    private String zeitBis;
    private int pauseMinuten;
    private String beschreibung;
    private Integer baustelleId;

    // ====================================
    // Getter Methods
    // ====================================

    public int getId() { return id; }
    public String getDatum() { return datum; }
    public String getZeitVon() { return zeitVon; }
    public String getZeitBis() { return zeitBis; }
    public int getPauseMinuten() { return pauseMinuten; }
    public String getBeschreibung() { return beschreibung; }
    public Integer getBaustelleId() { return baustelleId; }

    // ====================================
    // Utility Methods
    // ====================================

    public void setId(int id) { this.id = id; }
    public void setDatum(String datum) { this.datum = datum; }
    public void setZeitVon(String zeitVon) { this.zeitVon = zeitVon; }
    public void setZeitBis(String zeitBis) { this.zeitBis = zeitBis; }
    public void setPauseMinuten(int pauseMinuten) { this.pauseMinuten = pauseMinuten; }
    public void setBeschreibung(String beschreibung) { this.beschreibung = beschreibung; }
    public void setBaustelleId(Integer baustelleId) { this.baustelleId = baustelleId; }
}

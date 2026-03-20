package com.nicohoffmann.auftragserfassung.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entitätsklasse für eine Baustelle in der Datenbank.
 * Repräsentiert einen Eintrag in der Tabelle "baustellen".
 * @author Nico Hoffmann
 * @version 1.0
 */
@Entity(tableName = "baustellen")
public class Baustelle {

    // ====================================
    // Instance Variables
    // ====================================

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String adresse;
    private boolean isFavorit;

    // ====================================
    // Constructors
    // ====================================

    public Baustelle(String name, String adresse, boolean isFavorit) {
        this.name = name;
        this.adresse = adresse;
        this.isFavorit = isFavorit;
    }

    // ====================================
    // Getter Methods
    // ====================================

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAdresse() { return adresse; }
    public boolean isFavorit() { return isFavorit; }

    // ====================================
    // Utility Methods
    // ====================================

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setFavorit(boolean favorit) { isFavorit = favorit; }
}

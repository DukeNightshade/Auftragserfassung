package com.nicohoffmann.auftragserfassung.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "baustellen")
public class Baustelle {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String adresse;
    public boolean isFavorit;

    public Baustelle(String name, String adresse, boolean isFavorit) {
        this.name = name;
        this.adresse = adresse;
        this.isFavorit = isFavorit;
    }
}


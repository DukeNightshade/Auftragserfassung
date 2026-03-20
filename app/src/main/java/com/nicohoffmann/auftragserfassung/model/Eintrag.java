package com.nicohoffmann.auftragserfassung.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "eintraege",
        foreignKeys = @ForeignKey(
                entity = Baustelle.class,
                parentColumns = "id",
                childColumns = "baustelleId",
                onDelete = ForeignKey.SET_NULL
        )
)

public class Eintrag {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String datum;
    public String zeitVon;
    public String zeitBis;
    public int pauseMinuten;
    public String beschreibung;
    public Integer baustelleId;
}

package com.nicohoffmann.auftragserfassung.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.util.List;

/**
 * DAO-Interface für den Datenbankzugriff auf Einträge.
 * Stellt CRUD-Operationen sowie Such- und Listenabfragen bereit.
 * @author Nico Hoffmann
 * @version 1.0
 */
@Dao
public interface EintragDao {

    // ====================================
    // Business Logic Methods
    // ====================================

    @Query("SELECT * FROM eintraege ORDER BY datum DESC")
    LiveData<List<Eintrag>> getAlleEintraege();

    @Query("SELECT * FROM eintraege WHERE beschreibung LIKE '%' || :suchbegriff || '%'")
    LiveData<List<Eintrag>> suche(String suchbegriff);

    @Query("SELECT * FROM eintraege WHERE id = :id LIMIT 1")
    Eintrag getById(int id);

    @Query("SELECT * FROM eintraege WHERE datum = :datum")
    List<Eintrag> getEintraegeByDatum(String datum);


    // ====================================
    // Utility Methods
    // ====================================

    @Insert
    void insert(Eintrag eintrag);

    @Update
    void update(Eintrag eintrag);

    @Delete
    void delete(Eintrag eintrag);
}

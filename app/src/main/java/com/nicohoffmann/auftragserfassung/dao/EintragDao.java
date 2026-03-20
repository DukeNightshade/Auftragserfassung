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

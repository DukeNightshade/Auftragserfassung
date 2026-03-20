package com.nicohoffmann.auftragserfassung.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import java.util.List;

/**
 * DAO-Interface für den Datenbankzugriff auf Baustellen.
 * Stellt CRUD-Operationen sowie spezifische Abfragen bereit.
 * @author Nico Hoffmann
 * @version 1.0
 */
@Dao
public interface BaustelleDao {

    // ====================================
    // Business Logic Methods
    // ====================================

    @Query("SELECT * FROM baustellen ORDER BY name ASC")
    LiveData<List<Baustelle>> getAlleBaustellen();

    @Query("SELECT * FROM baustellen WHERE isFavorit = 1 LIMIT 1")
    Baustelle getFavoritBaustelle();

    // ====================================
    // Utility Methods
    // ====================================

    @Insert
    void insert(Baustelle baustelle);

    @Update
    void update(Baustelle baustelle);

    @Delete
    void delete(Baustelle baustelle);
}

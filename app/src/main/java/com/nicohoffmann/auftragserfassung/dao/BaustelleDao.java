package com.nicohoffmann.auftragserfassung.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import java.util.List;

@Dao
public interface BaustelleDao {

    @Insert
    void insert(Baustelle baustelle);

    @Update
    void update(Baustelle baustelle);

    @Delete
    void delete(Baustelle baustelle);

    @Query("SELECT * FROM baustellen ORDER BY name ASC")
    LiveData<List<Baustelle>> getAlleBaustellen();

    @Query("SELECT * FROM baustellen WHERE isFavorit = 1 LIMIT 1")
    Baustelle getFavoritBaustelle();
}

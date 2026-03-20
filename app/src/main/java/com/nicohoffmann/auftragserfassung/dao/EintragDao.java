package com.nicohoffmann.auftragserfassung.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.util.List;

@Dao
public interface EintragDao {

    @Insert
    void insert(Eintrag eintrag);

    @Update
    void update(Eintrag eintrag);

    @Delete
    void delete(Eintrag eintrag);

    @Query("SELECT * FROM eintraege ORDER BY datum DESC")
    LiveData<List<Eintrag>> getAlleEintraege();

    @Query("SELECT * FROM eintraege WHERE beschreibung LIKE '%' || :suchbegriff || '%'")
    LiveData<List<Eintrag>> suche(String suchbegriff);
}

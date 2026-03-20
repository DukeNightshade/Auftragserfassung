package com.nicohoffmann.auftragserfassung.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.nicohoffmann.auftragserfassung.dao.BaustelleDao;
import com.nicohoffmann.auftragserfassung.dao.EintragDao;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;

/**
 * Zentrale Room-Datenbankklasse der Anwendung.
 * Verwaltet alle DAOs und stellt eine Singleton-Instanz bereit.
 * @author Nico Hoffmann
 * @version 1.0
 */
@Database(entities = {Baustelle.class, Eintrag.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // ====================================
    // Static Variables
    // ====================================

    private static final String DATABASE_NAME = "auftragserfassung_db";

    // ====================================
    // Business Logic Methods
    // ====================================

    public abstract BaustelleDao baustelleDao();
    public abstract EintragDao eintragDao();

    // ====================================
    // Utility Methods
    // ====================================

    public static AppDatabase getInstance(Context context) {
        DatabaseHolder.init(context);
        return DatabaseHolder.instance;
    }

    // ====================================
    // Inner Classes
    // ====================================

    private static class DatabaseHolder {
        private static AppDatabase instance;

        private static void init(Context context) {
            if (instance == null) {
                instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                ).build();
            }
        }
    }
}

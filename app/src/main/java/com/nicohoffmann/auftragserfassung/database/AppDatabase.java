package com.nicohoffmann.auftragserfassung.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.nicohoffmann.auftragserfassung.dao.BaustelleDao;
import com.nicohoffmann.auftragserfassung.dao.EintragDao;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;

@Database(entities = {Baustelle.class, Eintrag.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BaustelleDao baustelleDao();
    public abstract EintragDao eintragDao();

    private static class DatabaseHolder {
        private static AppDatabase instance;

        private static void init(Context context) {
            if (instance == null) {
                instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "auftragserfassung_db"
                ).build();
            }
        }
    }

    public static AppDatabase getInstance(Context context) {
        DatabaseHolder.init(context);
        return DatabaseHolder.instance;
    }
}

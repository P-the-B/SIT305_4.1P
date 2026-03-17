package com.example.eventplanner.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// ties the entity and DAO together to a database file on  device
@Database(entities = {Event.class}, version = 1, exportSchema = false)
public abstract class EventDatabase extends RoomDatabase {

    public abstract EventDao eventDao();

    private static EventDatabase instance;

    //only one DB connection open at a time, 'synchronized' keeps it thread-safe
    public static synchronized EventDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            EventDatabase.class,
                            "event_database"
                    )
                    // wipes and rebuilds if schema changes rather than crashing
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
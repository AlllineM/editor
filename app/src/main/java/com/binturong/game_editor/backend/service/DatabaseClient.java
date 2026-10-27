package com.binturong.game_editor.backend.service;

import androidx.room.Room;
import android.content.Context;

public class DatabaseClient {
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "snake_database"
            ).build();
        }
        return instance;
    }
}


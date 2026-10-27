package com.binturong.game_editor.backend.service;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.binturong.game_editor.backend.dao.LevelDao;
import com.binturong.game_editor.backend.entity.Level;

@Database(entities = {Level.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LevelDao levelDao();
}


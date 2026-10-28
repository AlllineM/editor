package com.binturong.game_editor.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.binturong.game_editor.backend.entity.Level;

import java.util.List;

@Dao
public interface LevelDao {

    @Query("SELECT * FROM level")
    List<Level> getAll();

    @Query("SELECT * FROM level WHERE id = :id")
    Level getById(long id);

    @Query("SELECT COUNT(*) FROM level")
    Integer getCount();

    @Insert
    void insert(Level employee);

    @Update
    void update(Level employee);

    @Delete
    void delete(Level employee);
}
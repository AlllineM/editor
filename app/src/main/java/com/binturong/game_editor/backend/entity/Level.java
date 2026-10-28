package com.binturong.game_editor.backend.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "level")
public class Level {
    @PrimaryKey
    public int id;

    public String name;
    public int author;
    public int startSpeed;
    public int targetScore;
    public int difficulty;
    public int rating;

    public String blocks;
    public String author_name;
}
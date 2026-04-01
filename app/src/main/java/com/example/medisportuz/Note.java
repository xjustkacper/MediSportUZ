package com.example.medisportuz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String content;
    public String category;   // "trening", "dieta", "inne"
    public long createdAt;    // timestamp milisekund
}
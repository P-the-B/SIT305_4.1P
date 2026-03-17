package com.example.eventplanner.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// create event table from events class
@Entity(tableName = "events")
public class Event {
    // auto ID for each event
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String category;   // e.g. Work, Social, Travel
    public String location;
    public long dateTimeMillis; // stored as epoch ms for easy sorting

    // Required by Room - empty constructor
    public Event() {}

    public Event(String title, String category, String location, long dateTimeMillis) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.dateTimeMillis = dateTimeMillis;
    }
}
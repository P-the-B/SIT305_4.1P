package com.example.eventplanner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {

    @Insert
    void insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    // ASC = earliest event first
    @Query("SELECT * FROM events ORDER BY dateTimeMillis ASC")
    List<Event> getAllEventsSortedByDate();

    // used when loading an event into the edit
    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    Event getEventById(int eventId);
}
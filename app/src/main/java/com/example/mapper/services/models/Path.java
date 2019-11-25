package com.example.mapper.services.models;
// import statements
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room Database entity for the com.example.mapper.models.Path model
 */

@Entity(tableName = "paths")
public class Path {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;

    public Path(){ }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
package com.example.mapper.services.models;

//import statements
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room Database entity for the com.example.mapper.models.Visit model
 */

@Entity(tableName = "visits")
public class Visit {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;
    private String title;
    private String description;
    private Date visitDate;
    private long pathId;


    public Visit(String title,String description, Date visitDate){
        this.title = title;
        this.description = description;
        this.visitDate = visitDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }
}
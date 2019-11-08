package com.example.mapper.services.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "visits")
public class Visit {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;
    private String title;
    private Date visitDate;
    private int pathId;


    public Visit(String title, Date visitDate, int pathId){
        this.title = title;
        this.visitDate = visitDate;
        this.pathId = pathId;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }
}
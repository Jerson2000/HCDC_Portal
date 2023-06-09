package com.jerson.hcdc_portal.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "dashboard")
public class DashboardModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String offerNo;
    private String googleClassCode;
    private String subjCode;
    private String description;
    private String unit;
    private String days;
    private String time;
    private String room;
    private String lecLab;

    public DashboardModel(String offerNo, String googleClassCode, String subjCode, String description, String unit, String days, String time, String room, String lecLab) {
        this.offerNo = offerNo;
        this.googleClassCode = googleClassCode;
        this.subjCode = subjCode;
        this.description = description;
        this.unit = unit;
        this.days = days;
        this.time = time;
        this.room = room;
        this.lecLab = lecLab;
    }


    public int getId() {
        return id;
    }

    public String getOfferNo() {
        return offerNo;
    }

    public String getGoogleClassCode() {
        return googleClassCode;
    }

    public String getSubjCode() {
        return subjCode;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public String getDays() {
        return days;
    }

    public String getTime() {
        return time;
    }

    public String getRoom() {
        return room;
    }

    public String getLecLab() {
        return lecLab;
    }
}

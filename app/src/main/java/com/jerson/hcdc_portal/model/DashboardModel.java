package com.jerson.hcdc_portal.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "dashboard",primaryKeys = "index")
public class DashboardModel {
    private int index;
    private String offerNo;
    private String googleClassCode;
    private String subjCode;
    private String description;
    private String unit;
    private String days;
    private String time;
    private String room;
    private String lecLab;

    public DashboardModel(int index,String offerNo, String googleClassCode, String subjCode, String description, String unit, String days, String time, String room, String lecLab) {
        this.index = index;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getOfferNo() {
        return offerNo;
    }

    public void setOfferNo(String offerNo) {
        this.offerNo = offerNo;
    }

    public String getGoogleClassCode() {
        return googleClassCode;
    }

    public void setGoogleClassCode(String googleClassCode) {
        this.googleClassCode = googleClassCode;
    }

    public String getSubjCode() {
        return subjCode;
    }

    public void setSubjCode(String subjCode) {
        this.subjCode = subjCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getLecLab() {
        return lecLab;
    }

    public void setLecLab(String lecLab) {
        this.lecLab = lecLab;
    }
}

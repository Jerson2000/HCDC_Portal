package com.jerson.hcdc_portal.model;

public class DashboardModel {
    private String offerNo;
    private String gClassCode;
    private String subjCode;
    private String description;
    private String unit;
    private String days;
    private String time;
    private String room;
    private String lecLab;

    public DashboardModel() {
    }

    public DashboardModel(String offerNo, String gClassCode, String subjCode, String description, String unit, String days, String time, String room, String lecLab) {
        this.offerNo = offerNo;
        this.gClassCode = gClassCode;
        this.subjCode = subjCode;
        this.description = description;
        this.unit = unit;
        this.days = days;
        this.time = time;
        this.room = room;
        this.lecLab = lecLab;
    }

    public String getOfferNo() {
        return offerNo;
    }

    public void setOfferNo(String offerNo) {
        this.offerNo = offerNo;
    }

    public String getgClassCode() {
        return gClassCode;
    }

    public void setgClassCode(String gClassCode) {
        this.gClassCode = gClassCode;
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

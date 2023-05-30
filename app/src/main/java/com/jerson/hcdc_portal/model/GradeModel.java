package com.jerson.hcdc_portal.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "grade")
public class GradeModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ForeignKey(entity = GradeModel.Link.class, parentColumns = "id", childColumns = "grade")
    private int link_id;
    private String code;
    private String subject;
    private String description;
    private String unit;
    private String midGrade;
    private String midRemark;
    private String finalGrade;
    private String finalRemark;
    private String teacher;
    private String earnedUnits;
    private String average;



    public GradeModel(String code, String subject, String description, String unit, String midGrade, String midRemark, String finalGrade, String finalRemark, String teacher,String earnedUnits, String average) {
        this.code = code;
        this.subject = subject;
        this.description = description;
        this.unit = unit;
        this.midGrade = midGrade;
        this.midRemark = midRemark;
        this.finalGrade = finalGrade;
        this.finalRemark = finalRemark;
        this.teacher = teacher;
        this.earnedUnits = earnedUnits;
        this.average = average;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLink_id() {
        return link_id;
    }

    public void setLink_id(int link_id) {
        this.link_id = link_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public String getMidGrade() {
        return midGrade;
    }

    public void setMidGrade(String midGrade) {
        this.midGrade = midGrade;
    }

    public String getMidRemark() {
        return midRemark;
    }

    public void setMidRemark(String midRemark) {
        this.midRemark = midRemark;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getFinalRemark() {
        return finalRemark;
    }

    public void setFinalRemark(String finalRemark) {
        this.finalRemark = finalRemark;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getEarnedUnits() {
        return earnedUnits;
    }

    public void setEarnedUnits(String earnedUnits) {
        this.earnedUnits = earnedUnits;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }


    @Entity(tableName = "gradelink")
    public static class Link{
        @PrimaryKey(autoGenerate = true)
        private int id;
        private String link;
        private String text;

        public Link(String link, String text) {
            this.link = link;
            this.text = text;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}

package com.jerson.hcdc_portal.model;

public class GradeModel {
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

    public GradeModel() {
    }

    public GradeModel(String code, String subject, String description, String unit, String midGrade, String midRemark, String finalGrade, String finalRemark, String teacher) {
        this.code = code;
        this.subject = subject;
        this.description = description;
        this.unit = unit;
        this.midGrade = midGrade;
        this.midRemark = midRemark;
        this.finalGrade = finalGrade;
        this.finalRemark = finalRemark;
        this.teacher = teacher;
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
}

package com.jerson.hcdc_portal.model;

public class GradeLinksModel {
    private String semGradeLink;
    private String semGradeText;

    public GradeLinksModel(String semGradeLink, String semGradeText) {
        this.semGradeLink = semGradeLink;
        this.semGradeText = semGradeText;
    }

    public String getSemGradeText() {
        return semGradeText;
    }

    public void setSemGradeText(String semGradeText) {
        this.semGradeText = semGradeText;
    }

    public String getSemGradeLink() {
        return semGradeLink;
    }

    public void setSemGradeLink(String semGradeLink) {
        this.semGradeLink = semGradeLink;
    }
}

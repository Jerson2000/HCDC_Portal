package com.jerson.hcdc_portal.model;

public class EnrollLinksModel {
    private String periodLink;
    private String periodText;

    public EnrollLinksModel(String periodLink, String periodText) {
        this.periodLink = periodLink;
        this.periodText = periodText;
    }

    public String getPeriodLink() {
        return periodLink;
    }

    public void setPeriodLink(String periodLink) {
        this.periodLink = periodLink;
    }

    public String getPeriodText() {
        return periodText;
    }

    public void setPeriodText(String periodText) {
        this.periodText = periodText;
    }
}

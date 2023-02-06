package com.jerson.hcdc_portal.model;

public class AccountLinksModel {
    private String semAccountLink;
    private String semAccountText;

    public AccountLinksModel(String semAccountLink, String semAccountText) {
        this.semAccountLink = semAccountLink;
        this.semAccountText = semAccountText;
    }

    public String getSemAccountLink() {
        return semAccountLink;
    }

    public void setSemAccountLink(String semAccountLink) {
        this.semAccountLink = semAccountLink;
    }

    public String getSemAccountText() {
        return semAccountText;
    }

    public void setSemAccountText(String semAccountText) {
        this.semAccountText = semAccountText;
    }
}

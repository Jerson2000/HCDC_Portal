package com.jerson.hcdc_portal.model;

public class EnrollHistModel {
    private String offerNo;
    private String subjCode;
    private String description;
    private String units;

    public EnrollHistModel(String offerNo, String subjCode, String description, String units) {
        this.offerNo = offerNo;
        this.subjCode = subjCode;
        this.description = description;
        this.units = units;
    }

    public String getOfferNo() {
        return offerNo;
    }

    public void setOfferNo(String offerNo) {
        this.offerNo = offerNo;
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

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}

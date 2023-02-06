package com.jerson.hcdc_portal.model;

public class AccountModel {
    private String date;
    private String reference;
    private String description;
    private String period;
    private String added;
    private String deducted;
    private String runBal;
    private String dueText;
    private String due;

    public AccountModel(String date, String reference, String description, String period, String added, String deducted, String runBal, String dueText, String due) {
        this.date = date;
        this.reference = reference;
        this.description = description;
        this.period = period;
        this.added = added;
        this.deducted = deducted;
        this.runBal = runBal;
        this.dueText = dueText;
        this.due = due;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    public String getDeducted() {
        return deducted;
    }

    public void setDeducted(String deducted) {
        this.deducted = deducted;
    }

    public String getRunBal() {
        return runBal;
    }

    public void setRunBal(String runBal) {
        this.runBal = runBal;
    }

    public String getDueText() {
        return dueText;
    }

    public void setDueText(String dueText) {
        this.dueText = dueText;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }
}

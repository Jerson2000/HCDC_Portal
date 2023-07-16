package com.jerson.hcdc_portal.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "enrollhist")
public class EnrollHistModel {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int link_id;
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

    @Ignore
    public EnrollHistModel() {
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

    public String getOfferNo() {
        return offerNo;
    }

    public String getSubjCode() {
        return subjCode;
    }

    public String getDescription() {
        return description;
    }

    public String getUnits() {
        return units;
    }

    @Entity(tableName = "enrollhistlink")
    public static class Link{
        @PrimaryKey(autoGenerate = true)
        private int id;
        private String periodLink;
        private String periodText;

        public Link(String periodLink, String periodText) {
            this.periodLink = periodLink;
            this.periodText = periodText;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPeriodLink() {
            return periodLink;
        }

        public String getPeriodText() {
            return periodText;
        }
    }
}

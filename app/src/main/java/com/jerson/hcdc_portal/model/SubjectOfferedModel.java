package com.jerson.hcdc_portal.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubjectOfferedModel {
    @SerializedName("data")
    private List<SubjectOffered> subjectOfferedList;

    public List<SubjectOffered> getSubjectOfferedList() {
        return subjectOfferedList;
    }

    public void setSubjectOfferedList(List<SubjectOffered> subjectOfferedList) {
        this.subjectOfferedList = subjectOfferedList;
    }

    static public class SubjectOffered {
        @SerializedName("so_no")
        private String offeredNo;
        @SerializedName("subject_code")
        private String subjectCode;
        @SerializedName("description")
        private String subject;
        @SerializedName("units")
        private String units;
        @SerializedName("course")
        private String course;
        @SerializedName("blocked_section")
        private String section;
        @SerializedName("maximum")
        private String maximum;
        @SerializedName("enrolled")
        private String enrolled;
        @SerializedName("remaining_slot")
        private String slot;
        @SerializedName("nop")
        private int totalPage;

        public SubjectOffered() {
        }

        public SubjectOffered(String offeredNo, String subjectCode, String subject, String units, String course, String section, String maximum, String enrolled, String slot, int totalPage) {
            this.offeredNo = offeredNo;
            this.subjectCode = subjectCode;
            this.subject = subject;
            this.units = units;
            this.course = course;
            this.section = section;
            this.maximum = maximum;
            this.enrolled = enrolled;
            this.slot = slot;
            this.totalPage = totalPage;
        }

        public String getOfferedNo() {
            return offeredNo;
        }

        public void setOfferedNo(String offeredNo) {
            this.offeredNo = offeredNo;
        }

        public String getSubjectCode() {
            return subjectCode;
        }

        public void setSubjectCode(String subjectCode) {
            this.subjectCode = subjectCode;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getMaximum() {
            return maximum;
        }

        public void setMaximum(String maximum) {
            this.maximum = maximum;
        }

        public String getEnrolled() {
            return enrolled;
        }

        public void setEnrolled(String enrolled) {
            this.enrolled = enrolled;
        }

        public String getSlot() {
            return slot;
        }

        public void setSlot(String slot) {
            this.slot = slot;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(int totalPage) {
            this.totalPage = totalPage;
        }
    }
}

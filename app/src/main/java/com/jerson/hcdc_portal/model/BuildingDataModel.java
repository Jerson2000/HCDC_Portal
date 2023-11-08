package com.jerson.hcdc_portal.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BuildingDataModel {
    @SerializedName("building")
    private List<BuildingModel> building;

    public BuildingDataModel(List<BuildingModel> building) {
        this.building = building;
    }

    public List<BuildingModel> getBuilding() {
        return building;
    }

    public void setBuilding(List<BuildingModel> building) {
        this.building = building;
    }
}

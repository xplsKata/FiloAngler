package com.example.filoangler.Model;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class MapLocationsModel {

    private String LocationId;
    private String LocationName;
    private double LocationLatitude;
    private double LocationLongitude;
    private String LocationDescription;
    private boolean IsBeach;
    @PropertyName("Common Fish")
    private Map<String, CommonFishModel> CommonFish;

    public MapLocationsModel() {
        CommonFish = new HashMap<>();
    }

    public MapLocationsModel(String locationId,
                             String locationName,
                             String locationDescription,
                             double locationLatitude,
                             double locationLongitude,
                             boolean isBeach,
                             Map<String, CommonFishModel> commonFish) {
        this.LocationId = locationId;
        this.LocationName = locationName;
        this.LocationDescription = locationDescription;
        this.LocationLatitude = locationLatitude;
        this.LocationLongitude = locationLongitude;
        this.IsBeach = isBeach;
        this.CommonFish = commonFish;
    }

    public String getLocationId() {
        return LocationId;
    }

    public void setLocationId(String locationId) {
        this.LocationId = locationId;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        this.LocationName = locationName;
    }

    public String getLocationDescription() {
        return LocationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.LocationDescription = locationDescription;
    }

    public double getLocationLatitude() {
        return LocationLatitude;
    }

    public void setLocationLatitude(double locationLatitude) {
        LocationLatitude = locationLatitude;
    }

    public double getLocationLongitude() {
        return LocationLongitude;
    }

    public void setLocationLongitude(double locationLongitude) {
        LocationLongitude = locationLongitude;
    }

    public boolean getIsBeach() {
        return IsBeach;
    }

    public void setIsBeach(boolean beach) {
        IsBeach = beach;
    }

    @PropertyName("Common Fish")  // Add this annotation
    public Map<String, CommonFishModel> getCommonFish() {
        return CommonFish != null ? CommonFish : new HashMap<>();
    }

    @PropertyName("Common Fish")  // Add this annotation
    public void setCommonFish(Map<String, CommonFishModel> commonFish) {
        this.CommonFish = commonFish;
    }
}

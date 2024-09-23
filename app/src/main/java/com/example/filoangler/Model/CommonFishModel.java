package com.example.filoangler.Model;

public class CommonFishModel {

    private String FishId;
    private String FishName;
    private String FishImage;

    public CommonFishModel() {

    }

    public CommonFishModel(String fishId, String fishName, String fishImage) {
        FishId = fishId;
        FishName = fishName;
        FishImage = fishImage;
    }

    public String getFishId() {
        return FishId;
    }

    public void setFishId(String fishId) {
        FishId = fishId;
    }

    public String getFishName() {
        return FishName;
    }

    public void setFishName(String fishName) {
        FishName = fishName;
    }

    public String getFishImage() {
        return FishImage;
    }

    public void setFishImage(String fishImage) {
        FishImage = fishImage;
    }
}

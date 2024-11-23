package com.example.filoangler.Model;

public class CommonFishModel {
    private String FishId;    // Keeping PascalCase to match DB structure
    private String FishImage;
    private String FishName;

    public CommonFishModel() {
        // Required empty constructor for Firebase
    }

    public CommonFishModel(String fishId, String fishImage, String fishName) {
        this.FishId = fishId;
        this.FishImage = fishImage;
        this.FishName = fishName;
    }

    public String getFishId() {
        return FishId != null ? FishId : "";
    }

    public String getFishImage() {
        return FishImage != null ? FishImage : "";
    }

    public String getFishName() {
        return FishName != null ? FishName : "";
    }

    public void setFishId(String fishId) {
        this.FishId = fishId;
    }

    public void setFishImage(String fishImage) {
        this.FishImage = fishImage;
    }

    public void setFishName(String fishName) {
        this.FishName = fishName;
    }
}

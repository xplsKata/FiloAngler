package com.example.filoangler.Model;

import java.util.List;

public class GearDatabankModel {
    private String name;
    private String description;
    private List<String> tips_for_use;
    private List<String> maintenance_tips;
    private String GearImage;
    private String Gear3DModel;
    private String ImageBig;
    private String WellKnownBrands;
    private String GoodForBeginners;

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTipsForUse() {
        return tips_for_use;
    }

    public List<String> getMaintenanceTips() {
        return maintenance_tips;
    }

    public String getGearImage() {
        return GearImage;
    }

    public String getGear3DModel() {
        return Gear3DModel;
    }

    public String getImageBig() {
        return ImageBig;
    }

    public String getGoodForBeginners() {
        return GoodForBeginners;
    }

    public void setGoodForBeginners(String goodForBeginners) {
        GoodForBeginners = goodForBeginners;
    }

    public String getWellKnownBrands() {
        return WellKnownBrands;
    }

    public void setWellKnownBrands(String wellKnownBrands) {
        WellKnownBrands = wellKnownBrands;
    }
}
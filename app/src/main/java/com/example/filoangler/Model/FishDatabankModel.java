package com.example.filoangler.Model;

public class FishDatabankModel {

    private String FishName;
    private String FishImage;
    private String ImageBig;
    private String Fish3DModel;
    private String Description;
    private String Behavior;
    private String Habitat;
    private String Law;

    public FishDatabankModel() {
    }

    public FishDatabankModel(String fishName, String fishImage, String fish3DModel, String description, String behavior, String habitat, String law) {
        FishName = fishName;
        FishImage = fishImage;
        Fish3DModel = fish3DModel;
        Description = description;
        Behavior = behavior;
        Habitat = habitat;
        Law = law;
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

    public String getFish3DModel() {
        return Fish3DModel;
    }

    public void setFish3DModel(String fish3DModel) {
        Fish3DModel = fish3DModel;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getBehavior() {
        return Behavior;
    }

    public void setBehavior(String behavior) {
        Behavior = behavior;
    }

    public String getHabitat() {
        return Habitat;
    }

    public void setHabitat(String habitat) {
        Habitat = habitat;
    }

    public String getLaw() {
        return Law;
    }

    public void setLaw(String law) {
        Law = law;
    }

    public String getImageBig() {
        return ImageBig;
    }

    public void setImageBig(String imageBig) {
        ImageBig = imageBig;
    }
}

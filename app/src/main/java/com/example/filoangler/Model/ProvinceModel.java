package com.example.filoangler.Model;

public class ProvinceModel {

    private String name;
    private String region;
    private String key;

    public ProvinceModel(String name, String region, String key) {
        this.name = name;
        this.region = region;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

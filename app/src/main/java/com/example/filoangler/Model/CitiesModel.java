package com.example.filoangler.Model;

public class CitiesModel {

    private String name;
    private String province;
    private boolean city;

    public CitiesModel() {
    }

    public CitiesModel(String name, String province, boolean city) {
        this.name = name;
        this.province = province;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public boolean isCity() {
        return city;
    }

    public void setCity(boolean city) {
        this.city = city;
    }
}

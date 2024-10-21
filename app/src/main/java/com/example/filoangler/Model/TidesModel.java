package com.example.filoangler.Model;

import java.util.Date;

public class TidesModel {

    private String tideTime;
    private double tideHeight_mt;
    private Date tideDateTime;
    private String tide_type;

    public TidesModel() {
    }

    public TidesModel(String tideTime, double tideHeight_mt, Date tideDateTime, String tide_type) {
        this.tideTime = tideTime;
        this.tideHeight_mt = tideHeight_mt;
        this.tideDateTime = tideDateTime;
        this.tide_type = tide_type;
    }

    public String gettideTime() {
        return tideTime;
    }

    public void settideTime(String tideTime) {
        this.tideTime = tideTime;
    }

    public double gettideHeight_mt() {
        return tideHeight_mt;
    }

    public void settideHeight_mt(double tideHeight_mt) {
        this.tideHeight_mt = tideHeight_mt;
    }

    public Date gettideDateTime() {
        return tideDateTime;
    }

    public void settideDateTime(Date tideDateTime) {
        this.tideDateTime = tideDateTime;
    }

    public String gettide_type() {
        return tide_type;
    }

    public void settide_type(String tide_type) {
        this.tide_type = tide_type;
    }
}

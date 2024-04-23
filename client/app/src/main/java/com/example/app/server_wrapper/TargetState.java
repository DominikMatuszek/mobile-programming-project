package com.example.app.server_wrapper;

public class TargetState {
    private final double lon;
    private final double lat;
    private String scorer;

    public TargetState(double lon, double lat, String owner) {
        this.lon = lon;
        this.lat = lat;
        this.scorer = owner;
    }

    public TargetState(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
        this.scorer = null;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getScorer() {
        return scorer;
    }

    public void setScorer(String owner) {
        this.scorer = owner;
    }
}

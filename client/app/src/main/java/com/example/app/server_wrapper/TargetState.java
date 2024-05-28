package com.example.app.server_wrapper;

public class TargetState {
    private final double lon;
    private final double lat;
    private final String scorer;

    public TargetState(double lon, double lat, String owner) {
        this.lon = lon;
        this.lat = lat;
        this.scorer = owner;
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
}

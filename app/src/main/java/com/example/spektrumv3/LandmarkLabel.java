package com.example.spektrumv3;

import java.text.DecimalFormat;

public class LandmarkLabel {
    float confidence;
    String label;
    String entityId;
    double latitude;
    double longitude;

    public LandmarkLabel(float confidence, String label, String entityId) {
        this.confidence = confidence;
        this.label = label;
        this.entityId = entityId;
    }

    public LandmarkLabel(float confidence, String label, String entityId, double lat, double longitude) {
        this.confidence = confidence;
        this.label = label;
        this.entityId = entityId;
        this.latitude = lat;
        this.longitude = longitude;
    }


    @Override
    public String toString() {

        return label + " with " + new DecimalFormat("#0.00%").format(confidence) + " confidence.";
    }
}
package com.example.spektrumv3;

import java.text.DecimalFormat;

public class ImageLabel {
    float confidence;
    String label;
    String entityId;

    public ImageLabel(float confidence, String label, String entityId) {
        this.confidence = confidence;
        this.label = label;
        this.entityId = entityId;
    }

    @Override
    public String toString() {

        return entityId + " with " + new DecimalFormat("#0.00%").format(confidence) + " confidence.";
    }
}

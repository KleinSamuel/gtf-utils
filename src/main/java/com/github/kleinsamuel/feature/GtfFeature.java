package com.github.kleinsamuel;

import java.util.Map;

public class GtfFeature {

    private GtfBaseData baseData;

    public GtfFeature(String contig, String source, String type, int start, int end, Double score,
                      boolean isForwardStrand, Integer frame, Map<String, String> attributes) {
        this(new GtfBaseData(contig, source, type, start, end, score, isForwardStrand, frame, attributes));
    }

    public GtfFeature(GtfBaseData baseData) {
        this.baseData = baseData;
    }

    public GtfBaseData getBaseData() {
        return baseData;
    }

    public void setBaseData(GtfBaseData baseData) {
        this.baseData = baseData;
    }

    @Override
    public String toString() {
        return "GtfFeature{" +
                "baseData=" + baseData +
                '}';
    }
}

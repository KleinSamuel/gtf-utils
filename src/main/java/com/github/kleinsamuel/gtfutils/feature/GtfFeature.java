package com.github.kleinsamuel.gtfutils.feature;

import java.util.List;
import java.util.Map;

public class GtfFeature {

    // index of the feature in the list of features
    private int index = -1;
    private GtfBaseData baseData;

    public GtfFeature(int index, String contig, String source, String type, int start, int end, Double score,
                      boolean isForwardStrand, Integer frame, Map<String, List<String>> attributes) {
        this(index, new GtfBaseData(contig, source, type, start, end, score, isForwardStrand, frame, attributes));
    }

    public GtfFeature(int index, GtfBaseData baseData) {
        this.index = index;
        this.baseData = baseData;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

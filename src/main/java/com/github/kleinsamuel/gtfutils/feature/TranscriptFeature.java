package com.github.kleinsamuel.gtfutils.feature;

import com.github.kleinsamuel.gtfutils.GtfConstants;

import java.util.ArrayList;

public class TranscriptFeature extends GtfFeature {

    private boolean isGenerated;

    private ArrayList<GtfFeature> features;

    public TranscriptFeature() {
        this(-1, null, false);
    }

    public TranscriptFeature(GtfFeature gtfFeature) {
        this(gtfFeature.getIndex(), gtfFeature.getBaseData(), false);
    }

    public TranscriptFeature(GtfBaseData baseData, boolean isGenerated) {
        this(-1, baseData, isGenerated);
    }

    public TranscriptFeature(int index, GtfBaseData baseData, boolean isGenerated) {
        super(index, baseData);
        this.isGenerated = isGenerated;
        this.features = new ArrayList<>();
    }

    public ArrayList<GtfFeature> getFeatures() {
        return features;
    }

    public ArrayList<GtfFeature> getFeatures(String type) {
        return features.stream()
                .filter(f -> f.getBaseData().getType().equals(type))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void setFeatures(ArrayList<GtfFeature> features) {
        this.features = features;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    public String getTranscriptId() {
        return this.getBaseData().getAttributes(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY).get(0);
    }

    /**
     * Sorts the features of this transcript by start and end position (if start is equal).
     */
    public void sortFeatures() {
        this.features = features.stream().sorted((o1, o2) -> {
            if (o1.getBaseData().getStart() == o2.getBaseData().getStart()) {
                return Integer.compare(o1.getBaseData().getEnd(), o2.getBaseData().getEnd());
            } else {
                return Integer.compare(o1.getBaseData().getStart(), o2.getBaseData().getStart());
            }
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public String toString() {
        return "TranscriptFeature{" +
                "numFeatures=" + features.size() +
                ", isGenerated=" + isGenerated +
                ", baseData=" + this.getBaseData() +
                '}';
    }
}

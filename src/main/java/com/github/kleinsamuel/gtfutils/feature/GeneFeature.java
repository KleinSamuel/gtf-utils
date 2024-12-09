package com.github.kleinsamuel.gtfutils.feature;

import com.github.kleinsamuel.gtfutils.GtfConstants;

import java.util.ArrayList;

public class GeneFeature extends GtfFeature {

    private boolean isGenerated;

    private ArrayList<TranscriptFeature> transcripts;

    public GeneFeature() {
        this(-1, null, false);
    }

    public GeneFeature(GtfFeature gtfFeature) {
        this(gtfFeature.getIndex(), gtfFeature.getBaseData(), false);
    }

    public GeneFeature(GtfBaseData baseData, boolean isGenerated) {
        this(-1, baseData, isGenerated);
    }

    public GeneFeature(int index, GtfBaseData baseData, boolean isGenerated) {
        super(index, baseData);
        this.isGenerated = isGenerated;
        this.transcripts = new ArrayList<>();
    }

    public String getGeneId() {
        return this.getBaseData().getAttributes(GtfConstants.GENE_ID_ATTRIBUTE_KEY).get(0);
    }

    public ArrayList<TranscriptFeature> getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(ArrayList<TranscriptFeature> transcripts) {
        this.transcripts = transcripts;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    @Override
    public String toString() {
        return "GeneFeature{" +
                "numTranscripts=" + transcripts.size() +
                ", isGenerated=" + isGenerated +
                ", baseData=" + this.getBaseData() +
                '}';
    }
}

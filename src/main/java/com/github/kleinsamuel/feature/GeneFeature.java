package com.github.kleinsamuel.hierarchy;

import com.github.kleinsamuel.GtfBaseData;
import com.github.kleinsamuel.GtfConstants;
import com.github.kleinsamuel.GtfFeature;

import java.util.ArrayList;
import java.util.Map;

public class GeneFeature extends GtfFeature {

    private ArrayList<TranscriptFeature> transcripts;
    private boolean isGenerated;

    public GeneFeature(String contig, String source, String type, int start, int end, double score, boolean isForwardStrand, int frame, Map<String, String> attributes) {
        this(new GtfBaseData(contig, source, type, start, end, score, isForwardStrand, frame, attributes), false);
    }

    public GeneFeature(GtfBaseData baseData, boolean isGenerated) {
        super(baseData);
        this.isGenerated = isGenerated;
        this.transcripts = new ArrayList<>();
    }

    public GeneFeature(GtfBaseData baseData) {
        this(baseData, false);
    }

    public String getGeneId() {
        return this.getBaseData().getAttribute(GtfConstants.GENE_ID_ATTRIBUTE_KEY);
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

package com.github.kleinsamuel;

import java.util.HashMap;
import java.util.LinkedList;

public class GtfFeature {

    private final String contig;
    private final String source;
    private final String type;
    private int start;
    private int end;
    private final double score;
    private final boolean isForwardStrand;
    private final int frame;
    private final HashMap<String, LinkedList<String>> attributes;

    public GtfFeature(String contig, String source, String type, int start, int end, double score,
                      boolean isForwardStrand, int frame) {
        this.contig = contig;
        this.source = source;
        this.type = type;
        this.start = start;
        this.end = end;
        this.score = score;
        this.isForwardStrand = isForwardStrand;
        this.frame = frame;
        this.attributes = new HashMap<>();
    }

}

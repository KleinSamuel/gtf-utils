package com.github.kleinsamuel.gtfutils.feature;

import java.util.*;
import java.util.stream.Collectors;

public class GtfBaseData {

    private final int hashcode;

    private String contig;
    private String source;
    private String type;
    private int start;
    private int end;
    private Double score;
    private boolean isForwardStrand;
    private Integer frame;
    private Map<String, List<String>> attributes;

    public GtfBaseData(String contig, String source, String type, int start, int end, Double score,
                       boolean isForwardStrand, Integer frame, Map<String, List<String>> attributes) {
        this.contig = contig;
        this.source = source;
        this.type = type;
        this.start = start;
        this.end = end;
        this.score = score;
        this.isForwardStrand = isForwardStrand;
        this.frame = frame;
        this.attributes = GtfBaseData.copyAttributesSafely(attributes);

        this.hashcode = computeHashCode();
    }

    public int getHashcode() {
        return hashcode;
    }

    public String getContig() {
        return contig;
    }

    public void setContig(String contig) {
        this.contig = contig;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public boolean isForwardStrand() {
        return isForwardStrand;
    }

    public void setForwardStrand(boolean forwardStrand) {
        isForwardStrand = forwardStrand;
    }

    public Integer getFrame() {
        return frame;
    }

    public void setFrame(Integer frame) {
        this.frame = frame;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public List<String> getAttributes(String key) {
        return attributes.get(key);
    }

    public Set<String> getAttributeKeys(String prefix) {
        return attributes.keySet().stream().filter(entry -> entry.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    public static Map<String, List<String>> copyAttributesSafely(final Map<String, List<String>> attributes) {
        final Map<String, List<String>> modifiableDeepMap = new LinkedHashMap<>();

        for (final Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            final List<String> unmodifiableDeepList = List.copyOf(entry.getValue());
            modifiableDeepMap.put(entry.getKey(), unmodifiableDeepList);
        }

        return Collections.unmodifiableMap(modifiableDeepMap);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!other.getClass().equals(GtfBaseData.class)) {
            return false;
        }
        final GtfBaseData otherBaseData = (GtfBaseData) other;

        return otherBaseData.getContig().equals(getContig()) &&
                otherBaseData.getSource().equals(getSource()) &&
                otherBaseData.getType().equals(getType()) &&
                otherBaseData.getStart() == getStart() &&
                otherBaseData.getEnd() == getEnd() &&
                ((Double)otherBaseData.getScore()).equals(score) &&
                otherBaseData.getFrame().equals(getFrame()) &&
                otherBaseData.isForwardStrand() == isForwardStrand() &&
                otherBaseData.getAttributes().equals(getAttributes());
    }

    @Override
    public int hashCode() {
        return this.hashcode;
    }

    private int computeHashCode() {
        int hash = getContig().hashCode();
        hash = 31 * hash + getSource().hashCode();
        hash = 31 * hash + getType().hashCode();
        hash = 31 * hash + getStart();
        hash = 31 * hash + getEnd();
        hash = 31 * hash + Double.hashCode(getScore() != null ? getScore() : 0);
        hash = 31 * hash + (getFrame() != null ? getFrame() : 0);
        hash = 31 * hash + (isForwardStrand() ? 1 : 0);
        hash = 31 * hash + getAttributes().hashCode();

        return hash;
    }

    @Override
    public String toString() {
        return "GtfBaseData{" +
                "contig='" + contig + '\'' +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", score=" + score +
                ", isForwardStrand=" + isForwardStrand +
                ", frame=" + frame +
                ", attributes=" + attributes +
                '}';
    }
}

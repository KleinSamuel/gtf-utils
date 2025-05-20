package com.github.kleinsamuel.gtfutils.utils;

import java.time.Duration;

public class Timer {

    public Duration total;
    public Duration fileRead;
    public Duration parseLines;
    public Duration groupFeatures;
    public Duration checkFeatureBounds;
    public Duration inferIntrons;

    public Timer() {
        total = Duration.ZERO;
        fileRead = Duration.ZERO;
        parseLines = Duration.ZERO;
        groupFeatures = Duration.ZERO;
        checkFeatureBounds = Duration.ZERO;
        inferIntrons = Duration.ZERO;
    }

    public void addTotal(Duration total) {
        this.total = this.total.plus(total);
    }

    public void addFileRead(Duration fileRead) {
        this.fileRead = this.fileRead.plus(fileRead);
    }

    public void addParseLines(Duration parseLines) {
        this.parseLines = this.parseLines.plus(parseLines);
    }

    public void addGroupFeatures(Duration groupFeatures) {
        this.groupFeatures = this.groupFeatures.plus(groupFeatures);
    }

    public void addCheckFeatureBounds(Duration checkFeatureBounds) {
        this.checkFeatureBounds = this.checkFeatureBounds.plus(checkFeatureBounds);
    }

    public void addInferIntrons(Duration inferIntrons) {
        this.inferIntrons = this.inferIntrons.plus(inferIntrons);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Timing summary:\n");
        sb.append("Total time: ").append(total.toMillis()).append(" ms\n");
        sb.append("File read time: ").append(fileRead.toMillis()).append(" ms\n");
        sb.append("Parse lines time: ").append(parseLines.toMillis()).append(" ms\n");
        sb.append("Group features time: ").append(groupFeatures.toMillis()).append(" ms\n");
        sb.append("Check feature bounds time: ").append(checkFeatureBounds.toMillis()).append(" ms\n");
        sb.append("Infer introns time: ").append(inferIntrons.toMillis()).append(" ms\n");

        return sb.toString();
    }

    public static String formatDuration(Duration duration) {

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        long milliseconds = duration.toMillis() % 1000;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (hours > 0 || minutes > 0) {
            sb.append(minutes).append("m ");
        }

        if (hours > 0 || minutes > 0 || seconds > 0) {
            sb.append(seconds).append("s ");
        }

        sb.append(milliseconds).append("ms");

        return sb.toString().trim();
    }
}

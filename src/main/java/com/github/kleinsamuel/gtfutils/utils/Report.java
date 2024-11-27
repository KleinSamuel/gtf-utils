package com.github.kleinsamuel.gtfutils.utils;

import com.github.kleinsamuel.gtfutils.GtfError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Report {

    public int numHeaderLines = 0;
    public int numTotalLines = 0;

    public Timer timer;

    public TreeMap<Integer, ArrayList<GtfError>> errors;

    public Report() {
        this.timer = new Timer();
        this.errors = new TreeMap<>();
    }

    public void addError(int featureIndex, GtfError error) {
        if (!errors.containsKey(featureIndex)) {
            errors.put(featureIndex, new ArrayList<>());
        }
        errors.get(featureIndex).add(error);
    }

    public void printTimer() {
        System.out.println("### RUNTIME ###");
        System.out.printf("Total:\t\t\t\t\t%s\n", Timer.formatDuration(timer.total));
        System.out.printf("File read:\t\t\t\t%s\n", Timer.formatDuration(timer.fileRead));
        System.out.printf("Parse lines:\t\t\t%s\n", Timer.formatDuration(timer.parseLines));
        System.out.printf("Group features:\t\t\t%s\n", Timer.formatDuration(timer.groupFeatures));
        System.out.printf("Check feature bounds:\t%s\n", Timer.formatDuration(timer.checkFeatureBounds));
    }

    public void printSummary() {

        int numErrors = this.errors.values()
                .stream().flatMap(ArrayList::stream).filter(GtfError::isError).mapToInt(e -> 1).sum();

        int numWarnings = this.errors.values()
                .stream().flatMap(ArrayList::stream).filter(GtfError::isWarning).mapToInt(e -> 1).sum();

        int numLinesWithWarnings = this.errors.entrySet().stream()
                .filter(e -> e.getValue()
                        .stream().anyMatch(GtfError::isWarning))
                .mapToInt(e -> 1).sum();

        int numLinesWithErrors = this.errors.entrySet().stream()
                .filter(e -> e.getValue()
                        .stream().anyMatch(GtfError::isError))
                .mapToInt(e -> 1).sum();

        System.out.println("### SUMMARY ###");
        System.out.printf("Total lines:\t%d\n", numTotalLines);
        System.out.printf("Header lines:\t%d\n", numHeaderLines);
        System.out.printf("Feature lines:\t%d\n", (numTotalLines - numHeaderLines));
        System.out.printf("Lines with errors:\t%d\n", numLinesWithErrors);
        System.out.printf("Lines with warnings:\t%d\n", numLinesWithWarnings);
        System.out.println("Errors:\t\t\t" + numErrors);
        System.out.println("Warnings:\t\t" + numWarnings);

    }

    public void printWarnings() {

        int numWarnings = this.errors.values()
                .stream().flatMap(ArrayList::stream).filter(GtfError::isWarning).mapToInt(e -> 1).sum();

        System.out.println("### WARNINGS ###");

        if (numWarnings > 0) {
            this.errors.values().stream().flatMap(ArrayList::stream).filter(GtfError::isWarning)
                    .collect(Collectors.groupingBy(gtfError -> gtfError, Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.<GtfError, Long>comparingByValue(Comparator.reverseOrder()))
                    .forEach(e -> System.out.println(e.getKey().getMessage() + " (" + e.getValue() + " times)"));
        } else {
            System.out.println("No warnings found");
        }
    }

    public void printErrors() {

        int numErrors = this.errors.values()
                .stream().flatMap(ArrayList::stream).filter(GtfError::isError).mapToInt(e -> 1).sum();

        System.out.println("### ERRORS ###");

        if (numErrors > 0) {
            this.errors.values().stream().flatMap(ArrayList::stream).filter(GtfError::isError)
                    .collect(Collectors.groupingBy(gtfError -> gtfError, Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.<GtfError, Long>comparingByValue(Comparator.reverseOrder()))
                    .forEach(e -> System.out.println(e.getKey().getMessage() + " (" + e.getValue() + " times)"));
        } else {
            System.out.println("No errors found");
        }
    }
}

package com.github.kleinsamuel;

import com.github.kleinsamuel.feature.GeneFeature;
import com.github.kleinsamuel.feature.GtfFeature;
import com.github.kleinsamuel.feature.TranscriptFeature;
import com.github.kleinsamuel.utils.Report;

import java.io.*;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class GtfFile {

    private final Report report;

    private ArrayList<String> lines;
    private ArrayList<String> headerLines;

    private HashMap<String, Integer> featureTypes;
    private HashMap<String, Integer> contigs;
    private HashMap<String, Integer> attributeKeys;
    private HashMap<String, String> headerAttributes;

    private ArrayList<GtfFeature> features;

    private HashSet<Integer> parentIndices;
    private HashMap<String, GeneFeature> id2gene;
    private HashMap<String, TranscriptFeature> id2transcript;

    public GtfFile(File gtfFile) throws ParseException {
        this.report = new Report();

        Instant timeStart = Instant.now();

        this.readLines(gtfFile);
        this.parseHeader();
        this.parseLines();
        this.groupFeatures();
        this.checkFeatureBounds();

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addTotal(d);

        this.report.printTimer();
        this.report.printSummary();
        this.report.printErrors();
        this.report.printWarnings();
    }

    public Report getReport() {
        return report;
    }

    public GeneFeature getGeneFeature(String geneId) {
        return id2gene.get(geneId);
    }

    public void readLines(File gtfFile) throws ParseException {
        try {
            Instant timeStart = Instant.now();

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gtfFile)));
            if (gtfFile.getAbsolutePath().endsWith("gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gtfFile))));
            }

            String line;
            this.headerLines = new ArrayList<>();
            this.lines = new ArrayList<>();

            while ((line = br.readLine()) != null) {

                this.report.numTotalLines++;

                if (line.startsWith("#")) {

                    this.report.numHeaderLines++;
                    System.out.println("Header line: " + line);

                    headerLines.add(line);
                    continue;
                }

                lines.add(line);
            }

            br.close();

            Duration d = Duration.between(timeStart, Instant.now());
            this.report.timer.addFileRead(d);

        } catch (IOException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    private void parseHeader() {

        this.headerAttributes = new HashMap<>();

        headerLines.stream()
                .filter(s -> s.contains(GtfConstants.KEY_VALUE_SEPARATOR))
                .map(s -> s.split(GtfConstants.KEY_VALUE_SEPARATOR))
                .forEach(s -> {
                    if (s.length != 2) {
                        System.out.println("Header line does not contain exactly one key-value pair: " + Arrays.toString(s));
                        return;
                    }
                    String key = s[0];
                    if (key.startsWith(GtfConstants.COMMENT_START)) {
                        key = key.substring(1);
                    }
                    if (key.startsWith("!")) {
                        key = key.substring(1);
                    }
                    this.headerAttributes.put(key, s[1]);
                });
    }

    private void parseLines() {

        Instant timeStart = Instant.now();

        this.contigs = new HashMap<>();
        this.featureTypes = new HashMap<>();
        this.attributeKeys = new HashMap<>();

        this.features = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);
            int lineNumber = this.headerLines.size() + i + 1;

            String[] parts = line.split(GtfConstants.FIELD_DELIMITER);

            if (parts.length != 9) {
                this.report.addError(i, GtfError.INVALID_NUMBER_OF_COLUMNS);
                continue;
            }

            String contig = parts[0];

            if (contig == null || contig.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_CONTIG);
            } else {
                if (!this.contigs.containsKey(contig)) {
                    this.contigs.put(contig, 0);
                }
                this.contigs.put(contig, this.contigs.get(contig) + 1);
            }

            String source = parts[1];

            if (source == null || source.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_SOURCE);
            } else if (source.equals(".")) {
                source = null;
            }

            String type = parts[2];

            // the type of the feature is missing in the GTF file line
            if (type == null || type.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_TYPE);
                continue;
            }

            String typeParsed = GtfConfig.getDefault(type);

            // the type of the feature is not known
            if (typeParsed == null) {
                this.report.addError(i, GtfError.INVALID_VALUE_TYPE);
                continue;
            }
            // the type of the feature is not spelled exactly as the default type
            if (!type.equals(typeParsed)) {
                this.report.addError(i, GtfError.NOT_DEFAULT_TYPE);
            }
            // count the types
            if (!this.featureTypes.containsKey(typeParsed)) {
                this.featureTypes.put(typeParsed, 0);
            }
            this.featureTypes.put(typeParsed, this.featureTypes.get(typeParsed) + 1);

            String strand = parts[6];

            if (strand == null || strand.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_STRAND);
                continue;
            }

            boolean isForwardStrand;
            switch (strand) {
                case "+":
                    isForwardStrand = true;
                    break;
                case "-":
                    isForwardStrand = false;
                    break;
                default:
                    this.report.addError(i, GtfError.INVALID_VALUE_STRAND);
                    continue;
            }

            String startString = parts[3];

            if (startString == null || startString.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_START_POSITION);
                continue;
            }

            int start = 0;
            try {
                start = Integer.parseInt(startString);

                if (start < 0) {
                    throw new NumberFormatException("Negative start position");
                }

            } catch (NumberFormatException e) {
                this.report.addError(i, GtfError.INVALID_VALUE_START_POSITION);
                continue;
            }

            String endString = parts[4];

            if (endString == null || endString.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_END_POSITION);
                continue;
            }

            int end = 0;
            try {
                end = Integer.parseInt(endString);

                if (end < 0) {
                    throw new NumberFormatException("Negative end position");
                }

            } catch (NumberFormatException e) {
                this.report.addError(i, GtfError.INVALID_VALUE_END_POSITION);
                continue;
            }

            String frameString = parts[7];

            if (frameString == null || frameString.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_FRAME);
                continue;
            }

            Integer frame = null;
            try {
                frame = Integer.parseInt(frameString);

                if (frame < 0 || frame > 2) {
                    throw new NumberFormatException("Invalid frame");
                }

            } catch (NumberFormatException e) {
                if (!parts[7].equals(".")) {
                    this.report.addError(i, GtfError.INVALID_VALUE_FRAME);
                    continue;
                }
            }

            String scoreString = parts[5];

            if (scoreString == null || scoreString.isEmpty()) {
                this.report.addError(i, GtfError.MISSING_VALUE_SCORE);
                continue;
            }

            Double score = null;
            try {
                score = Double.parseDouble(scoreString);
            } catch (NumberFormatException e) {
                if (!parts[5].equals(".")) {
                    this.report.addError(i, GtfError.INVALID_VALUE_SCORE);
                    continue;
                }
            }

            HashMap<String, String> attributes = this.parseAttributes(parts[8].trim(), lineNumber);

            if (attributes == null) {
                this.report.addError(i, GtfError.MISSING_ATTRIBUTE_GENE_ID);
                this.report.addError(i, GtfError.MISSING_ATTRIBUTE_TRANSCRIPT_ID);
                continue;
            }

            // all features must have a gene_id attribute
            if (!attributes.containsKey(GtfConstants.GENE_ID_ATTRIBUTE_KEY)) {
                this.report.addError(i, GtfError.MISSING_ATTRIBUTE_GENE_ID);
                continue;
            }
            // all features except of type "gene" must have a transcript_id attribute
            if (!typeParsed.equals(GtfConfig.TYPE_GENE_DEFAULT) &&
                    !attributes.containsKey(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY)) {
                this.report.addError(i, GtfError.MISSING_ATTRIBUTE_TRANSCRIPT_ID);
                continue;
            }

            GtfFeature feature = new GtfFeature(i, contig, source, type, start, end, score, isForwardStrand,
                    frame, attributes);

            this.features.add(feature);
        }

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addParseLines(d);
    }

    private HashMap<String, String> parseAttributes(String linePart, int lineNumber) {

        HashMap<String, String> attributes = new HashMap<>();

        if (linePart == null || linePart.isEmpty()) {
            return null;
        }

        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        // 0 = key, 1 = space sep, 2 = ", 3 = value, 4 = ", 5 = ;
        int mode = 0;

        for (int i = -1; i < linePart.length(); i++) {
            switch (mode) {
                case 0:
                    if (!key.isEmpty()) {
                        if (value.isEmpty()) {
                            this.report.addError(lineNumber, GtfError.MALFORMED_ATTRIBUTES);
                            return null;
                        } else {

                            String keyLower = key.toString().toLowerCase();

                            if (!key.toString().equals(keyLower)) {
                                System.out.println("Key not lowercase: " + key);
                            }

                            attributes.put(keyLower, value.toString());
                        }
                    } else if (!value.isEmpty()) {
                        this.report.addError(lineNumber, GtfError.MALFORMED_ATTRIBUTES);
                        return null;
                    }

                    key.setLength(0);
                    value.setLength(0);
                    mode = 1;
                    break;
                case 1:
                    if (linePart.charAt(i) == ' ') {
                        mode = 2;
                    } else {
                        key.append(linePart.charAt(i));
                    }
                    break;
                case 2:
                    if (linePart.charAt(i) == '"') {
                        mode = 3;
                    } else {
                        this.report.addError(lineNumber, GtfError.MALFORMED_ATTRIBUTES);
                        return null;
                    }
                    break;
                case 3:
                    if (linePart.charAt(i) == '"') {
                        mode = 4;
                    } else {
                        value.append(linePart.charAt(i));
                    }
                    break;
                case 4:
                    if (linePart.charAt(i) == ';') {
                        mode = 0;
                    } else {
                        this.report.addError(lineNumber, GtfError.MALFORMED_ATTRIBUTES);
                        return null;
                    }
                    break;
            }
        }
        if (!key.isEmpty()) {
            if (value.isEmpty()) {
                this.report.addError(lineNumber, GtfError.MALFORMED_ATTRIBUTES);
                return null;
            } else {

                String keyLower = key.toString().toLowerCase();

                if (!key.toString().equals(keyLower)) {
                    System.out.println("Key not lowercase: " + key);
                }

                attributes.put(keyLower, value.toString());
            }
        } else if (!value.isEmpty()) {
            this.report.addError(lineNumber, GtfError.MALFORMED_ATTRIBUTES);
            return null;
        }

        return attributes;
    }

    private void groupFeatures() {

        Instant timeStart = Instant.now();

        this.parentIndices = new HashSet<>();
        this.id2gene = new HashMap<>();
        this.id2transcript = new HashMap<>();

        HashMap<String, ArrayList<GtfFeature>> transcriptId2child = new HashMap<>();

        for (int i = 0; i < this.features.size(); i++) {

            GtfFeature feature = this.features.get(i);

            if (feature.getBaseData().getType().equals(GtfConfig.TYPE_GENE_DEFAULT)) {

                String geneId = feature.getBaseData().getAttribute(GtfConstants.GENE_ID_ATTRIBUTE_KEY);

                if (this.id2gene.containsKey(geneId)) {
                    this.report.addError(i, GtfError.DUPLICATE_GENE_ENTRY);
                }

                GeneFeature geneFeature = new GeneFeature(feature);
                this.id2gene.put(geneId, geneFeature);
                this.parentIndices.add(i);

            } else if (feature.getBaseData().getType().equals(GtfConfig.TYPE_TRANSCRIPT_DEFAULT)) {

                String transcriptId = feature.getBaseData().getAttribute(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY);

                if (this.id2transcript.containsKey(transcriptId)) {
                    this.report.addError(i, GtfError.DUPLICATE_TRANSCRIPT_ENTRY);
                }

                TranscriptFeature transcriptFeature = new TranscriptFeature(feature);
                this.id2transcript.put(transcriptId, transcriptFeature);
                this.parentIndices.add(i);

            } else {

                String transcriptId = feature.getBaseData().getAttribute(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY);

                if (!transcriptId2child.containsKey(transcriptId)) {
                    transcriptId2child.put(transcriptId, new ArrayList<>());
                }
                transcriptId2child.get(transcriptId).add(feature);
            }
        }

        for (String transcriptId : transcriptId2child.keySet()) {

            GeneFeature parentGene = null;
            TranscriptFeature parentTranscript = this.id2transcript.get(transcriptId);

            String parentGeneId = null;

            if (parentTranscript != null) {
                parentGeneId = parentTranscript.getBaseData().getAttribute(GtfConstants.GENE_ID_ATTRIBUTE_KEY);

                parentGene = this.id2gene.get(parentGeneId);

                if (parentGene == null) {
                    this.report.addError(parentTranscript.getIndex(), GtfError.PARENT_MISSING_GENE);
                }
            }

            boolean parentGeneMismatch = false;

            // check if all child features have the same gene id as parent
            for (GtfFeature feature : transcriptId2child.get(transcriptId)) {

                String geneId = feature.getBaseData().getAttribute(GtfConstants.GENE_ID_ATTRIBUTE_KEY);

                if (parentGeneId == null) {
                    parentGeneId = geneId;
                } else if (!parentGeneId.equals(geneId)) {
                    parentGeneMismatch = true;
                    break;
                }
            }

            // add the parent gene mismatch error to the transcript (if it exists) and all child features
            if (parentGeneMismatch) {
                if (parentTranscript != null) {
                    this.report.addError(parentTranscript.getIndex(), GtfError.PARENT_GENE_AND_TRANSCRIPT_MISMATCH);
                }
                for (GtfFeature feature : transcriptId2child.get(transcriptId)) {
                    this.report.addError(feature.getIndex(), GtfError.PARENT_GENE_AND_TRANSCRIPT_MISMATCH);
                }
                continue;
            }

            if (parentTranscript == null) {
                parentGene = this.id2gene.get(parentGeneId);
            }

            for (GtfFeature feature : transcriptId2child.get(transcriptId)) {
                if (parentTranscript == null) {
                    this.report.addError(feature.getIndex(), GtfError.PARENT_MISSING_TRANSCRIPT);
                }
                if (parentGene == null) {
                    this.report.addError(feature.getIndex(), GtfError.PARENT_MISSING_GENE);
                }
            }

            // generate the parent transcript if it does not exist
            if (parentTranscript == null) {
                GtfFeature feature = transcriptId2child.get(transcriptId).getFirst();
                parentTranscript = this.generateDummyTranscriptFeature(feature);

                this.id2transcript.put(transcriptId, parentTranscript);
            }

            // generate the parent gene if it does not exist
            if (parentGene == null) {
                parentGene = this.generateDummyGeneFeature(parentTranscript);

                this.id2gene.put(parentGeneId, parentGene);
            }

            parentGene.getTranscripts().add(parentTranscript);

            for (GtfFeature feature : transcriptId2child.get(transcriptId)) {
                parentTranscript.getFeatures().add(feature);
            }
        }

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addGroupFeatures(d);
    }

    private GeneFeature generateDummyGeneFeature(GtfFeature baseFeature) {

        HashMap<String, String> geneAttributes = new HashMap<>();

        for (String key : baseFeature.getBaseData().getAttributeKeys("gene")) {
            geneAttributes.put(key, baseFeature.getBaseData().getAttribute(key));
        }

        GtfBaseData geneBaseData = new GtfBaseData(baseFeature.getBaseData().getContig(),
                baseFeature.getBaseData().getSource(),
                GtfConfig.TYPE_GENE_DEFAULT, -1, -1, null,
                baseFeature.getBaseData().isForwardStrand(), null, geneAttributes);

        return new GeneFeature(geneBaseData, true);
    }

    private TranscriptFeature generateDummyTranscriptFeature(GtfFeature baseFeature) {

        HashMap<String, String> transcriptAttributes = new HashMap<>();

        for (String key : baseFeature.getBaseData().getAttributeKeys("transcript")) {
            transcriptAttributes.put(key, baseFeature.getBaseData().getAttribute(key));
        }

        GtfBaseData transcriptBaseData = new GtfBaseData(baseFeature.getBaseData().getContig(),
                baseFeature.getBaseData().getSource(),
                GtfConfig.TYPE_TRANSCRIPT_DEFAULT, -1, -1, null,
                baseFeature.getBaseData().isForwardStrand(), null, transcriptAttributes);

        return new TranscriptFeature(transcriptBaseData, true);
    }

    private void checkFeatureBounds() {

        Instant timeStart = Instant.now();

        for (GeneFeature geneFeature : this.id2gene.values()) {

            int geneMinStart = Integer.MAX_VALUE;
            int geneMaxEnd = Integer.MIN_VALUE;

            for (TranscriptFeature transcriptFeature : geneFeature.getTranscripts()) {

                int transcriptMinStart = Integer.MAX_VALUE;
                int transcriptMaxEnd = Integer.MIN_VALUE;

                transcriptFeature.sortFeatures();

                for (GtfFeature feature : transcriptFeature.getFeatures()) {
                    if (!transcriptFeature.isGenerated()) {
                        // check if feature is inside transcript bounds
                        if (feature.getBaseData().getStart() < transcriptFeature.getBaseData().getStart()) {
                            this.report.addError(feature.getIndex(), GtfError.FEATURE_START_BEFORE_PARENT);
                        }
                        if (feature.getBaseData().getEnd() > transcriptFeature.getBaseData().getEnd()) {
                            this.report.addError(feature.getIndex(), GtfError.FEATURE_END_AFTER_PARENT);
                        }
                    }

                    // compute the min start and max end of the transcript
                    if (feature.getBaseData().getStart() < transcriptMinStart) {
                        transcriptMinStart = feature.getBaseData().getStart();
                    }
                    if (feature.getBaseData().getEnd() > transcriptMaxEnd) {
                        transcriptMaxEnd = feature.getBaseData().getEnd();
                    }
                }

                if (geneFeature.isGenerated()) {
                    // set the min start and max end of the transcript
                    transcriptFeature.getBaseData().setStart(transcriptMinStart);
                    transcriptFeature.getBaseData().setEnd(transcriptMaxEnd);
                } else {
                    // check if transcript bounds are inside gene bounds
                    if (transcriptFeature.getBaseData().getStart() < geneFeature.getBaseData().getStart()) {
                        this.report.addError(transcriptFeature.getIndex(), GtfError.FEATURE_START_BEFORE_PARENT);
                    }
                    if (transcriptFeature.getBaseData().getEnd() > geneFeature.getBaseData().getEnd()) {
                        this.report.addError(transcriptFeature.getIndex(), GtfError.FEATURE_END_AFTER_PARENT);
                    }

                    // check if transcript is defined as by min and max of its features
                    if (transcriptFeature.getBaseData().getStart() != transcriptMinStart ||
                            transcriptFeature.getBaseData().getEnd() != transcriptMaxEnd) {
                        this.report.addError(transcriptFeature.getIndex(), GtfError.BOUNDS_LARGER_THAN_CHILDREN);
                    }
                }

                // compute the min start and max end of the gene
                if (transcriptMinStart < geneMinStart) {
                    geneMinStart = transcriptMinStart;
                }
                if (transcriptMaxEnd > geneMaxEnd) {
                    geneMaxEnd = transcriptMaxEnd;
                }
            }

            if (geneFeature.isGenerated()) {
                // set the min start and max end of the gene
                geneFeature.getBaseData().setStart(geneMinStart);
                geneFeature.getBaseData().setEnd(geneMaxEnd);
            } else {
                // check if gene is defined as by min and max of its transcripts
                if (geneFeature.getBaseData().getStart() != geneMinStart ||
                        geneFeature.getBaseData().getEnd() != geneMaxEnd) {
                    this.report.addError(geneFeature.getIndex(), GtfError.BOUNDS_LARGER_THAN_CHILDREN);
                }
            }
        }

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addCheckFeatureBounds(d);
    }
}

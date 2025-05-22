package com.github.kleinsamuel.gtfutils;

import com.github.kleinsamuel.gtfutils.feature.GeneFeature;
import com.github.kleinsamuel.gtfutils.feature.GtfBaseData;
import com.github.kleinsamuel.gtfutils.feature.GtfFeature;
import com.github.kleinsamuel.gtfutils.feature.TranscriptFeature;
import com.github.kleinsamuel.gtfutils.region.Region;
import com.github.kleinsamuel.gtfutils.utils.Report;

import java.io.*;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GtfFile {

    private final File gtfFile;
    private Report report;

    private ArrayList<String> lines;
    private ArrayList<String> headerLines;

    private HashMap<String, Integer> featureTypes;
    private HashMap<String, Integer> contigs;
    private HashMap<String, Integer> attributeKeys;
    private HashMap<String, String> headerAttributes;

    private HashMap<String, Integer> contig2lineIndex;
    private int nextContigIndex;
    private String parsedContig;

    private ArrayList<GtfFeature> features;

    private HashSet<Integer> parentIndices;
    private HashMap<String, GeneFeature> id2gene;
    private HashMap<String, TranscriptFeature> id2transcript;

    public GtfFile(File gtfFile) {
        this.gtfFile = gtfFile;

        this.lines = new ArrayList<>();
        this.report = new Report();

        this.featureTypes = new HashMap<>();
        this.contigs = new HashMap<>();
        this.attributeKeys = new HashMap<>();
        this.headerAttributes = new HashMap<>();

        this.contig2lineIndex = new HashMap<>();
        this.nextContigIndex = 0;

        this.resetContigMaps();
    }

    private void resetContigMaps() {
        this.features = new ArrayList<>();
        this.parentIndices = new HashSet<>();
        this.id2gene = new HashMap<>();
        this.id2transcript = new HashMap<>();
    }

    public void parseNextContig() throws ParseException {

        if (this.lines.isEmpty()) {
            this.readLines(gtfFile);
        }

        if (this.nextContigIndex < 0) {
            throw new ParseException("All contigs have been parsed", 0);
        }

        Instant timeStart = Instant.now();

        this.resetContigMaps();

        this.nextContigIndex = this.parseLines(this.nextContigIndex);

        this.groupFeatures();
        this.checkFeatureBounds();
        this.inferIntrons();
        this.inferUtr();

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addParseLines(d);
    }

    public void parseAllContigs() throws ParseException {

        Instant timeStart = Instant.now();

        this.readLines(gtfFile);

        int start = 0;
        do {
            start = this.parseLines(start);
        } while (start >= 0);

        this.groupFeatures();
        this.checkFeatureBounds();
        this.inferIntrons();
        this.inferUtr();

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addTotal(d);
    }

    public ArrayList<String> getHeaderLines() {
        return headerLines;
    }

    public Report getReport() {
        return report;
    }

    public void printReport() {
        this.report.printTimer();
        this.report.printSummary();
        this.report.printErrors();
        this.report.printWarnings();
    }

    public GeneFeature getGeneFeature(String geneId) {
        return id2gene.get(geneId);
    }

    public Set<String> getAllGeneFeatureIds() {
        return id2gene.keySet();
    }

    public String getParsedContig() {
        return parsedContig;
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

    private int parseLines(int startIndex) {

        Instant timeStart = Instant.now();

        String currentContig = null;

        for (int i = startIndex; i < lines.size(); i++) {

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

            if (currentContig == null) {
                currentContig = contig;
                this.parsedContig = contig;
            } else if (!currentContig.equals(contig)) {

                Duration d = Duration.between(timeStart, Instant.now());
                this.report.timer.addParseLines(d);

                return i;
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

            HashMap<String, List<String>> attributes = this.parseAttributes(parts[8].trim(), lineNumber);

            if (attributes == null) {
                this.report.addError(i, GtfError.ATTRIBUTE_GENE_ID_MISSING);
                this.report.addError(i, GtfError.ATTRIBUTE_TRANSCRIPT_ID_MISSING);
                continue;
            }

            // all features must have a gene_id attribute
            if (!attributes.containsKey(GtfConstants.GENE_ID_ATTRIBUTE_KEY)) {
                this.report.addError(i, GtfError.ATTRIBUTE_GENE_ID_MISSING);
                continue;
            }
            // all features except of type "gene" must have a transcript_id attribute
            if (!typeParsed.equals(GtfConfig.TYPE_GENE_DEFAULT) &&
                    !attributes.containsKey(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY)) {
                this.report.addError(i, GtfError.ATTRIBUTE_TRANSCRIPT_ID_MISSING);
                continue;
            }

            GtfFeature feature = new GtfFeature(i, contig, source, type, start, end, score, isForwardStrand,
                    frame, attributes);

            this.features.add(feature);
        }

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addParseLines(d);

        return -1;
    }

    private HashMap<String, List<String>> parseAttributes(String linePart, int featureIndex) {

        HashMap<String, List<String>> attributes = new HashMap<>();

        if (linePart == null || linePart.isEmpty()) {
            return null;
        }

        String[] lineParts = linePart.split(";");

        for (int i = 0; i < lineParts.length; i++) {

            int sepIndex = lineParts[i].trim().indexOf(" ");

            if (sepIndex == -1) {
                this.report.addError(featureIndex, GtfError.MALFORMED_ATTRIBUTES);
                return null;
            }

            String key = lineParts[i].trim().substring(0, sepIndex).trim();
            String value = lineParts[i].trim().substring(sepIndex+1).trim();

            if (key.isEmpty()) {
                this.report.addError(featureIndex, GtfError.MALFORMED_ATTRIBUTES);
                return null;
            }

            if (key.equals(GtfConstants.GENE_ID_ATTRIBUTE_KEY)) {
                if (i != 0) {
                    this.report.addError(featureIndex, GtfError.ATTRIBUTE_GENE_ID_NOT_FIRST);
                }
                if (attributes.containsKey(key) && !attributes.get(key).equals(value)) {
                    this.report.addError(featureIndex, GtfError.ATTRIBUTE_GENE_ID_AMBIGUOUS);
                    return null;
                }
            }
            if (key.equals(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY)) {
                if (i != 1) {
                    this.report.addError(featureIndex, GtfError.ATTRIBUTE_TRANSCRIPT_ID_NOT_SECOND);
                }
                if (attributes.containsKey(key) && !attributes.get(key).equals(value)) {
                    this.report.addError(featureIndex, GtfError.ATTRIBUTE_TRANSCRIPT_ID_AMBIGUOUS);
                    return null;
                }
            }

            // check if the value is enclosed in double quotes and if not it must be a numeric value
            if (value.charAt(0) != '"' || value.charAt(value.length()-1) != '"') {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    this.report.addError(featureIndex, GtfError.MALFORMED_ATTRIBUTES);
                }
            } else {
                // remove the quotes for quoted textual values
                value = value.substring(1, value.length()-1);
            }

            if (!attributes.containsKey(key)) {
                attributes.put(key, new ArrayList<>());
            }

            attributes.get(key).add(value);
        }

        if (!attributes.containsKey(GtfConstants.GENE_ID_ATTRIBUTE_KEY)) {
            this.report.addError(featureIndex, GtfError.ATTRIBUTE_GENE_ID_MISSING);
        }
        if (!attributes.containsKey(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY)) {
            this.report.addError(featureIndex, GtfError.ATTRIBUTE_TRANSCRIPT_ID_MISSING);
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

                String geneId = feature.getBaseData().getAttributes(GtfConstants.GENE_ID_ATTRIBUTE_KEY).get(0);

                if (this.id2gene.containsKey(geneId)) {
                    this.report.addError(i, GtfError.DUPLICATE_GENE_ENTRY);
                }

                GeneFeature geneFeature = new GeneFeature(feature);
                this.id2gene.put(geneId, geneFeature);
                this.parentIndices.add(i);

            } else if (feature.getBaseData().getType().equals(GtfConfig.TYPE_TRANSCRIPT_DEFAULT)) {

                String transcriptId = feature.getBaseData().getAttributes(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY).get(0);

                if (this.id2transcript.containsKey(transcriptId)) {
                    this.report.addError(i, GtfError.DUPLICATE_TRANSCRIPT_ENTRY);
                }

                TranscriptFeature transcriptFeature = new TranscriptFeature(feature);
                this.id2transcript.put(transcriptId, transcriptFeature);
                this.parentIndices.add(i);

            } else {

                String transcriptId = feature.getBaseData().getAttributes(GtfConstants.TRANSCRIPT_ID_ATTRIBUTE_KEY).get(0);

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
                parentGeneId = parentTranscript.getBaseData().getAttributes(GtfConstants.GENE_ID_ATTRIBUTE_KEY).get(0);

                parentGene = this.id2gene.get(parentGeneId);

                if (parentGene == null) {
                    this.report.addError(parentTranscript.getIndex(), GtfError.PARENT_MISSING_GENE);
                }
            }

            boolean parentGeneMismatch = false;

            // check if all child features have the same gene id as parent
            for (GtfFeature feature : transcriptId2child.get(transcriptId)) {

                String geneId = feature.getBaseData().getAttributes(GtfConstants.GENE_ID_ATTRIBUTE_KEY).get(0);

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
                parentGene = this.generateDummyGeneFeature(parentTranscript, parentGeneId);

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

    private GeneFeature generateDummyGeneFeature(GtfFeature baseFeature, String parentGeneId) {

        HashMap<String, List<String>> geneAttributes = new HashMap<>();

        for (String key : baseFeature.getBaseData().getAttributeKeys("gene")) {
            geneAttributes.put(key, baseFeature.getBaseData().getAttributes(key));
        }

        geneAttributes.computeIfAbsent(GtfConstants.GENE_ID_ATTRIBUTE_KEY, k -> Collections.singletonList(parentGeneId));

        GtfBaseData geneBaseData = new GtfBaseData(baseFeature.getBaseData().getContig(),
                baseFeature.getBaseData().getSource(),
                GtfConfig.TYPE_GENE_DEFAULT, -1, -1, null,
                baseFeature.getBaseData().isForwardStrand(), null, geneAttributes);

        return new GeneFeature(geneBaseData, true);
    }

    private TranscriptFeature generateDummyTranscriptFeature(GtfFeature baseFeature) {

        HashMap<String, List<String>> transcriptAttributes = new HashMap<>();

        for (String key : baseFeature.getBaseData().getAttributeKeys("transcript")) {
            transcriptAttributes.put(key, baseFeature.getBaseData().getAttributes(key));
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

    private void inferIntrons() {

        Instant timeStart = Instant.now();

        for (GeneFeature geneFeature : this.id2gene.values()) {

            for (TranscriptFeature transcriptFeature : geneFeature.getTranscripts()) {

                transcriptFeature.sortFeatures();

                ArrayList<GtfFeature> exons = transcriptFeature.getFeatures(GtfConfig.TYPE_EXON_DEFAULT);

                for (int i = 0; i < exons.size() -1 ; i++) {

                    GtfBaseData baseData = new GtfBaseData(geneFeature.getBaseData().getContig(),
                            geneFeature.getBaseData().getSource(),
                            GtfConfig.TYPE_INTRON_DEFAULT,
                            exons.get(i).getBaseData().getEnd()+1, exons.get(i+1).getBaseData().getStart()-1,
                            null,
                            geneFeature.getBaseData().isForwardStrand(),
                            null,
                            new HashMap<>()
                    );

                    GtfFeature intron = new GtfFeature(transcriptFeature.getFeatures().size(), baseData);

                    transcriptFeature.getFeatures().add(intron);
                }
            }
        }

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addInferIntrons(d);
    }

    private void inferUtr() {

        Instant timeStart = Instant.now();

        for (GeneFeature geneFeature : this.id2gene.values()) {

            for (TranscriptFeature transcriptFeature : geneFeature.getTranscripts()) {

                transcriptFeature.sortFeatures();

                ArrayList<GtfFeature> cds = transcriptFeature.getFeatures(GtfConfig.TYPE_CDS_DEFAULT);

                // skip this transcript as utr can only be present if there are cds
                if (cds.isEmpty()) {
                    continue;
                }

                ArrayList<GtfFeature> utrFive = transcriptFeature.getFeatures(GtfConfig.TYPE_FIVE_PRIME_UTR_DEFAULT);
                ArrayList<GtfFeature> utrThree = transcriptFeature.getFeatures(GtfConfig.TYPE_THREE_PRIME_UTR_DEFAULT);
                ArrayList<GtfFeature> utr = transcriptFeature.getFeatures(GtfConfig.TYPE_UTR_DEFAULT);

                // there are already utr features present
                if (!utrFive.isEmpty() && !utrThree.isEmpty()) {
                    continue;
                }

                ArrayList<GtfFeature> exons = transcriptFeature.getFeatures(GtfConfig.TYPE_EXON_DEFAULT);

                int cdsStart = cds.get(0).getBaseData().getStart();
                int cdsEnd = cds.get(cds.size()-1).getBaseData().getEnd();

                ArrayList<GtfFeature> stopCodons = transcriptFeature.getFeatures(GtfConfig.TYPE_STOP_CODON_DEFAULT);

                if (!stopCodons.isEmpty()) {
                    cdsEnd = stopCodons.get(stopCodons.size()-1).getBaseData().getEnd()+1;
                }

                // determine utr regions

                ArrayList<Region> leftUtr = new ArrayList<>();
                ArrayList<Region> rightUtr = new ArrayList<>();

                for (int i = 0; i < exons.size(); i++) {

                    GtfFeature exon = exons.get(i);

                    // exon is entirely before cds
                    if (exon.getBaseData().getEnd() <= cdsStart) {
                        leftUtr.add(new Region(exon.getBaseData().getStart(), exon.getBaseData().getEnd()));
                        continue;
                    }
                    // exon is entirely after cds
                    if (exon.getBaseData().getStart() >= cdsEnd) {
                        rightUtr.add(new Region(exon.getBaseData().getStart(), exon.getBaseData().getEnd()));
                        continue;
                    }
                    // exon is entirely contained in cds
                    if (exon.getBaseData().getStart() >= cdsStart && exon.getBaseData().getEnd() <= cdsEnd) {
                        continue;
                    }
                    // exon contains the cds but has space to the left and right which is utr
                    if (exon.getBaseData().getStart() < cdsStart && exon.getBaseData().getEnd() > cdsEnd) {
                        leftUtr.add(new Region(exon.getBaseData().getStart(), cdsStart-1));
                        rightUtr.add(new Region(cdsEnd+1, exon.getBaseData().getEnd()));
                        continue;
                    }
                    // part of the exon is before cds
                    if (exon.getBaseData().getStart() < cdsStart && exon.getBaseData().getEnd() <= cdsEnd) {
                        leftUtr.add(new Region(exon.getBaseData().getStart(), cdsStart-1));
                        continue;
                    }
                    // part of the exon is after cds
                    if (exon.getBaseData().getStart() >= cdsStart && exon.getBaseData().getEnd() > cdsEnd) {
                        rightUtr.add(new Region(cdsEnd+1, exon.getBaseData().getEnd()));
                        continue;
                    }
                }

                for (Region r : leftUtr) {

                    GtfBaseData baseData = new GtfBaseData(
                            geneFeature.getBaseData().getContig(),
                            geneFeature.getBaseData().getSource(),
                            transcriptFeature.getBaseData().isForwardStrand() ? GtfConfig.TYPE_FIVE_PRIME_UTR_DEFAULT : GtfConfig.TYPE_THREE_PRIME_UTR_DEFAULT,
                            r.start(), r.end(),
                            null,
                            geneFeature.getBaseData().isForwardStrand(),
                            null,
                            new HashMap<>()
                    );

                    transcriptFeature.getFeatures().add(new GtfFeature(transcriptFeature.getFeatures().size(),
                            baseData));
                }

                for (Region r : rightUtr) {

                    GtfBaseData baseData = new GtfBaseData(
                            geneFeature.getBaseData().getContig(),
                            geneFeature.getBaseData().getSource(),
                            transcriptFeature.getBaseData().isForwardStrand() ? GtfConfig.TYPE_THREE_PRIME_UTR_DEFAULT : GtfConfig.TYPE_FIVE_PRIME_UTR_DEFAULT,
                            r.start(), r.end(),
                            null,
                            geneFeature.getBaseData().isForwardStrand(),
                            null,
                            new HashMap<>()
                    );

                    transcriptFeature.getFeatures().add(new GtfFeature(transcriptFeature.getFeatures().size(),
                            baseData));
                }
            }
        }

        Duration d = Duration.between(timeStart, Instant.now());
        this.report.timer.addInferUtrs(d);
    }
}

package com.github.kleinsamuel;

import java.util.HashSet;
import java.util.List;

public class GtfConfig {

    public static String TYPE_GENE_DEFAULT = "gene";
    public static HashSet<String> TYPE_GENE_SYNONYMS = new HashSet<>(List.of(new String[]{"gene", "Gene", "GENE"}));

    public static String TYPE_TRANSCRIPT_DEFAULT = "transcript";
    public static HashSet<String> typeSynonymsTranscript = new HashSet<>(List.of(new String[]{"transcript", "Transcript", "TRANSCRIPT"}));

    public static String TYPE_EXON_DEFAULT = "exon";
    public static HashSet<String> TYPE_EXON_SYNONYMS = new HashSet<>(List.of(new String[]{"exon", "Exon", "EXON"}));

    public static String TYPE_CDS_DEFAULT = "CDS";
    public static HashSet<String> TYPE_CDS_SYNONYMS = new HashSet<>(List.of(new String[]{"CDS", "cds", "Cds"}));

    public static String TYPE_START_CODON_DEFAULT = "start_codon";
    public static HashSet<String> TYPE_START_CODON_SYNONYMS = new HashSet<>(List.of(new String[]{"start_codon", "Start_codon", "START_CODON"}));

    public static String TYPE_STOP_CODON_DEFAULT = "stop_codon";
    public static HashSet<String> TYPE_STOP_CODON_SYNONYMS = new HashSet<>(List.of(new String[]{"stop_codon", "Stop_codon", "STOP_CODON"}));

    public static String TYPE_FIVE_PRIME_UTR_DEFAULT = "five_prime_utr";
    public static HashSet<String> TYPE_FIVE_PRIME_UTR_SYNONYMS = new HashSet<>(List.of(new String[]{"five_prime_utr", "Five_prime_utr", "FIVE_PRIME_UTR"}));

    public static String TYPE_THREE_PRIME_UTR_DEFAULT = "three_prime_utr";
    public static HashSet<String> TYPE_THREE_PRIME_UTR_SYNONYMS = new HashSet<>(List.of(new String[]{"three_prime_utr", "Three_prime_utr", "THREE_PRIME_UTR"}));

    public static String TYPE_SELENOCYSTEINE_DEFAULT = "selenocysteine";
    public static HashSet<String> TYPE_SELENOCYSTEINE_SYNONYMS = new HashSet<>(List.of(new String[]{"selenocysteine", "Selenocysteine", "SELENOCYSTEINE"}));

    public static String getDefault(String type) {
        if (TYPE_GENE_SYNONYMS.contains(type)) {
            return TYPE_GENE_DEFAULT;
        } else if (typeSynonymsTranscript.contains(type)) {
            return TYPE_TRANSCRIPT_DEFAULT;
        } else if (TYPE_EXON_SYNONYMS.contains(type)) {
            return TYPE_EXON_DEFAULT;
        } else if (TYPE_CDS_SYNONYMS.contains(type)) {
            return TYPE_CDS_DEFAULT;
        } else if (TYPE_START_CODON_SYNONYMS.contains(type)) {
            return TYPE_START_CODON_DEFAULT;
        } else if (TYPE_STOP_CODON_SYNONYMS.contains(type)) {
            return TYPE_STOP_CODON_DEFAULT;
        } else if (TYPE_FIVE_PRIME_UTR_SYNONYMS.contains(type)) {
            return TYPE_FIVE_PRIME_UTR_DEFAULT;
        } else if (TYPE_THREE_PRIME_UTR_SYNONYMS.contains(type)) {
            return TYPE_THREE_PRIME_UTR_DEFAULT;
        } else if (TYPE_SELENOCYSTEINE_SYNONYMS.contains(type)) {
            return TYPE_SELENOCYSTEINE_DEFAULT;
        } else {
            return null;
        }
    }
}

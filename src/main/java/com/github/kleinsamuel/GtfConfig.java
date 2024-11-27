package com.github.kleinsamuel;

import java.util.HashSet;
import java.util.List;

public class GtfConfig {

    public static String TYPE_GENE_DEFAULT = "gene";
    public static HashSet<String> TYPE_GENE_SYNONYMS = new HashSet<>(List.of(new String[]{
            "gene", "Gene", "GENE"}));

    public static String TYPE_TRANSCRIPT_DEFAULT = "transcript";
    public static HashSet<String> typeSynonymsTranscript = new HashSet<>(List.of(new String[]{
            "transcript", "Transcript", "TRANSCRIPT"}));

    public static String TYPE_EXON_DEFAULT = "exon";
    public static HashSet<String> TYPE_EXON_SYNONYMS = new HashSet<>(List.of(new String[]{
            "exon", "Exon", "EXON"}));

    public static String TYPE_CDS_DEFAULT = "CDS";
    public static HashSet<String> TYPE_CDS_SYNONYMS = new HashSet<>(List.of(new String[]{
            "CDS", "cds", "Cds"}));

    public static String TYPE_START_CODON_DEFAULT = "start_codon";
    public static HashSet<String> TYPE_START_CODON_SYNONYMS = new HashSet<>(List.of(new String[]{
            "start_codon", "Start_codon", "START_CODON", "START", "Start", "start"}));

    public static String TYPE_STOP_CODON_DEFAULT = "stop_codon";
    public static HashSet<String> TYPE_STOP_CODON_SYNONYMS = new HashSet<>(List.of(new String[]{
            "stop_codon", "Stop_codon", "STOP_CODON", "STOP", "Stop", "stop"}));

    public static String TYPE_FIVE_PRIME_UTR_DEFAULT = "five_prime_utr";
    public static HashSet<String> TYPE_FIVE_PRIME_UTR_SYNONYMS = new HashSet<>(List.of(new String[]{
            "five_prime_utr", "Five_prime_utr", "FIVE_PRIME_UTR", "5_prime_utr", "5_Prime_utr", "5_prime_UTR", "5_PRIME_UTR",
            "5'utr", "5'UTR", "5'Utr", "5_UTR", "5_utr", "5utr", "5UTR", "5Utr", "5UTR"}));

    public static String TYPE_THREE_PRIME_UTR_DEFAULT = "three_prime_utr";
    public static HashSet<String> TYPE_THREE_PRIME_UTR_SYNONYMS = new HashSet<>(List.of(new String[]{
            "three_prime_utr", "Three_prime_utr", "THREE_PRIME_UTR", "3_prime_utr", "3_Prime_utr", "3_prime_UTR", "3_PRIME_UTR",
            "3'utr", "3'UTR", "3'Utr", "3_UTR", "3_utr", "3utr", "3UTR", "3Utr", "3UTR"}));

    public static String TYPE_UTR_DEFAULT = "UTR";
    public static HashSet<String> TYPE_UTR_SYNONYMS = new HashSet<>(List.of(new String[]{
            "UTR", "utr", "Utr"}));

    public static String TYPE_SELENOCYSTEINE_DEFAULT = "selenocysteine";
    public static HashSet<String> TYPE_SELENOCYSTEINE_SYNONYMS = new HashSet<>(List.of(new String[]{
            "selenocysteine", "Selenocysteine", "SELENOCYSTEINE"}));

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
        } else if (TYPE_UTR_SYNONYMS.contains(type)) {
            return TYPE_UTR_DEFAULT;
        } else {
            return null;
        }
    }
}

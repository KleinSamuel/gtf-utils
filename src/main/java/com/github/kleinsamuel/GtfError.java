package com.github.kleinsamuel;

public enum GtfError {

    INVALID_NUMBER_OF_COLUMNS(0, "Wrong number of columns in line"),

    MISSING_VALUE_CONTIG(0, "Contig (1st column) is missing"),
    MISSING_VALUE_SOURCE(1, "Source (2nd column) is missing"),
    MISSING_VALUE_TYPE(0, "Type (3rd column) is missing"),
    MISSING_VALUE_START_POSITION(0, "Start position (4th column) is missing"),
    MISSING_VALUE_END_POSITION(0, "End position (5th column) is missing"),
    MISSING_VALUE_SCORE(1, "Score (6th column) is missing"),
    MISSING_VALUE_STRAND(0, "Strand (7th column) is missing"),
    MISSING_VALUE_FRAME(1, "Frame (8th column) is missing"),

    INVALID_VALUE_START_POSITION(0, "Start position is not a valid number"),
    INVALID_VALUE_END_POSITION(0, "End position is not a valid number"),
    INVALID_VALUE_TYPE(0, "Type is not valid"),
    INVALID_VALUE_STRAND(0, "Strand is not valid"),
    INVALID_VALUE_FRAME(0, "Frame is not valid"),
    INVALID_VALUE_SCORE(0, "Score is not valid"),

    NOT_DEFAULT_TYPE(1, "Type is not default"),

    MALFORMED_ATTRIBUTES(0, "Attributes are malformed"),
    MALFORMED_ATTRIBUTES_AMBIGUOUS_KEY(1, "Attributes contain ambiguous key"),
    MALFORMED_ATTRIBUTES_TEXTUAL_VALUE_NOT_ENCLOSED(1, "Textual attribute value is not enclosed in double quotes"),

    ATTRIBUTE_GENE_ID_MISSING(1, "Gene ID attribute is missing"),
    ATTRIBUTE_GENE_ID_AMBIGUOUS(0, "Gene ID attribute is ambiguous"),

    ATTRIBUTE_TRANSCRIPT_ID_MISSING(1, "Transcript ID attribute is missing"),
    ATTRIBUTE_TRANSCRIPT_ID_AMBIGUOUS(0, "Transcript ID attribute is ambiguous"),

    ATTRIBUTE_GENE_ID_NOT_FIRST(1, "Gene ID attribute is not the first attribute"),
    ATTRIBUTE_TRANSCRIPT_ID_NOT_SECOND(1, "Transcript ID attribute is not the second attribute"),

    PARENT_GENE_AND_TRANSCRIPT_MISMATCH(0, "Parent gene and transcript do not match"),

    PARENT_MISSING_GENE(1, "Missing its parent gene feature"),
    PARENT_MISSING_TRANSCRIPT(1, "Missing its parent transcript feature"),

    DUPLICATE_GENE_ENTRY(0, "Multiple features of type gene with the same gene_id found"),
    DUPLICATE_TRANSCRIPT_ENTRY(0, "Multiple features of type transcript with the same transcript_id found"),

    FEATURE_START_BEFORE_PARENT(0, "Feature starts before parent feature"),
    FEATURE_END_AFTER_PARENT(0, "Feature ends after parent feature"),

    BOUNDS_LARGER_THAN_CHILDREN(1, "Bounds of this parent are larger than children bounds");

    // 0 = error, 1 = warning
    private final int severity;
    private final String message;

    GtfError(int severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public int getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return severity == 0;
    }

    public boolean isWarning() {
        return severity == 1;
    }

}

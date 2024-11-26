package com.github.kleinsamuel;

public class InvalidLine {

    private String line;
    private GtfError reason;

    public InvalidLine(String line, GtfError reason) {
        this.line = line;
        this.reason = reason;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public GtfError getReason() {
        return reason;
    }

    public void setReason(GtfError reason) {
        this.reason = reason;
    }
}

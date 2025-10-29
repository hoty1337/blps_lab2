package com.djeno.model;

public class AnalysisReport {
    private final boolean success;
    private final String message;

    public AnalysisReport(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

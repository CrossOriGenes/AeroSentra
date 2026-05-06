package com.example.aerosentra.models.response;

import java.util.List;

public class AlertReportDetailsResponse {
    private boolean success;
    private Report report;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    // -------------- Report ------------------
    public static class Report {
        private String summary;
        private String details;
        private List<String> precautions;
        private List<String> highlights;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public List<String> getPrecautions() { return precautions; }
        public void setPrecautions(List<String> precautions) { this.precautions = precautions; }

        public List<String> getHighlights() { return highlights; }
        public void setHighlights(List<String> highlights) { this.highlights = highlights; }
    }

}

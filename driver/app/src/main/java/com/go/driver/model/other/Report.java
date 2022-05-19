package com.go.driver.model.other;

import java.io.Serializable;
import java.sql.Timestamp;

public class Report implements Serializable {

    private String      id;
    private String      reportFrom;
    private String      tripId;
    private String      reportedBy;
    private String      reportedTo;
    private String      issue;
    private String      details;
    private Timestamp   reportedAt;

    public Report() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportFrom() {
        return reportFrom;
    }

    public void setReportFrom(String reportFrom) {
        this.reportFrom = reportFrom;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReportedTo() {
        return reportedTo;
    }

    public void setReportedTo(String reportedTo) {
        this.reportedTo = reportedTo;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getReportedAt() {
        return reportedAt == null ? 0 : reportedAt.getTime();
    }

    public void setReportedAt(Long reportedAt) {
        this.reportedAt = reportedAt == null ? null : new Timestamp(reportedAt);
    }
}

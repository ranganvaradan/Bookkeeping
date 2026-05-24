package com.billiontech.bookkeeping.controller.dto;

import com.billiontech.bookkeeping.entity.AgentJob;

import java.time.OffsetDateTime;
import java.util.UUID;

public class JobResponse {

    private UUID id;
    private String status;
    private String resultSummary;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;

    public static JobResponse from(AgentJob job) {
        JobResponse r = new JobResponse();
        r.id = job.getId();
        r.status = job.getStatus();
        r.resultSummary = job.getResultSummary();
        r.startedAt = job.getStartedAt();
        r.completedAt = job.getCompletedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public String getResultSummary() { return resultSummary; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
}

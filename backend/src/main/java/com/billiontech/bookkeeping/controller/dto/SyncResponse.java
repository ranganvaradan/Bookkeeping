package com.billiontech.bookkeeping.controller.dto;

import java.util.UUID;

public class SyncResponse {

    private UUID jobId;

    public SyncResponse(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
}

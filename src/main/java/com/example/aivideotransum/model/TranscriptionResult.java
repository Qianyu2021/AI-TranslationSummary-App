package com.example.aivideotransum.model;

public class TranscriptionResult {
    private final String jobId;
    private final String result;
    private final JobStatus status;

    public TranscriptionResult(String jobId, String result, JobStatus status) {
        this.jobId = jobId;
        this.result = result;
        this.status = status;
    }

     // Getters
     public String getJobId() { return jobId; }
     public String getResult() { return result; }
     public JobStatus getStatus() { return status; }
}

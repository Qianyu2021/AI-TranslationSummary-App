package com.example.aivideotransum.model;

public class TranscriptionJob {
    private final String jobId;
    private final ProcessingRequest request;
    private JobStatus status;
    private String result;

    public TranscriptionJob(String jobId, ProcessingRequest request){
   
        this.request = request;
        this.status = JobStatus.PROCESSING;
        this.jobId = jobId;
    }

    public String getJobId() { return jobId; }
    public ProcessingRequest getRequest() { return request; }
    public JobStatus getJobStatus() { return status; }
    public String getResult() { return result;}

    public void setStatus(JobStatus status){
        this.status = status;
    }
    public void setResult(String result) { this.result = result; }
}

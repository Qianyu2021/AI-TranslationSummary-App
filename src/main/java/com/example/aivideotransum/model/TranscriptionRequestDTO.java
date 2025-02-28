package com.example.aivideotransum.model;

public class TranscriptionRequestDTO {
    private String jobId;
    private String url;
    private JobAction action;
    private String targetLanguage;

    public TranscriptionRequestDTO(String jobId, ProcessingRequest request) {
        this.jobId = jobId;
        this.url = request.getUrl();
        this.action = request.getAction();
        this.targetLanguage = request.getTargetLanguage();
    }

    public String getJobId() { return jobId; }
    public String getUrl() { return url; }
    public JobAction getAction() { return action; }
    public String getTargetLanguage() { return targetLanguage; }
}

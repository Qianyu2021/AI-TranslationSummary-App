package com.example.aivideotransum.model;

public class ProcessingRequest {
    private String url;
    private JobAction action; // "SUMMARIZE" or "TRANSLATE"
    private String targetLanguage; // Required for translation
   
    public ProcessingRequest(String url, JobAction action, String targetLanguage) {
        if (action == JobAction.TRANSLATE && (targetLanguage == null || targetLanguage.isBlank())) {
            throw new IllegalArgumentException("Target language required for translation.");
        }
        this.url = url;
        this.action = action;
        this.targetLanguage = targetLanguage;

    }

    // Getters
    public String getUrl() { return url; }
    public JobAction getAction() { return action; }
    public String getTargetLanguage() { return targetLanguage; }
   //ng getJobId() {return jobId; }
    public void setTargetLanguage(String targetLanguage) { 
        this.targetLanguage = targetLanguage; 
    }
}


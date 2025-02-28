package com.example.aivideotransum.service;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import com.example.aivideotransum.model.*;

@Component
public class JobTracker {
    private final ConcurrentHashMap<String, TranscriptionJob> jobs = new ConcurrentHashMap<>();

    public void addRequest(TranscriptionJob job){
        jobs.put(job.getJobId(), job);
    }

    public TranscriptionJob getJob(String jobId) {
        return jobs.get(jobId);
    }

    public void updateJob(TranscriptionJob newjob){
//need to add exception
        if(newjob == null) {
            throw new IllegalArgumentException("job is not valid!");
        }
        jobs.put(newjob.getJobId(), newjob);
        
    }
}
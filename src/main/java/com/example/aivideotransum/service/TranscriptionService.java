package com.example.aivideotransum.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.example.aivideotransum.model.*;
import java.util.UUID;

@Service
public class TranscriptionService {

    private final WebClient webClient;
    private final JobTracker jobtracker;

    public TranscriptionService(WebClient webClient, JobTracker jobtracker){
        this.webClient = webClient;
        this.jobtracker = jobtracker;
    }

    public Mono<String> submitTranscriptionJob(ProcessingRequest request){
        
        // Validate translation action
        if (request.getAction() == JobAction.TRANSLATE && 
            (request.getTargetLanguage() == null || request.getTargetLanguage().isBlank())) {
            return Mono.error(new IllegalArgumentException(
                "targetLanguage is required for translation"
            ));
        }

        String jobId = UUID.randomUUID().toString(); 
        TranscriptionJob job = new TranscriptionJob(jobId, request);
        jobtracker.addRequest(job);
        TranscriptionRequestDTO requestBody = new TranscriptionRequestDTO(jobId, request);
       
        return webClient.post()
                .uri("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(result -> {
                    job.setResult(result);
                    job.setStatus(JobStatus.PROCESSING);
                    jobtracker.updateJob(job);
                })
                .onErrorResume(error -> {
                    job.setStatus(JobStatus.FAILED);
                    jobtracker.updateJob(job);
                    return Mono.error(error);
                })
                .thenReturn(job.getJobId());
    }

    public Mono<TranscriptionResult> getJobResult(String jobId){
        return Mono.fromSupplier(()-> {
            TranscriptionJob job = jobtracker.getJob(jobId);
            return (job != null) ?
                    new TranscriptionResult(job.getJobId(), job.getResult(), job.getJobStatus()):
                    null;
        }).switchIfEmpty((Mono.error(new RuntimeException("job not found"))));
    }
}
            
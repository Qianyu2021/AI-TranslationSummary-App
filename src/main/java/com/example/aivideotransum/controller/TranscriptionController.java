
package com.example.aivideotransum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import com.example.aivideotransum.service.*;
import com.example.aivideotransum.model.*;



@RestController
@RequestMapping("/api/jobs")
public class TranscriptionController {
    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService){
        this.transcriptionService = transcriptionService;
    }

    @PostMapping
    public Mono<String> submitJob(@RequestBody ProcessingRequest request){
        return transcriptionService.submitTranscriptionJob(request)
                .onErrorResume(IllegalArgumentException.class, 
                    e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage())));
    }

    @GetMapping("/{jobId}")
    public Mono<TranscriptionResult> getJobStatus(@PathVariable String jobId) {
        return transcriptionService.getJobResult(jobId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
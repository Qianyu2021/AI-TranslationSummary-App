package com.example.aivideotransum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private static final String PYTHON_MICROSERVICE_URL = "http://localhost:5001";

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(PYTHON_MICROSERVICE_URL).build();
    }
}

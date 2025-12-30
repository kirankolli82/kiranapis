package com.kiran.musicapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quote-client")
public record QuoteClientProperties(String baseUrl, String gcpProjectId, String apiTokenSecretId) {
}

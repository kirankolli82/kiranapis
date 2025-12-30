package com.kiran.musicapi.stockdata.api.client;

import com.kiran.musicapi.config.QuoteClientProperties;
import com.kiran.musicapi.config.SecretManagerService;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class QuoteClientRequestFilter implements ClientRequestFilter {
	private final QuoteClientProperties quoteClientProperties;
	private final SecretManagerService secretManagerService;

	@Override
	public void filter(ClientRequestContext requestContext) {
		// Fetch API token from Google Cloud Secret Manager
		String apiToken = secretManagerService.getSecret(quoteClientProperties.gcpProjectId(),
				quoteClientProperties.apiTokenSecretId());

		URI uri = requestContext.getUri();
		URI newUri = UriBuilder.fromUri(uri).queryParam("api_token", apiToken).build();
		requestContext.setUri(newUri);
	}
}

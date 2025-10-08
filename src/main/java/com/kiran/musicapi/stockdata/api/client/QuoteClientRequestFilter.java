package com.kiran.musicapi.stockdata.api.client;

import com.kiran.musicapi.config.QuoteClientProperties;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
@AllArgsConstructor
public class QuoteClientRequestFilter implements ClientRequestFilter {
    private final QuoteClientProperties quoteClientProperties;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        URI uri = requestContext.getUri();
        URI newUri = UriBuilder.fromUri(uri)
                .queryParam("api_token", quoteClientProperties.apiToken())
                .build();
        requestContext.setUri(newUri);
    }
}

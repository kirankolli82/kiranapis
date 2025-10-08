package com.kiran.musicapi.config;

import com.kiran.musicapi.stockdata.api.client.QuoteClient;
import com.kiran.musicapi.stockdata.api.client.QuoteClientRequestFilter;
import com.kiran.musicapi.utils.RestClient;
import jakarta.ws.rs.client.ClientBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.reflections.Reflections;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@EnableConfigurationProperties(QuoteClientProperties.class)
@AllArgsConstructor
public class StockdataConfig {

    private final QuoteClientProperties quoteClientProperties;
    private final QuoteClientRequestFilter quoteClientRequestFilter;

    @Bean
    public QuoteClient quoteClient(){
        var client = ClientBuilder.newBuilder()
                .register(quoteClientRequestFilter).build();
        var target = client.target(quoteClientProperties.baseUrl());
        return ((ResteasyWebTarget) target).proxy(QuoteClient.class);
    }


}

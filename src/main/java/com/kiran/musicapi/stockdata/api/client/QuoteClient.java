package com.kiran.musicapi.stockdata.api.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

//@RestClient
public interface QuoteClient {
    @Path("data/quote")
    @GET
    String getQuote(@QueryParam("symbols") String symbol,
                    @QueryParam("key_by_ticker") Boolean keyByTicker);
}

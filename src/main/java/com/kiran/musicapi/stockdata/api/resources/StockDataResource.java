package com.kiran.musicapi.stockdata.api.resources;

import com.kiran.musicapi.stockdata.api.client.QuoteClient;
import com.kiran.musicapi.stockdata.api.contract.StockApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class StockDataResource {

	private final QuoteClient quoteClient;

	public StockDataResource(QuoteClient quoteClient) {
		this.quoteClient = quoteClient;
	}

	@GetMapping("/quotes") // NVDA,MSFT,AAPL,GOOGL,AMZN,META,TSLA
	public StockApiResponse quotes() {
		log.info("Fetching quotes");
		return quoteClient.getQuote("NVDA,MSFT,AAPL", true);
	}
}

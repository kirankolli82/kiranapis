package com.kiran.musicapi.stockdata.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Quote {
    private final String ticker;
    private final String name;
    private final String exchange_short;
    private final String exchange_long;
    private final String mic_code;
    private final String currency;
    private final Double price;
    private final Double day_high;
    private final Double day_low;
    private final Double day_open;
    private final Double _52_week_high;
    private final Double _52_week_low;
    private final Double market_cap;
    private final Double previous_close_price;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime previous_close_price_time;
    private final Double day_change;
    private final Long volume;
    private final Boolean is_extended_hours_price;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime last_trade_time;

    @JsonCreator
    public Quote(
            @JsonProperty("ticker") String ticker,
            @JsonProperty("name") String name,
            @JsonProperty("exchange_short") String exchange_short,
            @JsonProperty("exchange_long") String exchange_long,
            @JsonProperty("mic_code") String mic_code,
            @JsonProperty("currency") String currency,
            @JsonProperty("price") Double price,
            @JsonProperty("day_high") Double day_high,
            @JsonProperty("day_low") Double day_low,
            @JsonProperty("day_open") Double day_open,
            @JsonProperty("52_week_high") Double _52_week_high,
            @JsonProperty("52_week_low") Double _52_week_low,
            @JsonProperty("market_cap") Double market_cap,
            @JsonProperty("previous_close_price") Double previous_close_price,
            @JsonProperty("previous_close_price_time") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS") LocalDateTime previous_close_price_time,
            @JsonProperty("day_change") Double day_change,
            @JsonProperty("volume") Long volume,
            @JsonProperty("is_extended_hours_price") Boolean is_extended_hours_price,
            @JsonProperty("last_trade_time") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS") LocalDateTime last_trade_time
    ) {
        this.ticker = ticker;
        this.name = name;
        this.exchange_short = exchange_short;
        this.exchange_long = exchange_long;
        this.mic_code = mic_code;
        this.currency = currency;
        this.price = price;
        this.day_high = day_high;
        this.day_low = day_low;
        this.day_open = day_open;
        this._52_week_high = _52_week_high;
        this._52_week_low = _52_week_low;
        this.market_cap = market_cap;
        this.previous_close_price = previous_close_price;
        this.previous_close_price_time = previous_close_price_time;
        this.day_change = day_change;
        this.volume = volume;
        this.is_extended_hours_price = is_extended_hours_price;
        this.last_trade_time = last_trade_time;
    }

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getExchange_short() { return exchange_short; }
    public String getExchange_long() { return exchange_long; }
    public String getMic_code() { return mic_code; }
    public String getCurrency() { return currency; }
    public Double getPrice() { return price; }
    public Double getDay_high() { return day_high; }
    public Double getDay_low() { return day_low; }
    public Double getDay_open() { return day_open; }
    @JsonProperty("52_week_high")
    public Double get52_week_high() { return _52_week_high; }
    @JsonProperty("52_week_low")
    public Double get52_week_low() { return _52_week_low; }
    public Double getMarket_cap() { return market_cap; }
    public Double getPrevious_close_price() { return previous_close_price; }
    public LocalDateTime getPrevious_close_price_time() { return previous_close_price_time; }
    public Double getDay_change() { return day_change; }
    public Long getVolume() { return volume; }
    public Boolean getIs_extended_hours_price() { return is_extended_hours_price; }
    public LocalDateTime getLast_trade_time() { return last_trade_time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return Objects.equals(ticker, quote.ticker) && Objects.equals(name, quote.name) &&
                Objects.equals(mic_code, quote.mic_code) && Objects.equals(currency, quote.currency) &&
                Objects.equals(price, quote.price) && Objects.equals(previous_close_price, quote.previous_close_price) &&
                Objects.equals(previous_close_price_time, quote.previous_close_price_time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, name, mic_code, currency, price, previous_close_price, previous_close_price_time);
    }

    @Override
    public String toString() {
        return "Quote{" +
                "ticker='" + ticker + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", last_trade_time=" + last_trade_time +
                '}';
    }
}

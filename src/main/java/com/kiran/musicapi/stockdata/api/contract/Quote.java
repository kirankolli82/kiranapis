package com.kiran.musicapi.stockdata.api.contract;

import com.fasterxml.jackson.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@EqualsAndHashCode
@ToString
public final class Quote {
	private final String ticker;
	private final String name;
	@JsonProperty("exchange_short")
	private final String exchangeShort;
	@JsonProperty("exchange_long")
	private final String exchangeLong;
	@JsonProperty("mic_code")
	private final String micCode;
	private final String currency;
	@JsonProperty("price")
	@EqualsAndHashCode.Exclude
	private final BigDecimal price;
	@JsonProperty("day_high")
	@EqualsAndHashCode.Exclude
	private final BigDecimal dayHigh;
	@JsonProperty("day_low")
	@EqualsAndHashCode.Exclude
	private final BigDecimal dayLow;
	@JsonProperty("day_open")
	@EqualsAndHashCode.Exclude
	private final BigDecimal dayOpen;
	@JsonProperty("52_week_high")
	@EqualsAndHashCode.Exclude
	private final BigDecimal week52High;
	@JsonProperty("52_week_low")
	@EqualsAndHashCode.Exclude
	private final BigDecimal week52Low;
	@JsonProperty("market_cap")
	@EqualsAndHashCode.Exclude
	private final BigDecimal marketCap;
	@JsonProperty("previous_close_price")
	@EqualsAndHashCode.Exclude
	private final BigDecimal previousClosePrice;
	@JsonProperty("previous_close_price_time")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
	private final LocalDateTime previousClosePriceTime;
	@JsonProperty("day_change")
	@EqualsAndHashCode.Exclude
	private final BigDecimal dayChange;
	private final Long volume;
	@JsonProperty("is_extended_hours_price")
	private final Boolean isExtendedHoursPrice;
	@JsonProperty("last_trade_time")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
	private final LocalDateTime lastTradeTime;

	@JsonCreator
	public Quote(@JsonProperty("ticker") String ticker, @JsonProperty("name") String name,
			@JsonProperty("exchange_short") String exchangeShort, @JsonProperty("exchange_long") String exchangeLong,
			@JsonProperty("mic_code") String micCode, @JsonProperty("currency") String currency,
			@JsonProperty("price") BigDecimal price, @JsonProperty("day_high") BigDecimal dayHigh,
			@JsonProperty("day_low") BigDecimal dayLow, @JsonProperty("day_open") BigDecimal dayOpen,
			@JsonProperty("52_week_high") BigDecimal week52High, @JsonProperty("52_week_low") BigDecimal week52Low,
			@JsonProperty("market_cap") BigDecimal marketCap,
			@JsonProperty("previous_close_price") BigDecimal previousClosePrice,
			@JsonProperty("previous_close_price_time") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS") LocalDateTime previousClosePriceTime,
			@JsonProperty("day_change") BigDecimal dayChange, @JsonProperty("volume") Long volume,
			@JsonProperty("is_extended_hours_price") Boolean isExtendedHoursPrice,
			@JsonProperty("last_trade_time") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS") LocalDateTime lastTradeTime) {
		this.ticker = ticker;
		this.name = name;
		this.exchangeShort = exchangeShort;
		this.exchangeLong = exchangeLong;
		this.micCode = micCode;
		this.currency = currency;
		this.price = price;
		this.dayHigh = dayHigh;
		this.dayLow = dayLow;
		this.dayOpen = dayOpen;
		this.week52High = week52High;
		this.week52Low = week52Low;
		this.marketCap = marketCap;
		this.previousClosePrice = previousClosePrice;
		this.previousClosePriceTime = previousClosePriceTime;
		this.dayChange = dayChange;
		this.volume = volume;
		this.isExtendedHoursPrice = isExtendedHoursPrice;
		this.lastTradeTime = lastTradeTime;
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal priceForEquals() {
		return normaliseForTrailingZeros(price);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal dayHighForEquals() {
		return normaliseForTrailingZeros(dayHigh);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal dayLowForEquals() {
		return normaliseForTrailingZeros(dayLow);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal dayOpenForEquals() {
		return normaliseForTrailingZeros(dayOpen);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal week52HighForEquals() {
		return normaliseForTrailingZeros(week52High);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal week52LowForEquals() {
		return normaliseForTrailingZeros(week52Low);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal marketCapForEquals() {
		return normaliseForTrailingZeros(marketCap);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal previousClosePriceForEquals() {
		return normaliseForTrailingZeros(previousClosePrice);
	}

	@JsonIgnore
	@EqualsAndHashCode.Include
	public BigDecimal dayChangeForEquals() {
		return normaliseForTrailingZeros(dayChange);
	}

	private static BigDecimal normaliseForTrailingZeros(BigDecimal bd) {
		return bd == null ? null : bd.stripTrailingZeros();
	}

}

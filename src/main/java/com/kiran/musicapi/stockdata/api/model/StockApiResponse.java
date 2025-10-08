package com.kiran.musicapi.stockdata.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/** Immutable POJO representing the root response for the stock API. */
public final class StockApiResponse {
    private final Meta meta;
    private final Map<String, Quote> data;

    @JsonCreator
    public StockApiResponse(@JsonProperty("meta") Meta meta,
                            @JsonProperty("data") Map<String, Quote> data) {
        this.meta = meta;
        this.data = data == null ? null : java.util.Collections.unmodifiableMap(data);
    }

    public Meta getMeta() {
        return meta;
    }

    public Map<String, Quote> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockApiResponse that = (StockApiResponse) o;
        return Objects.equals(meta, that.meta) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meta, data);
    }

    @Override
    public String toString() {
        return "StockApiResponse{" +
                "meta=" + meta +
                ", data=" + data +
                '}';
    }
}

package com.kiran.musicapi.stockdata.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class Meta {
    private final int requested;
    private final int returned;

    @JsonCreator
    public Meta(@JsonProperty("requested") int requested,
                @JsonProperty("returned") int returned) {
        this.requested = requested;
        this.returned = returned;
    }

    public int getRequested() {
        return requested;
    }

    public int getReturned() {
        return returned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meta meta = (Meta) o;
        return requested == meta.requested && returned == meta.returned;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requested, returned);
    }

    @Override
    public String toString() {
        return "Meta{" +
                "requested=" + requested +
                ", returned=" + returned +
                '}';
    }
}

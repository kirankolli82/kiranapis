package com.kiran.musicapi.stockdata.api.contract;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class Meta {
	private final int requested;
	private final int returned;

	@JsonCreator
	public Meta(@JsonProperty("requested") int requested, @JsonProperty("returned") int returned) {
		this.requested = requested;
		this.returned = returned;
	}
}

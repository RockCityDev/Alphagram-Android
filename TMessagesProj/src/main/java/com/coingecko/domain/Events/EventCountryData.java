package com.coingecko.domain.Events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventCountryData {
    @JsonProperty("country")
    private String country;
    @JsonProperty("code")
    private String code;

}

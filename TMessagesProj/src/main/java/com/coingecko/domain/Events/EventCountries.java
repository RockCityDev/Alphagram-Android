package com.coingecko.domain.Events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventCountries {
    @JsonProperty("data")
    private List<EventCountryData> data;
    @JsonProperty("count")
    private String count;

}

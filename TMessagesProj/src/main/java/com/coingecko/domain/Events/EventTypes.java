package com.coingecko.domain.Events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventTypes {
    @JsonProperty("data")
    private List<String> data;
    @JsonProperty("count")
    private long count;

}

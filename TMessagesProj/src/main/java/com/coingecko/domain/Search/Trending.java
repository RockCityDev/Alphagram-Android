package com.coingecko.domain.Search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trending {
    @JsonProperty("coins")
    private List<TrendingCoin> coins = null;
    @JsonProperty("exchanges")
    private List<Object> exchanges = null;

}

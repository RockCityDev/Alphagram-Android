package com.coingecko.domain.Coins.CoinData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReposUrl {
    @JsonProperty("github")
    private List<String> github;
    @JsonProperty("bitbucket")
    private List<Object> bitbucket;

}

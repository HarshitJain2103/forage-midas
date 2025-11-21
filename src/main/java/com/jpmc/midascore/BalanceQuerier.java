package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Balance;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BalanceQuerier {

    private final RestTemplate restTemplate;
    private static final String BALANCE_URL = "http://localhost:33400/balance";

    public BalanceQuerier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Balance query(Long userId) {
        try {
            String url = BALANCE_URL + "?userId=" + userId;
            return restTemplate.getForObject(url, Balance.class);
        } catch (Exception e) {
            System.err.println("Error querying balance for userId " + userId + ": " + e.getMessage());
            return new Balance(0);
        }
    }
}
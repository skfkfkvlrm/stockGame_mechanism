package com.skfkfkvlrm.stockgame_spring.domain.stock;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure POJO Stock Order Matching Engine.
 * Independent of Spring Framework or Database Repositories.
 */
public class OrderMatcher {

    public MatchResult match(int requestedAmount, List<Order> counterOrders) {
        if (counterOrders == null || counterOrders.isEmpty() || requestedAmount <= 0) {
            return MatchResult.builder()
                    .matches(List.of())
                    .remainingAmount(requestedAmount)
                    .build();
        }

        List<MatchItem> matches = new ArrayList<>();
        int remainingAmount = requestedAmount;

        for (Order counterOrder : counterOrders) {
            if (remainingAmount <= 0) {
                break;
            }

            int matchAmount = Math.min(remainingAmount, counterOrder.getAmount());
            boolean fullyMatched = (matchAmount == counterOrder.getAmount());

            matches.add(MatchItem.builder()
                    .counterOrder(counterOrder)
                    .matchAmount(matchAmount)
                    .matchPrice(counterOrder.getPrice())
                    .fullyMatched(fullyMatched)
                    .build());

            remainingAmount -= matchAmount;
        }

        return MatchResult.builder()
                .matches(matches)
                .remainingAmount(remainingAmount)
                .build();
    }
}

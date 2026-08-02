package com.skfkfkvlrm.stockservice.domain.stock;

import java.util.ArrayList;
import java.util.List;

public class OrderMatcher {

    public MatchResult match(int requestedAmount, List<Order> counterOrders) {
        int remaining = requestedAmount;
        List<MatchItem> matches = new ArrayList<>();

        if (counterOrders == null || counterOrders.isEmpty()) {
            return new MatchResult(matches, remaining);
        }

        for (Order counter : counterOrders) {
            if (remaining <= 0) break;

            int matchAmount = Math.min(remaining, counter.getAmount());
            boolean isFullyMatched = (matchAmount == counter.getAmount());
            matches.add(new MatchItem(counter, matchAmount, counter.getPrice(), isFullyMatched));
            remaining -= matchAmount;
        }

        return new MatchResult(matches, remaining);
    }
}

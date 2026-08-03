package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchItem {
    private final Order counterOrder;
    private final int matchAmount;
    private final int matchPrice;
    private final boolean fullyMatched;

    public MatchItem(Order counterOrder, int matchAmount, int matchPrice, boolean fullyMatched) {
        this.counterOrder = counterOrder;
        this.matchAmount = matchAmount;
        this.matchPrice = matchPrice;
        this.fullyMatched = fullyMatched;
    }

    public Order getCounterOrder() { return counterOrder; }
    public int getMatchAmount() { return matchAmount; }
    public int getMatchPrice() { return matchPrice; }
    public boolean isFullyMatched() { return fullyMatched; }

    public int getMatchTotalPrice() {
        return matchPrice * matchAmount;
    }
}

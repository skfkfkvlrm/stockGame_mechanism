package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchItem {
    private Order counterOrder;
    private int matchAmount;
    private int matchPrice;
    private boolean fullyMatched;

    public int getMatchTotalPrice() {
        return matchAmount * matchPrice;
    }
}

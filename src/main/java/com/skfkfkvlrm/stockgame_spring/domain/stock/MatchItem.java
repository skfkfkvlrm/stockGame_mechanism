package com.skfkfkvlrm.stockgame_spring.domain.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchItem {
    private final Order counterOrder;
    private final int matchAmount;
    private final int matchPrice;
    private final boolean fullyMatched;

    public int getMatchTotalPrice() {
        return matchPrice * matchAmount;
    }
}

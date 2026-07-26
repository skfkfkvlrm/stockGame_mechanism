package com.skfkfkvlrm.stockgame_spring.domain.stock;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MatchResult {
    private final List<MatchItem> matches;
    private final int remainingAmount;

    public boolean hasMatches() {
        return matches != null && !matches.isEmpty();
    }

    public boolean isFullyMatched() {
        return remainingAmount == 0;
    }
}

package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MatchResult {
    private final List<MatchItem> matches;
    private final int remainingAmount;

    public MatchResult(List<MatchItem> matches, int remainingAmount) {
        this.matches = matches;
        this.remainingAmount = remainingAmount;
    }

    public List<MatchItem> getMatches() { return matches; }
    public int getRemainingAmount() { return remainingAmount; }

    public boolean hasMatches() {
        return matches != null && !matches.isEmpty();
    }

    public boolean isFullyMatched() {
        return remainingAmount == 0;
    }
}

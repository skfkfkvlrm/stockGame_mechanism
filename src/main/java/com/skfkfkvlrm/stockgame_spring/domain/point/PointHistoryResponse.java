package com.skfkfkvlrm.stockgame_spring.domain.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponse {
    private LocalDateTime historyDate;
    private String historyType;
    private String historyContent;
    private int pointChange;
}

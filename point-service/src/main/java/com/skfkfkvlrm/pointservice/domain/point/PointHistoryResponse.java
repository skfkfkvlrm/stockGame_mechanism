package com.skfkfkvlrm.pointservice.domain.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponse {
    private String historyDate;
    private String historyType;
    private String historyContent;
    private int pointChange;
}

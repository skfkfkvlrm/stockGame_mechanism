package com.skfkfkvlrm.pointservice.domain.point;

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
    private int historyId;
    private String studentId;
    private int amount;
    private String type;
    private String description;
    private LocalDateTime createdAt;
}

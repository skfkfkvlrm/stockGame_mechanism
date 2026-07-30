package com.skfkfkvlrm.stockgame_spring.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointAdjustmentRequest {
    private int amount;
    private String reason;
}

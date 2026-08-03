package com.skfkfkvlrm.stockservice.domain.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequest {
    private String name;
    private String content;
    private int publicationPrice;
    private int publicationBalance;
}

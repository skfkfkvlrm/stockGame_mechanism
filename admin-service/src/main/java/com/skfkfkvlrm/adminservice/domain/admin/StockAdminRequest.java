package com.skfkfkvlrm.adminservice.domain.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdminRequest {
    private String name;
    private String content;
    private int publicationPrice;
    private int publicationBalance;
    private String status;
}

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
public class StockRequest {
    private String name;
    private String content;
    private int publicationPrice;
    private int publicationBalance;
    private String status; // 'LISTED' | 'SUSPENDED' | 'DELISTED'

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getPublicationPrice() { return publicationPrice; }
    public void setPublicationPrice(int publicationPrice) { this.publicationPrice = publicationPrice; }
    public int getPublicationBalance() { return publicationBalance; }
    public void setPublicationBalance(int publicationBalance) { this.publicationBalance = publicationBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

package com.skfkfkvlrm.stockservice.domain.stock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    private String name;
    private String category;

    @Column(name = "publication_price")
    private int publicationPrice;

    @Column(name = "publication_amount")
    private int publicationAmount;

    @Column(name = "publication_balance")
    private int publicationBalance;

    private String description;
    private String status;
}

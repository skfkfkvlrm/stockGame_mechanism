package com.skfkfkvlrm.stockgame_spring.domain.stock;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int stockId;
    private String name;
    private String content;
    private int publicationBalance;
    private int publicationPrice;
    private int prevPrice;
    @CreationTimestamp
    private LocalDateTime createdDate;
}

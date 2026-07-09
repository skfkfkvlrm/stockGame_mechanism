package com.skfkfkvlrm.stockgame_spring.domain.point;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stock_transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @Column(name = "sell_order_id")
    private int sellOrderId;
    @Column(name = "buy_order_id")
    private int buyOrderId;
    private int amount;
    private int price;
}

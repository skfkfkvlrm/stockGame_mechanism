package com.skfkfkvlrm.stockservice.domain.stock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus content;

    @Enumerated(EnumType.STRING)
    private OrderStatus state;

    private int price;
    private int amount;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "stock_id")
    private int stockId;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}

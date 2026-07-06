package com.skfkfkvlrm.stockgame_spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;
    @Enumerated(EnumType.STRING)
    private OrderStatus content;
    private int price;
    private int amount;
    @Enumerated(EnumType.STRING)
    private OrderStatus state;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    private LocalDateTime deletedDate;
    private String studentId;
    private int stockId;


    @Override
    public String toString() {
        return "[id=" + orderId + ", content=" + content + ", price=" + price + ", amount=" + amount
                + ", state=" + state + ", createdDate=" + createdDate + "]";
    }
}
package com.skfkfkvlrm.stockgame_spring.domain.point;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "get_points")
public class GetPoint {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int getPointId;
    private int studentId;
    private String content;
    private int point;
    @CreationTimestamp
    private LocalDateTime createdDate;
}

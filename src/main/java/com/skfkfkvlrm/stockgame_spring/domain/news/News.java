package com.skfkfkvlrm.stockgame_spring.domain.news;

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
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int newsId;
    private String content;
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Override
    public String toString() {
        return "[newsNO=" + newsId + ", content=" + content + "]";
    }
}

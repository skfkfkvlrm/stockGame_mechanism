package com.skfkfkvlrm.stockgame_spring.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRankingResponse {
    private int rank;
    private String studentId;
    private String name;
    private int grade;
    private String className;
    private int classNumber;
    private int totalPoint;
}

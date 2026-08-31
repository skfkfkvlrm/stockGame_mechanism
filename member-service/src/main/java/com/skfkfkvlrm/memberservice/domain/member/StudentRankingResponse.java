package com.skfkfkvlrm.memberservice.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentRankingResponse {
    private Integer id;
    private int rank;
    private String studentId;
    private String name;
    private int grade;
    private String className;
    private int classNumber;
    private int totalPoint;
    private int totalCoupon;
}

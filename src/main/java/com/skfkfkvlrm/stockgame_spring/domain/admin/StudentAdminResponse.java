package com.skfkfkvlrm.stockgame_spring.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAdminResponse {
    private int id;
    private String studentId;
    private String name;
    private int grade;
    private String className;
    private int classNumber;
    private int totalPoint;
    private int totalCoupon;
}

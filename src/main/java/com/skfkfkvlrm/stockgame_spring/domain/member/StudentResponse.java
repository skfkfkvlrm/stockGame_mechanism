package com.skfkfkvlrm.stockgame_spring.domain.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StudentResponse {
    private String studentId;
    private String name;
    private int grade;
    private String className;
    private int classNumber;
    private int totalPoint;
    private String token;
}

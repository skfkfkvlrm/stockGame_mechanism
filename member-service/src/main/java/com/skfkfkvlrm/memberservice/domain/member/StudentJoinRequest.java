package com.skfkfkvlrm.memberservice.domain.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentJoinRequest {
    private String studentId;
    private String password;
    private String name;
    private int grade;
    private String className;
    private int classNumber;
}

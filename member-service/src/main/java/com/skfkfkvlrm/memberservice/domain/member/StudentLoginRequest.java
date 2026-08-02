package com.skfkfkvlrm.memberservice.domain.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentLoginRequest {
    private String studentId;
    private String password;
}

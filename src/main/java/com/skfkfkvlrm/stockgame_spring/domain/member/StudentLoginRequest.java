package com.skfkfkvlrm.stockgame_spring.domain.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StudentLoginRequest {
    private String studentId;
    private String password;
}

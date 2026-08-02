package com.skfkfkvlrm.memberservice.domain.member;

import java.util.List;

public interface MemberService {
    boolean join(StudentJoinRequest request);
    StudentResponse login(StudentLoginRequest request);
    boolean getIdCheck(String studentId);
    List<StudentRankingResponse> getStudentRanking();
    StudentResponse getMemberInfo(String studentId);
}

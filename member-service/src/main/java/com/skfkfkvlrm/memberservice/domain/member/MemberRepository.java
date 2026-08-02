package com.skfkfkvlrm.memberservice.domain.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface MemberRepository {
    int setMember(StudentJoinRequest request);
    Map<String, Object> findByStudentId(@Param("studentId") String studentId);
    int getIdCheck(@Param("studentId") String studentId);
    List<StudentRankingResponse> getStudentRanking();
}

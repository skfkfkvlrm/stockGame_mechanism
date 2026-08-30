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
    int updateStudentPoint(@Param("amount") int amount, @Param("studentId") String studentId);
    int insertGetPoint(@Param("studentId") String studentId, @Param("point") int point, @Param("content") String content);
    int deleteStudent(@Param("studentId") String studentId);
}

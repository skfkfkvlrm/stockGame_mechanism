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

    // 신규 추가: 자산 안전 검증 및 데모/세션 동기화 메서드
    Integer getStudentPoint(@Param("studentId") String studentId);
    int resetDemoStudent(@Param("studentId") String studentId);
    int clearGetPoints(@Param("studentId") String studentId);
    int resetAllStudents(@Param("defaultPoint") int defaultPoint);
    int restoreDeletedStudents();
    int clearAllStudentCoupons();
    List<Map<String, Object>> getAdminPointAdjustments();
}

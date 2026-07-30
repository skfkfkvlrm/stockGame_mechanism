package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.skfkfkvlrm.stockgame_spring.domain.member.StudentJoinRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface MemberRepository {
    // 회원가입
    int setMember(StudentJoinRequest request);

    // 로그인
    Map<String, Object> login(@org.apache.ibatis.annotations.Param("studentId") String studentId, @org.apache.ibatis.annotations.Param("password") String password);

    // 아이디로 단건 조회 (BCrypt 로그인용)
    Map<String, Object> findByStudentId(String studentId);

    // 비밀번호 업데이트 (마이그레이션용)
    void updatePassword(@org.apache.ibatis.annotations.Param("studentId") String studentId, @org.apache.ibatis.annotations.Param("password") String password);

    // 아이디 중복체크
    int getIdCheck(String studentId);

    // 관리자용 전체 학생 목록 조회
    java.util.List<com.skfkfkvlrm.stockgame_spring.domain.admin.StudentAdminResponse> getAllStudents();

    // 학생 총 포인트 순위 목록 조회
    java.util.List<StudentRankingResponse> getStudentRanking();

    // 포인트 지급/차감
    int updateStudentPoint(@org.apache.ibatis.annotations.Param("studentId") String studentId, @org.apache.ibatis.annotations.Param("amount") int amount);

    // 포인트 지급/차감 이력 생성
    int insertPointHistory(@org.apache.ibatis.annotations.Param("studentId") String studentId, @org.apache.ibatis.annotations.Param("amount") int amount, @org.apache.ibatis.annotations.Param("reason") String reason);
}
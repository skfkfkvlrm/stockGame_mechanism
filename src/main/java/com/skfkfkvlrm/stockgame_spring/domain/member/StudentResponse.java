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
    private String role;

    public StudentResponse() {}

    public StudentResponse(String studentId, String name, int grade, String className, int classNumber, int totalPoint, String token, String role) {
        this.studentId = studentId;
        this.name = name;
        this.grade = grade;
        this.className = className;
        this.classNumber = classNumber;
        this.totalPoint = totalPoint;
        this.token = token;
        this.role = role;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getClassNumber() { return classNumber; }
    public void setClassNumber(int classNumber) { this.classNumber = classNumber; }
    public int getTotalPoint() { return totalPoint; }
    public void setTotalPoint(int totalPoint) { this.totalPoint = totalPoint; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

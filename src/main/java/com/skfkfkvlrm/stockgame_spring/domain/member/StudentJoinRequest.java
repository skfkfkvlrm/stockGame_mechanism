package com.skfkfkvlrm.stockgame_spring.domain.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StudentJoinRequest {
    private String studentId;
    private String password;
    private String name;
    private int grade;
    private String className;
    private int classNumber;

    public StudentJoinRequest() {}

    public StudentJoinRequest(String studentId, String password, String name, int grade, String className, int classNumber) {
        this.studentId = studentId;
        this.password = password;
        this.name = name;
        this.grade = grade;
        this.className = className;
        this.classNumber = classNumber;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getClassNumber() { return classNumber; }
    public void setClassNumber(int classNumber) { this.classNumber = classNumber; }
}

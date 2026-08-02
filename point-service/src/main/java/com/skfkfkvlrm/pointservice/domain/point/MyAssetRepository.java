package com.skfkfkvlrm.pointservice.domain.point;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;

@Mapper
public interface MyAssetRepository {
    Map<String, Object> getStudentAssetInfo(@Param("studentId") String studentId);
    Integer getStudentPoint(@Param("studentId") String studentId);
    void setStudentPointDown(@Param("amount") int amount, @Param("studentId") String studentId);
    void setStudentPointUp(@Param("amount") int amount, @Param("studentId") String studentId);
}

package com.skfkfkvlrm.pointservice.domain.point;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MyPointHistoryRepository {
    List<PointHistoryResponse> getMyPointHistoryList(@Param("studentId") String studentId);
}

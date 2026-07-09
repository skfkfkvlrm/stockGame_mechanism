package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.skfkfkvlrm.stockgame_spring.domain.point.PointHistoryResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyPointHistoryRepository {
    List<PointHistoryResponse> getMyPointHistoryList(String studentId);
}

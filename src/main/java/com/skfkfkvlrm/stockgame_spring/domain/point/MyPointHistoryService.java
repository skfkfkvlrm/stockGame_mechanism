package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.skfkfkvlrm.stockgame_spring.domain.point.PointHistoryResponse;

import java.util.List;

public interface MyPointHistoryService {
    List<PointHistoryResponse> getMyPointHistoryList(String studentId);
}

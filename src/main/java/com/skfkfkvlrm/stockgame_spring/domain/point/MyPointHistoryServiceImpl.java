package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.skfkfkvlrm.stockgame_spring.domain.point.PointHistoryResponse;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyPointHistoryRepository;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyPointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPointHistoryServiceImpl implements MyPointHistoryService {
    private final MyPointHistoryRepository myPointHistoryRepository;

    @Override
    public List<PointHistoryResponse> getMyPointHistoryList(String studentId) {
        return myPointHistoryRepository.getMyPointHistoryList(studentId);
    }
}

package com.skfkfkvlrm.pointservice.domain.point;

import com.skfkfkvlrm.pointservice.domain.common.StockInfoResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface MyAssetRepository {
    Map<String, Object> getStudentAssetInfo(@Param("studentId") String studentId);
    Integer getStudentPoint(@Param("studentId") String studentId);
    void setStudentPointDown(@Param("amount") int amount, @Param("studentId") String studentId);
    void setStudentPointUp(@Param("amount") int amount, @Param("studentId") String studentId);

    Integer getPointValue(@Param("studentId") String studentId);
    Integer getTotalCoupon(@Param("studentId") String studentId);
    List<Integer> getMyStockNos(@Param("studentId") String studentId, @Param("state") String state);
    int getStockAmount(@Param("studentId") String studentId, @Param("stockId") int stockId, @Param("state") String state);
    String getStockName(@Param("stockId") int stockId);
    int getStockCurrentPrice(@Param("stockId") int stockId);
    int getAveragePrice(@Param("studentId") String studentId, @Param("stockId") int stockId, @Param("state") String state, @Param("content") String content);
    int getPurchasePrice(@Param("studentId") String studentId, @Param("stockId") int stockId, @Param("state") String state, @Param("content") String content);
    int getStockProfit(@Param("studentId") String studentId, @Param("stockId") int stockId, @Param("state") String state);

    List<StockInfoResponse> getMyStockAssetList(@Param("studentId") String studentId);
}

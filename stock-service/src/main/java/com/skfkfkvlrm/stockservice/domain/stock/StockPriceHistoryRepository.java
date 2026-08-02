package com.skfkfkvlrm.stockservice.domain.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    @Modifying
    @Query(value = "INSERT INTO stock_price_history (stock_id, trade_date, close_price, volume, created_date) " +
                   "VALUES (:stockId, :tradeDate, :price, :volume, NOW()) " +
                   "ON DUPLICATE KEY UPDATE close_price = :price, volume = volume + :volume", nativeQuery = true)
    void upsertDailyPrice(@Param("stockId") int stockId, 
                          @Param("tradeDate") LocalDate tradeDate, 
                          @Param("price") int price, 
                          @Param("volume") int volume);
}

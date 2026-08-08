# 순수 유저 간 거래량(발행잔량 제외) 기준 정렬 및 분리 표기 조치 보고서

## 1. 개요
기존 거래량 정렬 조건에서 최초 발행잔량(`pubAmount`)이 거래량에 포함되어 정렬되던 로직을 개편하였습니다.  
발행잔량을 제외하고 **발행잔량 소진 후 유저 간(P2P) 체결된 순수 실제 누적 거래량(`tradeVolume`)만을 추출하여 정렬**하도록 백엔드와 프론트엔드를 모두 수정했습니다.

---

## 2. 주요 수정 내역

1. **백엔드 DB 쿼리 및 DTO 추가 (`stock-service`)**:
   - `stockDetailMapper.xml`에 `getTradeVolume` 쿼리 신설 (`stock_transactions` 테이블 조인을 통해 실제 유저 간 누적 체결 수량 `SUM(t.amount)` 집계).
   - `StockDetailResponse.java` DTO에 `tradeVolume` 필드 추가 및 반환.

2. **프론트엔드 정렬 로직 및 UI 분리 (`StockList.jsx`)**:
   - 거래량 정렬 조건(`VOLUME`) 클릭 시 `pubAmount`(발행 잔량)가 아닌 **`tradeVolume`(유저 간 순수 체결 거래량)** 기준으로 내림차순 정렬되도록 변경.
   - 주식 목록 테이블 헤더를 **`발행 잔량`**과 **`거래량 (체결)`** 2개 컬럼으로 명확히 구분하여 표기.

---

## 3. 검증 결과

- **`GET /api/stock` API 엔드포인트 검증**:
  - `pubAmount`: `50` (발행 잔량)
  - `tradeVolume`: `0` (유저 간 체결 시 자동 누적 증가)
- **프론트엔드 프로덕션 빌드 검증 (`npm run build`)**: `built in 339ms` (에러 0건)

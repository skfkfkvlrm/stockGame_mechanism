# 🧪 E2E & Verification Test Scripts

이 디렉토리는 `stockGame_mechanism` 시스템의 DB 쿼리 검증, 자산 변동 그래프 계산 및 Puppeteer 기반 E2E 화면 캡처 테스트 스크립트를 보관합니다.

## 📋 파일별 역할

| 파일명 | 실행 환경 | 주요 역할 |
|---|---|---|
| `test_graph.js` | Node.js (`mysql2`) | 학생 포인트 원장(지급, 차감, 쿠폰, 매수/매도, 예약, 취소) 누적 계산 무결성 검증 |
| `test_graph2.js` | Node.js (`mysql2`) | 종목명 조인 및 세부 내역 매핑을 포함한 자산 시계열 데이터 쿼리 검증 |
| `test_graph3.js` | Node.js (`mysql2`) | 대시보드 자산 그래프 시계열 필터링 및 누적 잔액 시뮬레이션 |
| `capture_all.js` | Node.js (`puppeteer-core`) | 학생/관리자 로그인부터 주문, 대시보드까지 전체 화면 15종 일괄 자동 캡처 |
| `debug_stock_detail.js` | Node.js (`puppeteer-core`) | 종목 상세 페이지 및 호가창 컴포넌트 렌더링/에러 로깅 검증 |

## 🚀 실행 방법

```bash
# MySQL 컨테이너 (Port: 3307) 구동 상태에서 실행
node scripts/tests/test_graph.js

# 전체 화면 캡처 실행 (Chrome 브라우저 필요)
node scripts/tests/capture_all.js
```

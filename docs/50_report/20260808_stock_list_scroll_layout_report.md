# 주식 목록 스크롤 적용 및 사이드바 상하 높이 맞춤 보고서

## 1. 개요
21개 신규 종목 추가로 인해 주식 목록이 화면 하단으로 무한히 길어지던 현상을 조치하였습니다.  
사이드바(`Sidebar.css`)와 메인 패널(`MainLayout.jsx`)의 높이를 **`calc(100vh - 40px)`**로 통일시키고, 주식 테이블 패널(`StockList.css`) 내부에 전용 스크롤 및 **헤더 고정(Sticky Header)** 기능을 적용했습니다.

---

## 2. 주요 수정 내역

1. **메인 영역 상하 높이 일치화 (`MainLayout.jsx`)**:
   - `<main className="content glass-panel">` 영역에 `height: calc(100vh - 40px)`를 부여하여 좌측 사이드바와 상하 길이가 정확히 일치하도록 조치.

2. **주식 테이블 내부 스크롤 및 고정 헤더 적용 (`StockList.css`)**:
   - `.stock-table-wrapper`에 `max-height: 520px; overflow-y: auto;`를 적용하여 테이블 영역 내에서만 부드럽게 스크롤되도록 설정.
   - `.stock-table th`에 `position: sticky; top: 0; background: #ffffff; z-index: 10;`을 적용하여 스크롤 시에도 종목명/현재가/등락률 등 컬럼 헤더가 상단에 계속 고정되도록 디자인 개선.

---

## 3. 검증 결과

- **프론트엔드 프로덕션 빌드 (`npm run build`)**: `built in 377ms` (0 errors)
- 사이드바와 본문 영역의 상하 높이가 일치하며, 21개 종목 목록이 내부 스크롤로 깔끔하게 정돈되어 편안한 사용자 경험을 제공합니다.

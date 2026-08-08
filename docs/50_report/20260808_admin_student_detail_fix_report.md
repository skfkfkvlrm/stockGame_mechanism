# 관리자 패널 학생 상세보기 연동 수정 보고서

## 1. 문제 개요
관리자 패널(`AdminDashboard.jsx`)의 학생 목록에서 학생 클릭 시 열리는 상세보기 포트폴리오 모달창에서 학생의 자산/포인트/보유 주식 정보가 404 및 데이터 미연동으로 인해 표시되지 않는 현상을 조치했습니다.

---

## 2. 원인 및 조치 내역

1. **`point-service` 관리자 전용 학생 자산 대시보드 API 신설 (`MyAssetController.java`)**
   - GET `/api/asset/admin/students/{targetStudentId}/detail` 엔드포인트를 추가하여 대상 학생의 총자산(`totalAsset`), 보유 포인트(`totalPoint`), 총 수익률(`totalProfit`), 보유 주식 리스트(`myStocks`)를 반환하도록 조치했습니다.

2. **프론트엔드 연동 경로 및 데이터 바인딩 수정 (`AdminDashboard.jsx`)**
   - 기존의 존재하지 않던 `/admin/students/${student.studentId}/detail` 경로를 `/asset/admin/students/${student.studentId}/detail` 경로로 수정했습니다.
   - 응답 구조(`res.data.data`)를 학생 상세 모달 컴포넌트(`studentDetailData`)에 바인딩하여 보유 포인트, 평가액, 투자 수익률, 보유 주식 리스트가 모달 내에 즉시 표시되도록 반영했습니다.

---

## 3. 자가 검증 결과 (E2E 테스트)

- **학생 상세보기 API 호출 (`GET /api/asset/admin/students/testcoupon1/detail`)**:
  - `HTTP 200 OK`
  - 응답 데이터: `totalAsset: 27000`, `totalPoint: 27000`, `totalCoupon: 3`, `totalProfit: 0`, `myStocks: [...]` 정상 반환 확인.

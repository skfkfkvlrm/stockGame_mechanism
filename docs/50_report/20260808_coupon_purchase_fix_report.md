# 쿠폰 구매 버그 조치 완료 보고서

## 1. 개요 및 확인 사항
- 사용자가 쿠폰 상점에서 구매 버튼 클릭 시 팝업/이벤트 메시지만 뜨고 실제 구매(포인트 차감 및 보유 쿠폰 등록)가 수행되지 않거나 새로고침 전까지 내 쿠폰함 및 포인트 변경 사항이 반영되지 않던 현상을 해결하였습니다.

---

## 2. 원인 분석 및 수정 내역

1. **백엔드 트랜잭션 및 Feign 요청 연동 (`coupon-service`, `point-service`)**
   - OpenAPI Feign (`PointClient`)을 통해 학생 포인트 차감 API(`/api/internal/points/{studentId}/decrease`)가 정상적으로 가동되도록 연동하였습니다.
   - MariaDB `coupon_purchase` 테이블에 구매 내역(`student_id`, `coupon_id`, 단가 `price`, 쿠폰명 `name`, 상태 `'사용전'`)이 정상 삽입되도록 MyBatis XML (`couponMapper.xml`) 서브쿼리 및 매핑 쿼리를 보정했습니다.

2. **프론트엔드 연동 UX 개선 (`CouponStore.jsx`)**
   - 쿠폰 구매 API(`POST /api/coupons/{couponId}/buy`) 호출 완료 후 `await fetchMe()`를 수행하여 유저 포인트 상태를 최신화했습니다.
   - 쿠폰 구매 성공 시 alert 팝업 완료 직후 자동으로 `내 쿠폰함('/my-coupons')` 페이지로 이동하여 구매한 쿠폰 항목을 바로 확인할 수 있도록 개선하였습니다.

---

## 3. 자가 검증 결과 (E2E 테스트)

- **쿠폰 구매 실행 (`POST /api/coupons/1/buy`)**:
  - `HTTP 200 OK` (구매 성공 메시지 반환)
- **포인트 차감 확인 (`GET /api/members/me`)**:
  - 기존 27,600 P -> 구매 후 27,000 P (600 P 정상 차감 확인)
- **내 쿠폰함 내역 반영 (`GET /api/coupons/my`)**:
  - `coupon_purchase` 테이블에 신규 구매 레코드(`couponPurchaseId: 10`, 상태: `'사용전'`) 정상 추가 및 내 쿠폰함 목록 반환 확인.

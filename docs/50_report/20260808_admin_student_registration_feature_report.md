# 관리자 패널 신규 학생 등록 기능 추가 작업 보고서

## 1. 개요
관리자/교사 전용 대시보드(`AdminDashboard.jsx`)에서 신규 학생 계정을 손쉽게 추가할 수 있는 **"신규 학생 등록" 기능**을 신설하였습니다.  
학생 등록 시 설정된 기본 포인트 정책에 따라 **100,000 P 시드머니가 자동 지급**됩니다.

---

## 2. 주요 수정 내역

1. **백엔드 기본 시드머니 수정 (`member-service`)**:
   - `memberMapper.xml` 내 `setMember` 회원가입 인서트 쿼리의 기본 포인트 생성 값을 기존 `30000`에서 **`100000` P**로 일치화.

2. **프론트엔드 관리자 대시보드 UI/모달 추가 (`AdminDashboard.jsx`)**:
   - 학생 관리 탭 상단 검색바 우측에 **[➕ 신규 학생 등록]** 버튼 추가.
   - 클릭 시 모달이 팝업되어 **학생 아이디, 비밀번호, 이름, 학년, 반, 번호**를 입력할 수 있는 폼 제공.
   - 등록 완료 시 알림 메세지 표출 및 학생 목록 즉시 갱신 처리.

---

## 3. 검증 결과

- **API 신규 학생 가입 테스트 (`POST /api/members/join`)**:
  - `studentId: "test_student99"`, `name: "테스트학생"`, `grade: 5`, `className: "2"`, `classNumber: 15`
  - 응답: `{ "success": true, "message": "회원가입 성공", "data": true }`
- **DB 데이터 검증 (MariaDB)**:
  - `student_id`: `test_student99`
  - `total_point`: `100,000 P` 정수값 정상 초기화 확인
- **프론트엔드 프로덕션 빌드 (`npm run build`)**: `built in 325ms` (0 errors)

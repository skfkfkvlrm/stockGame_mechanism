# 학생 계정 탈퇴 시스템 고도화 및 데이터 정합성 보정 리포트

**작성일시:** 2026-08-30

## 1. 개요 및 배경
기존 시스템에서 학생 계정 삭제 시, students 테이블에서 데이터를 물리적으로 삭제(DELETE)하는 방식을 사용했습니다.
이로 인해 삭제된 학생이 생전에 걸어둔 대기 주문(orders.status = 'WAITING')이나 보유 중인 주식 데이터가 삭제되지 않고 고아 데이터(Orphan Data)로 DB에 남아 호가창을 무한정 차지하는 심각한 논리적 오류가 발생했습니다.

## 2. 신규 시스템 (논리 삭제 및 자산 자동 청산 파이프라인)
데이터 정합성을 보장하기 위해 다음과 같이 시스템을 대폭 개편했습니다.

### (1) Member Service (회원 관리)
- 계정 삭제 로직을 물리적 DELETE 쿼리에서 **논리 삭제** (UPDATE students SET status = 'DELETED') 로 전환.
- 랭킹 조회 및 단건 조회 시 status = 'ACTIVE' 인 사용자만 노출되도록 필터링 추가.
- 회원가입 시 탈퇴한 계정 아이디 재가입을 방지하는 중복 체크 방어 로직 구현.

### (2) Stock Service & Admin Service (자산 청산)
- **자동 청산 파이프라인**: 관리자가 학생을 삭제할 때 Admin Service가 OpenFeign을 통해 Stock Service의 liquidateStudentAssets API를 호출.
- **주문 강제 취소**: 탈퇴 학생의 모든 대기 주문(WAITING)을 CANCELLED로 변경하여 호가창에서 즉시 제거.
- **유령 주식 환수**: 탈퇴 학생이 보유 중인 주식(MATCHED)을 시장(SYSTEM_LP)으로 강제 귀속시켜 유통량 왜곡 방지.
- **포인트 몰수**: 학생의 잔여 포인트를 0으로 초기화하고, get_points 테이블에 청산 내역을 남겨 감사 로그(Audit) 기능 확보.

## 3. 예외 상황 발생 및 데이터 교정(Cleansing) 조치
신규 로직 배포 및 서버 재구동 전, 관리자 페이지에서 계정 삭제 버튼을 클릭하여 기존의 물리 삭제 로직이 실행되는 휴먼 에러가 발생했습니다.

- **증상**: 계정(ex. 310번)이 DB에서 물리 삭제되었으나, 해당 계정이 1000원 호가에 50주 매수를 걸어둔 대기 주문이 호가창에 고정되어 증발하지 않는 현상 발현.
- **해결 방안 (수동 쿼리 개입)**:
  - orders 테이블을 탐색하여, student_id가 현재 students 테이블에 존재하지 않는 모든 유령 계정의 데이터 탐지.
  - 대기 주문 강제 취소: UPDATE orders SET state = 'CANCELLED' WHERE state = 'WAITING' AND student_id NOT IN (SELECT student_id FROM students) AND student_id != 'SYSTEM_LP';
  - 유령 보유 주식 환수: UPDATE orders SET student_id = 'SYSTEM_LP' WHERE state = 'MATCHED' AND student_id NOT IN (SELECT student_id FROM students) AND student_id != 'SYSTEM_LP';
- **결과**: 유령 주문이 말끔히 취소되며 호가창 정상화 완료. 현재는 모든 서버가 정상 재구동되어 이후부터는 시스템 파이프라인에 의해 자동 청산됨을 확인함.

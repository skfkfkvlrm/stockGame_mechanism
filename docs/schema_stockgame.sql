-- ========================================================
-- StockGame (학급 모의투자 플랫폼) - Complete Database Schema DDL
-- Target DBMS: MySQL 8.0+ / MariaDB 10.5+ / ERDCloud Import
-- Tables: students, coupons, coupon_purchase, get_points,
--         stocks, orders, stock_transactions, stock_price_history, news
-- Performance Indexes: idx_orders_matching, idx_students_ranking etc.
-- ========================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS stock_transactions;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS stock_price_history;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS coupon_purchase;
DROP TABLE IF EXISTS coupons;
DROP TABLE IF EXISTS get_points;
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS students;

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================
-- 1. 학생 정보 테이블 (students)
-- 회원 계정, 로그인 인증, 학년/반/번호 정보 및 보유 자산 관리
-- ========================================================
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '학생 고유 식별자 PK',
    student_id VARCHAR(100) NOT NULL UNIQUE COMMENT '로그인 아이디 / 학번 (유니크)',
    password VARCHAR(100) NOT NULL COMMENT '암호화된 비밀번호 (BCrypt)',
    name VARCHAR(50) NOT NULL COMMENT '학생 성명',
    grade INT NOT NULL COMMENT '학년 (1~6학년)',
    class_name VARCHAR(50) NOT NULL COMMENT '학급명 (1~15반)',
    class_number INT NOT NULL COMMENT '출석 번호',
    register_year INT NOT NULL COMMENT '학급 등록 년도 (예: 2026)',
    total_coupon INT NOT NULL DEFAULT 0 COMMENT '보유 사용 전 쿠폰 수',
    total_point INT NOT NULL DEFAULT 100000 COMMENT '보유 가용 포인트 (초기 자본금 10만 P)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태 (ACTIVE, DELETED, SUSPENDED)',
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_STUDENT' COMMENT '접근 권한 (ROLE_STUDENT, ROLE_ADMIN)',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 일시',
    updated_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '계정 정보 수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학생 회원 정보 및 보유 자산 마스터 테이블';

CREATE INDEX idx_students_student_id ON students (student_id);
CREATE INDEX idx_students_ranking ON students (total_point DESC, student_id ASC);
CREATE INDEX idx_students_class ON students (grade, class_name, class_number);

-- ========================================================
-- 2. 상점 쿠폰 기본 정보 테이블 (coupons)
-- 학급 상점에서 판매하는 보상 아이템(숙제 면제권, 자리 변경권 등)
-- ========================================================
CREATE TABLE coupons (
    coupon_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '쿠폰 고유 식별자 PK',
    coupon_code VARCHAR(50) NULL COMMENT '쿠폰 식별 코드',
    name VARCHAR(100) NOT NULL COMMENT '쿠폰 상품명 (예: 숙제 1회 면제권)',
    price INT NOT NULL COMMENT '구매 필요 포인트',
    status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE' COMMENT '판매 상태 (ON_SALE, SOLD_OUT, STOPPED)',
    student_id VARCHAR(100) NULL COMMENT '등록 관리자 식별자',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '쿠폰 등록 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학급 보상 쿠폰 마스터 테이블';

CREATE INDEX idx_coupons_status ON coupons (status);

-- ========================================================
-- 3. 학생 쿠폰 구매 및 사용 내역 테이블 (coupon_purchase)
-- 포인트로 구매한 쿠폰의 보유 현황 및 실제 사용 상태 관리
-- ========================================================
CREATE TABLE coupon_purchase (
    purchase_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '쿠폰 구매 내역 식별자 PK',
    coupon_id INT NOT NULL COMMENT '구매한 쿠폰 FK',
    student_id VARCHAR(100) NOT NULL COMMENT '구매한 학생 식별자 / FK',
    name VARCHAR(100) NOT NULL COMMENT '구매 당시 쿠폰명',
    price INT NOT NULL COMMENT '구매 당시 결제 포인트',
    state VARCHAR(100) NOT NULL DEFAULT '사용전' COMMENT '사용 상태 (사용전, 사용, 취소)',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '쿠폰 구매 일시',
    updated_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '쿠폰 사용 처리 일시',
    CONSTRAINT fk_coupon_purchase_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학생 쿠폰 구매 및 사용 내역 테이블';

CREATE INDEX idx_coupon_purchase_student_date ON coupon_purchase (student_id, created_date DESC);
CREATE INDEX idx_coupon_purchase_coupon_id ON coupon_purchase (coupon_id);
CREATE INDEX idx_coupon_purchase_state ON coupon_purchase (state);

-- ========================================================
-- 4. 포인트 변동 이력 테이블 (get_points)
-- 교사/관리자의 상벌점 지급/차감 및 시스템 정산 내역
-- ========================================================
CREATE TABLE get_points (
    get_point_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 변동 이력 식별자 PK',
    student_id VARCHAR(100) NOT NULL COMMENT '대상 학생 식별자',
    point INT NOT NULL COMMENT '변동 포인트 (+지급, -차감)',
    content VARCHAR(300) NULL COMMENT '지급/차감 사유 (예: 1학기 청소 우수, 퀴즈 1등)',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '포인트 변동 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학생 포인트 지급 및 차감 변동 이력 테이블';

CREATE INDEX idx_get_points_student_date ON get_points (student_id, created_date DESC);

-- ========================================================
-- 5. 주식 종목 기본 정보 테이블 (stocks)
-- 가상 상장 기업 정보, 발행가, 전일종가, 상장/휴장 상태
-- ========================================================
CREATE TABLE stocks (
    stock_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '주식 종목 고유 식별자 PK',
    name VARCHAR(100) NOT NULL COMMENT '종목명 (예: 새콤달콤, SM엔터테인먼트, 학교매점)',
    content VARCHAR(100) NULL COMMENT '종목 설명 및 업종 카테고리',
    publication_balance INT NOT NULL DEFAULT 0 COMMENT '발행 주식 총 잔여 물량',
    publication_price INT NOT NULL COMMENT '최초 공모/상장 발행가',
    prev_price INT NULL COMMENT '전일 종가 (기준가 계산용)',
    ref_price INT NULL COMMENT 'VI 발동 기준가 / 임시 기준가',
    market_status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '시장 상태 (OPEN, CLOSED)',
    status VARCHAR(20) NOT NULL DEFAULT 'LISTED' COMMENT '상장 상태 (LISTED, DELISTED, SUSPENDED)',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '종목 상장 등록 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 종목 마스터 정보 테이블';

CREATE INDEX idx_stocks_status ON stocks (status);
CREATE INDEX idx_stocks_name ON stocks (name);
CREATE INDEX idx_stocks_market_status ON stocks (market_status);

-- ========================================================
-- 6. 주식 주문 요청 내역 테이블 (orders)
-- 지정가 매수/매도 주문 요청 및 미체결 호가 잔량
-- ========================================================
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '주문 고유 식별자 PK',
    student_id VARCHAR(100) NOT NULL COMMENT '주문 요청 학생 식별자',
    stock_id INT NOT NULL COMMENT '대상 주식 종목 FK',
    content VARCHAR(100) NOT NULL COMMENT '주문 유형 (매수, 매도, BUY, SELL)',
    price INT NOT NULL COMMENT '주문 지정가 (호가)',
    amount INT NOT NULL COMMENT '주문 요청/미체결 잔여 수량',
    state VARCHAR(100) NOT NULL DEFAULT '대기' COMMENT '주문 상태 (대기, 체결, 취소, 부분체결)',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '주문 접수 일시',
    updated_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '주문 체결/수정 일시',
    deleted_date DATETIME NULL COMMENT '주문 취소 일시',
    CONSTRAINT fk_orders_stock FOREIGN KEY (stock_id) REFERENCES stocks (stock_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 매수/매도 주문 요청 및 호가 대기 테이블';

-- 핵심 매칭 엔진 최적화 복합 인덱스 (OrderMatcher 탐색 속도 극대화)
CREATE INDEX idx_orders_matching ON orders (stock_id, state, content, price, created_date);
-- 학생별 보유 주식 및 주문 조회 최적화 인덱스
CREATE INDEX idx_orders_student_stock_state ON orders (student_id, stock_id, state);
CREATE INDEX idx_orders_created_date ON orders (created_date);

-- ========================================================
-- 7. 주식 체결 거래 내역 테이블 (stock_transactions)
-- 매수-매도 주문 간 매칭 엔진에 의해 성사된 실제 체결 기록
-- ========================================================
CREATE TABLE stock_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '체결 거래 고유 식별자 PK',
    buy_order_id INT NOT NULL COMMENT '체결 매수 주문 FK',
    sell_order_id INT NULL COMMENT '체결 매도 주문 FK (시스템 발행주 매수 시 NULL)',
    amount INT NOT NULL COMMENT '체결된 주식 수량',
    price INT NOT NULL COMMENT '체결 단가',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '체결 완료 일시',
    CONSTRAINT fk_transactions_buy_order FOREIGN KEY (buy_order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_sell_order FOREIGN KEY (sell_order_id) REFERENCES orders (order_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 거래 체결 완료 내역 테이블';

CREATE INDEX idx_transactions_buy_order ON stock_transactions (buy_order_id, created_date);
CREATE INDEX idx_transactions_sell_order ON stock_transactions (sell_order_id, created_date);
CREATE INDEX idx_transactions_created_date ON stock_transactions (created_date DESC);

-- ========================================================
-- 8. 주식 가격 일별/시계열 변동 히스토리 테이블 (stock_price_history)
-- ApexCharts 캔들스틱 및 일봉/분봉 시세 데이터 (OHLCV)
-- ========================================================
CREATE TABLE stock_price_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '시세 히스토리 고유 식별자 PK',
    stock_id INT NOT NULL COMMENT '주식 종목 FK',
    base_date DATE NOT NULL COMMENT '기준 일자 (YYYY-MM-DD)',
    open_price INT NOT NULL COMMENT '시가 (Open)',
    high_price INT NOT NULL COMMENT '고가 (High)',
    low_price INT NOT NULL COMMENT '저가 (Low)',
    close_price INT NOT NULL COMMENT '종가 (Close)',
    volume INT NOT NULL DEFAULT 0 COMMENT '당일 누적 거래량 (Volume)',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 생성 일시',
    updated_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_stock_date (stock_id, base_date),
    CONSTRAINT fk_price_history_stock FOREIGN KEY (stock_id) REFERENCES stocks (stock_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 일별 시세 변동 히스토리(OHLCV) 테이블';

CREATE INDEX idx_stock_price_history_stock_date ON stock_price_history (stock_id, base_date DESC);

-- ========================================================
-- 9. 투자 심리 및 AI 뉴스 테이블 (news)
-- Ollama 로컬 LLM 연동 5분 주기 동적 호재/악재 뉴스 기사
-- ========================================================
CREATE TABLE news (
    news_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '뉴스 고유 식별자 PK',
    content VARCHAR(500) NOT NULL COMMENT '뉴스 헤드라인 및 기사 본문',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '뉴스 발행 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='투자 심리 및 AI 생성 동적 시황 뉴스 테이블';

CREATE INDEX idx_news_created_date ON news (created_date DESC);

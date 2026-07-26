-- =========================================================
-- StockGame Database Performance Index Creation DDL
-- Target DBMS: MariaDB / MySQL
-- Description: Core transaction & query optimization indexes
-- =========================================================

-- 1. Order Book & Matching Engine Optimization
-- Optimizes order matching (stock_id, state='대기', content, price, created_date ASC)
CREATE INDEX idx_orders_matching 
ON orders (stock_id, state, content, price, created_date);

-- Optimizes student holding calculations and my-orders filtering
CREATE INDEX idx_orders_student_stock_state 
ON orders (student_id, stock_id, state);

-- 2. Student Leaderboard Ranking Optimization
-- Eliminates filesort for rank pagination (ORDER BY total_point DESC, student_id ASC)
CREATE INDEX idx_students_ranking 
ON students (total_point DESC, student_id ASC);

-- 3. Point History & Transaction Log Optimization
-- Optimizes coupon purchase history UNION queries
CREATE INDEX idx_coupon_purchase_student_date 
ON coupon_purchase (student_id, created_date DESC);

-- Optimizes point grant history queries
CREATE INDEX idx_get_points_student_date 
ON get_points (student_id, created_date DESC);

-- Optimizes stock transaction JOINs on buy orders
CREATE INDEX idx_transactions_buy_order 
ON stock_transactions (buy_order_id, created_date);

-- Optimizes stock transaction JOINs on sell orders
CREATE INDEX idx_transactions_sell_order 
ON stock_transactions (sell_order_id, created_date);

-- 4. Stock Chart & Daily History Optimization
-- Optimizes daily price history chart lookups
CREATE INDEX idx_stock_price_history_stock_date 
ON stock_price_history (stock_id, base_date DESC);

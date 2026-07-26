-- =========================================================
-- StockGame Database Performance Index Creation DDL
-- Target DBMS: MariaDB / MySQL
-- =========================================================

CREATE INDEX idx_orders_matching ON orders (stock_id, state, content, price, created_date);
CREATE INDEX idx_orders_student_stock_state ON orders (student_id, stock_id, state);
CREATE INDEX idx_students_ranking ON students (total_point DESC, student_id ASC);
CREATE INDEX idx_coupon_purchase_student_date ON coupon_purchase (student_id, created_date DESC);
CREATE INDEX idx_get_points_student_date ON get_points (student_id, created_date DESC);
CREATE INDEX idx_transactions_buy_order ON stock_transactions (buy_order_id, created_date);
CREATE INDEX idx_transactions_sell_order ON stock_transactions (sell_order_id, created_date);
CREATE INDEX idx_stock_price_history_stock_date ON stock_price_history (stock_id, base_date DESC);

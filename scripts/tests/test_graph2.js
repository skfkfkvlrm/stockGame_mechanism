const mysql = require('mysql2/promise');
async function run() {
  const conn = await mysql.createConnection({host:'localhost', port:3307, user:'root', password:'root', database:'mydb'});
  const [rows] = await conn.query(
    SELECT history_date, history_type, history_content, point_change 
    FROM ( 
        (SELECT created_date AS history_date, '지급' AS history_type, '기초 지원금' AS history_content, 100000 AS point_change FROM students WHERE student_id = 'abc' LIMIT 1)
        UNION ALL 
        (SELECT cp.created_date AS history_date, '쿠폰' AS history_type, cp.name AS history_content, -cp.price AS point_change FROM coupon_purchase cp WHERE cp.student_id = 'abc' OR cp.student_id = 295)
        UNION ALL 
        (SELECT gp.created_date AS history_date, CASE WHEN gp.point >= 0 THEN '지급' ELSE '차감' END AS history_type, gp.content AS history_content, gp.point AS point_change FROM get_points gp WHERE gp.student_id = 'abc' OR gp.student_id = 295)
        UNION ALL 
        (SELECT t.created_date AS history_date, '매수' AS history_type, COALESCE(s.name, CONCAT('종목 #', o.stock_id)) AS history_content, -(o.price * o.amount) AS point_change FROM stock_transactions t JOIN orders o ON t.buy_order_id = o.order_id LEFT JOIN stocks s ON o.stock_id = s.stock_id WHERE o.student_id = 'abc' OR o.student_id = 295)
        UNION ALL 
        (SELECT t.created_date AS history_date, '매도' AS history_type, COALESCE(s.name, CONCAT('종목 #', o.stock_id)) AS history_content, (o.price * o.amount) AS point_change FROM stock_transactions t JOIN orders o ON t.sell_order_id = o.order_id LEFT JOIN stocks s ON o.stock_id = s.stock_id WHERE o.student_id = 'abc' OR o.student_id = 295)
        UNION ALL 
        (SELECT o.created_date AS history_date, '매수예약' AS history_type, CONCAT(COALESCE(s.name, CONCAT('종목 #', o.stock_id)), ' 매수 예약') AS history_content, -(o.price * o.amount) AS point_change FROM orders o LEFT JOIN stocks s ON o.stock_id = s.stock_id WHERE (o.student_id = 'abc' OR o.student_id = 295) AND o.content IN ('매수', 'BUY') AND o.state = 'WAITING')
        UNION ALL 
        (SELECT o.created_date AS history_date, '주문취소' AS history_type, CONCAT(COALESCE(s.name, CONCAT('종목 #', o.stock_id)), ' 매수 취소 (환불)') AS history_content, (o.price * o.amount) AS point_change FROM orders o LEFT JOIN stocks s ON o.stock_id = s.stock_id WHERE (o.student_id = 'abc' OR o.student_id = 295) AND o.content IN ('매수', 'BUY') AND o.state IN ('취소', 'CANCELLED'))
    ) history
    ORDER BY history_date ASC
  );
  let cum = 0;
  for(let r of rows) {
    cum += Number(r.point_change);
    console.log(r.history_date.toISOString() + ' | change: ' + r.point_change + ' | cum: ' + cum + ' | ' + r.history_type);
  }
}
run();

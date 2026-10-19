const mysql = require('mysql2/promise');
async function run() {
  const conn = await mysql.createConnection({host:'localhost', port:3307, user:'root', password:'root', database:'mydb'});
  const [rows] = await conn.query(
    SELECT history_date, history_type, history_content, point_change 
    FROM ( 
        (SELECT created_date AS history_date, '지급' AS history_type, '기초' AS history_content, 100000 AS point_change FROM students WHERE id = 295 LIMIT 1)
        UNION ALL 
        (SELECT created_date AS history_date, '쿠폰' AS history_type, name AS history_content, -price AS point_change FROM coupon_purchase WHERE student_id = '295' OR student_id = 'abc')
        UNION ALL 
        (SELECT created_date AS history_date, CASE WHEN point >= 0 THEN '지급' ELSE '차감' END AS history_type, content AS history_content, point AS point_change FROM get_points WHERE student_id = '295' OR student_id = 'abc')
        UNION ALL 
        (SELECT t.created_date AS history_date, '매수' AS history_type, '매수' AS history_content, -(o.price * o.amount) AS point_change FROM stock_transactions t JOIN orders o ON t.buy_order_id = o.order_id WHERE o.student_id = '295' OR o.student_id = 'abc')
        UNION ALL 
        (SELECT t.created_date AS history_date, '매도' AS history_type, '매도' AS history_content, (o.price * o.amount) AS point_change FROM stock_transactions t JOIN orders o ON t.sell_order_id = o.order_id WHERE o.student_id = '295' OR o.student_id = 'abc')
        UNION ALL 
        (SELECT o.created_date AS history_date, '매수예약' AS history_type, '예약' AS history_content, -(o.price * o.amount) AS point_change FROM orders o WHERE (o.student_id = '295' OR o.student_id = 'abc') AND o.content IN ('매수', 'BUY') AND o.state IN ('WAITING'))
        UNION ALL 
        (SELECT o.created_date AS history_date, '주문취소' AS history_type, '취소' AS history_content, (o.price * o.amount) AS point_change FROM orders o WHERE (o.student_id = '295' OR o.student_id = 'abc') AND o.content IN ('매수', 'BUY') AND o.state IN ('취소', 'CANCELLED'))
    ) history ORDER BY history_date ASC
  );
  let cum = 0;
  for(let r of rows) {
    cum += Number(r.point_change);
    console.log(r.history_date.toISOString() + ' | change: ' + r.point_change + ' | cum: ' + cum + ' | ' + r.history_type);
  }
}
run();

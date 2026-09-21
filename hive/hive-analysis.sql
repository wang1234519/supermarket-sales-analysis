-- ============================================================
-- 超市销售分析 - Hive 分析脚本（在虚拟机 Hadoop 集群中执行）
--
-- 执行方式：hive -f hive-analysis.sql
--           或进入 Hive 命令行后逐段执行
--
-- 作用：对原始订单数据做清洗、统计计算，产出 16 张结果表。
--       结果表随后由 hive/sqoop-export.sh 导出到本地 MySQL，
--       本地 SpringBoot 项目只读取这些结果表做可视化展示。
--
-- 说明：第 0 步建原始表如已在课程 D4-D5 完成可跳过；
--       结果表列名与 MySQL 表（sql/init.sql）一一对应，勿改动。
-- ============================================================

CREATE DATABASE IF NOT EXISTS supermarket_dw;
USE supermarket_dw;

-- ============================================================
-- 0. 原始表（课程 D4-D5 已建可跳过）
--    数据装载请执行 hive/load-data.sql（先传文件、改路径）
-- ============================================================
CREATE TABLE IF NOT EXISTS dim_store (
    store_id   INT,
    store_name STRING,
    city       STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

CREATE TABLE IF NOT EXISTS dim_product (
    product_id   INT,
    product_name STRING,
    category     STRING,
    price        DECIMAL(10, 2)
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

CREATE TABLE IF NOT EXISTS fact_order_detail (
    order_id   BIGINT,
    product_id INT,
    store_id   INT,
    quantity   INT,
    amount     DECIMAL(12, 2),
    order_time STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

-- 数据装载单独放在 hive/load-data.sql 中执行（先传文件、改路径）

-- ============================================================
-- 1. 总览 KPI（1 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_kpi (
    total_sales      DECIMAL(16, 2) COMMENT '总销售额（元）',
    total_orders     BIGINT COMMENT '订单总数（去重）',
    total_quantity   BIGINT COMMENT '商品总销量（件）',
    avg_order_amount DECIMAL(12, 2) COMMENT '客单价（元）'
);

INSERT OVERWRITE TABLE result_kpi
SELECT sum(amount)                                          AS total_sales,
       count(DISTINCT order_id)                             AS total_orders,
       sum(quantity)                                        AS total_quantity,
       round(sum(amount) / count(DISTINCT order_id), 2)     AS avg_order_amount
FROM fact_order_detail;

-- ============================================================
-- 2. 品类销售（哪个品类最赚钱，7 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_category_sales (
    category VARCHAR(50) COMMENT '品类',
    sales    DECIMAL(16, 2) COMMENT '销售额（元）',
    orders   BIGINT COMMENT '订单数',
    quantity BIGINT COMMENT '销量（件）',
    share    DECIMAL(6, 2) COMMENT '销售额占比（%）'
);

INSERT OVERWRITE TABLE result_category_sales
SELECT p.category                                                     AS category,
       sum(f.amount)                                                  AS sales,
       count(DISTINCT f.order_id)                                     AS orders,
       sum(f.quantity)                                                AS quantity,
       round(sum(f.amount) / sum(sum(f.amount)) OVER () * 100, 2)     AS share
FROM fact_order_detail f
         JOIN dim_product p ON f.product_id = p.product_id
GROUP BY p.category;

-- ============================================================
-- 3. 时段销售（一天中哪个时段是高峰，24 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_hour_sales (
    hour     TINYINT COMMENT '小时（0-23）',
    sales    DECIMAL(16, 2) COMMENT '销售额（元）',
    orders   BIGINT COMMENT '订单数',
    quantity BIGINT COMMENT '销量（件）'
);

INSERT OVERWRITE TABLE result_hour_sales
SELECT hour(order_time)              AS hour,
       sum(amount)                   AS sales,
       count(DISTINCT order_id)      AS orders,
       sum(quantity)                 AS quantity
FROM fact_order_detail
GROUP BY hour(order_time);

-- ============================================================
-- 4. 月度销售趋势（半年生意涨还是跌，6 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_month_sales (
    month            STRING COMMENT '月份（yyyy-MM）',
    sales            DECIMAL(16, 2) COMMENT '销售额（元）',
    orders           BIGINT COMMENT '订单数',
    quantity         BIGINT COMMENT '销量（件）',
    avg_order_amount DECIMAL(12, 2) COMMENT '客单价（元）',
    growth_rate      DECIMAL(8, 2) COMMENT '环比增长率（%，首月为NULL）'
);

INSERT OVERWRITE TABLE result_month_sales
SELECT t.month,
       t.sales,
       t.orders,
       t.quantity,
       round(t.sales / t.orders, 2)                                                                          AS avg_order_amount,
       round((t.sales - lag(t.sales, 1) OVER (ORDER BY t.month)) / lag(t.sales, 1) OVER (ORDER BY t.month) * 100, 2) AS growth_rate
FROM (
         SELECT substr(order_time, 1, 7) AS month,
                sum(amount)              AS sales,
                count(DISTINCT order_id) AS orders,
                sum(quantity)            AS quantity
         FROM fact_order_detail
         GROUP BY substr(order_time, 1, 7)
     ) t;

-- ============================================================
-- 5. 门店业绩排行（哪家店经营最好，18 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_store_sales (
    store_id         INT COMMENT '门店编号',
    store_name       STRING COMMENT '门店名称',
    city             STRING COMMENT '所在城市',
    sales            DECIMAL(16, 2) COMMENT '销售额（元）',
    orders           BIGINT COMMENT '订单数',
    quantity         BIGINT COMMENT '销量（件）',
    avg_order_amount DECIMAL(12, 2) COMMENT '客单价（元）'
);

INSERT OVERWRITE TABLE result_store_sales
SELECT s.store_id,
       s.store_name,
       s.city,
       sum(f.amount)                                       AS sales,
       count(DISTINCT f.order_id)                          AS orders,
       sum(f.quantity)                                     AS quantity,
       round(sum(f.amount) / count(DISTINCT f.order_id), 2) AS avg_order_amount
FROM fact_order_detail f
         JOIN dim_store s ON f.store_id = s.store_id
GROUP BY s.store_id, s.store_name, s.city;

-- ============================================================
-- 6. 城市消费力（哪个城市消费力最强，5 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_city_sales (
    city        STRING COMMENT '城市',
    store_count INT COMMENT '门店数',
    sales       DECIMAL(16, 2) COMMENT '销售额（元）',
    orders      BIGINT COMMENT '订单数',
    share       DECIMAL(6, 2) COMMENT '销售额占比（%）'
);

INSERT OVERWRITE TABLE result_city_sales
SELECT s.city                                                         AS city,
       count(DISTINCT s.store_id)                                     AS store_count,
       sum(f.amount)                                                  AS sales,
       count(DISTINCT f.order_id)                                     AS orders,
       round(sum(f.amount) / sum(sum(f.amount)) OVER () * 100, 2)     AS share
FROM fact_order_detail f
         JOIN dim_store s ON f.store_id = s.store_id
GROUP BY s.city;

-- ============================================================
-- 7. 商品销售（畅销/滞销，含零销售商品，39 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_product_sales (
    product_id   INT COMMENT '商品编号',
    product_name STRING COMMENT '商品名称',
    category     STRING COMMENT '品类',
    price        DECIMAL(10, 2) COMMENT '单价（元）',
    sales        DECIMAL(16, 2) COMMENT '销售额（元）',
    quantity     BIGINT COMMENT '销量（件）',
    orders       BIGINT COMMENT '成交笔数'
);

INSERT OVERWRITE TABLE result_product_sales
SELECT p.product_id,
       p.product_name,
       p.category,
       p.price,
       coalesce(sum(f.amount), 0)    AS sales,
       coalesce(sum(f.quantity), 0)  AS quantity,
       count(f.product_id)           AS orders
FROM dim_product p
         LEFT JOIN fact_order_detail f ON p.product_id = f.product_id
GROUP BY p.product_id, p.product_name, p.category, p.price;

-- ============================================================
-- 8. 星期销售（一周哪天生意最好，7 行）
--    2026-01-05 是周一，故 pmod(datediff(...,'2026-01-05'),7)+1
--    得到 1=周一 ... 7=周日
-- ============================================================
CREATE TABLE IF NOT EXISTS result_weekday_sales (
    day_of_week TINYINT COMMENT '星期（1=周一 ... 7=周日）',
    sales       DECIMAL(16, 2) COMMENT '销售额（元）',
    orders      BIGINT COMMENT '订单数'
);

INSERT OVERWRITE TABLE result_weekday_sales
SELECT pmod(datediff(to_date(order_time), '2026-01-05'), 7) + 1 AS day_of_week,
       sum(amount)                                              AS sales,
       count(DISTINCT order_id)                                 AS orders
FROM fact_order_detail
GROUP BY pmod(datediff(to_date(order_time), '2026-01-05'), 7) + 1;

-- ============================================================
-- 9. 时段×工作日/周末 对比（48 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_hour_weekday_sales (
    hour     TINYINT COMMENT '小时（0-23）',
    day_type STRING COMMENT 'weekday=工作日 weekend=周末',
    sales    DECIMAL(16, 2) COMMENT '销售额（元）',
    orders   BIGINT COMMENT '订单数'
);

INSERT OVERWRITE TABLE result_hour_weekday_sales
SELECT hour(order_time) AS hour,
       if(pmod(datediff(to_date(order_time), '2026-01-05'), 7) + 1 IN (6, 7), 'weekend', 'weekday') AS day_type,
       sum(amount)                AS sales,
       count(DISTINCT order_id)   AS orders
FROM fact_order_detail
GROUP BY hour(order_time),
         if(pmod(datediff(to_date(order_time), '2026-01-05'), 7) + 1 IN (6, 7), 'weekend', 'weekday');

-- ============================================================
-- 10. 星期×时段 热力图（168 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_heatmap_sales (
    day_of_week TINYINT COMMENT '星期（1=周一 ... 7=周日）',
    hour        TINYINT COMMENT '小时（0-23）',
    sales       DECIMAL(16, 2) COMMENT '销售额（元）'
);

INSERT OVERWRITE TABLE result_heatmap_sales
SELECT pmod(datediff(to_date(order_time), '2026-01-05'), 7) + 1 AS day_of_week,
       hour(order_time)                                        AS hour,
       sum(amount)                                             AS sales
FROM fact_order_detail
GROUP BY pmod(datediff(to_date(order_time), '2026-01-05'), 7) + 1,
         hour(order_time);

-- ============================================================
-- 11. 每日销售（约 181 行，用于单月每日走势）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_daily_sales (
    day    STRING COMMENT '日期（yyyy-MM-dd）',
    sales  DECIMAL(16, 2) COMMENT '销售额（元）',
    orders BIGINT COMMENT '订单数'
);

INSERT OVERWRITE TABLE result_daily_sales
SELECT to_date(order_time)        AS day,
       sum(amount)                AS sales,
       count(DISTINCT order_id)   AS orders
FROM fact_order_detail
GROUP BY to_date(order_time);

-- ============================================================
-- 12. 品类×月份 趋势（约 42 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_category_month_sales (
    month    STRING COMMENT '月份（yyyy-MM）',
    category STRING COMMENT '品类',
    sales    DECIMAL(16, 2) COMMENT '销售额（元）'
);

INSERT OVERWRITE TABLE result_category_month_sales
SELECT substr(f.order_time, 1, 7) AS month,
       p.category                  AS category,
       sum(f.amount)               AS sales
FROM fact_order_detail f
         JOIN dim_product p ON f.product_id = p.product_id
GROUP BY substr(f.order_time, 1, 7), p.category;

-- ============================================================
-- 13. 城市×月份 趋势（约 30 行）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_city_month_sales (
    month STRING COMMENT '月份（yyyy-MM）',
    city  STRING COMMENT '城市',
    sales DECIMAL(16, 2) COMMENT '销售额（元）'
);

INSERT OVERWRITE TABLE result_city_month_sales
SELECT substr(f.order_time, 1, 7) AS month,
       s.city                      AS city,
       sum(f.amount)               AS sales
FROM fact_order_detail f
         JOIN dim_store s ON f.store_id = s.store_id
GROUP BY substr(f.order_time, 1, 7), s.city;

-- ============================================================
-- 14. 门店×月份（约 108 行，门店详情页月度走势）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_store_month_sales (
    store_id INT COMMENT '门店编号',
    month    STRING COMMENT '月份（yyyy-MM）',
    sales    DECIMAL(16, 2) COMMENT '销售额（元）',
    orders   BIGINT COMMENT '订单数',
    quantity BIGINT COMMENT '销量（件）'
);

INSERT OVERWRITE TABLE result_store_month_sales
SELECT store_id,
       substr(order_time, 1, 7)  AS month,
       sum(amount)               AS sales,
       count(DISTINCT order_id)  AS orders,
       sum(quantity)             AS quantity
FROM fact_order_detail
GROUP BY store_id, substr(order_time, 1, 7);

-- ============================================================
-- 15. 门店×品类（约 126 行，门店详情页品类构成）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_store_category_sales (
    store_id INT COMMENT '门店编号',
    category STRING COMMENT '品类',
    sales    DECIMAL(16, 2) COMMENT '销售额（元）',
    orders   BIGINT COMMENT '订单数',
    quantity BIGINT COMMENT '销量（件）'
);

INSERT OVERWRITE TABLE result_store_category_sales
SELECT f.store_id,
       p.category                   AS category,
       sum(f.amount)                AS sales,
       count(DISTINCT f.order_id)   AS orders,
       sum(f.quantity)              AS quantity
FROM fact_order_detail f
         JOIN dim_product p ON f.product_id = p.product_id
GROUP BY f.store_id, p.category;

-- ============================================================
-- 16. 门店×商品（约 700 行，门店详情页热销商品）
-- ============================================================
CREATE TABLE IF NOT EXISTS result_store_product_sales (
    store_id     INT COMMENT '门店编号',
    product_id   INT COMMENT '商品编号',
    product_name STRING COMMENT '商品名称',
    category     STRING COMMENT '品类',
    sales        DECIMAL(16, 2) COMMENT '销售额（元）',
    quantity     BIGINT COMMENT '销量（件）'
);

INSERT OVERWRITE TABLE result_store_product_sales
SELECT f.store_id,
       p.product_id,
       p.product_name,
       p.category,
       sum(f.amount)     AS sales,
       sum(f.quantity)   AS quantity
FROM fact_order_detail f
         JOIN dim_product p ON f.product_id = p.product_id
GROUP BY f.store_id, p.product_id, p.product_name, p.category;

-- ============================================================
-- 全部完成。验证：SELECT count(*) FROM 各结果表；
-- 之后执行 hive/sqoop-export.sh 导出到本地 MySQL。
-- ============================================================

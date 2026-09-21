-- ============================================================
-- 原始数据装载脚本（执行一次即可）
--
-- 用法：
--   1. 先把 8 个 txt 文件传到虚拟机（WinSCP / Xftp），例如 /root/data/ 下
--   2. 把下面 8 行 LOAD 的路径改成文件实际路径
--   3. 执行：hive -f load-data.sql
--   4. 验证：hive -e "SELECT COUNT(*) FROM supermarket_dw.fact_order_detail;"
--      应为 267822 行（6 个月合计）
--
-- 说明：课程 D4-D5 已建表并 LOAD 过数据的话，本脚本可跳过。
--       重复执行 LOAD 不会去重，会重复装载，所以只需执行一次。
-- ============================================================

USE supermarket_dw;

LOAD DATA LOCAL INPATH '/root/data/dim_store.txt' INTO TABLE dim_store;
LOAD DATA LOCAL INPATH '/root/data/dim_product.txt' INTO TABLE dim_product;
LOAD DATA LOCAL INPATH '/root/data/orders_2026-01.txt' INTO TABLE fact_order_detail;
LOAD DATA LOCAL INPATH '/root/data/orders_2026-02.txt' INTO TABLE fact_order_detail;
LOAD DATA LOCAL INPATH '/root/data/orders_2026-03.txt' INTO TABLE fact_order_detail;
LOAD DATA LOCAL INPATH '/root/data/orders_2026-04.txt' INTO TABLE fact_order_detail;
LOAD DATA LOCAL INPATH '/root/data/orders_2026-05.txt' INTO TABLE fact_order_detail;
LOAD DATA LOCAL INPATH '/root/data/orders_2026-06.txt' INTO TABLE fact_order_detail;

-- 验证行数
SELECT 'dim_store' AS tbl, COUNT(*) FROM dim_store
UNION ALL
SELECT 'dim_product', COUNT(*) FROM dim_product
UNION ALL
SELECT 'fact_order_detail', COUNT(*) FROM fact_order_detail;

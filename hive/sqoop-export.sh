#!/bin/bash
# ============================================================
# Sqoop 导出脚本：把 Hive 16 张结果表导出到本地 MySQL
# 在虚拟机中执行：bash sqoop-export.sh
#
# ⚠️ 前置条件（缺一不可）：
#   1. 已在 Hive 中执行 hive-analysis.sql（建原始表 + 建结果表 + 分析计算）
#   2. 原始表已 LOAD 数据（6 个月订单 + 门店 + 商品，共 8 个 txt）
#   3. 本地 MySQL 已建好结果表（sql/init.sql 或启动一次项目自动建表）
#   脚本自带检查，缺哪一步会直接提示，不会导出空数据/脏数据。
#
# 执行前请修改下面 4 个配置：
#   HOST_IP    : 宿主机 IP（VMware NAT 下，在宿主机 cmd 执行 ipconfig
#                查看 VMnet8 的 IPv4 地址，通常是 192.168.xx.1）
#   MYSQL_USER / MYSQL_PASS : 本地 MySQL 账号密码
#   HIVE_DB    : Hive 库名（与 hive-analysis.sql 中一致）
# ============================================================
HOST_IP="192.168.88.1"
MYSQL_USER="root"
MYSQL_PASS="root"
HIVE_DB="supermarket_dw"
MYSQL_DB="supermarket_sales"

# 16 张结果表（与 sql/init.sql 中的表一一对应）
TABLES="result_kpi result_category_sales result_hour_sales result_month_sales \
result_store_sales result_city_sales result_product_sales result_weekday_sales \
result_hour_weekday_sales result_heatmap_sales result_daily_sales \
result_category_month_sales result_city_month_sales result_store_month_sales \
result_store_category_sales result_store_product_sales"

# ---------- 0. 前置检查 1：Hive 结果表是否存在且非空（一次会话查完 16 张） ----------
echo "==> 检查 Hive 结果表是否就绪 ..."
check=$(hive -S -e "USE $HIVE_DB; \
SELECT 'result_kpi', COUNT(*) FROM result_kpi UNION ALL \
SELECT 'result_category_sales', COUNT(*) FROM result_category_sales UNION ALL \
SELECT 'result_hour_sales', COUNT(*) FROM result_hour_sales UNION ALL \
SELECT 'result_month_sales', COUNT(*) FROM result_month_sales UNION ALL \
SELECT 'result_store_sales', COUNT(*) FROM result_store_sales UNION ALL \
SELECT 'result_city_sales', COUNT(*) FROM result_city_sales UNION ALL \
SELECT 'result_product_sales', COUNT(*) FROM result_product_sales UNION ALL \
SELECT 'result_weekday_sales', COUNT(*) FROM result_weekday_sales UNION ALL \
SELECT 'result_hour_weekday_sales', COUNT(*) FROM result_hour_weekday_sales UNION ALL \
SELECT 'result_heatmap_sales', COUNT(*) FROM result_heatmap_sales UNION ALL \
SELECT 'result_daily_sales', COUNT(*) FROM result_daily_sales UNION ALL \
SELECT 'result_category_month_sales', COUNT(*) FROM result_category_month_sales UNION ALL \
SELECT 'result_city_month_sales', COUNT(*) FROM result_city_month_sales UNION ALL \
SELECT 'result_store_month_sales', COUNT(*) FROM result_store_month_sales UNION ALL \
SELECT 'result_store_category_sales', COUNT(*) FROM result_store_category_sales UNION ALL \
SELECT 'result_store_product_sales', COUNT(*) FROM result_store_product_sales;" 2>/dev/null)

if [ -z "$check" ]; then
  echo "❌ 部分结果表不存在或查询失败！"
  echo "   请先在 Hive 中执行：hive -f hive-analysis.sql"
  exit 1
fi

echo "$check"
if [ -z "$(echo "$check" | awk -F'\t' '$2 > 0 {print}')" ]; then
  echo "❌ 所有结果表都是 0 行——原始表还没有 LOAD 数据！"
  echo "   请先执行：hive -f load-data.sql（先传文件、改路径）"
  echo "   然后重新执行：hive -f hive-analysis.sql（重新计算）"
  exit 1
fi

# ---------- 0. 前置检查 2：虚拟机是否有 mysql 客户端 ----------
if ! command -v mysql >/dev/null 2>&1; then
  echo "❌ 虚拟机没有 mysql 客户端，无法先清空 MySQL 结果表。"
  echo "   解决：yum install -y mysql 装客户端；或在宿主机 Navicat 中手动清空各结果表。"
  exit 1
fi

# ---------- 0. 前置检查 3：定位 sqoop 命令（自动探测常见安装位置） ----------
if ! command -v sqoop >/dev/null 2>&1; then
  for d in /export/server/sqoop*/bin /export/server/sqoop/bin \
           /opt/sqoop*/bin /usr/local/sqoop*/bin /usr/local/sqoop/bin; do
    if [ -x "$d/sqoop" ]; then
      export PATH="$d:$PATH"
      echo "==> 自动找到 sqoop：$d"
      break
    fi
  done
fi
if ! command -v sqoop >/dev/null 2>&1; then
  echo "❌ 找不到 sqoop 命令！请先执行：ls /export/server/"
  echo "   找到 sqoop 目录后执行：export PATH=/export/server/<sqoop目录>/bin:\$PATH"
  echo "   若 sqoop 在 Docker 容器里，把 docker ps 输出发来，改用容器方式导出。"
  exit 1
fi

# ---------- 1. 导出前清空 MySQL 结果表，防止重复执行产生重复数据 ----------
for t in $TABLES; do
  echo "==> truncate $MYSQL_DB.$t"
  out=$(mysql -h "$HOST_IP" -u"$MYSQL_USER" -p"$MYSQL_PASS" \
        -e "TRUNCATE TABLE $MYSQL_DB.$t;" 2>&1) || {
    echo "$out" | grep -v "Using a password"
    if echo "$out" | grep -q "doesn't exist"; then
      echo "❌ 本地 MySQL 还没有结果表！请先在宿主机执行 sql/init.sql 建表"
      echo "   （或启动一次本项目，应用会自动创建 16 张结果表），"
      echo "   然后重新执行本脚本。"
    else
      echo "❌ 连不上宿主机 MySQL，请检查：HOST_IP / 账号密码 / MySQL 是否允许远程连接。"
    fi
    exit 1
  }
done

# ---------- 2. 逐表导出：Hive 结果表（HDFS 文件） -> MySQL ----------
# 采用经典 --export-dir 方式：直接读取 Hive 表在 HDFS 上的数据文件，
# 无需 hcatalog。Hive 默认字段分隔符为 \001，NULL 记为 \N。
# 前提：MySQL 表列顺序与 Hive 结果表一致（sql/init.sql 已保证）。
for t in $TABLES; do
  echo "==> exporting $t"
  sqoop export \
    --connect "jdbc:mysql://$HOST_IP:3306/$MYSQL_DB?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
    --driver com.mysql.cj.jdbc.Driver \
    --username "$MYSQL_USER" \
    --password "$MYSQL_PASS" \
    --table "$t" \
    --export-dir "/user/hive/warehouse/$HIVE_DB.db/$t" \
    --input-fields-terminated-by '\001' \
    --input-null-string '\\N' \
    --input-null-non-string '\\N' \
    --num-mappers 1 || {
    echo "❌ 导出 $t 失败。"
    echo "   常见原因排查："
    echo "   1. 报 Input path does not exist：Hive 仓库目录不是 /user/hive/warehouse"
    echo "      → 用 hive -e \"DESCRIBE FORMATTED $HIVE_DB.$t;\" | grep Location 查看真实路径，"
    echo "        改脚本里的 --export-dir 后重试"
    echo "   2. 报 Cannot initialize Cluster：Hadoop 配置问题"
    echo "      → 在 sqoop-env.sh 里加 export HADOOP_CONF_DIR=/export/server/hadoop-3.3.0/etc/hadoop"
    echo "   3. 其他报错：把报错信息完整贴出来"
    exit 1
  }
done

echo "==> 全部导出完成，可启动本地项目查看报表：http://localhost:8080"
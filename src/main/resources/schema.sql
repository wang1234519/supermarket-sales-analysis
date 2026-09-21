-- ============================================================
-- 超市销售分析 - 结果表结构（应用每次启动自动执行，幂等）
-- 这些表由虚拟机中的 Hive 分析产出，经 Sqoop 导出到本地 MySQL，
-- 本项目只负责读取结果表并做可视化展示，不做任何计算。
-- 表结构与 hive/hive-analysis.sql 中 Hive 结果表一一对应，
-- 完整建库脚本见 sql/init.sql。
-- ============================================================

-- 总览 KPI 结果表（1 行）
CREATE TABLE IF NOT EXISTS `result_kpi` (
    `total_sales`      DECIMAL(16,2) COMMENT '总销售额（元）',
    `total_orders`     BIGINT        COMMENT '订单总数（去重）',
    `total_quantity`   BIGINT        COMMENT '商品总销量（件）',
    `avg_order_amount` DECIMAL(12,2) COMMENT '客单价（元）'
) ENGINE = InnoDB COMMENT = '总览KPI结果表（Hive计算，Sqoop导出）';

-- 品类销售结果表（7 行）
CREATE TABLE IF NOT EXISTS `result_category_sales` (
    `category` VARCHAR(50)    COMMENT '品类',
    `sales`    DECIMAL(16,2)  COMMENT '销售额（元）',
    `orders`   BIGINT         COMMENT '订单数',
    `quantity` BIGINT         COMMENT '销量（件）',
    `share`    DECIMAL(6,2)   COMMENT '销售额占比（%）'
) ENGINE = InnoDB COMMENT = '品类销售结果表';

-- 时段销售结果表（24 行）
CREATE TABLE IF NOT EXISTS `result_hour_sales` (
    `hour`     TINYINT        COMMENT '小时（0-23）',
    `sales`    DECIMAL(16,2)  COMMENT '销售额（元）',
    `orders`   BIGINT         COMMENT '订单数',
    `quantity` BIGINT         COMMENT '销量（件）'
) ENGINE = InnoDB COMMENT = '时段销售结果表';

-- 月度销售结果表（6 行）
CREATE TABLE IF NOT EXISTS `result_month_sales` (
    `month`            CHAR(7)       COMMENT '月份（yyyy-MM）',
    `sales`            DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`           BIGINT        COMMENT '订单数',
    `quantity`         BIGINT        COMMENT '销量（件）',
    `avg_order_amount` DECIMAL(12,2) COMMENT '客单价（元）',
    `growth_rate`      DECIMAL(8,2)  COMMENT '环比增长率（%，首月为NULL）'
) ENGINE = InnoDB COMMENT = '月度销售结果表';

-- 门店业绩结果表（18 行）
CREATE TABLE IF NOT EXISTS `result_store_sales` (
    `store_id`         INT           COMMENT '门店编号',
    `store_name`       VARCHAR(50)   COMMENT '门店名称',
    `city`             VARCHAR(50)   COMMENT '所在城市',
    `sales`            DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`           BIGINT        COMMENT '订单数',
    `quantity`         BIGINT        COMMENT '销量（件）',
    `avg_order_amount` DECIMAL(12,2) COMMENT '客单价（元）'
) ENGINE = InnoDB COMMENT = '门店业绩结果表';

-- 城市消费力结果表（5 行）
CREATE TABLE IF NOT EXISTS `result_city_sales` (
    `city`        VARCHAR(50)   COMMENT '城市',
    `store_count` INT           COMMENT '门店数',
    `sales`       DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`      BIGINT        COMMENT '订单数',
    `share`       DECIMAL(6,2)  COMMENT '销售额占比（%）'
) ENGINE = InnoDB COMMENT = '城市消费力结果表';

-- 商品销售结果表（39 行，含零销售商品）
CREATE TABLE IF NOT EXISTS `result_product_sales` (
    `product_id`   INT           COMMENT '商品编号',
    `product_name` VARCHAR(100)  COMMENT '商品名称',
    `category`     VARCHAR(50)   COMMENT '品类',
    `price`        DECIMAL(10,2) COMMENT '单价（元）',
    `sales`        DECIMAL(16,2) COMMENT '销售额（元）',
    `quantity`     BIGINT        COMMENT '销量（件）',
    `orders`       BIGINT        COMMENT '成交笔数'
) ENGINE = InnoDB COMMENT = '商品销售结果表（畅销/滞销分析）';

-- 星期销售结果表（7 行）
CREATE TABLE IF NOT EXISTS `result_weekday_sales` (
    `day_of_week` TINYINT       COMMENT '星期（1=周一 ... 7=周日）',
    `sales`       DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`      BIGINT        COMMENT '订单数'
) ENGINE = InnoDB COMMENT = '星期销售结果表';

-- 时段×工作日/周末 对比结果表（48 行）
CREATE TABLE IF NOT EXISTS `result_hour_weekday_sales` (
    `hour`     TINYINT       COMMENT '小时（0-23）',
    `day_type` VARCHAR(10)   COMMENT 'weekday=工作日 weekend=周末',
    `sales`    DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`   BIGINT        COMMENT '订单数'
) ENGINE = InnoDB COMMENT = '时段x工作日/周末对比结果表';

-- 星期×时段 热力图结果表（168 行）
CREATE TABLE IF NOT EXISTS `result_heatmap_sales` (
    `day_of_week` TINYINT       COMMENT '星期（1=周一 ... 7=周日）',
    `hour`        TINYINT       COMMENT '小时（0-23）',
    `sales`       DECIMAL(16,2) COMMENT '销售额（元）'
) ENGINE = InnoDB COMMENT = '星期x时段热力图结果表';

-- 每日销售结果表（约 181 行）
CREATE TABLE IF NOT EXISTS `result_daily_sales` (
    `day`    CHAR(10)      COMMENT '日期（yyyy-MM-dd）',
    `sales`  DECIMAL(16,2) COMMENT '销售额（元）',
    `orders` BIGINT        COMMENT '订单数'
) ENGINE = InnoDB COMMENT = '每日销售结果表';

-- 品类×月份 趋势结果表（约 42 行）
CREATE TABLE IF NOT EXISTS `result_category_month_sales` (
    `month`    CHAR(7)       COMMENT '月份（yyyy-MM）',
    `category` VARCHAR(50)   COMMENT '品类',
    `sales`    DECIMAL(16,2) COMMENT '销售额（元）'
) ENGINE = InnoDB COMMENT = '品类x月份趋势结果表';

-- 城市×月份 趋势结果表（约 30 行）
CREATE TABLE IF NOT EXISTS `result_city_month_sales` (
    `month` CHAR(7)       COMMENT '月份（yyyy-MM）',
    `city`  VARCHAR(50)   COMMENT '城市',
    `sales` DECIMAL(16,2) COMMENT '销售额（元）'
) ENGINE = InnoDB COMMENT = '城市x月份趋势结果表';

-- 门店×月份 结果表（约 108 行）
CREATE TABLE IF NOT EXISTS `result_store_month_sales` (
    `store_id` INT           COMMENT '门店编号',
    `month`    CHAR(7)       COMMENT '月份（yyyy-MM）',
    `sales`    DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`   BIGINT        COMMENT '订单数',
    `quantity` BIGINT        COMMENT '销量（件）'
) ENGINE = InnoDB COMMENT = '门店x月份结果表';

-- 门店×品类 结果表（约 126 行）
CREATE TABLE IF NOT EXISTS `result_store_category_sales` (
    `store_id` INT           COMMENT '门店编号',
    `category` VARCHAR(50)   COMMENT '品类',
    `sales`    DECIMAL(16,2) COMMENT '销售额（元）',
    `orders`   BIGINT        COMMENT '订单数',
    `quantity` BIGINT        COMMENT '销量（件）'
) ENGINE = InnoDB COMMENT = '门店x品类结果表';

-- 门店×商品 结果表（约 700 行）
CREATE TABLE IF NOT EXISTS `result_store_product_sales` (
    `store_id`     INT           COMMENT '门店编号',
    `product_id`   INT           COMMENT '商品编号',
    `product_name` VARCHAR(100)  COMMENT '商品名称',
    `category`     VARCHAR(50)   COMMENT '品类',
    `sales`        DECIMAL(16,2) COMMENT '销售额（元）',
    `quantity`     BIGINT        COMMENT '销量（件）'
) ENGINE = InnoDB COMMENT = '门店x商品结果表';

-- ============================================================
-- 系统用户表（登录认证）
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色：admin/user',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '系统用户表';

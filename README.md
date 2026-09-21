# 超市销售分析平台（SpringBoot + MyBatis + MySQL + ECharts）

实训项目：对连锁超市 2026 年上半年（1~6 月）销售数据做多维分析，产出可视化报表，
支持登录认证与结果表数据浏览。

## 整体架构（数据链路）

```
原始数据(txt) → 虚拟机 HDFS/Hive（清洗、计算）
        │
        │  执行 hive/hive-analysis.sql 产出 16 张结果表
        ▼
Hive 结果表（supermarket_dw 库）
        │
        │  执行 hive/sqoop-export.sh
        ▼
本地 MySQL 结果表（supermarket_sales 库，16 张 result_* 表）
        │
        │  SpringBoot + MyBatis 只读取结果表（登录认证后访问）
        ▼
可视化报表（ECharts，8 个页面）
```

**本项目只做最后一环：读取 MySQL 中的结果表并展示。** 所有数据清洗、
统计计算（品类/时段/月度/门店/城市/商品等）都在虚拟机 Hive 中完成，
经 Sqoop 导出到本地 MySQL 后，本项目直接读取展示。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.5.16 + Java 24 + MyBatis 3.0.5 + spring-security-crypto（BCrypt） |
| 认证 | 会话 + 登录拦截器 + 页面过滤器；注册 / 退出 / 修改密码 |
| 数据库 | 本地 MySQL 8.x，库名 `supermarket_sales`（16 张结果表 + sys_user 用户表） |
| 虚拟机 | Hive 分析 + Sqoop 导出（脚本在 `hive/` 目录） |
| 前端 | 原生 HTML/CSS/JS + ECharts 5.6（本地引入，无需联网） |

## 目录结构

```
├── hive/
│   ├── hive-analysis.sql      # 虚拟机 Hive 分析脚本：产出 16 张结果表
│   ├── load-data.sql          # 虚拟机原始数据装载脚本（执行一次）
│   └── sqoop-export.sh        # 虚拟机 Sqoop 导出脚本：结果表 → 本地 MySQL
├── sql/init.sql               # 本地 MySQL 建库建表脚本（16 张结果表 + 用户表 DDL）
├── src/main/java/com/supermarket/
│   ├── SupermarketApplication.java
│   ├── common/                # 全局异常处理
│   ├── config/                # 登录拦截器、页面过滤器、Web 配置、默认账号初始化
│   ├── controller/            # REST 接口（7 个控制器：分析 6 个 + 认证 + 数据浏览）
│   ├── dto/                   # 请求/响应 DTO
│   ├── entity/                # 实体类（User）
│   ├── mapper/                # ResultMapper（结果表）/ UserMapper / DataMapper
│   └── service/               # 业务层（含 AuthService 登录认证）
└── src/main/resources/
    ├── application.yml        # 配置（数据库账号密码在此修改）
    ├── schema.sql             # 启动时自动建 16 张结果表 + sys_user（幂等）
    ├── mapper/*.xml
    └── static/                # 8 个页面 + ECharts
```

## 使用流程

### 第一步：虚拟机中做 Hive 分析

1. 把 8 个 txt 数据文件传到虚拟机，修改 `hive/load-data.sql` 中的路径后执行：
   ```bash
   hive -f load-data.sql
   ```
   验证：`SELECT COUNT(*) FROM supermarket_dw.fact_order_detail;` 应为 267822。
2. 执行分析脚本产出 16 张结果表：
   ```bash
   hive -f hive-analysis.sql
   ```

### 第二步：Sqoop 导出到本地 MySQL

1. 本地 MySQL 建好结果表：执行 `sql/init.sql`，或启动一次本项目自动建表；
2. 修改 `hive/sqoop-export.sh` 开头配置（宿主机 VMnet8 地址、账号密码），虚拟机中执行：
   ```bash
   bash sqoop-export.sh
   ```
   脚本带前置检查（Hive 表是否就绪、sqoop/mysql 是否可用），导出前自动 TRUNCATE 防重复。

### 第三步：启动本项目查看报表

1. 确认 `application.yml` 中 MySQL 账号密码正确；
2. 启动：IDEA 运行 `SupermarketApplication`，或 `mvnw spring-boot:run`；
3. 打开 http://localhost:8080 → 自动跳转登录页。

**默认管理员账号：admin / admin123**（首次启动自动创建），也可以自行注册新账号。

## 页面功能（8 个页面）

| 页面 | 功能 |
|---|---|
| 🔐 登录 / 注册 | 登录、注册（自动登录） |
| 📊 数据总览 | 6 张 KPI 卡 + 月度趋势、品类占比、时段分布（高峰标注）、门店 TOP10 |
| 🧺 品类分析 | 品类排行、占比环形图、品类月度趋势、品类内热销商品 TOP10 |
| ⏰ 时段分析 | 24 小时分布（高峰/次高峰识别）、星期对比、工作日 vs 周末、星期×时段热力图 |
| 📈 月度趋势 | 销售额+订单量双轴、环比增长率、月度明细表、任意月每日走势 |
| 🏪 门店分析 | 城市消费力、城市月度趋势、18 店排行榜、单店经营详情 |
| 🍎 商品分析 | 畅销/滞销 TOP10、商品明细（搜索/筛选/排序，前端完成） |
| 🗂️ 结果数据 | 16 张结果表明细分页浏览（Sqoop 导出的原始结果，只读） |

其他能力：所有图表右上角可**一键导出 PNG 图片**；顶栏显示当前用户、可退出登录；
未登录访问接口返回 401、访问页面自动跳转登录页；密码 BCrypt 加密存储。

## 16 张结果表说明

| 结果表 | 行数 | 页面用途 |
|---|---|---|
| result_kpi | 1 | 总览 KPI 卡 |
| result_category_sales | 7 | 品类占比、排行 |
| result_hour_sales | 14 | 时段分布（8:00~22:00 营业时段） |
| result_month_sales | 6 | 月度趋势、环比 |
| result_store_sales | 18 | 门店业绩排行 |
| result_city_sales | 5 | 城市消费力 |
| result_product_sales | 39 | 畅销/滞销、商品明细 |
| result_weekday_sales | 7 | 星期对比 |
| result_hour_weekday_sales | 28 | 工作日 vs 周末 |
| result_heatmap_sales | 98 | 星期×时段热力图 |
| result_daily_sales | 181 | 每日走势 |
| result_category_month_sales | 42 | 品类月度趋势 |
| result_city_month_sales | 30 | 城市月度趋势 |
| result_store_month_sales | 108 | 门店月度走势 |
| result_store_category_sales | 126 | 门店品类构成 |
| result_store_product_sales | 702 | 门店热销商品 |

## 核心 API

| 模块 | 接口 |
|---|---|
| 认证 | `POST /api/auth/login` `/register` `/password` `/logout`，`GET /api/auth/me` |
| 总览 | `/api/dashboard/summary` `/monthly` `/category-share` `/store-top` `/hourly` |
| 品类 | `/api/category/overview` `/trend` `/top-products?category=` `/categories` |
| 时段 | `/api/hour/distribution` `/weekday` `/weekday-compare` `/heatmap` |
| 月度 | `/api/monthly/trend` `/daily?month=2026-03` |
| 门店 | `/api/store/ranking` `/cities` `/city-trend` `/stores` `/{id}/detail` |
| 商品 | `/api/product/list` `/categories` |
| 数据 | `GET /api/data/tables`，`GET /api/data/{table}?page=&size=` |

除 `/api/auth/login`、`/api/auth/register`、`/api/auth/me` 外，其余接口均需登录（否则 401）。

## 常见问题

- **启动报 Access denied**：`application.yml` 中密码与 MySQL 不一致，修改即可。
- **端口被占用**：改 `server.port`，如 `--server.port=8081`。
- **忘记管理员密码**：注册新账号登录；或在 MySQL 中删除 sys_user 表数据后重启
  项目（自动重建 admin/admin123）。
- **Sqoop 导出报错**：脚本内置常见错误提示；`Cannot initialize Cluster` → 检查
  `sqoop-env.sh` 的 HADOOP_CONF_DIR；`NoClassDefFoundError: commons/lang/StringUtils`
  → 复制 Hive lib 里的 commons-lang-2.6.jar 到 sqoop/lib。
- **导出后页面仍提示无数据**：确认导出的是 `supermarket_sales` 库、表名与
  `sql/init.sql` 一致；刷新页面。
- **IDEA 里 Lombok 报红**：IDEA 设置中开启 Annotation Processors。

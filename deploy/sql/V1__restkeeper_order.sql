-- V1 restkeeper_order：订单/报表/历史库（逆向自 xfcy-order-api 与 xfcy-service-job 实体）
-- 字符集 utf8mb4 / utf8mb4_general_ci，引擎 InnoDB。
-- 含 job 模块历史表 t_his_order / t_his_order_detail（同库）与视图 v_order_detail_all。
-- 所有业务表带 shop_id + store_id（多租户拦截器追加条件，视图同样必须含这两列）。
-- 金额类 Java Integer → int；主键 Long → bigint。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------- t_order
CREATE TABLE IF NOT EXISTS `t_order` (
  `order_id`        varchar(32)  NOT NULL COMMENT '主键',
  `order_number`    varchar(32)  DEFAULT NULL COMMENT '流水号',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '桌台id',
  `pay_type`        int          DEFAULT NULL COMMENT '付款方式 0免单 1现金 2微信 3支付宝 4银行卡 5挂账',
  `pay_status`      int          DEFAULT 0 COMMENT '支付状态 0未付 1已付',
  `pay_amount`      int          DEFAULT 0 COMMENT '付款金额',
  `small_amount`    int          DEFAULT 0 COMMENT '抹零金额',
  `person_numbers`  int          DEFAULT 0 COMMENT '就餐人数',
  `total_amount`    int          DEFAULT 0 COMMENT '应收金额',
  `present_amount`  int          DEFAULT 0 COMMENT '赠菜金额',
  `free_amount`     int          DEFAULT 0 COMMENT '免单金额',
  `order_remark`    varchar(255) DEFAULT NULL COMMENT '整单备注',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `operator_name`   varchar(64)  DEFAULT NULL COMMENT '操作人',
  `buy_id`          varchar(32)  DEFAULT NULL COMMENT '购买人id',
  `order_source`    int          DEFAULT 0 COMMENT '订单来源 0收银端 1app',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_number` (`order_number`),
  KEY `idx_order_table` (`table_id`),
  KEY `idx_order_shop_store_create` (`shop_id`, `store_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单';

-- ---------------------------------------------------------------- t_order_detail
CREATE TABLE IF NOT EXISTS `t_order_detail` (
  `detail_id`       varchar(32)  NOT NULL COMMENT '主键',
  `order_id`        varchar(32)  DEFAULT NULL COMMENT '订单id',
  `order_number`    varchar(32)  DEFAULT NULL COMMENT '订单流水号',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '所属桌台',
  `detail_status`   int          DEFAULT NULL COMMENT '状态 1赠菜 2退菜',
  `dish_remark`     varchar(255) DEFAULT NULL COMMENT '菜品备注',
  `dish_id`         varchar(32)  DEFAULT NULL COMMENT '菜品id',
  `dish_type`       int          DEFAULT NULL COMMENT '类型 1菜品 2套餐',
  `dish_name`       varchar(128) DEFAULT NULL COMMENT '名称',
  `dish_price`      int          DEFAULT 0 COMMENT '单价',
  `dish_number`     int          DEFAULT 0 COMMENT '份数',
  `dish_amount`     int          DEFAULT 0 COMMENT '金额',
  `flavor_remark`   varchar(255) DEFAULT NULL COMMENT '口味备注',
  `present_remark`  varchar(255) DEFAULT NULL COMMENT '赠菜备注',
  `return_remark`   varchar(255) DEFAULT NULL COMMENT '退菜备注',
  `add_remark`      varchar(255) DEFAULT NULL COMMENT '加菜备注',
  `dish_category_name` varchar(128) DEFAULT NULL COMMENT '菜品分类名称',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`detail_id`),
  KEY `idx_detail_order` (`order_id`),
  KEY `idx_detail_table` (`table_id`),
  KEY `idx_detail_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细';

-- ---------------------------------------------------------------- t_order_detail_meal
-- 套餐拆单明细：相比 t_order_detail 无 add_remark 列（与实体一致）。
CREATE TABLE IF NOT EXISTS `t_order_detail_meal` (
  `detail_id`       varchar(32)  NOT NULL COMMENT '主键',
  `order_id`        varchar(32)  DEFAULT NULL COMMENT '订单id',
  `order_number`    varchar(32)  DEFAULT NULL COMMENT '订单流水号',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '所属桌台',
  `detail_status`   int          DEFAULT NULL COMMENT '状态 1赠菜 2退菜',
  `dish_remark`     varchar(255) DEFAULT NULL COMMENT '菜品备注',
  `dish_id`         varchar(32)  DEFAULT NULL COMMENT '菜品id',
  `dish_type`       int          DEFAULT NULL COMMENT '类型 1菜品 2套餐',
  `dish_name`       varchar(128) DEFAULT NULL COMMENT '名称',
  `dish_price`      int          DEFAULT 0 COMMENT '单价',
  `dish_number`     int          DEFAULT 0 COMMENT '份数',
  `dish_amount`     int          DEFAULT 0 COMMENT '金额',
  `flavor_remark`   varchar(255) DEFAULT NULL COMMENT '口味备注',
  `present_remark`  varchar(255) DEFAULT NULL COMMENT '赠菜备注',
  `return_remark`   varchar(255) DEFAULT NULL COMMENT '退菜备注',
  `dish_category_name` varchar(128) DEFAULT NULL COMMENT '菜品分类名称',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`detail_id`),
  KEY `idx_meal_order` (`order_id`),
  KEY `idx_meal_table` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单套餐拆单明细';

-- ---------------------------------------------------------------- t_reverse_order
-- 实体未标 @TableId，DDL 补 id 主键。
CREATE TABLE IF NOT EXISTS `t_reverse_order` (
  `id`              varchar(32)  NOT NULL COMMENT '主键（实体未标 @TableId，DDL 补 PK）',
  `order_id`        varchar(32)  DEFAULT NULL COMMENT '关联订单号',
  `order_number`    varchar(32)  DEFAULT NULL COMMENT '流水号',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '桌台id',
  `status`          int          DEFAULT NULL COMMENT '反结账状态 0失败 1成功',
  `amount`          int          DEFAULT 0 COMMENT '反结账金额',
  `remark`          varchar(255) DEFAULT NULL COMMENT '反结账备注',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `operator_name`   varchar(64)  DEFAULT NULL COMMENT '操作人名称',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`id`),
  KEY `idx_reverse_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='反结账记录';

-- ---------------------------------------------------------------- t_report_pay
-- is_deleted 有列但实体未加 @TableLogic（不会自动过滤）；主键为 Long。
CREATE TABLE IF NOT EXISTS `t_report_pay` (
  `id`              bigint       NOT NULL COMMENT '主键',
  `pay_date`        date         DEFAULT NULL COMMENT '营业日期',
  `pay_type`        int          DEFAULT NULL COMMENT '付款方式',
  `total_amount`    int          DEFAULT 0 COMMENT '应收金额',
  `present_amount`  int          DEFAULT 0 COMMENT '赠送金额',
  `small_amount`    int          DEFAULT 0 COMMENT '抹零金额',
  `free_amount`     int          DEFAULT 0 COMMENT '免单金额',
  `pay_amount`      int          DEFAULT 0 COMMENT '实收金额',
  `person_numbers`  int          DEFAULT 0 COMMENT '就餐人数',
  `pay_count`       int          DEFAULT 0 COMMENT '交易笔数',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除（实体未启用逻辑删除）',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_pay_day_type` (`pay_date`, `pay_type`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付汇总报表';

-- ---------------------------------------------------------------- t_report_dish
CREATE TABLE IF NOT EXISTS `t_report_dish` (
  `id`              bigint       NOT NULL COMMENT '主键',
  `pay_date`        date         DEFAULT NULL COMMENT '营业日期',
  `category`        varchar(64)  DEFAULT NULL COMMENT '分类',
  `dish_name`       varchar(128) DEFAULT NULL COMMENT '菜品名称',
  `dish_number`     int          DEFAULT 0 COMMENT '数量',
  `dish_money`      int          DEFAULT 0 COMMENT '金额',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`id`),
  KEY `idx_report_dish_day` (`pay_date`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜品销售报表';

-- ---------------------------------------------------------------- t_his_order（job 归档）
-- 历史表：remark 而非 order_remark；is_deleted 有列但未启用逻辑删除。
CREATE TABLE IF NOT EXISTS `t_his_order` (
  `order_id`        varchar(32)  NOT NULL COMMENT '主键',
  `order_number`    varchar(32)  DEFAULT NULL COMMENT '流水号',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '桌台id',
  `pay_type`        int          DEFAULT NULL COMMENT '付款方式',
  `pay_status`      int          DEFAULT NULL COMMENT '支付状态',
  `pay_amount`      int          DEFAULT 0 COMMENT '付款金额',
  `total_amount`    int          DEFAULT 0 COMMENT '应收金额',
  `small_amount`    int          DEFAULT 0 COMMENT '抹零金额',
  `present_amount`  int          DEFAULT 0 COMMENT '赠菜金额',
  `free_amount`     int          DEFAULT 0 COMMENT '免单金额',
  `person_numbers`  int          DEFAULT 0 COMMENT '就餐人数',
  `remark`          varchar(255) DEFAULT NULL COMMENT '整单备注',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `operator_name`   varchar(64)  DEFAULT NULL COMMENT '操作人',
  `buy_id`          varchar(32)  DEFAULT NULL COMMENT '购买人id',
  `order_source`    int          DEFAULT 0 COMMENT '订单来源',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除（实体未启用逻辑删除）',
  PRIMARY KEY (`order_id`),
  KEY `idx_his_order_number` (`order_number`),
  KEY `idx_his_order_shop_store` (`shop_id`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单归档';

-- ---------------------------------------------------------------- t_his_order_detail（job 归档）
-- 容纳 t_order_detail 与 t_order_detail_meal 两类拆单行，故含 add_remark。
CREATE TABLE IF NOT EXISTS `t_his_order_detail` (
  `detail_id`       varchar(32)  NOT NULL COMMENT '主键',
  `order_id`        varchar(32)  DEFAULT NULL COMMENT '订单id',
  `order_number`    varchar(32)  DEFAULT NULL COMMENT '订单流水号',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '所属桌台',
  `detail_status`   int          DEFAULT NULL COMMENT '状态',
  `dish_remark`     varchar(255) DEFAULT NULL COMMENT '菜品备注',
  `dish_id`         varchar(32)  DEFAULT NULL COMMENT '菜品id',
  `dish_type`       int          DEFAULT NULL COMMENT '类型',
  `dish_name`       varchar(128) DEFAULT NULL COMMENT '名称',
  `dish_price`      int          DEFAULT 0 COMMENT '单价',
  `dish_number`     int          DEFAULT 0 COMMENT '份数',
  `dish_amount`     int          DEFAULT 0 COMMENT '金额',
  `flavor_remark`   varchar(255) DEFAULT NULL COMMENT '口味备注',
  `present_remark`  varchar(255) DEFAULT NULL COMMENT '赠菜备注',
  `return_remark`   varchar(255) DEFAULT NULL COMMENT '退菜备注',
  `add_remark`      varchar(255) DEFAULT NULL COMMENT '加菜备注',
  `dish_category_name` varchar(128) DEFAULT NULL COMMENT '菜品分类名称',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除（实体未启用逻辑删除）',
  PRIMARY KEY (`detail_id`),
  KEY `idx_his_detail_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细归档';

-- ---------------------------------------------------------------- v_order_detail_all
-- 明细全量视图 = t_order_detail ∪ t_order_detail_meal。
-- 列取两个实体的并集中确定存在的字段（不含 add_remark，meal 表无此列）。
-- 视图必须包含 shop_id / store_id / last_update_time / is_deleted：
--   store/order 模块的多租户拦截器会对视图查询自动追加 shop_id + store_id 条件，
--   OrderDetailAllView 继承 BaseEntity 带 @TableLogic，缺列会运行时报错。
DROP VIEW IF EXISTS `v_order_detail_all`;
CREATE VIEW `v_order_detail_all` AS
SELECT
  `detail_id`,
  `order_id`,
  `order_number`,
  `detail_status`,
  `dish_type`,
  `dish_name`,
  `dish_price`,
  `dish_number`,
  `dish_category_name`,
  `dish_amount`,
  `shop_id`,
  `store_id`,
  `last_update_time`,
  `is_deleted`
FROM `t_order_detail`
UNION ALL
SELECT
  `detail_id`,
  `order_id`,
  `order_number`,
  `detail_status`,
  `dish_type`,
  `dish_name`,
  `dish_price`,
  `dish_number`,
  `dish_category_name`,
  `dish_amount`,
  `shop_id`,
  `store_id`,
  `last_update_time`,
  `is_deleted`
FROM `t_order_detail_meal`;

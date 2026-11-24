-- V1 restkeeper_store：菜品/桌台/员工/挂账库（逆向自 xfcy-store-api 实体 + Mapper）
-- 字符集 utf8mb4 / utf8mb4_general_ci，引擎 InnoDB。
-- 继承 BaseStoreEntity 的表含 shop_id + store_id；无逻辑删除的表（t_dish_flavor、
-- t_credit_company_user、t_remark、t_sell_calculation、t_setmeal_dish）按实体不加 is_deleted。
-- 金额类字段 Java Integer → int（单位语义沿用原系统）。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------- t_category
CREATE TABLE IF NOT EXISTS `t_category` (
  `category_id`     varchar(32)  NOT NULL COMMENT '分类id',
  `type`            int          DEFAULT NULL COMMENT '类型1 菜品 2 套餐',
  `name`            varchar(64)  DEFAULT NULL COMMENT '名称（实体列名为 name）',
  `t_order`         int          DEFAULT 0 COMMENT '顺序',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`category_id`),
  KEY `idx_category_shop_store_deleted` (`shop_id`, `store_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜品分类';

-- ---------------------------------------------------------------- t_dish
CREATE TABLE IF NOT EXISTS `t_dish` (
  `dish_id`         varchar(32)  NOT NULL COMMENT '菜品id',
  `dish_name`       varchar(128) DEFAULT NULL COMMENT '菜品名称',
  `category_id`     varchar(32)  DEFAULT NULL COMMENT '分类id',
  `price`           int          DEFAULT NULL COMMENT '价格',
  `code`            varchar(64)  DEFAULT NULL COMMENT '菜品code',
  `image`           varchar(255) DEFAULT NULL COMMENT '图片路径',
  `description`     varchar(255) DEFAULT NULL COMMENT '备注',
  `status`          int          DEFAULT 1 COMMENT '状态',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`dish_id`),
  KEY `idx_dish_category` (`category_id`),
  KEY `idx_dish_shop_store_deleted` (`shop_id`, `store_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜品';

-- ---------------------------------------------------------------- t_dish_flavor
-- 实体无逻辑删除，按实体建表。
CREATE TABLE IF NOT EXISTS `t_dish_flavor` (
  `id`              varchar(32)  NOT NULL COMMENT '主键id',
  `dish_id`         varchar(32)  DEFAULT NULL COMMENT '菜品id',
  `flavor_name`     varchar(64)  DEFAULT NULL COMMENT '口味名',
  `flavor_value`    varchar(128) DEFAULT NULL COMMENT '口味标签',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_flavor_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜品口味';

-- ---------------------------------------------------------------- t_setmeal
CREATE TABLE IF NOT EXISTS `t_setmeal` (
  `setmeal_id`      varchar(32)  NOT NULL COMMENT '套餐id',
  `category_id`     varchar(32)  DEFAULT NULL COMMENT '分类id',
  `setmeal_name`    varchar(128) DEFAULT NULL COMMENT '套餐名称',
  `price`           int          DEFAULT NULL COMMENT '价格',
  `status`          int          DEFAULT 1 COMMENT '状态',
  `code`            varchar(64)  DEFAULT NULL COMMENT '编码',
  `description`     varchar(255) DEFAULT NULL COMMENT '备注',
  `image`           varchar(255) DEFAULT NULL COMMENT '图片路径',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`setmeal_id`),
  KEY `idx_setmeal_category` (`category_id`),
  KEY `idx_setmeal_shop_store_deleted` (`shop_id`, `store_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='套餐';

-- ---------------------------------------------------------------- t_setmeal_dish
-- 实体无逻辑删除，按实体建表。
CREATE TABLE IF NOT EXISTS `t_setmeal_dish` (
  `id`              varchar(32)  NOT NULL COMMENT '主键id',
  `setmeal_id`      varchar(32)  DEFAULT NULL COMMENT '套餐id',
  `dish_id`         varchar(32)  DEFAULT NULL COMMENT '菜品id',
  `dish_name`       varchar(128) DEFAULT NULL COMMENT '菜品名称',
  `dish_copies`     int          DEFAULT 1 COMMENT '份数',
  `t_order`         int          DEFAULT 0 COMMENT '顺序',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_setmeal_dish_setmeal` (`setmeal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='套餐菜品';

-- ---------------------------------------------------------------- t_sell_calculation
-- 实体无 @TableId/@TableLogic，仍给出 id 主键以支持 MP 写入与按主键查询。
CREATE TABLE IF NOT EXISTS `t_sell_calculation` (
  `id`               varchar(32)  NOT NULL COMMENT '主键（实体未标 @TableId，DDL 补 PK）',
  `dish_id`          varchar(32)  DEFAULT NULL COMMENT '菜品Id',
  `dish_name`        varchar(128) DEFAULT NULL COMMENT '菜品名称',
  `dish_type`        int          DEFAULT NULL COMMENT '1 普通菜品 2 套餐',
  `sell_limit_total` int          DEFAULT 0 COMMENT '估清总数',
  `remainder`        int          DEFAULT 0 COMMENT '剩余数',
  `shop_id`          varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`         varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime     DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sell_calc_dish_store` (`dish_id`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='估清计算';

-- ---------------------------------------------------------------- t_remark
-- 实体无逻辑删除，按实体建表。
CREATE TABLE IF NOT EXISTS `t_remark` (
  `remark_id`       varchar(32)  NOT NULL COMMENT '备注id',
  `remark_name`     varchar(128) DEFAULT NULL COMMENT '备注名称（退菜原因/退单原因…）',
  `remark_value`    varchar(255) DEFAULT NULL COMMENT '备注',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`remark_id`),
  KEY `idx_remark_shop_store` (`shop_id`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='备注字典';

-- ---------------------------------------------------------------- t_table_area
CREATE TABLE IF NOT EXISTS `t_table_area` (
  `area_id`         varchar(32)  NOT NULL COMMENT '区域id',
  `area_name`       varchar(64)  DEFAULT NULL COMMENT '区域名称',
  `t_order`         int          DEFAULT 0 COMMENT '顺序',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`area_id`),
  KEY `idx_area_shop_store_deleted` (`shop_id`, `store_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='桌台区域';

-- ---------------------------------------------------------------- t_table
CREATE TABLE IF NOT EXISTS `t_table` (
  `table_id`        varchar(32)  NOT NULL COMMENT '桌台id',
  `area_id`         varchar(32)  DEFAULT NULL COMMENT '区域id',
  `table_name`      varchar(64)  DEFAULT NULL COMMENT '桌台名称',
  `table_seat_number` int        DEFAULT 0 COMMENT '座位数',
  `status`          int          DEFAULT 0 COMMENT '0空闲 1开桌 2锁桌',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`table_id`),
  KEY `idx_table_area` (`area_id`),
  KEY `idx_table_shop_store_status` (`shop_id`, `store_id`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='桌台';

-- ---------------------------------------------------------------- t_table_log
CREATE TABLE IF NOT EXISTS `t_table_log` (
  `log_id`          varchar(32)  NOT NULL COMMENT '开桌记录id',
  `table_id`        varchar(32)  DEFAULT NULL COMMENT '桌台id',
  `user_id`         varchar(32)  DEFAULT NULL COMMENT '操作人',
  `table_status`    int          DEFAULT NULL COMMENT '桌台操作记录 1开桌 2锁桌',
  `user_numbers`    int          DEFAULT 0 COMMENT '用餐人数',
  `create_time`     datetime     DEFAULT NULL COMMENT '开桌时间',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`log_id`),
  KEY `idx_table_log_table` (`table_id`),
  KEY `idx_table_log_shop_store` (`shop_id`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='开桌记录';

-- ---------------------------------------------------------------- t_staff
-- staff_id 为 ASSIGN_UUID，故长度放宽到 64。
CREATE TABLE IF NOT EXISTS `t_staff` (
  `staff_id`        varchar(64)  NOT NULL COMMENT '员工id',
  `staff_name`      varchar(64)  DEFAULT NULL COMMENT '员工姓名',
  `password`        varchar(128) DEFAULT NULL COMMENT '密码',
  `phone`           varchar(32)  DEFAULT NULL COMMENT '手机号',
  `sex`             varchar(16)  DEFAULT NULL COMMENT '性别',
  `id_number`       varchar(32)  DEFAULT NULL COMMENT '身份证号',
  `status`          int          DEFAULT 1 COMMENT '状态：1 在职启用，0 停用',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `uk_staff_shop_name` (`shop_id`, `staff_name`, `is_deleted`),
  KEY `idx_staff_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工';

-- ---------------------------------------------------------------- t_credit
CREATE TABLE IF NOT EXISTS `t_credit` (
  `credit_id`       varchar(32)  NOT NULL COMMENT '主键id',
  `credit_type`     int          DEFAULT NULL COMMENT '挂账类型 1公司 2个人',
  `company_name`    varchar(128) DEFAULT NULL COMMENT '公司名称（个人可为空）',
  `user_name`       varchar(64)  DEFAULT NULL COMMENT '用户名称',
  `phone`           varchar(32)  DEFAULT NULL COMMENT '手机号',
  `status`          int          DEFAULT 1 COMMENT '状态 1开启 0停用',
  `credit_amount`   int          DEFAULT 0 COMMENT '挂账金额',
  `total_repayment_amount` int   DEFAULT 0 COMMENT '总还款金额',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`credit_id`),
  KEY `idx_credit_shop_store_deleted` (`shop_id`, `store_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='挂账账户';

-- ---------------------------------------------------------------- t_credit_company_user
-- 实体无逻辑删除，按实体建表。
CREATE TABLE IF NOT EXISTS `t_credit_company_user` (
  `id`              varchar(32)  NOT NULL COMMENT '主键id',
  `credit_id`       varchar(32)  DEFAULT NULL COMMENT '挂账公司关联id',
  `user_name`       varchar(64)  DEFAULT NULL COMMENT '用户名',
  `phone`           varchar(32)  DEFAULT NULL COMMENT '手机号',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_credit_user_credit` (`credit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='挂账公司成员';

-- ---------------------------------------------------------------- t_credit_logs
CREATE TABLE IF NOT EXISTS `t_credit_logs` (
  `log_id`          varchar(32)  NOT NULL COMMENT '日志Id',
  `order_id`        varchar(32)  DEFAULT NULL COMMENT '订单Id',
  `credit_id`       varchar(32)  DEFAULT NULL COMMENT '挂账管理Id',
  `credit_type`     int          DEFAULT NULL COMMENT '挂账类型（Java 字段名 type）',
  `company_name`    varchar(128) DEFAULT NULL COMMENT '挂账公司名称',
  `user_name`       varchar(64)  DEFAULT NULL COMMENT '挂账人名称',
  `phone`           varchar(32)  DEFAULT NULL COMMENT '手机号',
  `order_amount`    int          DEFAULT 0 COMMENT '订单金额',
  `received_amount` int          DEFAULT 0 COMMENT '应收金额',
  `credit_amount`   int          DEFAULT 0 COMMENT '挂账金额',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`log_id`),
  KEY `idx_credit_log_order` (`order_id`),
  KEY `idx_credit_log_credit` (`credit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='挂账日志';

-- ---------------------------------------------------------------- t_credit_repayment
CREATE TABLE IF NOT EXISTS `t_credit_repayment` (
  `log_id`          varchar(32)  NOT NULL COMMENT '日志Id',
  `credit_type`     varchar(16)  DEFAULT NULL COMMENT '挂账类型 1公司 2个人',
  `credit_id`       varchar(32)  DEFAULT NULL COMMENT '挂账管理id',
  `company_name`    varchar(128) DEFAULT NULL COMMENT '挂账公司名称',
  `user_name`       varchar(64)  DEFAULT NULL COMMENT '还款人名称',
  `repayment_type`  varchar(32)  DEFAULT NULL COMMENT '还款方式',
  `repayment_amount` int         DEFAULT 0 COMMENT '还款金额',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `store_id`        varchar(32)  DEFAULT NULL COMMENT '门店id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`log_id`),
  KEY `idx_credit_repay_credit` (`credit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='挂账还款';

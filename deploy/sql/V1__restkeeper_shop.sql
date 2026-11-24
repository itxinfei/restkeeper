-- V1 restkeeper_shop：门店/品牌库（逆向自 xfcy-shop-api 实体 + Mapper）
-- 字符集 utf8mb4 / utf8mb4_general_ci，引擎 InnoDB。
-- 所有表带 shop_id（多租户拦截器自动追加条件），并含逻辑删除 is_deleted。
-- t_store 的 category/password 为 StoreMapper.xml resultMap 引用但实体无字段，
-- 保留列以兼容既有 XML 映射（业务不使用）。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------- t_brand
CREATE TABLE IF NOT EXISTS `t_brand` (
  `brand_id`       varchar(32)  NOT NULL COMMENT '商户id',
  `brand_name`     varchar(128) DEFAULT NULL COMMENT '餐饮类别',
  `category`       varchar(64)  DEFAULT NULL COMMENT '分类名称',
  `logo`           varchar(255) DEFAULT NULL COMMENT '图片地址',
  `contact`        varchar(64)  DEFAULT NULL COMMENT '联系人',
  `contact_phone`  varchar(32)  DEFAULT NULL COMMENT '联系人电话',
  `shop_id`        varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `last_update_time` datetime   DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`     tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`brand_id`),
  KEY `idx_brand_shop_deleted` (`shop_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='品牌';

-- ---------------------------------------------------------------- t_store
CREATE TABLE IF NOT EXISTS `t_store` (
  `store_id`        varchar(32)  NOT NULL COMMENT '门店主键id',
  `store_name`      varchar(128) DEFAULT NULL COMMENT '门店名称',
  `brand_id`        varchar(32)  DEFAULT NULL COMMENT '所属品牌',
  `province`        varchar(64)  DEFAULT NULL COMMENT '省',
  `city`            varchar(64)  DEFAULT NULL COMMENT '市',
  `area`            varchar(64)  DEFAULT NULL COMMENT '区',
  `address`         varchar(255) DEFAULT NULL COMMENT '地址',
  `category`        varchar(64)  DEFAULT NULL COMMENT '分类（XML 遗留列，业务不用）',
  `password`        varchar(128) DEFAULT NULL COMMENT '密码（XML 遗留列，实际在 t_store_manager）',
  `store_manager_id` varchar(32) DEFAULT NULL COMMENT '门店管理员id',
  `status`          int          DEFAULT 1 COMMENT '状态：正式运营1，停业0',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`store_id`),
  KEY `idx_store_shop_deleted` (`shop_id`, `is_deleted`),
  KEY `idx_store_brand` (`brand_id`),
  KEY `idx_store_manager` (`store_manager_id`),
  KEY `idx_store_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店';

-- ---------------------------------------------------------------- t_store_manager
CREATE TABLE IF NOT EXISTS `t_store_manager` (
  `store_manager_id`   varchar(32)  NOT NULL COMMENT '主键',
  `store_manager_name` varchar(64)  DEFAULT NULL COMMENT '门店管理员姓名',
  `store_manager_phone` varchar(32) DEFAULT NULL COMMENT '门店管理员电话',
  `password`           varchar(128) DEFAULT NULL COMMENT '密码',
  `status`             int          DEFAULT 1 COMMENT '状态',
  `shop_id`            varchar(32)  DEFAULT NULL COMMENT '所属商户id',
  `last_update_time`   datetime     DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`         tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`store_manager_id`),
  UNIQUE KEY `uk_manager_phone` (`store_manager_phone`),
  KEY `idx_manager_shop_deleted` (`shop_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店管理员';

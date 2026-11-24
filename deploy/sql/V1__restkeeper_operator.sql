-- V1 restkeeper_operator：运营平台库（逆向自 xfcy-operator-api 实体 + Mapper）
-- 字符集 utf8mb4 / utf8mb4_general_ci，引擎 InnoDB，与 deploy/sql/init.sql 建库一致。
-- 说明：金额/数量类 Java Integer 一律 int（单位语义沿用原系统，疑为分）。
-- 逆向来源：SysDictionary / OperatorUser / EnterpriseAccount。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------- t_sys_dict
CREATE TABLE IF NOT EXISTS `t_sys_dict` (
  `dict_id`    varchar(32)  NOT NULL COMMENT '字典id',
  `dict_name`  varchar(64)  DEFAULT NULL COMMENT '名称',
  `dict_data`  varchar(512) DEFAULT NULL COMMENT '字典数据',
  `category`   varchar(64)  DEFAULT NULL COMMENT '分类',
  `info`       varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  KEY `idx_sys_dict_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据字典';

-- ---------------------------------------------------------------- t_operator_user
-- 运营账号（登录用），实体无逻辑删除/时间戳字段，故不加 is_deleted。
CREATE TABLE IF NOT EXISTS `t_operator_user` (
  `uid`        varchar(32)  NOT NULL COMMENT '用户id',
  `loginname`  varchar(64)  NOT NULL COMMENT '登录账号',
  `loginpass`  varchar(128) DEFAULT NULL COMMENT '登录密码（密文）',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uk_operator_loginname` (`loginname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运营账号';

-- ---------------------------------------------------------------- t_enterprise_account
CREATE TABLE IF NOT EXISTS `t_enterprise_account` (
  `enterprise_id`   varchar(32)  NOT NULL COMMENT '企业id',
  `enterprise_name` varchar(128) DEFAULT NULL COMMENT '企业名称',
  `password`        varchar(128) DEFAULT NULL COMMENT '密码（后台下发，不返回明文）',
  `shop_id`         varchar(32)  DEFAULT NULL COMMENT '商户号（下发生成）',
  `applicant`       varchar(64)  DEFAULT NULL COMMENT '申请人',
  `phone`           varchar(32)  DEFAULT NULL COMMENT '手机号',
  `province`        varchar(64)  DEFAULT NULL COMMENT '省',
  `city`            varchar(64)  DEFAULT NULL COMMENT '市',
  `area`            varchar(64)  DEFAULT NULL COMMENT '区',
  `address`         varchar(255) DEFAULT NULL COMMENT '详细地址',
  `application_time` datetime    DEFAULT NULL COMMENT '申请时间',
  `expire_time`     datetime     DEFAULT NULL COMMENT '到期时间',
  `status`          int          DEFAULT 0 COMMENT '状态：0 试用，1 正式，-1 停用',
  `last_update_time` datetime    DEFAULT NULL COMMENT '最后更新时间',
  `is_deleted`      tinyint      DEFAULT 0 COMMENT '是否删除 1删除 0未删除',
  PRIMARY KEY (`enterprise_id`),
  KEY `idx_ent_name` (`enterprise_name`),
  KEY `idx_ent_status_deleted` (`status`, `is_deleted`),
  KEY `idx_ent_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='企业账号';

-- xfcy 本地开发库初始化（阶段 5 冒烟）
-- 库名沿用代码现状（restkeeper_*）；表结构由 deploy/sql/V1__restkeeper_*.sql 产出并导入。
-- 账号口径见《部署手册-中间件与数据》§3：root/123456 + 业务账号 xf/123456。
CREATE DATABASE IF NOT EXISTS restkeeper_operator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS restkeeper_shop     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS restkeeper_store    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS restkeeper_order    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 业务账号（最小授权：仅本项目的 4 个库；生产请按需收紧）
CREATE USER IF NOT EXISTS 'xf'@'%' IDENTIFIED BY '123456';
ALTER USER 'xf'@'%' IDENTIFIED BY '123456';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP ON restkeeper_operator.* TO 'xf'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP ON restkeeper_shop.*     TO 'xf'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP ON restkeeper_store.*    TO 'xf'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP ON restkeeper_order.*    TO 'xf'@'%';
FLUSH PRIVILEGES;

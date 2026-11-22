-- xfcy 本地开发库初始化（阶段 5 冒烟）
-- 库名沿用代码现状（restkeeper_*），表结构由 Flyway V1 产出，见 6.2 库表清单。
CREATE DATABASE IF NOT EXISTS restkeeper_operator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS restkeeper_shop     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS restkeeper_store    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS restkeeper_order    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

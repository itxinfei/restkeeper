# xfcy 中间件与数据部署方案（虚拟机 192.168.20.88）

> 状态：**中间件与数据已就绪**（2026-11-24 实测）：VM 上 mysql/redis/nacos/rabbitmq/es/kibana/minio/xxl-job/seata 均已运行；`restkeeper_*` 4 库 + xf 授权 + V1 DDL（3/3/15/8 + 视图）已导入；Nacos 8 个配置已发布（v3 Admin API）。剩余：启动应用做登录与主链路冒烟。
>
> **唯一权威**：`D:\MyCode\部署手册-中间件与数据.md`（版本、端口、账号）；本文只做 xfcy 落地与差异说明。

## 1. 环境现状（只读盘点结论）

| 项 | 值 |
|---|---|
| 主机 | 192.168.20.88，Ubuntu 24.04.5，7.7G 内存 / 97G 盘（可用约 80G） |
| Docker | 29.8.2 + Compose v5.6.0（插件式，命令 `docker compose`） |
| sudo | itxinfei / 123456 可用 |
| 镜像加速 | daemon.json 已配 docker.1ms.run / daocloud / xuanyuan / 1panel |
| 已有容器 | mysql 8.4.13(3306, root/123456，库 `xf_dev` + 多个其它项目库)、redis 7.4(6379，密码 123456)、nacos 2.4.3(8848)、minio(9000)、rabbitmq 3.13(5672)（据手册 §4） |

本轮曾新建 `xfcy-nacos`/`xfcy-redis`/`xfcy-rabbitmq`/`xfcy-mysql` 用于验证镜像与连通性，**已全部删除（含命名卷）**，端口已释放；`xfcy-es` 拉取被中断未建成。等待用户按手册完成安装/升级。

## 2. 部署决策总表

原则：**MySQL 复用现有实例**（已是手册定版 8.4.13）；Redis/Nacos/RabbitMQ 按手册升级到定版；ES/Kibana/MinIO 新增。

| 组件 | 决策 | 地址 | 版本/镜像（手册 §2） | 凭据（手册 §3） | 数据持久化 |
|---|---|---|---|---|---|
| MySQL | **复用现有实例** | 192.168.20.88:3306 | mysql:8.4.13（不可跨补丁降级） | root / **123456**，业务账号 xf / 123456 | 已有数据目录（他项目共用，禁止删库） |
| Redis | 升级现有实例 | 192.168.20.88:6379 | redis:8.10.3 | 密码 **123456** | 现有数据目录 |
| RabbitMQ | 升级现有实例 | 192.168.20.88:5672、15672 | rabbitmq:4.4.0-management | **xf / 123456** | 现有数据目录 |
| Nacos | 升级现有实例 | 192.168.20.88:8848、9848、**8080(控制台)** | nacos/nacos-server:v3.3.0（standalone） | admin / 123456；首波 `NACOS_AUTH_ENABLE=false`，镜像仍需 token/identity 才能启动 | nacos 数据目录 |
| Elasticsearch | 新增 | 192.168.20.88:9200、9300 | elasticsearch:9.5.7（单节点、安全关） | 无 | es-data |
| Kibana | 新增 | 192.168.20.88:5601 | kibana:9.5.7（必须与 ES 同版） | 无 | — |
| MinIO | 新增 | 192.168.20.88:9000、9001 | minio/minio:RELEASE.2026-06-06T02-44-06Z | minioadmin / **itxinfei123**（≥8 位） | minio-data |
| Seata / XXL-Job | 可选，后置 | 8091、7091 / **8086** | seataio/seata-server:2.6.0 / xuxueli/xxl-job-admin:3.3.2 | root/123456（xxl_job 库） | 需额外建表与注册配置，非冒烟必需 |

**已清理**：`xfcy-mysql`(13306)、`xfcy-redis`(16379) 等本轮自建容器与命名卷均已删除，3306/6379/8848/9848/5672/15672/9200 端口空闲，未触碰 mw-* / xfhj-*。

## 3. 数据规划

### 3.1 库与表

- 复用 mw-mysql，新建 4 个库（`deploy/sql/init.sql` 已定义）：`restkeeper_operator` / `restkeeper_shop` / `restkeeper_store` / `restkeeper_order`。
- 库名与 Nacos 中 provider 数据源一致；字符集 utf8mb4 / utf8mb4_general_ci。
- **表结构（已就绪）**：`deploy/sql/V1__restkeeper_{operator,shop,store,order}.sql` 已按实体 + Mapper XML 逆向产出 29 表 + `v_order_detail_all` 视图（utf8mb4 / InnoDB，含 shop_id/store_id 与 is_deleted）。语法已用 sqlglot 解析校验，待中间件起来后导入并做建表核对。

### 3.2 其它存储

- Redis：sa-token 会话 key 与其它项目共用实例（手册定版 8.10.3）；出现互相踢会话时再按 database 隔离。
- ES：按 `es-dubbo-provider.yml` 的 `xfcy-*` 索引集初始化（如需预建索引/映射，随 DDL 任务一并给出）。
- RabbitMQ：**xf / 123456**，应用按需声明 exchange/queue（`xf-order-status`、`xf-soldout`、`xf-credit`）。
- MinIO：作为通用对象存储（minioadmin/itxinfei123）；当前 OSS 上传仍走阿里云占位实现，切换需引入 S3 客户端（见 §8 待办）。

## 4. 配置对齐清单（关键）

已按手册统一口径改造完毕：仓库内 `deploy/nacos/` 与 `deploy/nacos-vm/` **内容一致**（地址 192.168.20.88，MySQL xf/123456，Redis 密码 123456，RabbitMQ xf/123456），bootstrap.yml 也已指向 192.168.20.88:8848。可用任一目录导入：

| 位置 | 现值（与手册一致） |
|---|---|
| `deploy/nacos/common.yml` | redis host 192.168.20.88:6379 + password `${REDIS_PASSWORD:123456}`；rabbitmq username/password `${RABBITMQ_USERNAME:xf}` / `${RABBITMQ_PASSWORD:123456}` |
| `deploy/nacos/*-dubbo-provider.yml` | jdbc url → `jdbc:mysql://192.168.20.88:3306/restkeeper_*`，username `${MYSQL_USERNAME:xf}`，password `${MYSQL_PASSWORD:123456}`；seata grouplist → 192.168.20.88:8091 |
| `xfcy-service/*/bootstrap.yml`、`xfcy-web/*/bootstrap.yml`、`xfcy-gateway/bootstrap.yml` | nacos discovery/config server-addr = 192.168.20.88:8848（已回写，无需启动覆盖） |
| `deploy/.env.example` | 同步 192.168.20.88 + xf/123456 + REDIS/RABBITMQ/NACOS token/MinIO 口径 |
| `deploy/sql/init.sql` | 新增业务账号 `xf/123456`（仅本 4 库授权） |

Nacos 配置导入（VM 上或本机指向 VM 均可）：

```bash
NACOS_SERVER=192.168.20.88:8848 bash deploy/nacos-vm/import.sh   # 遍历 deploy/nacos-vm/*.yml POST 到 /nacos/v1/cs/configs
# 若 VM 上的 Nacos 已升 3.3.0，也可用本地同内容版本：
NACOS_SERVER=192.168.20.88:8848 bash deploy/nacos/import.sh
```

> 若 VM 上已存在同名 dataId（此前导入过本地版），再次导入为覆盖更新，安全。

## 5. 执行步骤（按序）

1. ✅（已完成）清理自建冗余实例与命名卷，释放端口。
2. ✅（用户完成）按手册安装/升级：Redis 8.10.3、Nacos 3.3.0、RabbitMQ 4.4.0、ES 9.5.7、Kibana 9.5.7、MinIO；xxl-job 3.3.2 元数据表已建（8 表）。
3. ✅ `deploy/sql/init.sql` 已执行：4 库建成，`xf@%` 获 4 库 DML+DDL 授权。
4. ✅ 中间件健康检查通过（见 §6）。
5. ✅ Nacos 配置已导入：8 个 dataId 均返回 `code:0`（v3 Admin API）。
6. ✅ V1 DDL 已导入：operator 3 / shop 3 / store 15 / order 8 + `v_order_detail_all` 视图；视图可查（count=0），`t_staff.status` 为 int default 1，字符集 utf8mb4。
7. ⏳ 启动应用（bootstrap 已指向 192.168.20.88:8848），按 README 冒烟顺序验证。

## 6. 验证清单

- MySQL：`docker exec <mysql容器> mysql -h127.0.0.1 -uroot -p123456 -e "show databases"` → 含 4 个 `restkeeper_*`；各库表数 3/3/15/8，且 `SELECT COUNT(*) FROM restkeeper_order.v_order_detail_all` 可执行（列名以实际容器名为准，当前 VM 上为 `mysql`/`redis`）。
- Redis：`docker exec <redis容器> redis-cli -a 123456 ping` → PONG。
- RabbitMQ：管理台 `http://192.168.20.88:15672`（xf / 123456）；登录失败先确认升级后未回退到 guest。
- Nacos：控制台 `http://192.168.20.88:8080/nacos`（3.x 控制台端口，admin/123456）；配置列表含 common.yml、gateway.yml、各 provider yml、enterprise-web.yml。
  - 注：仓库 compose 的 healthcheck 优先探测 `/nacos/v1/console/health/readiness`，3.x 该端点可能 410，会回退到控制台首页探测（已加回退）。
- ES：`curl http://192.168.20.88:9200/_cluster/health?pretty`（9.x 默认安全开启，开发环境用 `xpack.security.enabled=false`）；Kibana `http://192.168.20.88:5601` 需与 ES 同版本。
- MinIO：`http://192.168.20.88:9001`（minioadmin / itxinfei123）。
- 应用：provider 日志出现 Nacos 注册成功；网关 401/200 口径验证。

## 7. 风险与回滚

- **共址风险**：MySQL/Redis 为其它项目共用，只做加法（建库、加账号、加配置），不做任何 DROP/重启；删除操作仅限本轮自建的 xfcy-* 容器与其命名卷。
- **DDL 缺失**：未导入 V1 前不要启动 provider，避免大量 SQL 异常日志。
- **端口撞车**：xfcy 网关 `server.port=8080` 与 Nacos 3.x 控制台 8080 同机时会冲突；需错开时改网关端口（不要动 Nacos）。XXL-Job 已按手册用 8086。
- **Nacos 资源**：standalone + 512m 堆，启动偏慢（约 3–5 分钟）；如不稳定可显式设 `JVM_XMN: 256m`。
- **升级不可逆**：MySQL 8.4.13 数据目录不可降级（手册 §5-1）；Redis/Nacos/RabbitMQ 升级前建议先备份持久化目录。
- **回滚**：移除本轮新增的 xfcy-es/kibana/minio 容器与其命名卷即可；MySQL 复用实例不做变更即无回滚成本。

## 8. 待办（不属本轮范围）

- **OSS 切 MinIO**：当前 enterprise-web 使用 aliyun-sdk-oss（OSSClient）；MinIO 是 S3 协议，需引入 `aws-java-sdk-s3`/MinIO Java SDK 与配置项后再切换，否则仅部署了 MinIO 也不会被使用。
- **ES 9.x 索引与分词**：确认是否安装与 ES 9.5.7 匹配的分词插件（IK 需对应版本）；现有代码用 wildcard 检索，不依赖 IK。
- **Seata 建表**：store/order provider 已配 `seata.enabled=true`，启用前需在 store/order 库建 `undo_log` 等表并在 Nacos 注册事务组。
- **XXL-Job 元数据**：`xxl_job` 库与执行器注册（job bootstrap 指向 192.168.20.88:8086）。

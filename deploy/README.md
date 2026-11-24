# xfcy 本地开发环境（阶段 5 冒烟）

本目录提供中间件与 Nacos 配置清单，用于跑通 Sa-Token 会话、租户上下文与端到端登录。

> **版本唯一权威**：`D:\MyCode\部署手册-中间件与数据.md`（中间件定版、端口、账号）。本文件只做 xfcy 落地说明，不另立版本；手册变更时以手册为准回写这里。

## 0. 版本基线（引自部署手册 §2）

| 组件 | 定版 |
|---|---|
| JDK / Spring Boot | 25.0.3 / 4.1.3 |
| MySQL / Redis | 8.4.13 / 8.10.3 |
| Elasticsearch / Kibana | 9.5.7 / 9.5.7 |
| Nacos / RabbitMQ | 3.3.0 / 4.4.0 |
| MinIO | RELEASE.2026-06-06T02-44-06Z |
| XXL-Job / Seata | 3.3.2 / 2.6.0 |

账号口径（手册 §3）：root/admin/minioadmin 或 itxinfei，密码 `123456`；MinIO 根密码 `itxinfei123`（≥8 位）。

## 1. 启动中间件

```powershell
docker compose -f deploy/docker-compose.yml up -d
```

含：Nacos 3.3.0(8848/9848，控制台 8080) / MySQL 8.4.13(3306, root/123456, 自动建 4 库 + 业务账号 xf/123456) /
Redis 8.10.3(6379, 密码 123456) / RabbitMQ 4.4.0(5672, xf/123456) /
Elasticsearch 9.5.7(9200，单节点无鉴权) / Kibana 9.5.7(5601) / MinIO(9000/9001, minioadmin/itxinfei123)。

可选（XXL-Job Admin、Seata）：

```powershell
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.optional.yml --profile xxljob up -d
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.optional.yml --profile seata up -d
```

> XXL-Job 控制台端口 8086（避开 Nacos 8080）；Seata 需另建 `xxl_job`/seata 相关表与注册配置。

## 2. 导入 Nacos 配置

```powershell
pwsh deploy/nacos/import.ps1          # 或 bash deploy/nacos/import.sh
```

导入清单（dataId → 作用）：

| dataId | 作用 |
|---|---|
| common.yml | Redis(带密码)/RabbitMQ(xf/123456)/短信（共享） |
| operator-dubbo-provider.yml | operator 库数据源 |
| shop-dubbo-provider.yml | shop 库数据源 |
| store-dubbo-provider.yml | store 库数据源 + Seata |
| order-dubbo-provider.yml | order 库数据源 + Seata |
| es-dubbo-provider.yml | ES 连接（store 库回填数据源 + ES uris） |
| gateway.yml | 网关路由 + 鉴权白名单 |
| enterprise-web.yml | OSS/文件上传 |

> 连接地址统一 `192.168.20.88`，凭据见手册 §3；库名沿用代码现状 `restkeeper_*`（见 `deploy/sql/init.sql`）。

## 3. 启动应用（覆盖 Nacos 地址）

应用 `bootstrap.yml` 已默认指向 `192.168.20.88:8848`（discovery + config），无需再覆盖；确需改动可用同名环境变量覆盖。

## 4. 冒烟顺序

1. Nacos 控制台（`http://192.168.20.88:8080/nacos`）确认服务注册；
2. Redis 会话：登录后 `docker exec <redis容器> redis-cli -a 123456 KEYS '*satoken*'`;
3. provider 直连：开台/点餐/结账；
4. 网关：带 `Authorization` 头访问受保护端点，验证 401/200；
5. 端到端：运营/企业/店长/员工四套登录。

## 5. 表结构

全仓无任何历史 `.sql`；29 张表 + `v_order_detail_all` 视图的 DDL 已由实体 + Mapper XML 逆向产出为 `deploy/sql/V1__restkeeper_{operator,shop,store,order}.sql`（未接入 Flyway 自动迁移，需手工导入）：

```bash
docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_operator < deploy/sql/V1__restkeeper_operator.sql
docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_shop     < deploy/sql/V1__restkeeper_shop.sql
docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_store    < deploy/sql/V1__restkeeper_store.sql
docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_order    < deploy/sql/V1__restkeeper_order.sql
```

库表清单与字段依据见 `docs/requirements.md` 6.2。`t_staff.status` 含在内。

> 逆向不确定项已在各 SQL 文件头注释标注（如金额单位、XML 遗留列 `t_store.category/password`、视图列范围）。

## 6. 虚拟机部署（192.168.20.88）

中间件与数据在 VM 上的部署方案、配置对齐与验证清单见 [docs/MIDDLEWARE_DEPLOYMENT.md](../docs/MIDDLEWARE_DEPLOYMENT.md)。
VM 专用 Nacos 配置在 `deploy/nacos-vm/`（地址 192.168.20.88、复用现有 MySQL/Redis，凭据同手册 §3）：

```bash
NACOS_SERVER=192.168.20.88:8848 bash deploy/nacos-vm/import.sh
```

> 注意：xfcy 网关 `server.port` 也是 8080，若应用与 Nacos 部署在同一台机，二者会抢 8080；需要错开时改网关端口（不要动 Nacos）。

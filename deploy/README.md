# xfcy 本地开发环境（阶段 5 冒烟）

本目录提供本地中间件与 Nacos 配置清单，用于跑通 Sa-Token 会话、租户上下文与端到端登录。

## 1. 启动中间件

```powershell
docker compose -f deploy/docker-compose.yml up -d
```

含：Nacos 3.1.1(8848) / MySQL 8.4(3306, root/root, 自动建 4 库) / Redis 7(6379) /
RabbitMQ 3.13(5672, guest/guest) / Elasticsearch 8.17(9200, 单节点无鉴权)。

可选（XXL-Job Admin、Seata）：

```powershell
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.optional.yml --profile xxljob up -d
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.optional.yml --profile seata up -d
```

## 2. 导入 Nacos 配置

```powershell
pwsh deploy/nacos/import.ps1          # 或 bash deploy/nacos/import.sh
```

导入清单（dataId → 作用）：

| dataId | 作用 |
|---|---|
| common.yml | Redis/RabbitMQ/短信（共享） |
| operator-dubbo-provider.yml | operator 库数据源 |
| shop-dubbo-provider.yml | shop 库数据源 |
| store-dubbo-provider.yml | store 库数据源 + Seata |
| order-dubbo-provider.yml | order 库数据源 + Seata |
| es-dubbo-provider.yml | ES 连接 |
| gateway.yml | 网关路由 + 鉴权白名单 |
| enterprise-web.yml | OSS/文件上传 |

> 连接地址统一 `127.0.0.1`；库名沿用代码现状 `restkeeper_*`（见 `deploy/sql/init.sql`）。

## 3. 启动应用（覆盖 Nacos 地址）

应用 `bootstrap.yml` 默认连内网 `192.168.200.128:8848`，本地用环境变量覆盖：

```powershell
$env:SPRING_CLOUD_NACOS_DISCOVERY_SERVERADDR = "127.0.0.1:8848"
$env:SPRING_CLOUD_NACOS_CONFIG_SERVERADDR    = "127.0.0.1:8848"
# 然后启动 gateway / provider / web（IDE 或 mvn spring-boot:run）
```

## 4. 冒烟顺序

1. Nacos 控制台确认服务注册；
2. Redis 会话：登录后 `redis-cli KEYS *satoken*`；
3. provider 直连：开台/点餐/结账；
4. 网关：带 `Authorization` 头访问受保护端点，验证 401/200；
5. 端到端：运营/企业/店长/员工四套登录。

## 5. 表结构

全仓无 `.sql`，DDL 由 Flyway V1 产出（见 `docs/requirements.md` 6.2 库表清单，共 29 表 + 1 视图）。
本地冒烟前需先执行 V1；`t_staff.status` 含在内。

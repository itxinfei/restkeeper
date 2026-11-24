# xfcy 中间件与数据部署方案（虚拟机 192.168.20.88）

> 状态：准备中。中间件安装由用户执行；本轮已完成环境准备（自建 xfcy-* 容器已清理让出端口、VM 版 Nacos 配置已备好在 `deploy/nacos-vm/`），待安装完成后做健康检查与配置导入。目标：使 manager/operator/cashier/microapp 五端可从 mock 切到 real。

## 1. 环境现状（只读盘点结论）

| 项 | 值 |
|---|---|
| 主机 | 192.168.20.88，Ubuntu 24.04.5，7.7G 内存 / 97G 盘（可用约 80G） |
| Docker | 29.8.2 + Compose v5.6.0（插件式，命令 `docker compose`） |
| sudo | itxinfei / 123456 可用 |
| 镜像加速 | daemon.json 已配 docker.1ms.run / daocloud / xuanyuan / 1panel |
| 已有容器 | **mw-mysql**（3306，root/123456，库 `xf_dev` + 多个其它项目库）、**mw-redis**（6379，密码 123456）、xfhj-mysql（3307，smartaster 项目）、xfhj-redis（6380）、mw-nginx（80，当前未运行） |

本轮曾新建 `xfcy-nacos`(8848/9848)、`xfcy-redis`(16379)、`xfcy-rabbitmq`(5672/15672)、`xfcy-mysql`(13306) 用于验证镜像与连通性，**已全部删除（含命名卷）**，端口已释放；`xfcy-es` 拉取被中断未建成。等待用户在 VM 上安装中间件。

## 2. 部署决策总表

原则：**数据库复用现有实例**；Nacos/RabbitMQ/ES 新建（机器上没有）。

| 组件 | 决策 | 地址 | 版本/镜像 | 凭据 | 数据持久化 |
|---|---|---|---|---|---|
| MySQL | **复用 mw-mysql** | 127.0.0.1:3306 | 8.4 | root / **123456** | 已有数据卷（他项目共用，禁止删库） |
| Redis | **复用 mw-redis** | 127.0.0.1:6379 | 7.4-alpine | 密码 **123456** | 共用实例（无其它应用在线，风险低） |
| RabbitMQ | 新建 xfcy-rabbitmq | 127.0.0.1:5672、15672 | rabbitmq:3.13-management-alpine | guest/guest | 命名卷 rabbitmq-data |
| Nacos | 新建 xfcy-nacos | 127.0.0.1:8848、9848 | nacos/nacos-server:v3.1.1（standalone） | 3.x 强制鉴权：需 `NACOS_AUTH_TOKEN`(Base64≥32B) + `IDENTITY_KEY`/`IDENTITY_VALUE` | 命名卷 nacos-data |
| Elasticsearch | 新建 xfcy-es | 127.0.0.1:9200、9300 | elasticsearch:8.17.0（单节点、安全关） | 无 | 命名卷 es-data |
| Seata / XXL-Job | 可选，后置 | 8091 / 8080 | 见 `deploy/docker-compose.optional.yml` | — | 需额外建表与注册配置，非冒烟必需 |

**已清理**：`xfcy-mysql`(13306)、`xfcy-redis`(16379) 等本轮自建容器与命名卷均已删除，3306/6379/8848/9848/5672/15672/9200 端口空闲，未触碰 mw-* / xfhj-*。

## 3. 数据规划

### 3.1 库与表

- 复用 mw-mysql，新建 4 个库（`deploy/sql/init.sql` 已定义）：`restkeeper_operator` / `restkeeper_shop` / `restkeeper_store` / `restkeeper_order`。
- 库名与 Nacos 中 provider 数据源一致；字符集 utf8mb4 / utf8mb4_general_ci。
- **表结构（已就绪）**：`deploy/sql/V1__restkeeper_{operator,shop,store,order}.sql` 已按实体 + Mapper XML 逆向产出 29 表 + `v_order_detail_all` 视图（utf8mb4 / InnoDB，含 shop_id/store_id 与 is_deleted）。语法已用 sqlglot 解析校验，待中间件起来后导入并做建表核对。

### 3.2 其它存储

- Redis：sa-token 会话 key 与其它项目共用实例；当前无并发应用，接受 db0 默认前缀，出现互相踢会话时再按 database 隔离。
- ES：按 `es-dubbo-provider.yml` 的 `xfcy-*` 索引集初始化（如需预建索引/映射，随 DDL 任务一并给出）。
- RabbitMQ：guest/guest，应用按需声明 exchange/queue（`xf-order-status`、`xf-soldout`、`xf-credit`）。

## 4. 配置对齐清单（关键）

复用 mw-mysql/mw-redis 后，端口保持 3306/6379，但**凭据从 root/root 变为 123456**。仓库已提供改好的 VM 专用版：`deploy/nacos-vm/*.yml`（地址 192.168.20.88、MySQL root/123456、Redis 密码 123456），无需手工改值：

| 位置 | 原值（本地版） | VM 版 |
|---|---|---|
| `deploy/nacos/common.yml` | redis host 127.0.0.1:6379（无密码） | `deploy/nacos-vm/common.yml`：host 192.168.20.88 + password 123456 |
| `deploy/nacos/*-dubbo-provider.yml` | jdbc url …127.0.0.1:3306…，`${MYSQL_PASSWORD:root}` | `deploy/nacos-vm/*-dubbo-provider.yml`：url 指向 192.168.20.88，`${MYSQL_PASSWORD:123456}` |
| `xfcy-service/*/src/main/resources/bootstrap.yml` | nacos server-addr 192.168.200.128:8848 | 启动覆盖为 **192.168.20.88:8848**（discovery + config） |
| `deploy/.env.example` | MYSQL/REDIS 占位符（本地默认 root） | 保持不变；VM 场景用环境变量覆盖为 123456，勿写回文件 |

Nacos 配置导入（VM 上或本机指向 VM 均可）：

```bash
NACOS_SERVER=192.168.20.88:8848 bash deploy/nacos-vm/import.sh   # 遍历 deploy/nacos-vm/*.yml POST 到 /nacos/v1/cs/configs
```

> 若 VM 上已存在同名 dataId（此前导入过本地版），再次导入为覆盖更新，安全。

## 5. 执行步骤（按序）

1. （已完成）清理自建冗余实例与命名卷，释放 3306/6379/8848/9848/5672/15672/9200。
2. 复用库初始化：对复用 MySQL（root/123456）执行 `deploy/sql/init.sql`（幂等 CREATE DATABASE IF NOT EXISTS）。
3. 完成 ES 部署：`docker compose up -d elasticsearch`（拉取约 800MB，需 `vm.max_map_count=262144`，已设置并持久化到 `/etc/sysctl.d/99-es.conf`）。
4. 校验中间件：MySQL/Redis/RabbitMQ/Nacos/ES 端口与健康（见 §6）。
5. 导入 Nacos 配置：`NACOS_SERVER=192.168.20.88:8848 bash deploy/nacos-vm/import.sh`，确认 dataId 齐备。
6. （表结构）导入 V1 DDL（文件已产出在 `deploy/sql/`）：

   ```bash
   docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_operator < deploy/sql/V1__restkeeper_operator.sql
   docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_shop     < deploy/sql/V1__restkeeper_shop.sql
   docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_store    < deploy/sql/V1__restkeeper_store.sql
   docker exec -i <mysql容器> mysql -uroot -p123456 restkeeper_order    < deploy/sql/V1__restkeeper_order.sql
   ```

   导入后核对表数：operator 3 / shop 3 / store 15 / order 8（含 2 张历史表）+ 视图 1。
7. 启动应用（bootstrap 覆盖 nacos 地址），按 README 冒烟顺序验证。

## 6. 验证清单

- MySQL：`docker exec <mysql容器> mysql -h127.0.0.1 -uroot -p123456 -e "show databases"` → 含 4 个 `restkeeper_*`；各库表数 3/3/15/8，且 `SELECT COUNT(*) FROM restkeeper_order.v_order_detail_all` 可执行（列名以实际容器名为准，当前 VM 上为 `mysql`/`redis`）。
- Redis：`docker exec <redis容器> redis-cli -a 123456 ping` → PONG。
- RabbitMQ：管理台 `http://192.168.20.88:15672`（guest/guest）。
- Nacos：`http://192.168.20.88:8848/nacos` 可登录；配置列表含 common.yml、gateway.yml、各 provider yml、enterprise-web.yml（VM 版来自 `deploy/nacos-vm/`）。
  - 注：仓库 compose 的 healthcheck 用 `/nacos/v1/console/health/readiness`，Nacos 3.1.1 该端点返回 410，容器会显示 unhealthy（功能不受影响）；建议改为控制台首页或 v3 健康端点。
- ES：`curl http://127.0.0.1:9200/_cluster/health?pretty`。
- 应用：provider 日志出现 Nacos 注册成功；网关 401/200 口径验证。

## 7. 风险与回滚

- **共址风险**：mw-mysql/mw-redis 为其它项目共用，只做加法（建库、加密码配置），不做任何 DROP/重启；删除操作仅限本轮自建的 xfcy-* 容器与其命名卷。
- **DDL 缺失**：未导入 V1 前不要启动 provider，避免大量 SQL 异常日志。
- **Nacos 资源**：standalone + 512m 堆，曾出现 Xmn=Xmx 的 JVM 参数（compose 触发），启动偏慢（约 3–5 分钟）；如不稳定可显式设 `JVM_XMN: 256m`。
- **回滚**：`docker rm -f xfcy-es xfcy-nacos xfcy-rabbitmq` 并 `docker volume rm xfcy_es-data xfcy_nacos-data xfcy_rabbitmq-data`；mw-* 不做变更即无回滚成本。

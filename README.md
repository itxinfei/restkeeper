> 本仓库在「餐掌柜」公开课程项目基础上二次开发。原始代码著作权归原课程机构所有，本仓库作者只对改造与新增部分以 Apache-2.0 授权，原始代码不在授权范围内。归属说明见 [NOTICE](NOTICE)。

<h1 align="center">心飞餐饮 · XinFei Dining</h1>

<h4 align="center">多租户餐饮 SaaS —— 运营中心 / 商家管家 / 触屏收银 / 扫码点餐，共用同一套门店、菜品、订单数据</h4>

<p align="center">
  <a href="https://gitee.com/itxinfei/xfcy/stargazers"><img src="https://gitee.com/itxinfei/xfcy/badge/star.svg?theme=gvp" alt="star"></a>
  <a href="https://gitee.com/itxinfei/xfcy/members"><img src="https://gitee.com/itxinfei/xfcy/badge/fork.svg?theme=gvp" alt="fork"></a>
  <img src="https://img.shields.io/badge/status-%E4%BA%8C%E6%AC%A1%E5%BC%80%E5%8F%91%E4%B8%AD-brightgreen.svg" alt="status">
  <img src="https://img.shields.io/badge/type-%E8%AF%BE%E7%A8%8B%E4%BA%8C%E5%BC%80-lightgrey.svg" alt="type">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F.svg?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2025.1.4-6DB33F.svg?logo=spring&logoColor=white" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/JDK-25-007396.svg?logo=openjdk&logoColor=white" alt="JDK">
  <img src="https://img.shields.io/badge/Nacos-3.1-2B6CB0.svg" alt="Nacos">
  <img src="https://img.shields.io/badge/Sa--Token-1.46-4FC08D.svg" alt="Sa-Token">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.17-2176AE.svg" alt="MyBatis-Plus">
  <img src="https://img.shields.io/badge/Seata-2.5-1C7ED6.svg" alt="Seata">
  <img src="https://img.shields.io/badge/MySQL-8.4-4479A1.svg?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7-DC382D.svg?logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/RabbitMQ-3.13-FF6600.svg?logo=rabbitmq&logoColor=white" alt="RabbitMQ">
  <img src="https://img.shields.io/badge/Elasticsearch-8.17-005571.svg?logo=elasticsearch&logoColor=white" alt="Elasticsearch">
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="license">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3-42b883.svg?logo=vue.js&logoColor=white" alt="Vue">
  <img src="https://img.shields.io/badge/TypeScript-5-3178C6.svg?logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Vite-6-646CFF.svg?logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/Taro-4-07C160.svg" alt="Taro">
  <img src="https://img.shields.io/badge/%E5%BE%AE%E4%BF%A1%E5%B0%8F%E7%A8%8B%E5%BA%8F-07C160.svg?logo=wechat&logoColor=white" alt="WeChat Mini Program">
</p>

<p align="center">
  <a href="#一这是什么">这是什么</a> ·
  <a href="#二四个端">四个端</a> ·
  <a href="#三业务流程">业务流程</a> ·
  <a href="#四技术架构">技术架构</a> ·
  <a href="#五界面与原型">界面与原型</a> ·
  <a href="#六本地起步">本地起步</a> ·
  <a href="#七相关链接">相关链接</a> ·
  <a href="#八版权说明">版权</a>
</p>

---

## 一、这是什么

「心飞餐饮」是「餐掌柜」课程项目的二次开发版：一套面向餐饮行业的多租户 SaaS。集团或单店在同一套系统内完成 **点餐 → 收银 → 挂账 → 经营报表** 的闭环，靠 `shop_id`（集团）/ `store_id`（门店）双租户字段隔离数据。

课程原版的技术栈（Spring Boot 2.1.6 + Spring Cloud Greenwich.SR2，2019 年前后）已整体升级：当前是 Spring Boot 4.1.1 + JDK 25 + Spring Cloud 2025.1.4 微服务，模块统一 `xfcy-*` 命名（包名沿用 `com.restkeeper`）；前端在 `xfcy-ui/next/` 以 pnpm monorepo 重写，按 [DESIGN.md](DESIGN.md) 的设计规范落地。项目处于活跃二次开发阶段：后端五个业务域 + 四端 BFF 已跑通核心链路，前端逐页施工中。

## 二、四个端

| 端 | 谁在用 | 干什么 | 代码位置 |
|---|---|---|---|
| 平台运营中心 | 平台运营方 | 企业账号开通/编辑/停用/回收站、字典、运营人员 | 前端 `xfcy-ui/next/apps/operator` · BFF `operator-web`（8083） |
| 商家管家 | 集团管理员 / 门店经理 | 品牌与门店、菜品套餐、桌台区域、员工权限、沽清、挂账、经营报表 | 前端 `xfcy-ui/next/apps/manager` · BFF `enterprise-web`（8081） |
| 触屏收银 | 门店收银员 | 开台、点餐、加菜退菜、现金/挂账结账、换台、反结账 | 前端 `xfcy-ui/next/apps/cashier` · BFF `cash-web`（8085） |
| 扫码点餐 | 顾客 | 微信扫码进店，浏览分类菜品、购物车下单、支付 | 前端 `xfcy-ui/next/apps/microapp`（Taro 4 → 小程序/H5）· BFF `microapp-web`（8087，`/microApp`） |

切端逻辑：运营方管准入，集团/店长管自己的店，收银员管台面，顾客管自己那一单 —— 权限边界清楚，互不越界。B 端三端与 C 端共用同一套门店、菜品、订单、桌台数据。

## 三、业务流程

### 1、入驻开通

运营人员在运营中心开通企业账号（8 位随机 `shopId` + 6 位随机密码，经 RabbitMQ 异步短信下发）→ 试用 7 天，到期可转正式/续费 → 企业登录管家端维护品牌、门店、门店经理、菜品分类/菜品/套餐、桌台区域、员工与权限。

### 2、堂食主线（收银端）

开台（`POST /table/log/open`）→ 点餐（`POST /order/add`）→ 加菜（同端点带 `orderId`）/ 退菜（`POST /order/returnDish`）→ 结账：现金 `POST /order/pay` 或挂账 `POST /order/pay/credit`（Seata 分布式事务，同步累加挂账主体额度）→ 桌台自动清空。已结订单可走反结账（`POST /order/reverse`）回退为未付并留痕；挂账后续在管家端还款（`POST /credit/repayment`）。

### 3、顾客扫码点餐

扫桌上二维码进入门店页 → 按分类/搜索（ES）浏览菜品与套餐 → 购物车下单（复用 `/order/add`）→ 支付（开发期打桩）。

> 状态机、金额分单位、租户隔离等细则见 [docs/BUSINESS.md](docs/BUSINESS.md)，需求全量清单见 [docs/requirements.md](docs/requirements.md)。

## 四、技术架构

Spring Cloud 微服务：Nacos 注册与配置中心，网关统一路由 + Sa-Token 认证（token 走 `Authorization` 头）+ 租户头注入，Redis 共享会话，MyBatis-Plus 分库，Seata 分布式事务（挂账链路），Elasticsearch 菜品检索，RabbitMQ 异步消息（如开通短信）。

调用分层：客户端 → `xfcy-gateway`（8080）→ 各端 BFF（`xfcy-web`）→ 业务 provider（`xfcy-service`）→ MySQL / Redis / ES / RabbitMQ。

| 模块 | 职责 |
|---|---|
| `xfcy-common` | 公共工具与基础组件 |
| `xfcy-gateway` | API 网关：路由、CORS、Sa-Token 校验、租户上下文注入 |
| `xfcy-service`（父工程） | 微服务业务层 |
| `xfcy-service-common` | 服务层公共依赖 |
| `xfcy-operator` / `xfcy-operator-api` | 运营域：企业账号、字典、运营人员 |
| `xfcy-shop` / `xfcy-shop-api` | 集团域：品牌、门店 |
| `xfcy-store` / `xfcy-store-api` | 门店域：菜品、套餐、桌台、员工、沽清、挂账、报表 |
| `xfcy-order` / `xfcy-order-api` | 订单域：开台、点餐、结账、反结账 |
| `xfcy-es` / `xfcy-es-api` | 菜品检索（Elasticsearch） |
| `xfcy-service-job` | 定时任务（XXL-Job） |
| `xfcy-service-mq` | 消息消费（RabbitMQ） |
| `xfcy-web`（父工程） | BFF 表现层 |
| `xfcy-enterprise-web` | 管家端 BFF（8081） |
| `xfcy-operator-web` | 运营中心 BFF（8083） |
| `xfcy-cash-web` | 收银端 BFF（8085） |
| `xfcy-microapp-web` | C 端 BFF（8087，context-path `/microApp`，独立 StpLogic） |
| `xfcy-web-common` | 表现层公共依赖 |

## 五、界面与原型

- **高保真静态原型**：[docs/ui/](docs/ui/) 下 8 个页面（管家端 manager / 运营中心 operator / 收银端 cashier / C 端 microapp），纯 HTML 可直接用浏览器打开。
- **设计系统**：[DESIGN.md](DESIGN.md)（视觉方向：品类标准）+ `xfcy-ui/next/packages/ui`（@xfcy/ui 组件库、tokens 与状态语义）。
- **新前端工程**：`xfcy-ui/next/`（pnpm monorepo：`apps/` 五应用 + `packages/` ui、api、auth）。
- 旧工程 `xfcy-ui/xfcy-guanjia`、`xfcy-yunyin`、`xfcy-weixin`、`xfcy-diancan` 保留作视觉与交互参考，不再是现状。

## 六、本地起步

### 1、中间件（Docker Compose）

```powershell
docker compose -f deploy/docker-compose.yml up -d     # Nacos 3.1.1 / MySQL 8.4 / Redis 7 / RabbitMQ 3.13 / Elasticsearch 8.17
pwsh deploy/nacos/import.ps1                          # 导入 Nacos 配置清单
```

可选中间件（XXL-Job Admin、Seata）：

```powershell
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.optional.yml --profile xxljob up -d
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.optional.yml --profile seata  up -d
```

库表：先执行 [deploy/sql/init.sql](deploy/sql/init.sql) 建 4 个业务库，表结构由 Flyway V1 自动产出。

### 2、后端

应用 `bootstrap.yml` 默认连内网 Nacos（192.168.200.128:8848），本地需覆盖地址后再启动：

```powershell
$env:SPRING_CLOUD_NACOS_DISCOVERY_SERVERADDR = "127.0.0.1:8848"
$env:SPRING_CLOUD_NACOS_CONFIG_SERVERADDR    = "127.0.0.1:8848"
# 然后启动 gateway / 各 web / 各 provider（IDE 或 mvn spring-boot:run）
```

环境要求：JDK 25 + Maven。冒烟顺序与验证点见 [deploy/README.md](deploy/README.md)。

### 3、前端

```bash
cd xfcy-ui/next
pnpm install
pnpm dev        # 全部应用；也可 pnpm --filter @xfcy/manager dev 单独起一端
```

环境要求：Node ≥ 18 + pnpm。

## 七、相关链接

- 代码仓库：<https://gitee.com/itxinfei/xfcy>（前后端同仓，前端在 `xfcy-ui/`）
- 原课程演示站：<https://pip.itcast.cn/eatManager>

![公众号](docs/心飞为你飞.jpg)

QQ 群 661543188 ｜ 邮箱 747011882@qq.com

## 八、版权说明

本仓库在「餐掌柜」公开课程项目基础上二次开发，原始代码著作权归原课程机构所有。

作者改造与新增的部分——品牌更名、界面与文案调整、文档、构建与部署配置——以 **Apache-2.0** 授权，全文见 [LICENSE](LICENSE)。该授权不覆盖原始课程代码，也不构成对原始代码的再许可；完整归属说明见 [NOTICE](NOTICE)。

`xfcy-ui/xfcy-guanjia/` 与 `xfcy-ui/xfcy-yunyin/` 各带一个 MIT LICENSE（`Copyright (c) 2018 Chong Guo`），那是随脚手架模板附带的文件，授权来自模板原作者。本仓库不撤销它，但它不覆盖课程业务代码——这两层权利要分开看，商用前请分别与前端模板作者、课程机构确认。

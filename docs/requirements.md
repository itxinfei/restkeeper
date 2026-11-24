# 餐掌柜 RestKeeper 需求文档

> 版本：v2.4（重构版）｜更新日期：2026-11-22
> 本文档基于代码事实梳理：5 个 api 模块 28 个 HTTP Interface 服务契约（含 2 个空占位契约）、5 个 provider 全部 Controller 与 ServiceImpl、三个 web 端既有业务入口、gateway/mq/job 现状，作为本轮技术栈升级、功能补全与前端重写的开发依据。
>
> **v2.4 相对 v2.3 的主要变化**（阶段 4/5 收口同步，2026-11-22）
> 1. K9 业务规则已下沉 provider（开通/编辑/续费）；K8 店长登录加固；K1 两处契约注解已补（`GET /brand/list`、`GET /table/area/list`）；K12 网关改 8080、补 routes/CORS，阶段 5 已换 reactor Sa-Token AuthFilter；OSS 换 aliyun-sdk-oss；
> 2. Sa-Token 1.46 全链路落地（四套登录 + 切店签发、网关校验注入租户头），JWTUtil 与 jjwt 依赖已删除；
> 3. 10.1 阶段进度、10.3 端口现状、K8/K12 处置已同步；**未落地**：Resilience4j、K6/K14/K15/K17 与中间件冒烟。
>
> **v2.3 相对 v2.2 的主要变化**（运维与横切契约补证，全部回代码核实）
> 1. 新增 6.1「注册名、端口与 context-path」（provider/mq/job 逐个实证；operator/shop/es 本地无 server.port，端口在 Nacos common.yml，仓库不可见）与 6.2「库表清单」：全仓 @TableName 去重共 **29 张表 + 1 视图**（业务侧 27 表 + v_order_detail_all，job 独有 t_his_order/t_his_order_detail）；全仓库无任何 .sql 文件，均为 Flyway V1 待产出输入；
> 2. 第七章补「返回体分层」：统一信封 BaseResponse/ResponseAdvisor/GlobalExceptionHandler 只存在于 web_common（BFF 层）；五个 provider + mq + job 全仓无任何 @ControllerAdvice，裸返回、异常走 Spring 默认错误体（K17）；
> 3. ES 条目勘误：现状仅只读检索，全仓不存在菜品 → 索引的任何写入/同步代码，索引数据来源缺失（K16）；新增 7.8 MQ 链路（三发一收、默认交换机直投、sms_exchange 声明未用，K18）与 7.9 XXL-Job 链路（汇总→汇总→归档物理删除、套餐主明细孤儿，K19）；
> 4. 精确化员工短信表述：sendMessage 是 private 预留方法，MQ 发送代码已写好但无调用方。
>
> **v2.2 相对 v2.1 的主要变化**（经两轮只读代码核查，20 项业务行为 + 契约参数 + 旧 web/网关现状逐条实证）
> 1. 订正业务行为描述：provider 开通/编辑企业**直通保存不写状态不到期重算**（7 天试用、续费基准 now 等规则现存于旧 BFF）；还原**不改状态**；退菜是「插一条 detailStatus=2 退菜标记明细 + 原明细数量 -1」且**只冲 totalAmount**；结账金额字段全部前端透传；开台在 provider 单端点内「先置桌台开台后写日志」；还款只写 t_credit_repayment 不写 CreditLogs；
> 2. 新增第十章「旧 BFF → 新 provider 契约迁移对照表」与 web/网关端口现状，阶段 4 直接照表施工；
> 3. 第十一章已知缺口由 6 项扩到 15 项：沽清回加 NPE、反结账不校验已付、店长登录不拦截停用、网关与收银端同占 8085、登录参数命名错位、无乐观锁等实证缺陷；
> 4. 补全横向约定：Result 真实字段、登录参数命名差异、报表日期类型分化（LocalDate / LocalDateTime）、两个空契约占位不生成代理、桌台锁定态 2。

> **v2.1 的变化**：端点路径全部对齐 provider 契约真实路径（v2.0 混入旧 Dubbo web 路径，如 `/enterprise/pageList`、`/store/switchStore`、`/order/pay/orderId/{id}`，均已作废）；收录 P0/P1 共 8 个新端点；新增状态机章节；新增已知缺口章节。

---

## 一、项目概述

餐掌柜 RestKeeper 是面向餐饮行业的多租户 SaaS 管理系统，为餐饮集团/单店提供点餐、收银、挂账、经营报表全流程数字化能力，支持五端协同：

| 端 | 使用角色 | 后端模块 | 形态 |
|---|---|---|---|
| 平台运营中心 | 平台运营人员 | xfcy-operator-web（BFF） | Vue3 重写 |
| 餐掌柜管家端（企业端） | 企业账号、门店经理 | xfcy-enterprise-web（BFF） | Vue3 重写 |
| 餐掌柜收银端 | 门店收银员 | xfcy-cash-web（BFF） | Vue3 重写（触屏大按钮） |
| 点餐 H5 / 微信小程序 | 顾客（C 端） | **xfcy-microapp-web（本轮新增，规划 8087、/microApp）** | Taro 4 双端 |
| 收银 APP（Flutter） | 门店收银员 | 既有原生应用，本轮不动 | Flutter |

后端为 Maven 多模块微服务：`xfcy-common`、`xfcy-service-common`、`xfcy-web-common` 三个公共模块；5 个 `*_api` 契约模块 + 5 个 provider（operator/shop/store/order/es）+ mq/job 两个辅助服务 + gateway；三个现存 web BFF；全部完成后含新增 microapp_web 共 23 个模块。

## 二、角色、登录入口与多租户模型

### 2.1 角色

| 角色 | 所属端 | 说明 |
|---|---|---|
| 平台运营人员 | 运营中心 | 管理企业账号、平台字典 |
| 企业账号（商家） | 管家端 | 一个企业账号对应一个餐饮集团（shop_id），管理品牌、全部门店 |
| 门店经理 | 管家端 | 被企业分配管理若干门店，登录后须选择/切换当前门店 |
| 门店员工（收银员） | 收银端 | 归属具体门店，开桌/点餐/结账 |
| 顾客（C 端） | H5/小程序 | 扫码点餐支付，会话与 B 端隔离 |

### 2.2 四套 B 端登录入口（均在 provider 契约层）

| 角色 | 契约 | 端点 | 参数（均 @RequestParam） |
|---|---|---|---|
| 运营人员 | IOperatorUserService | `POST /operator/user/login` | loginName、loginPass |
| 企业账号 | IEnterpriseAccountService | `POST /enterprise/account/login` | shopId、phone、loginPass |
| 门店经理 | IStoreManagerService | `POST /store/manager/login` | shopId、phone、loginPass |
| 门店员工 | IStaffService | `POST /staff/login` | shopId、**loginName（员工姓名，不是手机号）**、**password（命名与前三套不同）** |

管家端登录页为「企业账号 / 门店经理」双身份，由 BFF 按身份分发到对应 provider 端点。四套登录已统一换 Sa-Token 签发（阶段 5，2026-11-22）：loginId 为业务主键，会话携带 shopId/storeId/loginUserId/loginUserName/userType，网关校验后经 x-tenant-* 头注入租户上下文；JWTUtil 与 jjwt 依赖已删除。

> ✅ **K13 阶段 4 已处理**：enterprise/operator BFF 已按契约映射（enterprise LoginController 注释实证），cash 端待联调逐口验证。契约参数命名不统一（员工 password / 其余 loginPass）为现状，前端须按契约映射。
> ✅ **K8 已修（阶段 5，2026-11-22）**：登录查询排除 status=0；密码校验先于门店绑定校验，提示语改为「门店管理员不存在或已停用」。

### 2.3 多租户模型

- 双租户字段：`shop_id`（集团维度）、`store_id`（门店维度）。
- provider 侧隔离策略：
  - **operator**：无租户插件（平台级数据）；
  - **shop**：单租户 shop_id（品牌、门店、门店经理）；
  - **store / order**：双租户 shop_id + store_id，全表生效，租户值缺失直接抛异常，不产生跨店数据。
- MyBatis-Plus 手写 SQL（@Select/XML）**不自动追加**租户与逻辑删除条件，必须在 SQL 中显式书写；MP Wrapper / IService 方法自动追加。
- 服务间调用经 HTTP 请求头透传：`X-Tenant-Shop-Id`、`X-Tenant-Store-Id`、`X-Login-User-Id`、`X-Login-User-Name`；入站 Filter 还原 ThreadLocal（TenantContext key：loginUserId / loginUserName），请求结束必须 clear（虚拟线程下线程复用）。
- 登录 SQL 无租户上下文，使用 `@InterceptorIgnore(tenantLine="true")` 豁免并在 SQL 中显式带 shop_id。

## 三、状态机与全局业务规则

| 对象 | 取值 | 含义 / 规则 |
|---|---|---|
| 企业账号 status | 0 试用 / 1 正式 / **-1 停用** | 试用 7 天到期、到期重算、**正式禁改回试用**等规则已下沉 provider（阶段 4，K9）；停用后不可登录；**无独立启用端点** |
| 门店 status | 1 营业 / 0 停业 | 经理可管门店列表、按省分组只含营业门店；管理端分页含全部状态 |
| 门店员工 status | 1 在职 / 0 停用 | t_staff 新增列（Flyway V1 落地，`default 1`）；停用/已删除员工禁止登录；启用走通用 `PUT /staff` 改回 status=1，无独立启用端点 |
| 菜品 / 套餐 status | 1 起售 / 0 停售 | 停售菜品连带停售关联套餐；删除菜品前先停售关联套餐再逻辑删除 |
| 订单 payStatus | 0 未付 / 1 已付 | 反结账把订单回退为 0（reverse 端点本身**不校验原单是否已付**，K7） |
| 订单 payType | 0 免单/1 现金/2 微信/3 支付宝/4 银行卡/5 挂账 | 取值与 `OrderEntity.payType` 的 @Schema 一致 |
| 订单明细 detailStatus | 1 正常/加菜，2 退菜 | 退菜不改删原明细，插一条 status=2 的标记明细 |
| 桌台 status | 0 空闲(TABLE_STATUS_FREE) / 1 开台(TABLE_STATUS_OPENED) / **2 锁定(TABLE_STATUS_LOCKED)** | 结账置空闲；反结账**不**回退桌台；锁定态目前仅旧收银端桌台面板计数使用（K15：常量注释行号错位） |
| 沽清 remainder | 整数；**无记录表示不限量，约定返回 -1** | 扣减封底为 0（仅在记录存在时扣）；退菜仅对限量菜品（remainder>0）回加，且回加方法只 update 不 insert，无记录会 NPE（K6）；表无逻辑删除字段，删除为物理删除 |

其他全局规则：

1. **金额**：数据库与接口均为 Integer 分；前端负责元/分换算。
2. **密码**：全部 BCrypt（PasswordUtil.encode/matches）。企业开通生成 6 位随机数字密码，经 mq 服务（RabbitMQ `ACCOUNT_QUEUE`）异步短信下发，重置密码无论手填或随机都会发短信；员工 addStaff 生成 8 位随机数字密码，**但当前不发短信**——`StaffServiceImpl#sendMessage`（:133）是 private 预留方法，方法体内 MQ 发送代码已写好但 addStaff 不调用它（注释自陈"开通时在事务提交后调用"），dev 阶段需显式接线或改为后台展示/其他通道。
3. **逻辑删除**：`is_deleted` 列 + `@TableLogic`；回收站查询必须手写 `is_deleted=1` SQL 绕过自动条件。**还原只翻 is_deleted，不改 status**——停用账号还原后仍不可登录，须再编辑启用。
4. **分页契约**：统一 `PageResult<T>`（records/total/page/size/pages，`PageResult.of(IPage)`）；ES 检索用 `SearchResult<T>`；旧 web 层 PageVO/IPage 信封阶段 4 淘汰。
5. **主键**：除 Staff 为 ASSIGN_UUID 外，EnterpriseAccount、Dish、OrderEntity、Table、StoreManager 等均为雪花 ASSIGN_ID（字符串型）。
6. **打包规则**：菜品与口味（DishFlavor）同事务先更新菜品后「先删后建」口味；套餐与套餐菜（SetMealDish）同理。
7. **订单明细关联**：明细必须按 `order_id` 关联主单；明细自身的 orderNumber 是带前缀的派生流水号，不能用于关联。
8. **结账金额透传**：`pay` / `pay/credit` 的 payType、payAmount、smallAmount（抹零）、personNumbers 等全部由请求体透传，provider **不重算、不做勾稽校验**；前端必须自行算准。
9. **退菜**：每次固定退一份；插一条 detailStatus=2 的退菜标记明细（份数 1、菜品价格/金额从原明细拷贝为正值、returnRemark=remarks 列表 toString），原明细数量 -1 并重算其 dishAmount；主单**只重算 totalAmount（减一份单价）**，payAmount/smallAmount/presentAmount/freeAmount 不动——已付后退菜会产生实收与应收差额（K10）；前置：原明细须为正常/加菜态且数量 >0，否则抛业务异常；remarks 类型为 `List<String>`。
10. **挂账还款**：超额还款（repaymentAmount > 当前 creditAmount）后端拒绝；通过后 Credit 的 creditAmount（欠款额）扣减、totalRepaymentAmount 累加，写一条 t_credit_repayment；**不写 CreditLogs**，挂账流水不能用 CreditLogs 统计还款。
11. **并发**：全工程实体无 @Version 乐观锁，开台/换台/结账清台只按 status 当前值判断，存在并发竞态（K11）；前端不得把桌台状态当纯本地缓存。

## 四、功能需求（按端，端点为 provider 契约权威路径）

> 服务名（Nacos 注册名沿用历史命名）：operator-dubbo-provider、shop-dubbo-provider、store-dubbo-provider、order-dubbo-provider、es-dubbo-provider。
> 标注：🆕 P0/P1 新增 ｜ 🔜 provider 已具备、阶段 4 web 层适配 ｜ ⏸️ P2 后置 ｜ ⚠️ 有附加条件/缺陷（见第十一章）。

### 4.1 平台运营中心（operator 域）

> 页面（蓝图 v1，待局部修订确认后定稿）：登录、工作台、企业账号列表、回收站、字典管理、运营人员管理、个人资料。

| 功能 | 端点 | 说明 |
|---|---|---|
| 运营人员登录 | `POST /operator/user/login` | loginName/loginPass，BCrypt 比对 |
| 运营人员分页 | `GET /operator/user/page/{pageNum}/{pageSize}?name=` | name 可选；增改 ⏸️ P2（首期页面只读置灰） |
| 企业账号分页 | `GET /enterprise/account/page/{pageNum}/{pageSize}?enterpriseName=` | enterpriseName 可选模糊，仅未删除 |
| 回收站分页 🆕 | `GET /enterprise/account/removed/page/{pageNum}/{pageSize}?enterpriseName=` | 手写 SQL 查 is_deleted=1，按申请时间倒序 |
| 开通企业 | `POST /enterprise/account/add` | 生成 8 位 shopId（随机数字，首位可 0，递归查重但**无 DB 唯一约束**）+ 6 位随机密码 BCrypt，经 MQ 短信下发。✅ **阶段 4 已下沉 provider（K9）**：add 统一计算申请时间与到期（试用 7 天 / 正式 validityDay 天） |
| 账号详情 | `GET /enterprise/account/{id}` | |
| 账号编辑 | `PUT /enterprise/account` | ✅ **阶段 4 已下沉 provider（K9）**：正式禁改回试用；试用转正式/续费一律以 **now** 为基准重算到期（原剩余天数作废）、重置 applicationTime |
| 停用账号 🆕 | `PUT /enterprise/account/forbidden/{id}` | status → -1 |
| 逻辑删除 / 还原 | `DELETE /enterprise/account/{id}`、`PUT /enterprise/account/recovery/{id}` | 还原仅 set is_deleted=0，**status 保持原值** |
| 重置密码 | `PUT /enterprise/account/resetPwd?id=&password=` | 参数名 **password**（旧 BFF VO 叫 pwd）；留空则随机 6 位；BCrypt；均发短信 |
| 企业账号登录 | `POST /enterprise/account/login` | shopId/phone/loginPass；notIn(status,-1) 排除停用（status 为 null 同样查不到） |
| 字典项查询 | `GET /sysDict/list/{category}` | 字典增改 ⏸️ P2；✅ 阶段 4 已在 operator_web 新增转发（SysDictController，旧模块缺失） |

### 4.2 餐掌柜管家端（shop + store + order 域）

> 页面（蓝图 v1，待局部修订确认后定稿）：登录（企业/门店经理双身份）、门店选择/切换、工作台、品牌管理、门店管理、门店经理管理、菜品分类、菜品管理、套餐管理、桌台区域、员工管理、沽清管理、挂账单位与用餐人、挂账流水与还款、营业设置、经营报表。

**登录与门店切换**

| 功能 | 端点 | 说明 |
|---|---|---|
| 门店经理登录 | `POST /store/manager/login` | shopId/phone/loginPass，BCrypt；✅ 阶段 5 已修（K8）：排除 status=0、密码校验先于门店绑定校验 |
| 当前经理可管门店 | `GET /store/manager/list` | 契约在 IStoreService（getStoreListByManagerId），无参，经理 id 取自 TenantContext.getLoginUserId，恒带 status=1 且 store_manager_id=当前经理，返回 List<StoreDTO> |
| 切换当前操作门店 | `PUT /store/switch/{storeId}` | 返回 Result（字段见第七章），重签含 storeId 的令牌 |
| 集团门店分页（管理用） | `GET /store/page/{pageNo}/{pageSize}?name=` | 路径变量名是 **pageNo**；含停业门店 |

**集团管理（shop 域）**

| 模块 | 功能与端点 |
|---|---|
| 品牌 | `GET /brand/page/{pageNo}/{pageSize}`、`POST /brand`；品牌下拉 `getBrandList()` ✅ 阶段 4 已补 `@GetExchange("/list")` |
| 门店 | `GET /store/page`、`POST /store`、`GET /store/{id}`、`PUT /store`；🆕 `PUT /store/disable/{id}`、🆕 `PUT /store/enable/{id}`；`GET /store/province/list`、`GET /store/province/{province}`（province 为 "all" 或空串都表示全国，恒仅返回营业门店，返回 List<StoreDTO>） |
| 门店经理 | `GET /store/manager/page/{pageNo}/{pageSize}?criteria=`（pageNo）、`POST /store/manager/add`（name/phone/storeIds:List&lt;String&gt;）、`PUT /store/manager/update`（storeManagerId/name/phone/storeIds，先清空原归属再按列表绑定）、`DELETE /store/manager/{storeManagerId}`、`PUT /store/manager/pause/{storeManagerId}`（契约方法名 pauseStoreManager，置 status=0，不校验存在，无独立启用端点） |

**门店运营（store 域）**

| 模块 | 功能与端点 |
|---|---|
| 菜品分类 | `POST /dish/category/add`（name,type）、`PUT /dish/category/{id}?name=`、`GET /dish/category/page/{pageNum}/{pageSize}`、`GET /dish/category/type/{type}`（1 菜品/2 套餐；返回 **List&lt;Map&lt;String,Object&gt;&gt;** 非实体）、`DELETE /dish/category/{id}`；分类改名/删除的增强 ⏸️ P2 |
| 菜品 | `POST /dish`（DishWithFlavors）、`PUT /dish`、`GET /dish/{id}`（resultMap 带分类+口味）、`GET /dish/enable/list/{categoryId}?name=`（点餐端可售，name 可选、实现为精确 eq）、🆕 `GET /dish/manage/page/{pageNum}/{pageSize}?name=&categoryId=&status=`（2 个 path + 3 个可选 query；name 模糊，status 留空含停售）、`PUT /dish/forbidden`、`PUT /dish/enable`、`DELETE /dish`（@RequestBody List&lt;String&gt; ids） |
| 套餐 | `GET /setMeal/page/{pageNum}/{pageSize}?name=`、`POST /setMeal`（SetMealWithDishes）、`PUT /setMeal`、`GET /setMeal/{id}`、`PUT /setMeal/forbidden`、`PUT /setMeal/enable`、`DELETE /setMeal`（ids 数组）；套餐菜品 `GET /setMeal/dish/list/{setMealId}`、`GET /setMeal/dish/copies?dishId=&setMealId=` |
| 桌台与区域 | 区域 `POST /table/area/add`（接口共 2 个方法）、区域列表 `listTableArea()` ✅ 阶段 4 已补 `@GetExchange("/list")`；桌台 `POST /table/add`、`GET /table/area/{areaId}/page/{pageNum}/{pageSize}`、`GET /table/count/{areaId}/{status}`、`GET /table/{id}`、`PUT /table`；开台见 4.3（在 ITableLogService）；桌台删除等增强 ⏸️ P2 |
| 员工 | `GET /staff/page/{pageNum}/{pageSize}?staffName=&phone=` 🆕、`POST /staff/add`（addStaff：生成 8 位密码 BCrypt，**不发短信**）、`POST /staff`（✅ 阶段 5 起统一 BCrypt 加密，未指定密码生成 8 位随机，仅收银端快速入口使用）、🆕 `PUT /staff`（编辑/启用）、🆕 `PUT /staff/pause/{id}`（停用）、`POST /staff/login`（loginName=员工姓名） |
| 沽清 | 🆕 `GET /sellCalculation/page/{pageNum}/{pageSize}?dishName=&dishType=`（dishType 为 Integer）、`GET /sellCalculation/remainder/{dishId}`（无记录返回 -1）、`PUT /sellCalculation/decrease?dishId=&dishNumber=`（**dishNumber:Integer**，封底 0，仅记录存在时扣）、`POST /sellCalculation/add?dishId=&dishNumber=`（**dishNumber:int**，无记录则新建，✅ K6 已修）、`POST /sellCalculation`（@RequestBody SellCalculation 裸保存）、`DELETE /sellCalculation`（ids 数组，物理删除） |
| 挂账单位 | `POST /credit/add`（CreditWithCompanyUsers，含关联用餐人）、`GET /credit/page/{pageNum}/{pageSize}?userName=`、`GET /credit/{id}`（queryById 回显，带关联用餐人）、`GET /credit/get/{id}`（getById 单表）、`PUT /credit`（updateInfo 修改，CreditWithCompanyUsers）、`POST /credit/saveOrUpdate`（裸 Credit）；用餐人 `GET /credit/companyUser/list/{creditId}` |
| 挂账流水 | `GET /credit/log/page/{pageNum}/{pageSize}/{creditId}`（creditId 为 path，无 query）、`GET /credit/log/list?creditId=&start=&end=`（**start/end 为 LocalDateTime、`yyyy-MM-dd HH:mm:ss`、均可选**，勿与报表的 LocalDate 混用）、`POST /credit/log`；EasyExcel 导出在 BFF 层 🔜（仅 enterprise_web 有，CreditController#export） |
| 挂账还款 | `POST /credit/repayment`（CreditRepayment；超额拒绝；creditAmount 扣减、totalRepaymentAmount 累加；不写 CreditLogs） |
| 营业设置 | `GET /remark/list`、`PUT /remark/update`（@RequestBody List&lt;Remark&gt; 整批） |
| 文件服务 | **web 层能力，不在 28 个 provider 契约内**：旧 enterprise_web 暴露 `POST /fileUpload`、`POST /imageUploadResize`，直连 OSSClient；✅ 阶段 4 已换 aliyun-sdk-oss 3.18.5：FileStorage 抽象，dev 本地磁盘、其它环境 OSS，OSSClient 手动装配 |

**经营报表（order 域读模型）**

> 除挂账流水 list 外，所有报表 start/end 均为 **LocalDate（ISO `yyyy-MM-dd`）、必填 query 参数**；区间语义为 `lastUpdateTime >= start AND < end`，前端传「今天/明天」才是当日。

| 契约 | 端点 |
|---|---|
| IOrderService | `GET /order/report/currentAmount?start=&end=`、`/order/report/hourCollect?start=&end=&type=`（type 必填，1 金额/2 单数）、`/order/report/payType?start=&end=`、`/order/report/privilege?start=&end=` |
| IOrderDetailService | `GET /order/detail/report/categoryAmount?start=&end=`、`/categoryCount`、`/dishRank`；按明细 lastUpdateTime 过滤且固定 dishType=1（单品，不含套餐） |
| IReportPayService | `GET /report/pay/dayAmount?start=&end=`（区间趋势）、`/payType`、`/privilege` |
| IReportDishService | `GET /report/dish/categoryAmount?start=&end=`（饼图）、`/dishRank` |

### 4.3 餐掌柜收银端（store + order + es 域）

> 页面（蓝图 v1，待局部修订确认后定稿）：登录、桌台总览（区域 + 空闲/占用/锁定计数）、点餐页、下单成功页；开台、退菜、结账（含挂账/换台/反结账）弹窗。

| 模块 | 端点 / 编排 |
|---|---|
| 登录 | `POST /staff/login`（shopId、loginName=员工姓名、password） |
| 桌台 | 区域列表（K1 修复后 `GET /table/area/list`）+ `GET /table/area/{areaId}/page/...`、`GET /table/count/{areaId}/{status}`（status 0/1/2） |
| 开台 | **单一端点** `POST /table/log/open`（@RequestBody TableLog：tableId、userNumbers）。provider 内部顺序：校验桌台空闲（否则抛异常）→ 校验人数 ≤ tableSeatNumber → **先置桌台 status=1 开台 → 后写 TableLog**。BFF 只调一次，不需要再发 `PUT /table` |
| 点餐检索 | ES：`GET /search/dish/code/{code}/page/{pageNum}/{pageSize}`（条码）、`/search/dish/code/{code}/type/{type}/...`、`/search/dish/name/{name}/type/{type}/...`；分类可售菜 `GET /dish/enable/list/{categoryId}` |
| 下单 / 加菜 | **同一端点** `POST /order/add`（@GlobalTransactional）：body 不带 orderId 为下单（生成流水号、逐菜沽清校验扣减），带已存在 orderId 为加菜（saveOrUpdate 更新主单、追加明细） |
| 退菜 | `POST /order/returnDish`（@RequestBody DetailDTO：detailId + `List<String> remarks`，一次退一份）；插 detailStatus=2 标记明细、原明细数量 -1、仅回加限量菜品沽清（remainder>0）、主单仅 totalAmount 减一份单价（规则 9） |
| 现金结账 | `POST /order/pay`（@RequestBody OrderEntity；金额字段前端透传；provider 更新订单 → 桌台置空闲 → 套餐按优惠率拆单品写 OrderDetailMeal） |
| 挂账结账 | `POST /order/pay/credit`（@RequestBody CreditPayRequest：**仅两字段 `order`(OrderEntity) 与 `credit`(creditId/creditAmount/creditUserName)**；@GlobalTransactional）：个人挂账按姓名字符串等值比对、公司挂账查成员表按姓名匹配，不存在即拒 → creditAmount 累加 → 写 CreditLogs → 桌台置空闲 → 生成套餐拆单。⚠️ CreditLogs 有重复赋值瑕疵（K14） |
| 换台 | `PUT /order/changeTable?orderId=&targetTableId=`（目标桌不存在或非空闲即抛异常；原桌置空闲、目标桌开台、对目标桌写 TableLog、订单改挂目标桌；本地 @Transactional） |
| 反结账 | 先 `GET /order/number/{orderNumber}` 🆕 按流水号查**已付**订单（含明细；非已付或查无返回 null，作为前端入口约束），确认后 `POST /order/reverse`（IReverseOrderService，@RequestBody ReverseOrder：orderId + remark）：订单 payStatus 置 0、写 t_reverse_order（回填 orderNumber/tableId/amount=原单 payAmount/createTime/operatorName）。**reverse 本身不校验原单已付、不恢复桌台、不回冲沽清**（K7） |
| 沽清维护 | `POST /sellCalculation`（body）、`DELETE /sellCalculation`（ids）；另见 4.2 沽清行 |

### 4.4 点餐 H5 / 微信小程序后端（xfcy-microapp-web，阶段 6 新增）

- 端口 8087，context-path `/microApp`；HTTP Interface 复用 store/order 契约，不新建数据访问层；
- C 端接口范围：按门店浏览分类与可售菜品/套餐、购物车提交订单（复用 `/order/add`）、加菜、模拟支付（dev 打桩：微信 code2session、JSAPI）；
- 认证：Sa-Token 独立 StpLogic，C 端会话与 B 端隔离；
- Taro 4 前端 4 个页面（蓝图 v1）：门店菜单（分类+菜品+套餐详情）、购物车、下单、支付结果。

## 五、核心业务流程

1. **入驻开通**：运营开通企业（shopId 8 位随机数 + 6 位随机密码 BCrypt，经 RabbitMQ `ACCOUNT_QUEUE` 短信下发；**试用 status=0、到期时间 now+7 天由 provider 统一计算（阶段 4 已下沉，K9）**）→ 企业登录维护品牌/门店/门店经理/菜品分类/菜品套餐/桌台区域/员工。
2. **堂食主线**：员工登录 → 开台（桌台须空闲、人数不超座位数；桌台置开台并写 TableLog）→ 点餐（Order 主单生成流水号 + OrderDetail 明细，逐菜做沽清校验与扣减，@GlobalTransactional）→ 加菜（同端点带 orderId）/退菜（插退菜标记明细、原明细 -1、仅冲 totalAmount、限量菜回补沽清）→ 结账（现金/挂账；桌台置空闲；套餐明细按优惠率拆单品写 OrderDetailMeal 供菜品维度报表）。
3. **挂账结账（分布式事务）**：校验挂账单位与用餐人（个人比对姓名、公司比对成员表）→ order 库更新订单、store 库累加 Credit 额度并写 CreditLogs，两库由 Apache Seata 2.5 `@GlobalTransactional` 保证一致 → 后续管家端 `POST /credit/repayment` 还款（超额拒绝；只写 t_credit_repayment，不写 CreditLogs）。
4. **反结账**：按流水号查已付订单（`GET /order/number/{orderNumber}`，含明细）→ 填原因提交 `POST /order/reverse`：订单 payStatus 回退 0，向 t_reverse_order 写留痕（操作人取租户头）。**实证：不校验原单已付、不恢复桌台、不回冲沽清、不写负向明细**——反结后桌台仍显示空闲，订单以未付态重新结账即可。
5. **顾客扫码点餐**：进入门店页 → 浏览分类菜品（ES 按编码/名称检索）→ 购物车下单 → 支付（dev 打桩）。
6. **门店切换**：门店经理选择门店 → `PUT /store/switch/{storeId}` 重签含 storeId 的令牌 → 后续请求租户头随之变化，双租户插件自动隔离。
7. **续费/转正（旧 BFF 规则，阶段 4 已下沉 provider）**：试用转正式、正式续费均以**当前时间**为基准加 validityDay，原剩余有效期作废；正式账号禁止改回试用；账号「启用」通过编辑提交 status 实现，无独立端点。

## 六、服务与数据域

| provider | api 模块 | 契约数 | 主要实体 |
|---|---|---|---|
| operator | xfcy-operator-api | 3 | EnterpriseAccount、OperatorUser、SysDictionary |
| shop | xfcy-shop-api | 3 | Brand、Store、StoreManager；DTO：StoreDTO |
| store | xfcy-store-api | 15 | Dish、DishCategory、DishFlavor、SetMeal、SetMealDish、Table、TableArea、TableLog、Staff、Credit、CreditCompanyUser、CreditLogs、CreditRepayment、Remark、SellCalculation |
| order | xfcy-order-api | 6 | OrderEntity、OrderDetailEntity、OrderDetailMealEntity、OrderDetailAllView（读视图）、ReverseOrder、ReportDishEntity、ReportPay；DTO：DetailDTO、CreditPayRequest |
| es | xfcy-es-api | 1 | DishEs（@Document 索引视图，SearchResult 包装） |

合计 28 个服务契约（**含 2 个空占位契约**：`IDishFlavorService`、`IOrderDetailMealService`——接口体为空、无 Controller 落地，相关本地能力由不实现契约的 MP ServiceImpl 承载；web 端**不得**为这两个接口生成 HTTP 代理或注册端点）。

复合请求体 DTO：DishWithFlavors、SetMealWithDishes、CreditWithCompanyUsers、CreditPayRequest；读模型 DTO：CurrentAmountCollectDTO、CurrentHourCollectDTO、PayTypeCollectDTO、PrivilegeDTO、DayAmountCollectDTO、OrderDetailAllView、StoreDTO、DetailDTO。

辅助服务：`xfcy-service-mq`（短信：阿里云 dysmsapi20170525，dev 打桩；消费 RabbitMQ 账号队列，链路见 7.8）、`xfcy-service-job`（XXL-Job 3.5 定时任务，独立的报表聚合 + 历史归档实现，见 7.9）、`xfcy-gateway`（统一入口、CORS 收口、Sa-Token reactor 鉴权——阶段 5 已实现）。

每个业务库独立 Flyway 迁移（`V1__init.sql`），DDL 按实体与现存 Mapper XML 逆向产出；**t_staff.status 新列随 V1 一并落地**。

### 6.1 注册名、端口与 context-path（仓库 bootstrap.yml 实证）

| 模块 | Nacos 注册名（历史命名沿用） | 本地端口 | context-path |
|---|---|---|---|
| operator provider | operator-dubbo-provider | 本地 bootstrap.yml **无 server.port**（在 Nacos common.yml，仓库不可见，中间件阶段核实） | 仓库内未见配置，待核实 |
| shop provider | shop-dubbo-provider | 同上 | 同上 |
| es provider | es-dubbo-provider | 同上 | 同上 |
| store provider | store-dubbo-provider | **9991** | 同上 |
| order provider | order-dubbo-provider | **9992** | 同上 |
| mq | restkeeper-mq | **8089** | 同上 |
| job | restkeeper-job | **8001**；XXL-Job executor 通信口 **9999**（双数据源） | 同上 |
| 三个 web + gateway | — | 8081 / 8083 / 8085；gateway 8080（K12 已改） | 见 10.3 |
| microapp_web（规划） | — | 8087 | `/microApp` |

provider 均无 servlet context-path 配置，路径前缀全部由契约类级 `@HttpExchange` 承担；全部服务注册中心指向既有 Nacos（192.168.200.128:8848，内网，**本轮不改动该环境**）。

### 6.2 库表清单（Flyway V1 输入，@TableName 全量去重实证）

物理库名与 jdbc 数据源在 Nacos（仓库不可见），下表按 provider 数据域分组；表结构已由 V1 DDL 产出（`deploy/sql/V1__restkeeper_{operator,shop,store,order}.sql`，共 29 表 + 1 视图），列以实体 + Mapper XML 为准，含 shop_id/store_id 租户列、is_deleted 逻辑删除列、字符集与索引。

| 数据域 | 表 / 视图 |
|---|---|
| operator（3 表） | t_enterprise_account、t_operator_user、t_sys_dict |
| shop（3 表） | t_brand、t_store、t_store_manager |
| store（15 表） | t_category、t_dish、t_dish_flavor、t_setmeal、t_setmeal_dish、t_table、t_table_area、t_table_log、t_staff（**含新增 status 列，default 1**）、t_credit、t_credit_company_user、t_credit_logs、t_credit_repayment、t_remark、t_sell_calculation |
| order（6 表 + 1 视图） | t_order、t_order_detail、t_order_detail_meal、t_reverse_order、t_report_pay、t_report_dish；视图 **v_order_detail_all**（已随 V1 产出：两明细表 UNION ALL，含租户列） |
| job 归档独有（2 表，与 order 同库） | t_his_order、t_his_order_detail |
| es | 无关系表；索引 **dish**（DishEs `@Document(indexName="dish")`），mapping / IK 词典 / 初始化数据方案随中间件阶段产出（K16） |

合计 **27 张业务表 + 2 张历史表 + 1 视图**。注意 job 模块自带一整套**复制的**实体与 Mapper（包名 `com.restkeeper.entity`，不依赖 order_api），映射 t_order / t_order_detail / t_order_detail_meal / v_order_detail_all 及 t_report_*、t_his_*，迁移时两边字段定义要一起对齐。

## 七、接口约定与横切机制

1. **契约即接口**：`@HttpExchange` 接口由 provider 端 `@RestController implements` 继承映射（不重复注解），web 端由 HttpServiceProxyFactory + `@LoadBalanced` RestClient.Builder 生成代理；RestClient.Builder 每次 clone 后再挂租户拦截器，避免拦截器重复累积。
2. **返回体分层（实证）**：
   - **provider 层裸返回**：登录/切店返回 `com.restkeeper.utils.Result`，真实字段为 **`status`(int)、`desc`、`data`(Object)、`token`**（不是 code/message）；写操作直接返回 boolean；列表分页 PageResult；ES SearchResult；业务错误直接抛 `BussinessException`（common 模块，仅 message）。**阶段 5 起 provider 侧由 service_common 的 `ProviderExceptionAdvice` 统一收敛**（BussinessException/IllegalArgumentException → `Result`(status=500,desc)，兜底 Exception 记日志）；原「全仓无 @ControllerAdvice」现状已由 K17 修复。
   - **BFF 层统一信封**：web_common 的 `ResponseAdvisor`（@RestControllerAdvice(basePackages="com.restkeeper") + ResponseBodyAdvice）把 Result 原样透传、Boolean 包 BaseResponse（false → code 300）、PageVO 包装、异常包 400；`BaseResponse` 字段 success/message/code/data（200 成功 / 300 操作失败 / 400 异常，@JsonInclude(NON_NULL)）；`GlobalExceptionHandler` 兜底 BussinessException、NotLoginException、NotRole/NotPermissionException 与兜底 Exception。阶段 4 已确认保留（`ResponseAdvisor`/`GlobalExceptionHandler` 仍在 web_common），**前端契约以 BFF 信封为准，直连 provider 的返回不做包装**。
3. **参数风格**：集合/复杂体 @RequestBody（停起售、删除收 List&lt;String&gt; ids）；多散参用 @RequestParam；路径变量命名 pageNo（shop 域）与 pageNum（其余）并存，迁移时以契约为准。
4. **日期格式分化**：报表查询参数 LocalDate + `@DateTimeFormat(iso=DATE)`（yyyy-MM-dd，必填）；挂账流水 list 的 start/end 为 LocalDateTime + `yyyy-MM-dd HH:mm:ss`（可选）。
5. **事务**：本地 @Transactional 在 Service 层；跨库 @GlobalTransactional 标在 OrderServiceImpl 的 addOrder 与 payByCredit 两个方法上。
6. **ES**：Spring Data Elasticsearch（Elasticsearch Java Client）+ ES 8.17 + IK；`DishEs @Document(indexName="dish")`，查询以 shop_id/store_id/is_deleted 走 filter、code/dish_name 用 wildcard 保持 SQL like 语义、按 last_update_time 倒序。**现状 es provider 仅只读检索：全仓无任何索引写入/同步代码（无 save/index/Repository、无 MQ 监听、无 Logstash 配置），索引初始数据来源缺失（K16）**——"菜品/套餐变更同步索引"是待建设能力，不是现状。
7. **服务韧性**：Resilience4j 超时/重试/熔断（✅ 阶段 5 已接入：web_common `HttpClientFactory` 按服务名用 Retry + CircuitBreaker 装饰 HTTP Interface 代理；默认 3 次重试 / 10s 超时 / 50% 熔断，可按实例在 yml 覆盖）。
8. **短信 MQ 链路（实证）**：RabbitMQ 队列名 `account_queue`（SystemCode.SMS_ACCOUNT_QUEUE）。三个发送方——operator `EnterpriseAccountServiceImpl:62`（开通/重置密码）、shop `StoreManagerServiceImpl:236`（店长开通）、store `StaffServiceImpl:142`（**private 预留，当前无调用方**）——均为 `convertAndSend("account_queue", json)` 单参形式，**走默认交换机按队列名直投**；restkeeper-mq 的 `SmsMessageListener` @RabbitListener 消费后调阿里云 dysmsapi 发送。operator 的 RabbitMQConfig 另声明了 `sms_exchange` + `account_queue_key` 绑定，但**没有任何发送方使用该交换机（K18）**；listener 的 catch 中使用 printStackTrace，违反 java.md。
9. **定时任务链路（实证）**：XXL-Job 3.5，执行器名 restkeeper-job，handler 名 `generateReportHandler`（须与 xxl-job-admin 中配置一致）。执行顺序：reportPay 汇总写 t_report_pay → reportDish 汇总写 t_report_dish → `OrderHistoryServiceImpl.exportToHistory` 把 lastUpdateTime < 今天的订单搬入 t_his_order / t_his_order_detail，**并物理删除 t_order / t_order_detail / t_order_detail_meal 原记录**（单事务 @Transactional(rollbackFor=Exception)、无分页全量搬运）。归档时 dishType=1 明细拷贝后删除、套餐只搬 meal 拆单记录，**套餐主明细（dishType=2）既不迁历史也不删除，主单被删后成为孤儿数据（K19）**；job 内用 System.out.println 打日志，与 K18 的 printStackTrace 一并在后置阶段整改。

## 八、非功能需求（重构目标）

1. 运行基线：JDK 25（虚拟线程）、Spring Boot 4.1.1、Spring Cloud 2025.1.4、SCA 2025.1.0.0；全反应堆编译通过。
2. 去 Dubbo：28 契约改 Spring HTTP Interface，provider/web 两端复用一套接口。
3. 认证统一：Sa-Token 1.46 + Redis 共享会话替换自研 JWT；BCrypt 替换全部 MD5 密码。
4. 多租户可靠：双 TenantLineInnerInterceptor + PaginationInnerInterceptor（分页最后）；HTTP 头透传替换 RpcContext；ThreadLocal 必清理。
5. 可观测：actuator + Prometheus + Micrometer Tracing（Zipkin）；核心链路 JUnit5 + Mockito + Testcontainers。
6. 接口文档：springdoc 3.1.1 自动生成（OpenAPI）。
7. 前端：Vue 3.5 + TS + Vite 8 + Element Plus + Pinia + ECharts 6，pnpm monorepo；Taro 4.3 + React 18 + NutUI 产 H5/微信小程序。

## 九、技术栈（定稿）

- 基础：JDK 25、Boot 4.1.1、Cloud 2025.1.4、SCA 2025.1.0.0、Nacos Client 3.1.1
- 通信：Spring HTTP Interface + RestClient；Apache Seata 2.5.0（自动代理）；Resilience4j（Spring Cloud CircuitBreaker，阶段 5 已接入）
- 数据：MyBatis-Plus 3.5.17（boot4 starter）、HikariCP、mysql-connector-j、Flyway、Redis(Lettuce)、Spring Data Elasticsearch + ES 8.17 + IK
- 认证/文档：Sa-Token 1.46（servlet/reactor/redis-jackson）、spring-security-crypto(BCrypt)、springdoc 3.1.1
- 工具：fastjson2 2.0.66、EasyExcel 4.0.3、XXL-Job 3.5.0、dysmsapi20170525、aliyun-sdk-oss 3.18.5、Lombok 1.18.48
- 前端：pnpm workspace（apps/operator、enterprise、h5 + packages/shared）、Vue 3.5/TS/Vite 8/Element Plus/Pinia/ECharts 6；Taro 4.3.1 + React 18 + NutUI（先双端 spike）

## 十、分阶段计划、迁移对照与当前进度

### 10.1 阶段进度

| 阶段 | 内容 | 状态（2026-11-22） |
|---|---|---|
| 1 | 地基三层升级（BOM/JDK25、common 桥接、租户透传三件套、叶子 pom 锁版） | ✅ 完成（已提交） |
| 2 | api 契约层 @HttpExchange 化（28 契约、PageResult、复合 DTO、@Schema 迁移） | ✅ 完成（已提交） |
| 3 | provider 适配（薄 Controller、MP 3.5.17、双租户、Seata 2.5、ES Spring Data、BCrypt 登录） | ✅ 主体完成（已提交） |
| P0/P1 补端点 | 8 个缺口端点 + 员工登录加固 + 账号注释勘误 | ✅ 完成（已提交） |
| 4 | web BFF 适配 + 网关 | ✅ 完成（已提交） |
| 5 | Sa-Token 全链路落地 + 中间件环境冒烟 | 🚧 进行中（Sa-Token 全链路已落地并编译通过；中间件冒烟待环境就绪） |
| 6 | 新增 xfcy-microapp-web（8087、/microApp、C 端 StpLogic） | 未开始 |
| 7 | 前端 pnpm monorepo 重写三端 + Taro 点餐 spike | 未开始 |

### 10.2 旧 BFF → 新 provider 契约迁移对照表（阶段 4 施工依据）

| 业务 | 旧 web 路径（方法） | 新 provider 契约 |
|---|---|---|
| 企业账号分页 | `GET /enterprise/pageList/{page}/{pageSize}` | `GET /enterprise/account/page/{pageNum}/{pageSize}` |
| 企业开通 | `POST /enterprise/add` | `POST /enterprise/account/add` |
| 企业删除 | `DELETE /enterprise/deleteById/{id}` | `DELETE /enterprise/account/{id}` |
| 企业停用 | `PUT /enterprise/forbidden/{id}` | `PUT /enterprise/account/forbidden/{id}` |
| 企业还原 | `PUT /enterprise/recovery/{id}` | `PUT /enterprise/account/recovery/{id}` |
| 重置密码 | `PUT /enterprise/resetPwd`（VO 字段 pwd） | `PUT /enterprise/account/resetPwd?id=&password=` |
| 运营/企业/店长登录 | `POST /login`（无类级前缀，enterprise 按 type 分流） | 各自 `/operator/user/login`、`/enterprise/account/login`、`/store/manager/login` |
| 经理门店列表 | `GET /store/listManagerStores` | `GET /store/manager/list` |
| 门店切换 | `GET /store/switchStore/{storeId}` | `PUT /store/switch/{storeId}` |
| 门店分页 | `GET /store/pageList/{page}/{pageSize}` | `GET /store/page/{pageNo}/{pageSize}` |
| 品牌下拉 | `GET /brand/brandList` | `getBrandList()` → 补注解后 `GET /brand/list`（K1） |
| 店长前缀 | 类级 **/storeManger**（拼写错误），分页用 POST | `/store/manager`，分页 `GET /page/{pageNo}/{pageSize}?criteria=` |
| 区域列表 | `GET /table/listTableArea`（cash） | `listTableArea()` → 补注解后 `GET /table/area/list`（K1） |
| 开台 | `PUT /table/openTable/{tableId}/{numbers}` | `POST /table/log/open`（TableLog body；桌台联动在 provider 内） |
| 桌台分页 | `GET /table/search/{areaId}/{page}/{pageSize}` | `GET /table/area/{areaId}/page/{pageNum}/{pageSize}` |
| 可售菜品 | `GET /dish/findEnableDishList/{categoryId}` | `GET /dish/enable/list/{categoryId}` |
| 菜品改状态 | `PUT /dish/updateStatus` | `PUT /dish/forbidden` / `PUT /dish/enable`（ids 数组） |
| 菜品删除 | `DELETE /dish/delete` | `DELETE /dish`（ids 数组 body） |
| 加菜 | `POST /order/plusDish/orderId/{orderId}` | `POST /order/add`（body 带 orderId） |
| 退菜 | `POST /order/returnDish/{detailId}`（body 为 List&lt;String&gt;） | `POST /order/returnDish`（@RequestBody DetailDTO） |
| 现金结账 | `POST /order/pay/orderId/{orderId}` | `POST /order/pay`（@RequestBody OrderEntity） |
| 挂账结账 | `pay(order, creditDTO)` 双参 Dubbo 重载（已不存在） | `POST /order/pay/credit`（@RequestBody CreditPayRequest） |
| 换台 | `GET /order/changeTable/{orderId}/{targetTableId}` | `PUT /order/changeTable?orderId=&targetTableId=` |
| 反结账 | `POST /order/reverse/{orderId}` | `POST /order/reverse`（@RequestBody ReverseOrder） |
| 条码检索 | `GET /dish/queryByCode/{code}/{page}/{pageSize}`（cash 调 es） | `GET /search/dish/code/{code}/page/{pageNum}/{pageSize}` |
| 沽清新增/删除 | `POST /sellCalculation/add`、`DELETE /sellCalculation/delete` | `POST /sellCalculation`、`DELETE /sellCalculation`（body） |
| 经营报表 | 日期走路径变量（如 `/report/datePayTypeCollect/{start}/{end}`） | 各 report 端点 query 参数 start/end（LocalDate 必填） |
| 挂账流水 | `GET /credit/creditLog/{page}/{pageSize}/{creditId}` | `GET /credit/log/page/{pageNum}/{pageSize}/{creditId}` |
| 挂账流水导出 | `GET /credit/export/creditId/{creditId}/start/{start}/end/{end}` | BFF 调 `GET /credit/log/list`（LocalDateTime）后 EasyExcel 导出，端点路径 BFF 自定义 |
| 分页信封 | IPage/PageVO | PageResult（对前端响应字段保持兼容映射） |

### 10.3 web / gateway 端口现状（仓库内 bootstrap.yml 实证）

| 模块 | 端口 | 备注 |
|---|---|---|
| operator_web | 8083 | bootstrap.yml:21-33 残留整段 dubbo 配置，阶段 4 删除 |
| enterprise_web | 8081 | 无 context-path；OSS 老 starter 硬编码 2.1.1.RELEASE |
| cash_web | **8085** | 无 context-path |
| gateway | **8080** | ✅ K12 已修：端口改 8080、routes 已补录、CORS 收口、阶段 5 已换 reactor Sa-Token AuthFilter、openfeign 已删 |
| microapp_web（规划） | 8087 | context-path `/microApp` |

三个 web 的本地 bootstrap.yml 均**未配置** server.servlet.context-path，旧路径前缀全部由类级 @RequestMapping 承担；Nacos common.yml 中是否另有 context-path 需在中间件阶段核实。

### 10.4 阶段 4 任务清单

1. 三个 web 的 19 个 `@Reference` 文件（operator 2 / enterprise 12 / cash 5）全部换 HttpServiceProxyFactory 客户端 Bean（租户拦截器已接入；Resilience4j 于阶段 5 接入 web_common）；
2. 按 10.2 对照表逐端点改写 Controller；挂账结账编排拆为 pay/payByCredit 两个调用；
3. 登录参数映射统一（K13：password/loginPass + 旧 VO 交叉错位）；沽清散参命名统一（dishNum/dishNumber）✅ 阶段 5 已统一；
4. PageVO → PageResult；javax → jakarta（EasyExcel 导出保留在 enterprise CreditController#export）；Guava/commons-lang 旧调用改 JDK；
5. 修复 K1 两处契约缺注解（`GET /brand/list`、`GET /table/area/list`）并回归编译；
6. operator_web **新增** sysDict 转发（旧模块没有）；删 3 份 springfox SwaggerConfiguration 与 swagger2 @Api 注解（web_common 已换 springdoc）；
7. 网关：解决 8085 端口冲突、从 Nacos 补录 routes、删 openfeign、CORS 收口、reactor Sa-Token 鉴权替换 JWT AuthFilter；删 operator_web bootstrap 的 dubbo 段；
8. OSS：enterprise_web 换 aliyun-sdk-oss 并手动配 OSSClient Bean（dev 本地磁盘），FileUploadController 的 @RefreshScope/@Value 随之调整；
9. **业务规则落点**：7 天试用、到期重算（基准 now）、正式禁改试用目前只存在于旧 operator BFF（K9），新 BFF 必须重新实现或下沉 provider；
10. 验收：三个 web + gateway 编译通过，全反应堆 `mvn compile` 全绿。

> ✅ **阶段 4 收口（2026-11-22）**：以上 10 项全部完成，全反应堆 `mvn compile -DskipTests` BUILD SUCCESS；K9 已下沉 provider，OSS 已换 aliyun-sdk-oss，sysDict 转发已新增，网关端口/routes/CORS 已改造（reactor Sa-Token 鉴权留阶段 5）。

> ✅ **阶段 5 第一批收口（2026-11-22）**：Sa-Token 1.46 全链路落地——operator/shop/store provider 与 gateway 引入 sa-token starter（redis-jackson 共享会话；token-name=Authorization、有效期 3 天、并发/共享/读头不读 Cookie）；运营/企业/店长/员工四套登录与切店改 `StpUtil.login` 签发 + 会话属性（shopId/storeId/loginUserId/loginUserName/userType）；网关 AuthFilter 改 reactor `SaReactorSyncHolder` + `StpUtil.checkLogin()` 校验并经 x-tenant-* 头注入租户；K8 已修；删除 JWTUtil 与 jjwt 依赖。全反应堆 `mvn compile -DskipTests` 通过。**第二批（2026-11-22）**：K17 provider 统一异常 advice（service_common `ProviderExceptionAdvice`）、Resilience4j 接入 web_common（Retry+CircuitBreaker 装饰 HTTP 客户端）、员工 `POST /staff` 改加密、K14 去重并修正 receivedAmount。**遗留**：中间件环境冒烟（Nacos/Redis/MySQL/RabbitMQ/ES，连接配置多在 Nacos common.yml）与端到端登录联调，待环境就绪。

### 10.5 阶段 5 遗留与待排期（2026-11-22 审查）

| 项 | 现状 | 处置 |
|---|---|---|
| 中间件冒烟 | Nacos/Redis/MySQL/RabbitMQ/ES 实例未就绪，连接配置多在 Nacos common.yml（仓库不可见） | 环境就绪后验证 Sa-Token Redis 会话、reactor 上下文、端到端登录 |

**后置（用户确认「以后再说」）**：docker-compose 全套、Nacos 配置清单（含 gateway.yml 路由、common.yml context-path 核实）、正式部署文档、Prometheus/Grafana/Zipkin 面板；mq 短信真实通道联调；5 库 Flyway DDL 在数据层改造时随代码产出（t_staff.status 含在内）。

## 十一、已知缺口与后置项（实证）

| 编号 | 问题 | 影响 | 处置 |
|---|---|---|---|
| K1 | `IBrandService.getBrandList()`、`ITableAreaService.listTableArea()` 契约与 Controller 方法上均无方法级映射注解 | 品牌下拉、区域列表 HTTP 不可达 | ✅ 阶段 4 已补 `@GetExchange("/list")`（代码实证） |
| K2 | t_staff 无 status 列，实体已加字段 | 员工停用/登录加固运行时依赖该列 | Flyway V1 补 `status int default 1`，此前勿联调员工登录 |
| K3 | 三个 web 共 19 个文件仍 import Dubbo @Reference、3 份 @EnableSwagger2 配置、swagger2 注解遍布，而 pom 已无 dubbo 依赖 | 三 web 模块当前编译不过，前端无 BFF 可调 | 阶段 4 整体重写（10.4） |
| K4 | 企业账号「启用」无独立端点 | 启用须选试用/正式并重算到期 | 产品决策：走编辑接口，非缺陷 |
| K5 | 菜品管理端分页经类级 resultMap 嵌套 select 装配分类/口味，存在 N+1（每页约 20 次子查询） | 教学规模可接受 | 后续优化改分步查询或 XML join |
| K6 | `SellCalculationServiceImpl.add` 按 dishId 查不到记录时直接 NPE，只 update 不 insert/upsert；且 add 散参 dishNum 与 decrease 的 dishNumber 命名不一致 | 对未建沽清记录的菜品退菜回加会 500 | ✅ 阶段 5 已修：add 改 upsert（无记录则 insert）+ 参数名统一 dishNumber |
| K7 | `ReverseOrderServiceImpl.reverse` 不校验原单 payStatus 是否已付 | 裸调端点可反结未付订单 | 前端入口已由 `GET /order/number/{orderNumber}` 限已付；建议 provider 补已付前置校验 |
| K8 | 门店经理登录不排除 status=0/-1（停用者仍可登录）；且「至少绑定一家门店」校验先于密码校验 | 停用店长可登录；无门店时密码错误也提示门店问题 | ✅ 阶段 5 已修：StoreManagerServiceImpl 登录查询加 status≠0 条件、校验顺序调整、新增经理补 status=ENABLED |
| K9 | provider 的企业 add 不写 status/expireTime/applicationTime、update 不重算到期；7 天试用、续费基准 now、正式禁改试用只存在于旧 Dubbo BFF | 直连新端点开通的账号 status=null 无法登录；BFF 不重写则业务规则丢失 | ✅ 阶段 4 已下沉 provider：EnterpriseAccountServiceImpl#add/updateById 单点计算与校验（7 天试用 / 基准 now / 正式禁改试用） |
| K10 | 退菜只冲订单 totalAmount，不回冲 payAmount/抹零/赠菜/免单 | 已付后退菜产生实收与应收差额 | 与产品确认是否接受；如需红冲需补账单差额处理 |
| K11 | 全工程无 @Version，开台/换台/清台仅按 status 现值判断 | 并发操作存在竞态覆盖 | 教学项目可接受；前端勿缓存桌台状态，必要时加乐观锁 |
| K12 | gateway 与 cash_web 同占 8085；网关仓库内无 routes、无 CORS、仍用自研 JWT AuthFilter、pom 残留 openfeign | 同机启动端口冲突；鉴权/CORS 与目标态不符 | ✅ 阶段 4 改端口 8080、补路由、CORS 收口、删 openfeign；阶段 5 已换 reactor Sa-Token AuthFilter |
| K13 | 登录参数命名不统一（员工 password / 其余 loginPass），旧 BFF VO 字段交叉错位；分页路径变量 pageNo/pageNum 并存 | BFF 迁移时极易 400 | ✅ 阶段 4 已映射（enterprise/operator，代码实证）；cash 待联调逐口验证 |
| K14 | 挂账结账写 CreditLogs 时 creditAmount 重复赋值、orderAmount/receivedAmount 取订单总额而非本次实收；还款不写 CreditLogs | 挂账流水金额字段语义可疑；流水页统计不到还款 | ✅ 阶段 5 已修：creditAmount 去重；receivedAmount 改本次实收 payAmount（空则回退订单总额） |
| K16 | ES `dish` 索引无任何写入/同步代码：es provider 只用 ElasticsearchOperations 只读查询，全仓无 save/index/Repository、无变更监听、无全量重建入口 | 索引没有数据来源，中间件环境搭好后收银端检索依然查不到菜品 | 中间件阶段定同步方案（推荐：菜品写后 MQ 异步双写 + 提供全量重建接口），并补索引 mapping 与 IK 词典初始化 |
| K17 | 五个 provider + mq + job 全仓无 @ControllerAdvice/@ExceptionHandler，pom 不依赖 web_common；BussinessException 抛出后走 Spring 默认错误体（无统一 code/信封） | HTTP Interface 客户端难以按结构解析业务错误；BFF 直连 provider 后两层错误格式不一致 | ✅ 阶段 5 已修：service_common 新增 `ProviderExceptionAdvice`（BussinessException/IllegalArgumentException 回 desc，兜底 Exception 记日志），provider 启动类 scanBasePackages 扫描生效 |
| K18 | MQ 三个发送方全部直投默认交换机的 account_queue；RabbitMQConfig 声明的 `sms_exchange`/`account_queue_key` 绑定从未被使用；SmsMessageListener catch 后 `e.printStackTrace()` 吞异常 | 交换机/绑定是死配置；短信发送失败只留标准错误流、无重试无告警 | 后置：统一改走命名交换机或删除死配置；catch 改 slf4j 占位符日志并按需配置死信队列 |
| K19 | job 归档 exportToHistory 物理删除业务订单（非软删、无分页全量搬运、单大事务）；套餐主明细 dishType=2 既不迁历史也不删除，主单删除后残留为孤儿；job 用 System.out.println | 历史订单只能查 t_his_*；t_order_detail 残留套餐头；订单量大时单次事务有风险 | 教学规模可接受、后置；整改时补套餐头归档/删除、分页批处理、日志改 slf4j |
| P2 | 运营人员/字典增改、列表多条件筛选、区域/桌台删除、分类改名增强 | 首期相关页面只读或不提供 | 首期后置 |

## 十二、提交与验收纪律

- 每阶段结束先给 `git diff`，确认后提交；commit message 中文 `type: 简述`；
- 每个修复点给可复制验证命令（AI 执行用 Git Bash 语法、人工执行用 PowerShell）与预期结果，跑通才算完成；
- 需求或端点范围变化时，**先更新本文档再开发**。

# 心飞餐饮 · 业务逻辑梳理

> 权威来源：`docs/requirements.md`（v2.4，28 契约 / 29 表 / 状态机 / 已知缺口）+ 后端契约实证。
> 本文是前端五端（运营中心 / 管家端 / 收银端 / C 端）开发的业务基准；端点细节以各端 provider 契约为准，本文只固化「业务上必须怎样」。

## 1. 一句话定位

多租户餐饮 SaaS：集团/单店在一套系统内完成 **点餐 → 收银 → 挂账 → 经营报表** 闭环，双租户（`shop_id` 集团 / `store_id` 门店）隔离。

## 2. 角色 × 端 × 任务地图

| 角色 | 端 | 核心任务 | 关键约束 |
|---|---|---|---|
| 平台运营人员 | 运营中心 | 开通/停用/编辑/删除/还原企业账号、重置密码；查字典、运营人员、服务监控 | 平台级数据，无租户；账号 status=-1 停用后不可登录 |
| 企业账号（集团） | 管家端 | 管品牌、全部门店、门店经理；看经营报表 | 一个企业账号 = 一个 `shop_id` |
| 门店经理 | 管家端 | 登录后选择/切换所辖门店；维护菜品、套餐、桌台区域、员工、沽清、挂账、营业设置 | 只能操作 `store_manager_id` 绑定且 status=1 的门店 |
| 门店收银员 | 收银端（触屏） | 开台、点餐、加菜、退菜、结账（现金/挂账）、换台、反结账、沽清维护 | 归属具体门店；触屏大按钮、强对比 |
| 顾客 | C 端 H5/小程序 | 扫码进店、浏览分类与菜品/套餐、购物车下单、支付 | 会话与 B 端隔离；移动优先、单手操作 |

> 管家端登录页是「企业账号 / 门店经理」**双身份**：同一登录表单按身份分发到 `POST /enterprise/account/login` 或 `POST /store/manager/login`。

## 3. 多租户与登录会话

- **双租户字段**：`shop_id`（集团）、`store_id`（门店）。
- 隔离策略：operator 无租户插件；shop 单租户 `shop_id`；store/order 双租户，**租户值缺失直接抛异常**。
- 会话：Sa-Token 签发，loginId 为业务主键，会话携带 `shopId / storeId / loginUserId / loginUserName / userType`；网关校验后经 `x-tenant-*` 头注入租户上下文。
- **登录参数不统一（现状）**：员工用 `loginName`（员工姓名）+ `password`；其余三套用 `phone` + `loginPass`。前端必须按契约映射，不可统一。
- 门店切换：`PUT /store/switch/{storeId}` **重签含 storeId 的令牌**，之后所有请求租户头随之变化。

## 4. 核心实体与状态机

| 对象 | 取值 | 业务含义 / 规则 |
|---|---|---|
| 企业账号 `status` | 0 试用 / 1 正式 / -1 停用 | 试用 7 天到期；到期重算；**正式禁改回试用**；停用后不可登录；**无独立启用端点**（启用=编辑提交 status） |
| 门店 `status` | 1 营业 / 0 停业 | 经理侧门店列表恒只含营业门店；集团管理分页含全部状态 |
| 员工 `status` | 1 在职 / 0 停用 | 停用/已删除员工禁止登录；启用走通用 `PUT /staff` 改回 1 |
| 菜品 / 套餐 `status` | 1 起售 / 0 停售 | 停售菜品**连带停售关联套餐**；删除菜品前先停售关联套餐再逻辑删除 |
| 订单 `payStatus` | 0 未付 / 1 已付 | 反结账把订单回退为 0 |
| 订单 `payType` | 0 免单 / 1 现金 / 2 微信 / 3 支付宝 / 4 银行卡 / 5 挂账 | 与 `OrderEntity.payType` 的 `@Schema` 一致 |
| 订单明细 `detailStatus` | 1 正常/加菜 / 2 退菜 | 退菜**不删原明细**，插一条 `detailStatus=2` 的标记明细 |
| 桌台 `status` | 0 空闲 / 1 开台 / 2 锁定 | 结账置空闲；**反结账不回退桌台**；锁定态目前仅旧收银端计数使用 |
| 沽清 `remainder` | 整数；**无记录 = 不限量，约定返回 -1** | 扣减封底 0（仅记录存在时扣）；退菜仅对限量菜（remainder>0）回加；表无逻辑删除，删除为物理删除 |

> 前端展示约定：已结=成功绿、挂账=警告橙、未结=危险红、沽清=中性灰、停售=危险红、启用=成功绿。**任何端不得混用**。

## 5. 核心业务流程

### 5.1 入驻开通
运营开通企业（8 位随机 `shopId` + 6 位随机密码 BCrypt，经 RabbitMQ `account_queue` 异步短信下发；试用 `status=0`、到期 `now+7 天`）→ 企业登录维护品牌 / 门店 / 门店经理 / 菜品分类 / 菜品套餐 / 桌台区域 / 员工。

### 5.2 堂食主线（收银端）
1. 员工登录（`POST /staff/login`）。
2. **开台**：单一端点 `POST /table/log/open`（body `TableLog`：tableId、userNumbers）。provider 内部：校验桌台空闲 → 校验人数 ≤ 座位数 → **先置桌台 status=1 → 后写 TableLog**。BFF 只调一次。
3. **点餐**：`POST /order/add`（body 不带 orderId = 下单；provider 生成流水号 + 逐菜沽清校验扣减，`@GlobalTransactional`）。
4. **加菜**：同端点、body 带已存在 orderId（追加明细）。
5. **退菜**：`POST /order/returnDish`（body `DetailDTO`：detailId + `List<String> remarks`，一次退一份）。插 `detailStatus=2` 标记明细、原明细数量 -1、仅回加限量菜沽清、**主单只重算 `totalAmount`（减一份单价）**。
6. **结账**：
   - 现金 `POST /order/pay`（body `OrderEntity`，金额字段前端透传）；
   - 挂账 `POST /order/pay/credit`（body `CreditPayRequest`：`order` + `credit`，`@GlobalTransactional`）。
   - 两者都会：更新订单 → **桌台置空闲** → 套餐按优惠率拆单品写 `OrderDetailMeal`。

### 5.3 挂账结账（分布式事务）
校验挂账主体（个人按姓名等值、公司查成员表按姓名）→ order 库更新订单、store 库累加 `Credit.creditAmount` 并写 `CreditLogs`（Seata `@GlobalTransactional`）→ 后续管家端 `POST /credit/repayment` 还款（**超额拒绝**；`creditAmount` 扣减、`totalRepaymentAmount` 累加、写 `t_credit_repayment`；**不写 CreditLogs**，挂账流水不能用 CreditLogs 统计还款）。

### 5.4 反结账
`GET /order/number/{orderNumber}` 按流水号查**已付**订单（含明细；非已付或查无返回 null，作为前端入口约束）→ 确认后 `POST /order/reverse`（body `ReverseOrder`：orderId + remark）：`payStatus` 置 0、写 `t_reverse_order` 留痕。
**实证**：reverse 不校验原单已付、不恢复桌台、不回冲沽清、不写负向明细 —— 反结后桌台仍显示空闲，订单以未付态重新结账即可。

### 5.5 顾客扫码点餐
进入门店页 → 浏览分类菜品（ES 按编码/名称检索）→ 购物车下单（复用 `/order/add`）→ 支付（dev 打桩）。

### 5.6 续费 / 转正
试用转正式、正式续费一律以 **now** 为基准加 `validityDay`，原剩余有效期作废；正式账号禁止改回试用。

### 5.7 逻辑删除与还原
`is_deleted` + `@TableLogic`；回收站查 `is_deleted=1` 手写 SQL。**还原只翻 `is_deleted`、不改 `status`** —— 停用账号还原后仍不可登录，须再编辑启用。

## 6. 关键业务规则（前端必须遵守）

1. **金额**：库与接口均为 Integer **分**；前端负责元/分换算，展示统一 `¥` + 两位小数 + 等宽数字。
2. **结账金额透传**：`pay` / `pay/credit` 的 payType、payAmount、smallAmount（抹零）、personNumbers 全部由请求体透传，**provider 不重算、不勾稽**；前端必须自行算准。
3. **密码**：全部 BCrypt。企业开通 6 位随机数字；员工 `addStaff` 生成 8 位随机数字但**当前不发短信**（`StaffServiceImpl#sendMessage` 为 private 预留，无调用方）。
4. **分页契约**：统一 `PageResult<T>`（records / total / page / size / pages）；ES 用 `SearchResult<T>`。
5. **主键**：Staff 为 `ASSIGN_UUID`，其余多为雪花 `ASSIGN_ID`（字符串）。
6. **打包规则**：菜品与口味、套餐与套餐菜均「先更新主表，再先删后建子表」。
7. **订单明细关联**：按 `order_id` 关联主单；明细自身 `orderNumber` 是派生流水号，**不能用于关联**。
8. **并发**：全工程无 `@Version` 乐观锁，开台/换台/清台只按 status 现值判断，**前端不得把桌台状态当本地缓存**。

## 7. 各端页面 → 业务任务 → 契约

### 运营中心（operator）
| 页面 | 业务任务 | 关键契约 |
|---|---|---|
| 登录 | 运营人员登录 | `POST /operator/user/login` |
| 企业账号 | 分页 / 开通 / 编辑 / 停用 / 删除 / 还原 / 重置密码 | `/enterprise/account/*` |
| 回收站 | 查已删除账号 / 还原 | `/enterprise/account/removed/page/...`、`/recovery/{id}` |
| 字典管理 | 按类查字典项 | `GET /sysDict/list/{category}` |
| 运营人员 | 分页（只读，增改 P2） | `GET /operator/user/page/...` |
| 服务监控 | 只读监控面板 | 无契约（示意） |

### 管家端（shop + store + order）
| 页面 | 业务任务 | 关键契约 |
|---|---|---|
| 登录（双身份） | 企业 / 门店经理登录 | `/enterprise/account/login`、`/store/manager/login` |
| 门店切换 | 切当前操作门店 | `GET /store/manager/list`、`PUT /store/switch/{storeId}` |
| 工作台 | 今日 KPI / 最新订单 / 桌台盘 / 沽清余量 | `/order/report/currentAmount` |
| 品牌 / 门店 / 门店经理 | 集团基础数据 | `/brand/*`、`/store/*`、`/store/manager/*` |
| 菜品分类 / 菜品 / 套餐 | 菜单维护 | `/dish/category/*`、`/dish/*`、`/setMeal/*` |
| 桌台区域 / 桌台 | 区域与桌台维护 | `/table/area/*`、`/table/*` |
| 员工管理 | 分页 / 新增 / 编辑 / 停用 | `/staff/*` |
| 挂账管理 | 单位与用餐人 / 流水 / 还款 | `/credit/*`、`/credit/log/*`、`/credit/repayment` |
| 沽清管理 | 分页 / 余量 / 增减 / 删除 | `/sellCalculation/*` |
| 营业设置 | 备注整批更新 | `/remark/list`、`/remark/update` |
| 经营报表 | 订单 / 菜品 / 支付多维 | `/order/report/*`、`/order/detail/report/*`、`/report/*` |

### 收银端（store + order + es）
| 页面 | 业务任务 | 关键契约 |
|---|---|---|
| 登录 | 员工登录 | `POST /staff/login` |
| 桌台总览 | 区域 + 桌台状态盘 | `/table/area/list`、`/table/area/{areaId}/page/...`、`/table/count/{areaId}/{status}` |
| 点餐页 | 分类检索 / 加菜 / 退菜 / 换台 / 结账 / 挂账 / 反结账 | `/search/dish/*`、`/dish/enable/list/{categoryId}`、`/order/add`、`/order/returnDish`、`/order/pay`、`/order/pay/credit`、`/order/changeTable`、`/order/number/{no}`、`/order/reverse` |

### C 端（microapp）
| 页面 | 业务任务 | 关键契约 |
|---|---|---|
| 门店菜单 | 分类 + 菜品/套餐详情 | `/dish/category/type/{type}`、`/dish/enable/list/{categoryId}` |
| 购物车 / 下单确认 | 购物车提交 | `/order/add` |
| 支付结果 | 结果展示 | dev 打桩 |

## 8. 数据域与表（29 表 + 1 视图）

| 域 | 表 |
|---|---|
| operator（3） | `t_enterprise_account`、`t_operator_user`、`t_sys_dict` |
| shop（3） | `t_brand`、`t_store`、`t_store_manager` |
| store（15） | `t_category`、`t_dish`、`t_dish_flavor`、`t_setmeal`、`t_setmeal_dish`、`t_table`、`t_table_area`、`t_table_log`、`t_staff`（含 status）、`t_credit`、`t_credit_company_user`、`t_credit_logs`、`t_credit_repayment`、`t_remark`、`t_sell_calculation` |
| order（6 + 1 视图） | `t_order`、`t_order_detail`、`t_order_detail_meal`、`t_reverse_order`、`t_report_pay`、`t_report_dish`；视图 `v_order_detail_all` |
| job 归档（2） | `t_his_order`、`t_his_order_detail` |
| es | 索引 `dish`（**当前无任何写入/同步代码，K16**） |

## 9. 已知缺口对前端的硬约束

| 缺口 | 对前端的影响 |
|---|---|
| K7 反结账不校验已付 | 入口必须用 `GET /order/number/{orderNumber}` 限已付，**不得直接暴露反结账按钮** |
| K10 退菜只冲 totalAmount | 已付后退菜会产生实收/应收差额，UI 必须提示「差额需人工处理」 |
| K11 无乐观锁 | 桌台/订单状态每次操作后**重新拉取**，不得本地假设 |
| K16 ES 无写入 | 收银端条码/名称检索在索引无数据时恒空，UI 需空状态提示而非报错 |
| K19 job 物理删单 | 历史订单只在 `t_his_*`；报表区间跨归档时数据可能不连续，UI 需标注 |
| 员工密码不发短信 | 员工管理新增后**必须展示初始密码**或提供重置入口，否则员工无法登录 |
| 逻辑删除还原不改 status | 回收站还原后账号仍不可登录，UI 需提示「还原后仍需启用」 |

## 10. 术语表

开台 · 换台 · 退菜 · 反结账 · 沽清（限量，-1 不限）· 挂账（个人/公司）· 还款 · 抹零（smallAmount）· 集团（shop_id）· 门店（store_id）· 流水号（orderNumber）· 双人套餐拆单（OrderDetailMeal）。

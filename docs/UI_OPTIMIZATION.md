# 心飞餐饮 · 页面 UI 设计优化方案

> 基线：`DESIGN.md`（canon 品类标准：干净、工整、任务优先）+ `docs/BUSINESS.md`（业务逻辑）。
> 对象：`xfcy-ui/next/` 五端 —— manager（管家端）、operator（运营中心）、cashier（收银端）、microapp（C 端 Taro）、playground。
> 目标：**让每一页的信息架构、状态语义、操作效率都达到「满分工整」**，并消除跨端不一致。

## 1. 设计基线（不可违背）

- **任务优先**：一线操作最少点击、最大命中区；B 端一屏看全关键数据。
- **状态可见**：支付状态、桌台状态、沽清余量、上下架一眼可读，且**附文字不只靠颜色**。
- **数据可信**：金额 `¥` + 两位小数 + 等宽数字；**演示数据必须显式标注**，不得与真实数据混排。
- **克制**：强调色只用于动作与关键状态；不引入 token 外色值。
- **无障碍**：WCAG 2.1 AA；收银端命中区 ≥ 48px；C 端移动优先、单手可达。

## 2. 现状盘点

### 2.1 全局

| 项 | 现状 | 问题 |
|---|---|---|
| 设计 token | `packages/ui/tokens.css`，各端引用 | ✅ 已统一 |
| 组件库 | `@xfcy/ui` 12 组件（Button/Input/Select/Table/Modal/Panel/Kpi/Tag/Tabs/Field/Pagination/Empty） | ✅ 已统一，Vitest 61 用例 |
| 应用布局 | manager / operator 各一份 `AppLayout.vue` + 一份 `layout.css` | ❌ **两套近乎重复实现**（前缀 `xfm-` / `xfo-`），维护双倍 |
| 收银端布局 | `PosLayout.vue` + `layout.css`（`xfc-`） | ⚠️ 与 B 端未复用顶部用户菜单/时间逻辑 |
| 页面骨架 | 各页自己拼 `toolbar / kpis / cols / side` | ❌ 无统一页面头（标题+说明+主操作）与筛选栏规范 |
| 轻提示 | Dishes/Credit/Soldout/Reports 各写 `noticeOpen + XfModal` | ❌ **4 处重复且语义错误**（用模态承载 toast，遮罩打断操作） |
| 空状态 | `XfEmpty` 仅 Dishes/Tables/Placeholder 使用 | ❌ 列表/面板空态不统一 |
| 用户菜单 | 三端各写 avatar + 外部点击关闭 | ❌ 重复逻辑 |
| user store | manager/operator/cashier 各一份 `useUserStore` | ❌ 高度重复 |
| 响应式 | 三端各有 `@media` 断点，阈值不一（560/900/1100/1180） | ⚠️ 断点未按 DESIGN.md §7 统一 |
| 路由守卫 | 三端各一份 `beforeEach` | ⚠️ 重复但简单，可后置 |
| 登录页 | 三端各一份 `LoginView.vue` | ⚠️ 视觉一致但代码重复（含双身份分发逻辑） |

### 2.2 管家端（manager）

| 页面 | 现状 | 主要问题 | 优先级 |
|---|---|---|---|
| 工作台 Dashboard | mock 演示数据 + real 仅拉 `currentAmount` | ❌ **数据可信度**：翻台率恒「—」；最新订单/桌台盘/沽清余量在 real 模式仍是演示数据，会误导店长 | **P0** |
| 菜品管理 Dishes | 4 Tab（菜品/套餐/分类/口味），功能完整 | ⚠️ 提示用模态；口味库为占位；批量操作缺反馈 | P1 |
| 套餐管理 combos | `PlaceholderView` 占位 | ❌ 无独立页（套餐已在 Dishes 的「套餐」Tab 覆盖）→ 应**移除该导航项**或改为锚点跳转 | P1 |
| 桌台管理 Tables | 区域 + 桌台网格 | ⚠️ 无空状态统一；状态色语义需复核 | P1 |
| 员工管理 Staff | 列表/新增/编辑/停用 | ⚠️ 新增后**初始密码无展示/重置入口**（后端不发短信，见 BUSINESS §9）→ 员工无法登录 | **P0** |
| 挂账管理 Credit | 列表/新增/编辑/还款/停用 | ⚠️ 无「挂账流水」入口（`/credit/log/*` 未接入）；还款超额无前置校验提示 | P1 |
| 沽清管理 Soldout | 列表/新增/加减/删除 | ⚠️ 与 Dishes 的「余量」列口径需一致 | P2 |
| 经营报表 Reports | 纯 CSS 柱状图，无导出 | ⚠️ 无图表库（DESIGN 提到 ECharts 6）；无导出；口径说明用模态 | P1 |

### 2.3 运营中心（operator）

| 页面 | 现状 | 主要问题 | 优先级 |
|---|---|---|---|
| 企业账号 | 完整（开通/编辑/停用/删除/还原/重置密码） | ⚠️ 筛选/分页为**前端本地过滤**，数据量大时不可扩展 | P1 |
| 回收站 | 列表/还原 | ⚠️ 未提示「还原后仍需启用」（BUSINESS §5.7） | **P0** |
| 字典管理 | 只读列表 | ✅ 符合 P2 后置定位 | P2 |
| 运营人员 | 只读 | ✅ 符合 P2 后置定位 | P2 |
| 服务监控 | 只读面板 | ⚠️ 无真实数据源，需标注示意 | P2 |

### 2.4 收银端（cashier）

| 页面 | 现状 | 主要问题 | 优先级 |
|---|---|---|---|
| 桌台总览 | 区域 + 桌台卡 | ⚠️ 状态色/计数口径需与 `table/count` 对齐 | P1 |
| 点餐页 | 分类锚点 + 菜品网格 + 订单面板 + 结账弹窗 | ✅ 结构符合 DESIGN §5.2；⚠️ 换台/退菜/反结账入口未确认完整 | P1 |
| 登录 | 员工登录 | ✅ | — |

### 2.5 C 端（microapp）

| 页面 | 现状 | 主要问题 | 优先级 |
|---|---|---|---|
| 门店菜单 | 门店头 + 搜索 + 分类锚点 + 2 列大图 | ✅ 结构符合 DESIGN §5.3 | — |
| 购物车 / 下单确认 / 支付结果 | 已有 | ⚠️ 支付为 dev 打桩，需显著标注 | P1 |

## 3. 全局优化设计

### G1 统一页面骨架（P0）
建立三套可复用骨架类（放各端 `layout.css` 或抽到 `@xfcy/ui`）：

```
.xf-page            { display:flex; flex-direction:column; gap:16px; }
.xf-page__head      { display:flex; align-items:flex-end; gap:12px; }
.xf-page__title     { font-size:20/600; }
.xf-page__desc      { font-size:13; color:var(--c-text-2); }
.xf-page__head .spacer { flex:1 }
.xf-toolbar         { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.xf-toolbar__grow   { flex:1 }
```
- 每页结构固定：**页面头（标题 + 说明 + 主操作）→ 工具栏（筛选左 / 操作右）→ 内容**。
- 表格页统一：工具栏在上、表格在中、分页在下右。

### G2 统一轻提示 Toast（P0）
新增 `XfToast`（`packages/ui`）：
- `useToast()` → `toast.success(message)` / `toast.error(message)`；自动消失 2.5s，右下角堆叠，`role=status`/`alert`。
- 替换 Dishes/Credit/Soldout/Reports 的 `noticeOpen + XfModal`。
- 删除确认仍用 `XfModal`（模态语义正确，保留）。

### G3 统一空状态（P0）
- 列表/面板空态一律用 `XfEmpty`（图标位 + 主文案 + 副文案 + 一个主操作）。
- 区分三种：**无数据**（「暂无菜品」+ 新增按钮）、**无搜索结果**（「未找到匹配『xx』」+ 清除筛选）、**加载失败**（错误文案 + 重试）。

### G4 数据可信度（P0）
- **演示数据必须带标识**：mock 模式页面顶部显示 `演示数据` 标签（`XfTag tone=warning`）。
- **real 模式不得留演示兜底**：接口失败时显示错误 + 重试，不得静默渲染假数据。
- Dashboard real 模式补齐：桌台盘（`/table/count/*`）、沽清余量（`/sellCalculation/page`）、最新订单（`/order/...`）；确实无契约的指标（翻台率）显示「暂无口径」而非「—」。

### G5 应用布局复用（P1）
- 抽 `packages/ui` 的 `XfAppShell`（侧栏 + 顶栏 + 用户菜单 + 门店切换插槽），manager/operator 共用；收银端保留 `PosLayout` 但复用顶栏用户菜单。
- 统一响应式断点：`≥1280` 全量 / `1024–1280` 侧栏收起 / `<1024` 侧栏转顶部（DESIGN §7）。
- 清理 `manager/layout.css` 中重复的 `.xfm-form`（440 行 vs 555 行，值还不一致）。

### G6 状态语义与枚举文案（P0）
- 建立**唯一枚举映射表**（放 `@xfcy/api` 或 `@xfcy/ui`）：
  - 支付：未结/已结/挂账 → danger/success/warning
  - 桌台：空闲/开台/锁定 → neutral/primary/info
  - 菜品/套餐：起售/停售 → success/danger
  - 账号：试用/正式/停用 → warning/success/neutral
  - 员工：在职/停用 → success/neutral
- 页面不得自行硬编码颜色判断，统一走 `toneOf(type, value)`。

### G7 业务缺口兜底（P0）
- **员工初始密码**：新增员工成功后必须展示初始密码（后端返回或前端提示「密码已生成，请告知员工」）；提供重置入口。
- **回收站还原**：还原成功提示「已还原，如账号为停用状态需再次编辑启用」。
- **反结账入口**：仅对已付订单显示，附原因输入与「不恢复桌台/不回冲沽清」提示（BUSINESS §5.4）。
- **退菜差额**：已付后退菜提示「差额需人工处理」（BUSINESS §5.2）。

## 4. 逐页优化设计（摘要）

### 管家端
- **Dashboard**：顶部 `演示数据` 标识；KPI 4 项（营业额/单量/客单价/翻台率）；主区最新订单表 + 右栏桌台盘/沽清余量；real 模式全部接契约，无契约指标显式「暂无口径」。
- **Dishes**：保留 4 Tab；移除导航中的「套餐管理」独立项（改为 Dishes 内 Tab）；口味库占位标注「P2」；提示改 Toast。
- **Staff**：新增/编辑后展示初始密码；状态切换二次确认；列表脱敏（已做）。
- **Credit**：补「挂账流水」子 Tab（`/credit/log/page`），还款前校验额度并提示超额；停用需二次确认。
- **Soldout**：与 Dishes 余量口径统一（remainder=-1 → 「不限」）；加减按钮禁用态明确。
- **Reports**：引入轻量图表（CSS 柱状可保留，补悬浮数值与空态）；补「导出」（`/credit/export` 或报表导出）；口径说明改 Toast/抽屉。

### 运营中心
- **EnterpriseAccounts**：本地过滤改服务端分页（`enterpriseName` 模糊）；状态筛选与套餐筛选合并到工具栏；开通成功后展示随机密码。
- **RecycleBin**：还原提示「仍需启用」；空态。
- **Monitor**：标注「示意」。

### 收银端
- **Tables**：状态色走统一枚举；空区/空桌提示。
- **Order**：补齐换台/退菜/反结账入口；挂账结账校验主体；触屏命中区复核 ≥48px。

### C 端
- **Menu**：搜索无结果空态；支付打桩标注。
- **Cart/Confirm/Result**：金额等宽数字；安全区避让复核。

## 5. 落地批次

| 批次 | 内容 | 影响面 |
|---|---|---|
| **B1（全局基础）** ✅ | G1 骨架类、G2 Toast、G3 空状态、G6 枚举 tone | 全部页面 |
| **B2（数据可信）** ✅ | G4 演示标识 + real 兜底治理、Dashboard 补齐 | manager / operator |
| **B3（业务兜底）** ✅ | G7 员工密码/回收站提示、反结账入口、退菜差额 | manager/operator/cashier |
| **B4（复用重构）** ✅ | G5 AppShell 抽公共 + 断点统一 + 清理重复 CSS | 三端 |
| **B5（页面精修）** ✅ | 逐页按 §4 精修（报表图表/导出、挂账流水、收银入口补全） | 逐页 |

> B1/B2 已落地：`XfDemoBadge` / `XfErrorState` / `XfToastHost` / `tone.ts` / `layout.css`；Dashboard real 模式已接 `currentAmount` + `table.countByStatus` + `soldout.page` + `order.page`，无契约的翻台率显式「暂无口径」；manager 5 页与 operator 2 页具备错误态 + 重试。配套后端：新增 `GET /order/page/{n}/{s}` 只读契约、`POST /staff/add` 返回初始密码。
>
> B3 已落地：收银端退菜在 real 模式调 `order.returnDish(detailId)`（detailId 由当前订单明细按 `dishId` + `detailStatus=1` 反查），已付订单退菜强提示「差额需人工处理」；反结账入口改为流水号反查（`GET /order/number/{orderNumber}` 只返回已付单），确认框明示「不恢复桌台、不回冲沽清、不自动退款」，再调 `POST /order/reverse`。订单面板本地 notice 统一改为全局 Toast。
>
> B4 已落地：`@xfcy/ui` 新增 `XfAppShell`（侧栏 + 顶栏 + 导航 + 用户菜单 + 内容插槽）与 `XfUserMenu`（B 端默认 / `touch` 触屏态）。管家端与运营中心共用同一套布局与导航断点；收银端 `PosLayout` 保留三栏结构但复用 `XfUserMenu`。三端断点统一为 1280 / 1024 / 768 / 560（CSS 变量不能用于 `@media`，故在各样式文件内保持一致）；删除 manager/operator 重复的框架与用户菜单样式，manager 重复的 `.xfm-form` 合并为单一定义。
>
> B5 已落地：
> - **挂账**：`CreditView` 拆为「挂账主体 / 挂账流水」双 Tab；流水页支持主体 + 起止时间筛选、分页表格与「导出流水」（real 下载 BFF xlsx，mock 本地生成 CSV），还款前展示并校验「剩余欠款额度」超额前置提示，停用/启用改为二次确认。`@xfcy/api` 新增 `credit.logPage` / `credit.exportLogs` 与 `CreditLog` 类型，mock 同步补 `/credit/log/page` 路由与流水演示数据。
> - **报表**：分时柱状图改为悬浮/聚焦显示数值，无数据时整图空态；新增「导出 CSV」（KPI + 分时 + 收款方式，mock/real 通用）。
> - **收银**：挂账结账需选择挂账主体（复用 enterprise-web `/credit/page`）并填写挂账人姓名，前端与后端同口径校验停用/超额；`TablesView` 补空态。
> - **运营**：字典/运营人员页补错误态 + 重试，停用失败本地回滚；回收站保留「仍需启用」提示；管家端导航移除已被菜品 Tab 覆盖的「套餐管理」项。
> - **后端**：`OrderServiceImpl.payByCredit` 补齐挂账主体存在性/启用状态/金额校验（`creditAmount` 为剩余欠款额度）。

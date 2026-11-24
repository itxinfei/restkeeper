> 本目录是 [xfcy](https://gitee.com/itxinfei/xfcy) 主仓库的前端部分（原独立仓库 xfcy-ui 已并入主仓库）。原始课程代码著作权归原课程机构所有，改造与新增部分以 Apache-2.0 授权，归属说明见 [NOTICE](NOTICE)。

<h1 align="center">心飞餐饮前端 · xfcy-ui</h1>

<h4 align="center">现行前端在 next/ —— pnpm monorepo：管家端 / 运营中心 / 触屏收银 / C 端点餐，外加组件库与接口层</h4>

<p align="center">
  <a href="https://gitee.com/itxinfei/xfcy/stargazers"><img src="https://gitee.com/itxinfei/xfcy/badge/star.svg?theme=gvp" alt="star"></a>
  <a href="https://gitee.com/itxinfei/xfcy/members"><img src="https://gitee.com/itxinfei/xfcy/badge/fork.svg?theme=gvp" alt="fork"></a>
  <img src="https://img.shields.io/badge/status-%E4%BA%8C%E6%AC%A1%E5%BC%80%E5%8F%91%E4%B8%AD-brightgreen.svg" alt="status">
  <img src="https://img.shields.io/badge/type-%E8%AF%BE%E7%A8%8B%E4%BA%8C%E5%BC%80-lightgrey.svg" alt="type">
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="license">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3-42b883.svg?logo=vue.js&logoColor=white" alt="Vue">
  <img src="https://img.shields.io/badge/TypeScript-5-3178C6.svg?logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Vite-6-646CFF.svg?logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/Taro-4-07C160.svg" alt="Taro">
  <img src="https://img.shields.io/badge/pnpm-monorepo-F6922B.svg?logo=pnpm&logoColor=white" alt="pnpm">
</p>

<p align="center">
  <a href="#一这是什么">这是什么</a> ·
  <a href="#二现行前端">现行前端</a> ·
  <a href="#三旧工程">旧工程</a> ·
  <a href="#四环境要求">环境</a> ·
  <a href="#五快速开始">快速开始</a> ·
  <a href="#六来源与版权">版权</a> ·
  <a href="https://gitee.com/itxinfei/xfcy">后端仓库</a>
</p>

---

## 一、这是什么

「心飞餐饮」的前端代码，现已并入后端主仓库 [xfcy](https://gitee.com/itxinfei/xfcy)——前后端同仓演进，改一次字段不用跨库对。目录下有两代工程：

- **`next/`** —— 现行前端。pnpm monorepo，五个应用 + 三个共享包，本次二次开发的成果，按主仓库 [DESIGN.md](../DESIGN.md) 的设计规范落地。
- **`xfcy-guanjia` / `xfcy-yunyin` / `xfcy-weixin` / `xfcy-diancan`** —— 课程原样保留的学习副本，仅作视觉与交互参考，不参与现行构建。

## 二、现行前端

`next/` 采用 pnpm workspace（`packages/*` + `apps/*`）：

| 类型 | 目录 | 说明 |
|---|---|---|
| 应用 | `apps/manager` | 商家管家端（Vue 3 + Vite） |
| 应用 | `apps/operator` | 平台运营中心（Vue 3 + Vite） |
| 应用 | `apps/cashier` | 触屏收银端（Vue 3 + Vite，触屏交互） |
| 应用 | `apps/microapp` | C 端点餐（Taro 4 → 微信小程序 / H5） |
| 应用 | `apps/playground` | 组件预览页（@xfcy/ui 调试用） |
| 包 | `packages/ui` | @xfcy/ui 组件库（tokens、状态语义、toast 等） |
| 包 | `packages/api` | @xfcy/api 接口层（传输抽象：B 端 axios / C 端 Taro.request，可注入） |
| 包 | `packages/auth` | @xfcy/auth 登录与会话 |

设计规范在主仓库 [DESIGN.md](../DESIGN.md)，高保真静态原型在 [docs/ui/](../docs/ui/)。

## 三、旧工程

| 目录 | 端 | 技术栈 |
|---|---|---|
| `xfcy-guanjia/` | 门店管理端 | Vue 2 + vue-cli |
| `xfcy-yunyin/` | 总部运营端 | Vue 2 + TypeScript |
| `xfcy-diancan/` | 顾客点餐（平板/手机） | Flutter |
| `xfcy-weixin/` | 微信小程序 | Taro 2 |

四个工程各自独立、年份不一，构建工具、包管理器、目录约定都不统一——它们记录的是课程原貌，不是现状。要在此基础上做二次开发，直接用 `next/`。

## 四、环境要求

- `next/`：Node ≥ 18 + pnpm
- 旧工程：Node 16+；Flutter SDK 3.x（diancan）；微信开发者工具（预览 weixin 产出）

## 五、快速开始

```bash
cd xfcy-ui/next
pnpm install
pnpm dev                              # 同时启动全部应用
pnpm --filter @xfcy/manager dev       # 单独起管家端
pnpm --filter @xfcy/microapp dev:h5   # C 端 H5；dev:weapp 出微信小程序产物
```

旧工程进各自目录 `npm install && npm run dev` 即可（weixin 为 `npm run dev:weapp`，diancan 为 `flutter pub get && flutter run`）——注意它们是学习副本，仅用于对照旧貌。

## 六、来源与版权

本项目在传智播客研究院「餐掌柜」公开课程项目基础上二次开发，原始代码著作权归原课程机构所有。

作者改造与新增的部分以 **Apache-2.0** 授权，全文见 [LICENSE](LICENSE)。该授权不覆盖原始课程代码，也不构成对原始代码的再许可；完整归属说明见 [NOTICE](NOTICE)。

关于子目录里的 LICENSE：`xfcy-guanjia/` 和 `xfcy-yunyin/` 下各有一个 MIT LICENSE（`Copyright (c) 2018 Chong Guo`），那是随前端脚手架模板附带的文件，授权来自模板原作者，不是本目录给出的。本目录不撤销它，也不代表它扩展到课程业务代码——课程代码的权利状态和这几个脚手架 LICENSE 并不是一回事。真要商用，得分别找前端模板作者和课程机构确认。

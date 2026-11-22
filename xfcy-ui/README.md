> 本仓库在「餐掌柜」公开课程项目基础上二次开发。原始代码著作权归原课程机构所有，本仓库作者只对改造与新增部分以 Apache-2.0 授权，原始代码不在授权范围内。归属说明见 [NOTICE](NOTICE)。

<h1 align="center">心飞餐饮前端 · xfcy-ui</h1>

<h4 align="center">餐饮 SaaS 的四端前端 —— 管理端、运营端、点餐平板、微信小程序，收在一个仓库里</h4>

<p align="center">
  <a href="https://gitee.com/itxinfei/xfcy-ui/stargazers"><img src="https://gitee.com/itxinfei/xfcy-ui/badge/star.svg?theme=gvp" alt="star"></a>
  <a href="https://gitee.com/itxinfei/xfcy-ui/members"><img src="https://gitee.com/itxinfei/xfcy-ui/badge/fork.svg?theme=gvp" alt="fork"></a>
  <img src="https://img.shields.io/badge/status-%E5%AD%A6%E4%B9%A0%E5%BD%92%E6%A1%A3-lightgrey.svg" alt="status">
  <img src="https://img.shields.io/badge/type-%E8%AF%BE%E7%A8%8B%E9%A1%B9%E7%9B%AE-lightgrey.svg" alt="type">
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="license">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Vue-2%20%2F%203-42b883.svg?logo=vue.js&logoColor=white" alt="vue">
  <img src="https://img.shields.io/badge/TypeScript-4-3178C6.svg?logo=typescript&logoColor=white" alt="ts">
  <img src="https://img.shields.io/badge/Flutter-3-02569B.svg?logo=flutter&logoColor=white" alt="flutter">
  <img src="https://img.shields.io/badge/Taro-3-07C160.svg" alt="taro">
  <img src="https://img.shields.io/badge/WeChat-小程序-07C160.svg?logo=wechat&logoColor=white" alt="wechat">
</p>

<p align="center">
  <a href="#一这是什么">这是什么</a> ·
  <a href="#二四个端">四个端</a> ·
  <a href="#三环境要求">环境</a> ·
  <a href="#四快速开始">快速开始</a> ·
  <a href="#六来源与版权">版权</a> ·
  <a href="https://gitee.com/itxinfei/xfcy">后端仓库</a>
</p>

---

## 一、这是什么

「心飞餐饮」餐饮 SaaS 的四端前端，一个仓库装四个独立工程。后端在 [xfcy](https://gitee.com/itxinfei/xfcy)。

为什么装在一起：这四个端的接口是同一套，改一次后端字段经常要四个端一起对，分仓反而天天跨库找代码。

## 二、四个端

| 目录 | 端 | 技术栈 | 跑法 |
|---|---|---|---|
| `xfcy-guanjia/` | 门店管理端 | Vue | `npm install && npm run dev` |
| `xfcy-yunyin/` | 总部运营端 | Vue + TypeScript | `npm install && npm run dev` |
| `xfcy-diancan/` | 顾客点餐（平板/手机） | Flutter | `flutter pub get && flutter run` |
| `xfcy-weixin/` | 微信小程序 | Taro | `npm install && npm run dev:weapp` |

四个端的分工基本是餐饮 SaaS 的标准切法：

- **管理端**给单店店长用，管本店的菜品、订单、员工、桌台
- **运营端**给总部用，管多店、营销、结算这些跨店的事
- **点餐端**装在门店平板上，顾客自助下单
- **小程序**走微信生态，扫码点餐、查看订单

目录名按业务端命名：`xfcy-guanjia` 门店管理、`xfcy-yunyin` 总部运营、`xfcy-diancan` 平板点餐、`xfcy-weixin` 微信小程序。四个工程各自独立，构建与依赖互不相干，要单独拎一个出来用也不会互相拖累。

## 三、环境要求

- Node 16+（两个 Vue 工程和小程序）
- Flutter SDK 3.x（点餐端）
- 微信开发者工具（预览小程序时需要）

小程序端跑起来后要把产出的 `dist` 目录用开发者工具打开，不能直接当 H5 预览——Taro 的微信端有专门的编译目标。

## 四、快速开始

1. 进对应端的目录
2. 装依赖
3. 起本地服务

```bash
# 门店管理端
cd xfcy-guanjia
npm install
npm run dev
```

```bash
# 微信小程序
cd xfcy-weixin
npm install
npm run dev:weapp
```

## 五、改造提示

代码是课程原样保留的学习副本，没做重构。要拿来改造成自己的项目，先看清楚每个端的技术栈版本差异——这是四个不同年份的工程，构建工具、包管理器、目录约定都不统一。硬要拉齐，工作量比重新写一版小不了多少。

## 六、来源与版权

本项目在传智播客研究院「餐掌柜」公开课程项目基础上二次开发，原始代码著作权归原课程机构所有。

作者改造与新增的部分以 **Apache-2.0** 授权，全文见 [LICENSE](LICENSE)。该授权不覆盖原始课程代码，也不构成对原始代码的再许可；完整归属说明见 [NOTICE](NOTICE)。

关于子目录里的 LICENSE：`xfcy-guanjia/` 和 `xfcy-yunyin/` 下各有一个 MIT LICENSE（`Copyright (c) 2018 Chong Guo`），那是随前端脚手架模板附带的文件，授权来自模板原作者，不是本仓库给出的。本仓库不撤销它，也不代表它扩展到课程业务代码——课程代码的权利状态和这几个脚手架 LICENSE 并不是一回事。真要商用，得分别找前端模板作者和课程机构确认。

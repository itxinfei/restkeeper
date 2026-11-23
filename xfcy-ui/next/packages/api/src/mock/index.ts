/**
 * mock 数据源与传输。
 *
 * 切换方式：配置 mock === true（默认，Vite 端取 VITE_USE_MOCK，默认 '1'）。
 * 命中 mock 时客户端使用本地传输，所有请求在本地应答，不发出真实 HTTP。
 * B 端沿用 axios adapter；C 端（Taro）使用传输无关的 createMockTransport。
 */

import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  CreditPayload,
  CreditRepaymentPayload,
  CurrentAmountCollectDTO,
  Dish,
  EnterpriseAccount,
  PageResult,
  ProviderResult,
  SellCalculation,
  Staff,
  StoreDTO,
  SysDictionary,
  Table,
  TableArea,
  TableLog,
} from '../types'
import { getApiConfig } from '../config'
import type { HttpTransport } from '../transport'
import {
  mockCategories,
  mockCredits,
  mockCreditLogs,
  mockDictItems,
  mockDishes,
  mockEnterpriseAccounts,
  mockHourCollect,
  mockOperatorUsers,
  mockOrders,
  mockPayTypeCollect,
  mockPrivilege,
  mockRemovedEnterpriseAccounts,
  mockSellCalculations,
  mockSetMeals,
  mockStaffs,
  mockTableAreas,
  mockTables,
} from './data'
import type { MockOperatorUser } from './data'
import { microappRoutes } from './microapp'
import type { MockCtx, MockRoute } from './types'

/** 是否启用 mock（默认开启，便于前端独立演示） */
export function isMockMode(): boolean {
  return getApiConfig().mock
}

/** mock 未命中路由时抛出，便于定位缺失契约 */
export class MockNotFoundError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'MockNotFoundError'
  }
}

const MOCK_TOKEN = 'xfcy-mock-token'

function ok<T>(data: T, token?: string): ProviderResult<T> {
  return { status: 200, desc: 'ok', data, token }
}

function paginate<T>(items: T[], page: number, size: number): PageResult<T> {
  const safePage = Number.isFinite(page) && page > 0 ? Math.floor(page) : 1
  const safeSize = Number.isFinite(size) && size > 0 ? Math.floor(size) : 10
  const start = (safePage - 1) * safeSize
  return {
    records: items.slice(start, start + safeSize),
    total: items.length,
    page: safePage,
    size: safeSize,
    pages: Math.max(1, Math.ceil(items.length / safeSize)),
  }
}

/** 兼容 axios（字符串）与通用传输（对象）两种 body 形态 */
function parseBody(data: unknown): any {
  if (typeof data === 'string') {
    if (data.length === 0) return undefined
    try {
      return JSON.parse(data)
    } catch {
      return undefined
    }
  }
  return data
}

const bEndRoutes: MockRoute[] = [
  /* ---------------- auth ---------------- */
  {
    method: 'POST',
    pattern: /^\/enterprise\/account\/login$/,
    handler: ({ params }) => {
      if (!params.shopId || !params.phone || !params.loginPass) {
        return { status: 500, desc: '登录参数不完整' }
      }
      return ok(
        {
          enterpriseId: 'ent-demo',
          enterpriseName: '心飞餐饮（示意）',
          shopId: String(params.shopId),
          phone: String(params.phone),
          status: 1,
        },
        MOCK_TOKEN,
      )
    },
  },
  {
    method: 'POST',
    pattern: /^\/store\/manager\/login$/,
    handler: ({ params }) => {
      if (!params.shopId || !params.phone || !params.loginPass) {
        return { status: 500, desc: '登录参数不完整' }
      }
      return ok(
        {
          storeManagerId: 'mgr-demo',
          storeManagerName: '李国栋',
          storeManagerPhone: String(params.phone),
          status: 1,
        },
        MOCK_TOKEN,
      )
    },
  },
  {
    method: 'POST',
    pattern: /^\/operator\/user\/login$/,
    handler: ({ params }) => {
      if (!params.loginName || !params.loginPass) {
        return { status: 500, desc: '登录参数不完整' }
      }
      return ok({ uid: 'op-demo', loginname: String(params.loginName) }, MOCK_TOKEN)
    },
  },
  {
    method: 'POST',
    pattern: /^\/staff\/login$/,
    handler: ({ params }) => {
      if (!params.shopId || !params.loginName || !params.password) {
        return { status: 500, desc: '登录参数不完整' }
      }
      return ok(
        { staffId: 'staff-demo', staffName: String(params.loginName), status: 1 },
        MOCK_TOKEN,
      )
    },
  },

  /* ---------------- store / manager ---------------- */
  {
    method: 'GET',
    pattern: /^\/store\/manager\/list$/,
    handler: () =>
      mockTableAreas.map((area, index) => ({
        storeId: `store-${index + 1}`,
        storeName: `心飞餐饮 · ${area.areaName}店（示意）`,
        storeManagerId: 'mgr-demo',
        address: '北京市（示意）',
      })) as StoreDTO[],
  },
  {
    method: 'PUT',
    pattern: /^\/store\/switch\/[^/]+$/,
    handler: () => ok(true, MOCK_TOKEN),
  },

  /* ---------------- enterprise（operator 域） ---------------- */
  {
    method: 'GET',
    pattern: /^\/enterprise\/account\/removed\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const segments = url.split('/')
      const page = Number(segments[5])
      const size = Number(segments[6])
      const name = params.enterpriseName ? String(params.enterpriseName).toLowerCase() : ''
      const filtered = mockRemovedEnterpriseAccounts.filter(
        (item) => !name || item.enterpriseName.toLowerCase().includes(name),
      )
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/enterprise\/account\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const segments = url.split('/')
      const page = Number(segments[4])
      const size = Number(segments[5])
      const name = params.enterpriseName ? String(params.enterpriseName).toLowerCase() : ''
      const filtered = mockEnterpriseAccounts.filter(
        (item) => item.isDeleted !== 1 && (!name || item.enterpriseName.toLowerCase().includes(name)),
      )
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'POST',
    pattern: /^\/enterprise\/account\/add$/,
    handler: ({ body }) => {
      const account = (body ?? {}) as EnterpriseAccount
      if (!account.enterpriseName) return false
      const id = String(
        mockEnterpriseAccounts.reduce((max, item) => Math.max(max, Number(item.enterpriseId) || 0), 0) + 1,
      )
      mockEnterpriseAccounts.unshift({ ...account, enterpriseId: id, isDeleted: 0 })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/enterprise\/account$/,
    handler: ({ body }) => {
      const account = (body ?? {}) as EnterpriseAccount
      const index = mockEnterpriseAccounts.findIndex((item) => item.enterpriseId === account.enterpriseId)
      if (index < 0) return false
      mockEnterpriseAccounts[index] = { ...mockEnterpriseAccounts[index], ...account }
      return true
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/enterprise\/account\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const index = mockEnterpriseAccounts.findIndex((item) => item.enterpriseId === id)
      if (index < 0) return false
      const [removed] = mockEnterpriseAccounts.splice(index, 1)
      mockRemovedEnterpriseAccounts.unshift({
        ...removed,
        isDeleted: 1,
        deletedAt: new Date().toLocaleString('zh-CN', { hour12: false }),
        deletedBy: '王运营',
      })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/enterprise\/account\/recovery\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const index = mockRemovedEnterpriseAccounts.findIndex((item) => item.enterpriseId === id)
      if (index < 0) return false
      const [restored] = mockRemovedEnterpriseAccounts.splice(index, 1)
      const { deletedAt: _deletedAt, deletedBy: _deletedBy, ...account } = restored
      mockEnterpriseAccounts.unshift({ ...account, isDeleted: 0 })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/enterprise\/account\/forbidden\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const account = mockEnterpriseAccounts.find((item) => item.enterpriseId === id)
      if (!account) return false
      account.status = -1
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/enterprise\/account\/resetPwd$/,
    handler: ({ params }) => {
      const id = params.id ? String(params.id) : ''
      const account = mockEnterpriseAccounts.find((item) => item.enterpriseId === id)
      if (!account) return false
      account.password = params.password ? String(params.password) : account.password
      return true
    },
  },
  {
    method: 'GET',
    pattern: /^\/enterprise\/account\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      return (
        mockEnterpriseAccounts.find((item) => item.enterpriseId === id) ??
        mockRemovedEnterpriseAccounts.find((item) => item.enterpriseId === id) ??
        null
      )
    },
  },

  /* ---------------- sys dict / operator user（operator 域） ---------------- */
  {
    method: 'GET',
    pattern: /^\/sysDict\/list\/[^/]+$/,
    handler: ({ url }) => {
      const category = url.split('/').pop() as string
      return mockDictItems.filter((item) => item.category === category)
    },
  },
  {
    method: 'GET',
    pattern: /^\/operator\/user\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const segments = url.split('/')
      const page = Number(segments[4])
      const size = Number(segments[5])
      const name = params.name ? String(params.name).toLowerCase() : ''
      const filtered = mockOperatorUsers.filter(
        (item) => !name || item.loginname.toLowerCase().includes(name),
      )
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'POST',
    pattern: /^\/sysDict\/add$/,
    handler: ({ body }) => {
      const item = (body ?? {}) as SysDictionary
      if (!item.dictName) return false
      mockDictItems.push({
        ...item,
        dictId: String(mockDictItems.reduce((max, row) => Math.max(max, Number(row.dictId) || 0), 0) + 1),
      })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/sysDict$/,
    handler: ({ body }) => {
      const item = (body ?? {}) as { dictId?: string }
      const index = mockDictItems.findIndex((row) => row.dictId === item.dictId)
      if (index < 0) return false
      mockDictItems[index] = { ...mockDictItems[index], ...item }
      return true
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/sysDict\/[^/]+$/,
    handler: ({ url }) => {
      const dictId = url.split('/').pop() as string
      const index = mockDictItems.findIndex((row) => row.dictId === dictId)
      if (index < 0) return false
      mockDictItems.splice(index, 1)
      return true
    },
  },
  {
    method: 'POST',
    pattern: /^\/operator\/user\/add$/,
    handler: ({ body }) => {
      const user = (body ?? {}) as MockOperatorUser
      if (!user.loginname) return false
      mockOperatorUsers.push({
        ...user,
        uid: String(mockOperatorUsers.reduce((max, row) => Math.max(max, Number(row.uid) || 0), 0) + 1),
      })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/operator\/user$/,
    handler: ({ body }) => {
      const user = (body ?? {}) as { uid?: string }
      const index = mockOperatorUsers.findIndex((row) => row.uid === user.uid)
      if (index < 0) return false
      mockOperatorUsers[index] = { ...mockOperatorUsers[index], ...user }
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/operator\/user\/resetPwd$/,
    handler: ({ params }) => {
      const uid = params.uid ? String(params.uid) : ''
      const user = mockOperatorUsers.find((row) => row.uid === uid)
      if (!user) return false
      user.loginpass = params.password ? String(params.password) : user.loginpass
      return true
    },
  },

  /* ---------------- dish ---------------- */
  {
    method: 'GET',
    pattern: /^\/search\/dish\/code\/[^/]+\/page\/\d+\/\d+$/,
    handler: ({ url }) => {
      const parts = url.split('/')
      const code = decodeURIComponent(parts[parts.length - 4] ?? '')
      const page = Number(parts[parts.length - 2])
      const size = Number(parts[parts.length - 1])
      const keyword = code.trim().toLowerCase()
      const filtered = keyword
        ? mockDishes.filter(
            (dish) =>
              (dish.code ?? '').toLowerCase().includes(keyword) ||
              dish.name.toLowerCase().includes(keyword),
          )
        : mockDishes
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/dish\/manage\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const [, , , , pageStr, sizeStr] = url.split('/')
      const page = Number(pageStr)
      const size = Number(sizeStr)
      const name = params.name ? String(params.name).toLowerCase() : ''
      const categoryId = params.categoryId ? String(params.categoryId) : ''
      const status = params.status !== undefined && params.status !== '' ? Number(params.status) : undefined
      const filtered = mockDishes.filter((dish) => {
        if (name && !dish.name.toLowerCase().includes(name) && !(dish.code ?? '').toLowerCase().includes(name)) {
          return false
        }
        if (categoryId && dish.categoryId !== categoryId) return false
        if (status !== undefined && dish.status !== status) return false
        return true
      })
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/dish\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const dish = mockDishes.find((item) => item.id === id)
      return dish ?? null
    },
  },
  {
    method: 'POST',
    pattern: /^\/dish$/,
    handler: ({ body }) => {
      const payload = (body ?? {}) as { dish?: Dish }
      const dish = payload.dish
      if (!dish) return false
      const id = String(mockDishes.reduce((max, item) => Math.max(max, Number(item.id) || 0), 0) + 1)
      mockDishes.unshift({ ...dish, id, dishCategory: mockCategories.find((c) => c.categoryId === dish.categoryId) })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/dish$/,
    handler: ({ body }) => {
      const payload = (body ?? {}) as { dish?: Dish }
      const dish = payload.dish
      if (!dish?.id) return false
      const index = mockDishes.findIndex((item) => item.id === dish.id)
      if (index < 0) return false
      mockDishes[index] = { ...mockDishes[index], ...dish, dishCategory: mockCategories.find((c) => c.categoryId === dish.categoryId) }
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/dish\/(forbidden|enable)$/,
    handler: ({ url, body }) => {
      const action = url.split('/').pop() as string
      const status = action === 'enable' ? 1 : 0
      const ids = (body ?? []) as string[]
      ids.forEach((id) => {
        const dish = mockDishes.find((item) => item.id === id)
        if (dish) dish.status = status
      })
      return true
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/dish$/,
    handler: ({ body }) => {
      const ids = new Set((body ?? []) as string[])
      for (let i = mockDishes.length - 1; i >= 0; i--) {
        const id = mockDishes[i].id
        if (id !== undefined && ids.has(id)) mockDishes.splice(i, 1)
      }
      return true
    },
  },

  /* ---------------- dish category ---------------- */
  {
    method: 'GET',
    pattern: /^\/dish\/category\/page\/\d+\/\d+$/,
    handler: ({ url }) => {
      const [, , , , pageStr, sizeStr] = url.split('/')
      return paginate(mockCategories, Number(pageStr), Number(sizeStr))
    },
  },
  {
    method: 'POST',
    pattern: /^\/dish\/category\/add$/,
    handler: ({ params }) => {
      const name = String(params.name ?? '')
      if (!name) return false
      mockCategories.push({
        categoryId: `cat-${mockCategories.length + 1}`,
        type: Number(params.type ?? 1),
        name,
        torder: mockCategories.length + 1,
      })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/dish\/category\/[^/]+$/,
    handler: ({ url, params }) => {
      const id = url.split('/').pop() as string
      const category = mockCategories.find((item) => item.categoryId === id)
      if (!category) return false
      category.name = String(params.name ?? category.name)
      return true
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/dish\/category\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const index = mockCategories.findIndex((item) => item.categoryId === id)
      if (index < 0) return false
      mockCategories.splice(index, 1)
      return true
    },
  },

  /* ---------------- set meal ---------------- */
  {
    method: 'GET',
    pattern: /^\/setMeal\/page\/\d+\/\d+$/,
    handler: ({ url }) => {
      const [, , , pageStr, sizeStr] = url.split('/')
      return paginate(mockSetMeals, Number(pageStr), Number(sizeStr))
    },
  },

  /* ---------------- staff（员工） ---------------- */
  {
    method: 'GET',
    pattern: /^\/staff\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const segments = url.split('/')
      const page = Number(segments[3])
      const size = Number(segments[4])
      const name = params.staffName ? String(params.staffName).toLowerCase() : ''
      const phone = params.phone ? String(params.phone) : ''
      const filtered = mockStaffs.filter((item) => {
        if (name && !item.staffName.toLowerCase().includes(name)) return false
        if (phone && !(item.phone ?? '').includes(phone)) return false
        return true
      })
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'POST',
    pattern: /^\/staff\/add$/,
    handler: ({ body }) => {
      const staff = (body ?? {}) as Staff
      if (!staff.staffName) return null
      const rawPassword = staff.password
        ? String(staff.password)
        : String(Math.floor(10000000 + Math.random() * 90000000))
      const record = { ...staff } as Staff
      delete record.password
      mockStaffs.unshift({
        ...record,
        staffId: `S${String(mockStaffs.length + 1).padStart(3, '0')}`,
        status: staff.status ?? 1,
      })
      return rawPassword
    },
  },
  {
    method: 'PUT',
    pattern: /^\/staff$/,
    handler: ({ body }) => {
      const staff = (body ?? {}) as Staff
      const index = mockStaffs.findIndex((item) => item.staffId === staff.staffId)
      if (index < 0) return false
      mockStaffs[index] = { ...mockStaffs[index], ...staff }
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/staff\/pause\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const staff = mockStaffs.find((item) => item.staffId === id)
      if (!staff) return false
      staff.status = 0
      return true
    },
  },

  /* ---------------- credit（挂账） ---------------- */
  {
    method: 'GET',
    pattern: /^\/credit\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const segments = url.split('/')
      const page = Number(segments[3])
      const size = Number(segments[4])
      const name = params.userName ? String(params.userName).toLowerCase() : ''
      const filtered = mockCredits.filter((item) => {
        if (!name) return true
        const hit = `${item.companyName ?? ''}${item.userName ?? ''}`.toLowerCase()
        return hit.includes(name)
      })
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/credit\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      const credit = mockCredits.find((item) => item.creditId === id)
      if (!credit) return null
      // 回显为 BFF CreditVO 形状（主键字段名为 id）
      return {
        id: credit.creditId,
        creditType: credit.creditType ?? 2,
        companyName: credit.companyName,
        userName: credit.userName,
        phone: credit.phone,
        creditAmount: credit.creditAmount,
        status: credit.status,
        users: credit.users ?? [],
      } satisfies CreditPayload
    },
  },
  {
    method: 'POST',
    pattern: /^\/credit\/add$/,
    handler: ({ body }) => {
      const payload = (body ?? {}) as CreditPayload
      if (!payload.userName && !payload.companyName) return false
      const creditId = `CR${String(mockCredits.length + 1).padStart(3, '0')}`
      mockCredits.unshift({
        creditId,
        creditType: payload.creditType,
        companyName: payload.companyName,
        userName: payload.userName,
        phone: payload.phone,
        status: payload.status ?? 1,
        creditAmount: payload.creditAmount ?? 0,
        totalRepaymentAmount: 0,
        users: (payload.users ?? []).map((user, index) => ({
          id: `${creditId}-U${index + 1}`,
          creditId,
          userName: user.userName,
          phone: user.phone,
        })),
      })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/credit$/,
    handler: ({ body }) => {
      const payload = (body ?? {}) as CreditPayload
      const index = mockCredits.findIndex((item) => item.creditId === payload.id)
      if (index < 0) return false
      const prev = mockCredits[index]
      mockCredits[index] = {
        ...prev,
        creditType: payload.creditType,
        companyName: payload.companyName,
        userName: payload.userName,
        phone: payload.phone,
        status: payload.status ?? prev.status,
        creditAmount: payload.creditAmount ?? prev.creditAmount,
        users: (payload.users ?? prev.users ?? []).map((user, i) => ({
          id: prev.users?.[i]?.id ?? `${prev.creditId ?? 'CR'}-U${i + 1}`,
          creditId: prev.creditId,
          userName: user.userName,
          phone: user.phone,
        })),
      }
      return true
    },
  },
  {
    method: 'POST',
    pattern: /^\/credit\/repayment$/,
    handler: ({ body }) => {
      const payload = (body ?? {}) as CreditRepaymentPayload
      const credit = mockCredits.find((item) => item.creditId === payload.creditId)
      if (!credit) return false
      const amount = payload.repaymentAmount ?? 0
      // 与后端一致：超额拒绝；creditAmount 为剩余欠款，还款时扣减
      if (amount <= 0 || (credit.creditAmount ?? 0) < amount) return false
      credit.creditAmount = (credit.creditAmount ?? 0) - amount
      credit.totalRepaymentAmount = (credit.totalRepaymentAmount ?? 0) + amount
      return true
    },
  },
  {
    method: 'GET',
    pattern: /^\/credit\/log\/page\/\d+\/\d+\/[^/]+$/,
    handler: ({ url }) => {
      const segments = url.split('/')
      const pageNum = Number(segments[4])
      const pageSize = Number(segments[5])
      const creditId = decodeURIComponent(segments[6])
      return paginate(
        mockCreditLogs.filter((row) => row.creditId === creditId),
        pageNum,
        pageSize,
      )
    },
  },

  /* ---------------- sellCalculation（沽清） ---------------- */
  {
    method: 'GET',
    pattern: /^\/sellCalculation\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const segments = url.split('/')
      const page = Number(segments[3])
      const size = Number(segments[4])
      const name = params.dishName ? String(params.dishName).toLowerCase() : ''
      const dishType = params.dishType !== undefined && params.dishType !== '' ? Number(params.dishType) : undefined
      const filtered = mockSellCalculations.filter((row) => {
        if (name && !(row.dishName ?? '').toLowerCase().includes(name)) return false
        if (dishType !== undefined && row.dishType !== dishType) return false
        return true
      })
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/sellCalculation\/remainder\/[^/]+$/,
    handler: ({ url }) => {
      const dishId = url.split('/').pop() as string
      return mockSellCalculations.find((row) => row.dishId === dishId)?.remainder ?? -1
    },
  },
  {
    method: 'POST',
    pattern: /^\/sellCalculation$/,
    handler: ({ body }) => {
      const record = (body ?? {}) as SellCalculation
      if (!record.dishId) return false
      const exist = mockSellCalculations.find((row) => row.dishId === record.dishId)
      if (exist) {
        exist.dishName = record.dishName ?? exist.dishName
        exist.sellLimitTotal = record.sellLimitTotal ?? exist.sellLimitTotal
        exist.remainder = record.remainder ?? exist.remainder
      } else {
        mockSellCalculations.push({
          ...record,
          id: `SC${String(mockSellCalculations.length + 1).padStart(3, '0')}`,
        })
      }
      return true
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/sellCalculation$/,
    handler: ({ body }) => {
      const ids = new Set((body ?? []) as string[])
      for (let i = mockSellCalculations.length - 1; i >= 0; i--) {
        const id = mockSellCalculations[i].id
        if (id !== undefined && ids.has(id)) mockSellCalculations.splice(i, 1)
      }
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/sellCalculation\/decrease$/,
    handler: ({ params }) => {
      const dishId = String(params.dishId ?? '')
      const num = Number(params.dishNumber ?? 1)
      const row = mockSellCalculations.find((item) => item.dishId === dishId)
      if (row && row.remainder !== undefined && row.remainder >= 0) {
        row.remainder = Math.max(0, row.remainder - num)
      }
    },
  },
  {
    method: 'POST',
    pattern: /^\/sellCalculation\/add$/,
    handler: ({ params }) => {
      const dishId = String(params.dishId ?? '')
      const num = Number(params.dishNumber ?? 1)
      const row = mockSellCalculations.find((item) => item.dishId === dishId)
      if (row) {
        if (row.remainder !== undefined && row.remainder >= 0) row.remainder += num
        return
      }
      const dish = mockDishes.find((item) => item.id === dishId)
      mockSellCalculations.push({
        id: `SC${String(mockSellCalculations.length + 1).padStart(3, '0')}`,
        dishId,
        dishName: dish?.name,
        dishType: dish?.categoryId === 'cat-combo' ? 2 : 1,
        sellLimitTotal: num,
        remainder: num,
      })
    },
  },

  /* ---------------- table ---------------- */
  {
    method: 'GET',
    pattern: /^\/table\/area\/list$/,
    handler: () =>
      mockTableAreas.map((area) => ({
        areaId: area.areaId,
        areaName: area.areaName,
        indexNumber: area.indexNumber,
        tableCount: mockTables.filter((table) => table.areaId === area.areaId).length,
      })),
  },
  {
    method: 'POST',
    pattern: /^\/table\/area\/add$/,
    handler: ({ body }) => {
      const area = (body ?? {}) as TableArea
      if (!area.areaName) return false
      mockTableAreas.push({ ...area, areaId: `area-${mockTableAreas.length + 1}` })
      return true
    },
  },
  {
    method: 'GET',
    pattern: /^\/table\/area\/[^/]+\/page\/\d+\/\d+$/,
    handler: ({ url }) => {
      const segments = url.split('/')
      const areaId = segments[3]
      const page = Number(segments[5])
      const size = Number(segments[6])
      return paginate(mockTables.filter((table) => table.areaId === areaId), page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/table\/count\/[^/]+\/\d+$/,
    handler: ({ url }) => {
      const segments = url.split('/')
      const areaId = segments[3]
      const status = Number(segments[4])
      return mockTables.filter((table) => table.areaId === areaId && table.status === status).length
    },
  },
  {
    method: 'GET',
    pattern: /^\/table\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      return mockTables.find((table) => table.tableId === id) ?? null
    },
  },
  {
    method: 'POST',
    pattern: /^\/table\/add$/,
    handler: ({ body }) => {
      const table = (body ?? {}) as Table
      if (!table.tableName) return false
      mockTables.push({
        ...table,
        tableId: `T${String(mockTables.length + 1).padStart(2, '0')}`,
        status: table.status ?? 0,
      })
      return true
    },
  },
  {
    method: 'PUT',
    pattern: /^\/table$/,
    handler: ({ body }) => {
      const table = (body ?? {}) as Table
      const index = mockTables.findIndex((item) => item.tableId === table.tableId)
      if (index < 0) return false
      mockTables[index] = { ...mockTables[index], ...table }
      return true
    },
  },
  {
    method: 'POST',
    pattern: /^\/table\/log\/open$/,
    handler: ({ body }) => {
      const log = (body ?? {}) as TableLog
      const table = mockTables.find((item) => item.tableId === log.tableId)
      if (!table) return false
      table.status = 1
      return true
    },
  },
  {
    method: 'GET',
    pattern: /^\/table\/log\/open\/[^/]+$/,
    handler: ({ url }) => {
      const tableId = url.split('/').pop() as string
      const table = mockTables.find((item) => item.tableId === tableId)
      if (!table || table.status !== 1) return null
      return {
        logId: `log-${tableId}`,
        tableId,
        tableStatus: 1,
        userNumbers: table.tableSeatNumber ?? 2,
        createTime: new Date().toLocaleString('zh-CN', { hour12: false }),
      } satisfies TableLog
    },
  },

  /* ---------------- order / report ---------------- */
  {
    method: 'GET',
    pattern: /^\/order\/report\/currentAmount$/,
    handler: (): CurrentAmountCollectDTO => ({
      payTotal: 864200,
      payTotalCount: 42,
      noPayTotal: 157400,
      noPayTotalCount: 3,
      totalPerson: 128,
      currentPerson: 24,
    }),
  },
  {
    method: 'GET',
    pattern: /^\/order\/report\/hourCollect$/,
    handler: ({ params }) => {
      const type = params.type !== undefined ? Number(params.type) : 1
      // type=2 时后端把单数放进 totalAmount，这里按同一字段返回示意单数
      const countMap: Record<number, number> = {
        10: 3, 11: 7, 12: 14, 13: 9, 14: 4, 17: 2, 18: 8, 19: 15, 20: 11, 21: 6,
      }
      return type === 2
        ? mockHourCollect.map((row) => ({
            currentDateHour: row.currentDateHour,
            totalAmount: countMap[row.currentDateHour ?? -1] ?? 0,
          }))
        : mockHourCollect
    },
  },
  {
    method: 'GET',
    pattern: /^\/order\/report\/payType$/,
    handler: () => mockPayTypeCollect,
  },
  {
    method: 'GET',
    pattern: /^\/order\/report\/privilege$/,
    handler: () => mockPrivilege,
  },
  {
    method: 'GET',
    pattern: /^\/order\/number\/[^/]+$/,
    handler: ({ url }) => {
      const orderNumber = url.split('/').pop() as string
      // 与真实契约一致：只返回已付订单，作为反结账入口约束
      return (
        mockOrders.find(
          (order) => order.orderNumber === orderNumber && order.payStatus === 1,
        ) ?? null
      )
    },
  },
  {
    method: 'POST',
    pattern: /^\/order\/returnDish$/,
    handler: ({ body }) => {
      const { detailId } = (body ?? {}) as { detailId?: string; remarks?: string[] }
      if (!detailId) return false
      for (const order of mockOrders) {
        const detail = order.orderDetails?.find((item) => item.detailId === detailId)
        if (detail) {
          detail.detailStatus = 2
          return true
        }
      }
      return false
    },
  },
  {
    method: 'POST',
    pattern: /^\/order\/reverse$/,
    handler: ({ body }) => {
      const { orderId } = (body ?? {}) as { orderId?: string; remark?: string }
      const target = mockOrders.find((order) => order.orderId === orderId)
      // 仅已付订单允许反结账，与真实契约一致
      if (!target || target.payStatus !== 1) return false
      target.payStatus = 0
      return true
    },
  },
  {
    method: 'GET',
    pattern: /^\/order\/page\/\d+\/\d+$/,
    handler: ({ url, params }) => {
      const [, , pageNum, pageSize] = url.split('/')
      const page = Number(pageNum)
      const size = Number(pageSize)
      const keyword = params.orderNumber ? String(params.orderNumber) : ''
      const filtered = mockOrders.filter((order) => {
        if (params.payStatus !== undefined && order.payStatus !== Number(params.payStatus)) return false
        if (params.payType !== undefined && order.payType !== Number(params.payType)) return false
        if (keyword && !String(order.orderNumber ?? '').includes(keyword)) return false
        return true
      })
      return paginate(filtered, page, size)
    },
  },
  {
    method: 'GET',
    pattern: /^\/order\/[^/]+$/,
    handler: ({ url }) => {
      const id = url.split('/').pop() as string
      return mockOrders.find((order) => order.orderId === id) ?? null
    },
  },
]

/** C 端路由先注册，保证 /order/micro-order-* 等具体路由优先于 B 端通用路由 */
const routes: MockRoute[] = [...microappRoutes, ...bEndRoutes]

/** 路由匹配与执行（axios adapter 与通用传输共用） */
export function dispatchMock(
  method: string,
  url: string,
  params: Record<string, unknown>,
  body: unknown,
): unknown {
  const route = routes.find((item) => item.method === method && item.pattern.test(url))
  if (!route) {
    throw new MockNotFoundError(`[mock] 未实现的请求：${method} ${url}`)
  }
  const ctx: MockCtx = { url, params, body: parseBody(body) }
  return route.handler(ctx)
}

/** axios 自定义 adapter：命中 mock 路由时本地应答（B 端默认） */
export const mockAdapter: AxiosAdapter = async (config: InternalAxiosRequestConfig) => {
  const method = (config.method ?? 'get').toUpperCase()
  const url = config.url ?? ''
  const data = dispatchMock(method, url, (config.params ?? {}) as Record<string, unknown>, config.data)
  const response: AxiosResponse = {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  }
  return response
}

/** 传输无关的 mock 传输（C 端 Taro / 测试用） */
export function createMockTransport(): HttpTransport {
  return {
    async request(req) {
      const data = dispatchMock(
        req.method.toUpperCase(),
        req.url,
        req.params ?? {},
        req.data,
      )
      return { status: 200, data }
    },
  }
}

/** 供外部读取的 mock 数据（演示/测试用） */
export {
  mockCategories,
  mockCredits,
  mockCreditLogs,
  mockDictItems,
  mockDishes,
  mockEnterpriseAccounts,
  mockHourCollect,
  mockOperatorUsers,
  mockOrders,
  mockPayTypeCollect,
  mockPrivilege,
  mockRemovedEnterpriseAccounts,
  mockSellCalculations,
  mockSetMeals,
  mockStaffs,
  mockTableAreas,
  mockTables,
}
export {
  mockMicroappCategories,
  mockMicroappDishes,
  mockMicroappPayMethods,
  mockMicroappStore,
} from './microapp'
export type {
  MockMicroappCategory,
  MockMicroappDish,
  MockMicroappStore,
  MockPayMethod,
} from './microapp'

/**
 * mock 数据源与 axios mockAdapter。
 *
 * 切换方式：VITE_USE_MOCK === '1'（默认 '1'，即默认走 mock）。
 * 命中 mock 时 http 实例会挂载本 adapter，所有请求在本地应答，不发出真实 HTTP。
 */

import { AxiosError } from 'axios'
import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  CurrentAmountCollectDTO,
  Dish,
  PageResult,
  ProviderResult,
  StoreDTO,
  TableArea,
} from '../types'
import {
  mockCategories,
  mockDishes,
  mockOrders,
  mockSetMeals,
  mockTableAreas,
  mockTables,
} from './data'

/** 是否启用 mock（默认开启，便于前端独立演示） */
export function isMockMode(): boolean {
  return (import.meta.env.VITE_USE_MOCK ?? '1') === '1'
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

function parseBody(data: unknown): any {
  if (typeof data !== 'string' || data.length === 0) return undefined
  try {
    return JSON.parse(data)
  } catch {
    return undefined
  }
}

interface MockCtx {
  url: string
  params: Record<string, unknown>
  body: any
}

type MockHandler = (ctx: MockCtx) => unknown

interface MockRoute {
  method: string
  pattern: RegExp
  handler: MockHandler
}

const routes: MockRoute[] = [
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

  /* ---------------- enterprise ---------------- */
  {
    method: 'GET',
    pattern: /^\/enterprise\/account\/page\/\d+\/\d+$/,
    handler: () =>
      paginate(
        [
          {
            enterpriseId: 'ent-demo',
            enterpriseName: '心飞餐饮（示意）',
            shopId: '12345678',
            phone: '13800000000',
            status: 1,
          },
        ],
        1,
        10,
      ) as PageResult<unknown>,
  },

  /* ---------------- dish ---------------- */
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
    handler: () => [],
  },
  {
    method: 'GET',
    pattern: /^\/order\/report\/payType$/,
    handler: () => [],
  },
  {
    method: 'GET',
    pattern: /^\/order\/report\/privilege$/,
    handler: () => ({}),
  },
  {
    method: 'GET',
    pattern: /^\/order\/number\/[^/]+$/,
    handler: ({ url }) => {
      const orderNumber = url.split('/').pop() as string
      return mockOrders.find((order) => order.orderNumber === orderNumber) ?? null
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

/** axios 自定义 adapter：命中 mock 路由时本地应答 */
export const mockAdapter: AxiosAdapter = async (config: InternalAxiosRequestConfig) => {
  const method = (config.method ?? 'get').toUpperCase()
  const url = config.url ?? ''
  const route = routes.find((item) => item.method === method && item.pattern.test(url))
  if (!route) {
    throw new AxiosError(`[mock] 未实现的请求：${method} ${url}`, 'MOCK_NOT_FOUND', config)
  }
  const data = await route.handler({
    url,
    params: (config.params ?? {}) as Record<string, unknown>,
    body: parseBody(config.data),
  })
  const response: AxiosResponse = {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  }
  return response
}

/** 供外部读取的 mock 数据（演示/测试用） */
export { mockCategories, mockDishes, mockOrders, mockSetMeals, mockTableAreas, mockTables }

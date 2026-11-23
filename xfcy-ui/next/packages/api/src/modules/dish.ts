/**
 * 菜品 / 菜品分类 / 套餐模块（store 域）。
 * 契约：
 * - IDishService          @HttpExchange("/dish")
 * - IDishCategoryService  @HttpExchange("/dish/category")
 * - ISetMealService       @HttpExchange("/setMeal")
 * 网关：/dish/**、/setMeal/** → enterprise-web。
 */

import { http, unwrap } from '../http'
import type {
  Dish,
  DishCategory,
  DishWithFlavors,
  PageResult,
  SetMeal,
  SetMealDish,
  SetMealWithDishes,
} from '../types'

/* ---------------- 菜品 ---------------- */

/** 管理端菜品分页：GET /dish/manage/page/{pageNum}/{pageSize}?name=&categoryId=&status= */
export async function page(
  pageNum: number,
  pageSize: number,
  name?: string,
  categoryId?: string,
  status?: number,
): Promise<PageResult<Dish>> {
  const { data } = await http.get(`/dish/manage/page/${pageNum}/${pageSize}`, {
    params: { name, categoryId, status },
  })
  return unwrap<PageResult<Dish>>(data)
}

/** 菜品详情：GET /dish/{id} */
export async function getById(id: string): Promise<Dish> {
  const { data } = await http.get(`/dish/${id}`)
  return unwrap<Dish>(data)
}

/** 点餐端可售菜品：GET /dish/enable/list/{categoryId}?name= */
export async function enableList(categoryId: string, name?: string): Promise<Dish[]> {
  const { data } = await http.get(`/dish/enable/list/${categoryId}`, { params: { name } })
  return unwrap<Dish[]>(data)
}

/**
 * 点餐端按编码检索（ES）：GET /search/dish/code/{code}/page/{n}/{s}
 * 注意：ES 索引当前无写入代码（K16），检索可能恒空——调用方需空态提示。
 */
export async function searchByCode(
  code: string,
  pageNum = 1,
  pageSize = 20,
): Promise<PageResult<Dish>> {
  const { data } = await http.get(
    `/search/dish/code/${encodeURIComponent(code)}/page/${pageNum}/${pageSize}`,
  )
  return unwrap<PageResult<Dish>>(data)
}

/** 新增菜品（含口味）：POST /dish */
export async function save(payload: DishWithFlavors): Promise<boolean> {
  const { data } = await http.post('/dish', payload)
  return unwrap<boolean>(data)
}

/** 修改菜品（含口味）：PUT /dish */
export async function update(payload: DishWithFlavors): Promise<boolean> {
  const { data } = await http.put('/dish', payload)
  return unwrap<boolean>(data)
}

/** 停售：PUT /dish/forbidden（body: string[] ids） */
export async function forbidden(ids: string[]): Promise<boolean> {
  const { data } = await http.put('/dish/forbidden', ids)
  return unwrap<boolean>(data)
}

/** 起售：PUT /dish/enable（body: string[] ids） */
export async function enable(ids: string[]): Promise<boolean> {
  const { data } = await http.put('/dish/enable', ids)
  return unwrap<boolean>(data)
}

/** 删除：DELETE /dish（body: string[] ids） */
export async function remove(ids: string[]): Promise<boolean> {
  const { data } = await http.delete('/dish', { data: ids })
  return unwrap<boolean>(data)
}

/* ---------------- 菜品分类 ---------------- */

/** 分类分页：GET /dish/category/page/{pageNum}/{pageSize} */
export async function categoryPage(pageNum: number, pageSize: number): Promise<PageResult<DishCategory>> {
  const { data } = await http.get(`/dish/category/page/${pageNum}/${pageSize}`)
  return unwrap<PageResult<DishCategory>>(data)
}

/** 按类型取分类：GET /dish/category/type/{type}（1 菜品 / 2 套餐），返回 Map 列表 */
export async function categoryByType(type: number): Promise<Array<Record<string, unknown>>> {
  const { data } = await http.get(`/dish/category/type/${type}`)
  return unwrap<Array<Record<string, unknown>>>(data)
}

/** 新增分类：POST /dish/category/add?name=&type= */
export async function categoryAdd(name: string, type: number): Promise<boolean> {
  const { data } = await http.post('/dish/category/add', null, { params: { name, type } })
  return unwrap<boolean>(data)
}

/** 修改分类名：PUT /dish/category/{id}?name= */
export async function categoryUpdate(id: string, name: string): Promise<boolean> {
  const { data } = await http.put(`/dish/category/${id}`, null, { params: { name } })
  return unwrap<boolean>(data)
}

/** 删除分类：DELETE /dish/category/{id} */
export async function categoryRemove(id: string): Promise<boolean> {
  const { data } = await http.delete(`/dish/category/${id}`)
  return unwrap<boolean>(data)
}

/* ---------------- 套餐 ---------------- */

/** 套餐分页：GET /setMeal/page/{pageNum}/{pageSize}?name= */
export async function setMealPage(
  pageNum: number,
  pageSize: number,
  name?: string,
): Promise<PageResult<SetMeal>> {
  const { data } = await http.get(`/setMeal/page/${pageNum}/${pageSize}`, { params: { name } })
  return unwrap<PageResult<SetMeal>>(data)
}

/** 套餐详情：GET /setMeal/{id} */
export async function setMealGetById(id: string): Promise<SetMeal> {
  const { data } = await http.get(`/setMeal/${id}`)
  return unwrap<SetMeal>(data)
}

/** 新增套餐：POST /setMeal（SetMealWithDishes） */
export async function setMealSave(payload: SetMealWithDishes): Promise<boolean> {
  const { data } = await http.post('/setMeal', payload)
  return unwrap<boolean>(data)
}

/** 修改套餐：PUT /setMeal */
export async function setMealUpdate(payload: SetMealWithDishes): Promise<boolean> {
  const { data } = await http.put('/setMeal', payload)
  return unwrap<boolean>(data)
}

/** 套餐停售：PUT /setMeal/forbidden（ids 数组） */
export async function setMealForbidden(ids: string[]): Promise<boolean> {
  const { data } = await http.put('/setMeal/forbidden', ids)
  return unwrap<boolean>(data)
}

/** 套餐起售：PUT /setMeal/enable（ids 数组） */
export async function setMealEnable(ids: string[]): Promise<boolean> {
  const { data } = await http.put('/setMeal/enable', ids)
  return unwrap<boolean>(data)
}

/** 删除套餐：DELETE /setMeal（ids 数组） */
export async function setMealRemove(ids: string[]): Promise<boolean> {
  const { data } = await http.delete('/setMeal', { data: ids })
  return unwrap<boolean>(data)
}

/** 套餐内含菜品：GET /setMeal/dish/list/{setMealId} */
export async function setMealDishList(setMealId: string): Promise<SetMealDish[]> {
  const { data } = await http.get(`/setMeal/dish/list/${setMealId}`)
  return unwrap<SetMealDish[]>(data)
}

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { enterprise, isMockMode } from '@xfcy/api'
import type { EnterpriseAccount } from '@xfcy/api'

export type EnterpriseStatus = '试用' | '正式' | '禁用'
export type Plan = '基础版' | '标准版' | '旗舰版'

export const planOptions: Plan[] = ['基础版', '标准版', '旗舰版']

/** 演示扩展字段（真实契约无 plan/shopCount/deletedAt/deletedBy） */
export type EnterpriseDraft = EnterpriseAccount & {
  plan?: string
  shopCount?: number
  deletedAt?: string
  deletedBy?: string
}

export type EnterpriseRow = {
  id: string
  code: string
  name: string
  contact: string
  phone: string
  shopCount: number
  plan: string
  status: EnterpriseStatus
  statusValue: number | undefined
  expireDate: string
  createdAt: string
}

export type RecycleRow = {
  id: string
  code: string
  name: string
  contact: string
  shopCount: number
  deletedAt: string
  deletedBy: string
}

/** 一次拉取的页大小：演示数据量小，前端做筛选/分页 */
const PAGE_SIZE = 1000

export function statusText(status?: number): EnterpriseStatus {
  if (status === 0) return '试用'
  if (status === -1) return '禁用'
  return '正式'
}

export function toEnterpriseRow(account: EnterpriseAccount): EnterpriseRow {
  const draft = account as EnterpriseDraft
  return {
    id: account.enterpriseId,
    code: account.shopId,
    name: account.enterpriseName,
    contact: account.applicant ?? '—',
    phone: account.phone,
    shopCount: draft.shopCount ?? 0,
    plan: draft.plan ?? '基础版',
    status: statusText(account.status),
    statusValue: account.status,
    expireDate: account.expireTime ?? '—',
    createdAt: (account.applicationTime ?? '').slice(0, 10) || '—',
  }
}

export function toRecycleRow(account: EnterpriseAccount): RecycleRow {
  const draft = account as EnterpriseDraft
  return {
    id: account.enterpriseId,
    code: account.shopId,
    name: account.enterpriseName,
    contact: account.applicant ?? '—',
    shopCount: draft.shopCount ?? 0,
    deletedAt: draft.deletedAt ?? account.lastUpdateTime ?? '—',
    deletedBy: draft.deletedBy ?? '—',
  }
}

/**
 * 企业账号 + 回收站共享状态（跨页）。
 *
 * 恢复写回链：restore() → enterprise.recovery(id)（mock/真实同一契约）
 * → load() 重新拉取 → accounts/removedAccounts 更新
 * → EnterpriseAccountsView 与 RecycleBinView 同时读到最新列表。
 */
export const useEnterpriseStore = defineStore('enterprise', () => {
  const accounts = ref<EnterpriseAccount[]>([])
  const removedAccounts = ref<EnterpriseAccount[]>([])
  const loading = ref(false)
  const loaded = ref(false)
  const error = ref('')

  const enterpriseRows = computed<EnterpriseRow[]>(() => accounts.value.map(toEnterpriseRow))
  const recycleRows = computed<RecycleRow[]>(() => removedAccounts.value.map(toRecycleRow))

  async function load() {
    if (loading.value) return
    loading.value = true
    error.value = ''
    try {
      const [page, removed] = await Promise.all([
        enterprise.page(1, PAGE_SIZE),
        enterprise.removedPage(1, PAGE_SIZE),
      ])
      accounts.value = page.records
      removedAccounts.value = removed.records
      loaded.value = true
    } catch (err) {
      error.value = err instanceof Error ? err.message : '企业账号加载失败'
    } finally {
      loading.value = false
    }
  }

  function refresh() {
    return load()
  }

  async function create(payload: EnterpriseDraft, validityDay?: number) {
    await enterprise.add(payload, validityDay)
    await load()
  }

  async function update(payload: EnterpriseDraft, validityDay?: number) {
    await enterprise.update(payload, validityDay)
    await load()
  }

  /** 启用/停用：启用走 update(status=1)，停用走 forbidden 契约 */
  async function toggleStatus(row: EnterpriseRow) {
    const account = accounts.value.find((item) => item.enterpriseId === row.id)
    if (!account) return
    if (row.statusValue === -1) {
      await enterprise.update({ ...account, status: 1 })
    } else {
      await enterprise.forbidden(row.id)
    }
    await load()
  }

  /** 逻辑删除：进入回收站 */
  async function remove(id: string) {
    await enterprise.remove(id)
    await load()
  }

  /** 恢复：写回企业列表（跨页共享状态） */
  async function restore(id: string) {
    await enterprise.recovery(id)
    await load()
  }

  /**
   * 彻底删除：后端契约无物理删除接口，仅 mock 模式本地生效。
   * real 模式抛出异常，由界面提示。
   */
  async function destroy(id: string) {
    if (!isMockMode()) {
      throw new Error('后端未提供物理删除接口，彻底删除仅在 mock 模式可用')
    }
    removedAccounts.value = removedAccounts.value.filter((item) => item.enterpriseId !== id)
  }

  return {
    accounts,
    removedAccounts,
    loading,
    loaded,
    error,
    enterpriseRows,
    recycleRows,
    load,
    refresh,
    create,
    update,
    toggleStatus,
    remove,
    restore,
    destroy,
  }
})

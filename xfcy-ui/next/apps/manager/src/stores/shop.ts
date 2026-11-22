import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export type Shop = {
  id: string
  name: string
  /** 侧栏品牌位展示用短名 */
  short: string
}

/**
 * 门店状态（示意）：首版仅做本地切换，不请求后端。
 */
export const useShopStore = defineStore('shop', () => {
  const shops = ref<Shop[]>([
    { id: 'guomao', name: '心飞餐饮 · 国贸店', short: '国贸店' },
    { id: 'wangjing', name: '心飞餐饮 · 望京店', short: '望京店' },
    { id: 'zhongguancun', name: '心飞餐饮 · 中关村店', short: '中关村店' },
  ])
  const currentShopId = ref<string>('guomao')

  const currentShop = computed<Shop>(
    () => shops.value.find((shop) => shop.id === currentShopId.value) ?? shops.value[0],
  )
  const shopOptions = computed(() =>
    shops.value.map((shop) => ({ label: shop.name, value: shop.id })),
  )

  function switchShop(id: string) {
    if (shops.value.some((shop) => shop.id === id)) {
      currentShopId.value = id
    }
  }

  return { shops, currentShopId, currentShop, shopOptions, switchShop }
})

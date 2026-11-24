import { useEffect, useRef, useState } from 'react'
import { Input, ScrollView, Text, View } from '@tarojs/components'
import { mockMicroappCategories, mockMicroappDishes, mockMicroappStore } from '@xfcy/api'
import CartBar from '../../components/CartBar'
import DishCard from '../../components/DishCard'
import { fetchMenu, searchMenu, useMockData } from '../../api/service'
import type { Category, Dish, StoreInfo } from '../../types'
import './index.scss'

/**
 * 门店菜单（原型 microapp-menu.html）
 * 结构：门店头 → 搜索 → 分类锚点 → 菜品大图卡（2 列）→ 底部购物车条
 * 数据：@xfcy/api microapp 模块（默认本地 mock，可切真实后端）
 */
export default function Menu() {
  const mockMode = useMockData()
  const [keyword, setKeyword] = useState('')
  const [activeCat, setActiveCat] = useState(mockMicroappCategories[0].id)
  const [anchor, setAnchor] = useState('')
  const [store, setStore] = useState<StoreInfo>(mockMicroappStore)
  const [categories, setCategories] = useState<Category[]>(() =>
    mockMode ? mockMicroappCategories.map((c) => ({ id: c.id, name: c.name })) : [],
  )
  const [dishes, setDishes] = useState<Dish[]>(mockMode ? mockMicroappDishes : [])
  const [searchResults, setSearchResults] = useState<Dish[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [searchError, setSearchError] = useState('')
  const seqRef = useRef(0)
  const aliveRef = useRef(true)

  /* 菜单加载：mock 模式立即以本地数据渲染，真实模式拉取后替换 */
  const loadMenu = () => {
    aliveRef.current = true
    setLoading(true)
    setLoadError('')
    fetchMenu()
      .then((menu) => {
        if (!aliveRef.current) return
        setStore(menu.store)
        setCategories(menu.categories)
        setDishes(menu.dishes)
        setActiveCat((prev) =>
          prev && menu.categories.some((c) => c.id === prev)
            ? prev
            : (menu.categories[0]?.id ?? ''),
        )
      })
      .catch((err: unknown) => {
        console.error('[microapp] 菜单加载失败', err)
        if (!aliveRef.current) return
        setLoadError(err instanceof Error ? err.message : '菜单加载失败，请重试')
      })
      .finally(() => {
        if (aliveRef.current) setLoading(false)
      })
  }

  useEffect(() => {
    loadMenu()
    return () => {
      aliveRef.current = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  /* 关键词检索（ES 索引：菜品 + 套餐） */
  const kw = keyword.trim()
  useEffect(() => {
    if (!kw) {
      seqRef.current += 1
      setSearchResults(null)
      setSearchError('')
      return
    }
    const seq = ++seqRef.current
    searchMenu(kw)
      .then((rows) => {
        if (seqRef.current === seq) {
          setSearchResults(rows)
          setSearchError('')
        }
      })
      .catch((err: unknown) => {
        if (seqRef.current !== seq) return
        setSearchResults([])
        setSearchError(err instanceof Error ? err.message : '检索失败，请重试')
      })
  }, [kw])

  const searchResult = kw ? (searchResults ?? []) : null

  const scrollTo = (id: string) => {
    setActiveCat(id)
    setAnchor(`cat-${id}`)
  }

  return (
    <View className="xf-page menu">
      {/* 门店头 */}
      <View className="menu__head">
        <View>
          <Text className="menu__name">{store.name}</Text>
          <Text className="menu__sub">
            堂食点餐 · 桌台 {store.tableName}
          </Text>
        </View>
        <View className="menu__mode">
          <Text className="menu__mode-item">配送</Text>
          <Text className="menu__mode-item is-on">自取</Text>
        </View>
      </View>

      {/* 搜索 */}
      <View className="menu__searchwrap">
        <Input
          className="menu__search"
          placeholder="🔍 搜索菜品，如「红烧肉」"
          value={keyword}
          onInput={(e: { detail: { value: string } }) => setKeyword(e.detail.value)}
        />
      </View>

      {/* 加载失败：显式报错 + 重试（不再静默空白） */}
      {loadError ? (
        <View className="menu__error">
          <Text className="xf-empty__icon">⚠️</Text>
          <Text className="xf-empty__title">菜单加载失败</Text>
          <Text className="xf-empty__desc">{loadError}</Text>
          <View className="menu__retry" onClick={loadMenu}>
            {loading ? '加载中…' : '重新加载'}
          </View>
        </View>
      ) : null}

      {/* 分类锚点 */}
      {!searchResult && !loadError && (
        <ScrollView scrollX className="menu__cats">
          <View className="menu__cats-inner">
            {categories.map((cat) => (
              <View
                key={cat.id}
                className={`menu__cat ${activeCat === cat.id ? 'is-on' : ''}`}
                onClick={() => scrollTo(cat.id)}
              >
                {cat.name}
              </View>
            ))}
          </View>
        </ScrollView>
      )}

      {/* 菜品区 */}
      <ScrollView
        scrollY
        className="menu__body"
        scrollIntoView={anchor}
        scrollWithAnimation
      >
        {searchResult ? (
          <View>
            <Text className="menu__sec">搜索「{kw}」 · {searchResult.length} 道</Text>
            {searchResult.length === 0 ? (
              searchError ? (
                <View className="xf-empty">
                  <View className="xf-empty__icon">⚠️</View>
                  <Text className="xf-empty__title">检索失败</Text>
                  <Text className="xf-empty__desc">{searchError}</Text>
                </View>
              ) : (
                <View className="xf-empty">
                  <View className="xf-empty__icon">🍽️</View>
                  <Text className="xf-empty__title">没有找到相关菜品</Text>
                  <Text className="xf-empty__desc">换个关键词试试</Text>
                </View>
              )
            ) : (
              <View className="menu__grid">
                {searchResult.map((d) => (
                  <DishCard key={d.id} dish={d} />
                ))}
              </View>
            )}
          </View>
        ) : (
          categories.map((cat) => {
            const list = dishes.filter((d) => d.categoryId === cat.id)
            return (
              <View key={cat.id} id={`cat-${cat.id}`} className="menu__section">
                <Text className="menu__sec">
                  {cat.name} · {list.length} 道
                </Text>
                <View className="menu__grid">
                  {list.map((d) => (
                    <DishCard key={d.id} dish={d} />
                  ))}
                </View>
              </View>
            )
          })
        )}
      </ScrollView>

      <CartBar />
    </View>
  )
}

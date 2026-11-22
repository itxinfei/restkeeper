import { useState } from 'react'
import { Input, ScrollView, Text, View } from '@tarojs/components'
import CartBar from '../../components/CartBar'
import DishCard from '../../components/DishCard'
import { mockCategories, mockDishes, mockStore } from '../../api/mock'
import './index.scss'

/**
 * 门店菜单（原型 microapp-menu.html）
 * 结构：门店头 → 搜索 → 分类锚点 → 菜品大图卡（2 列）→ 底部购物车条
 */
export default function Menu() {
  const [keyword, setKeyword] = useState('')
  const [activeCat, setActiveCat] = useState(mockCategories[0].id)
  const [anchor, setAnchor] = useState('')

  const kw = keyword.trim()
  const searchResult = kw
    ? mockDishes.filter((d) => d.name.includes(kw) || d.desc.includes(kw))
    : null

  const scrollTo = (id: string) => {
    setActiveCat(id)
    setAnchor(`cat-${id}`)
  }

  return (
    <View className="xf-page menu">
      {/* 门店头 */}
      <View className="menu__head">
        <View>
          <Text className="menu__name">{mockStore.name}</Text>
          <Text className="menu__sub">
            堂食点餐 · 桌台 {mockStore.tableName}
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

      {/* 分类锚点 */}
      {!searchResult && (
        <ScrollView scrollX className="menu__cats">
          <View className="menu__cats-inner">
            {mockCategories.map((cat) => (
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
              <View className="xf-empty">
                <View className="xf-empty__icon">🍽️</View>
                <Text className="xf-empty__title">没有找到相关菜品</Text>
                <Text className="xf-empty__desc">换个关键词试试</Text>
              </View>
            ) : (
              <View className="menu__grid">
                {searchResult.map((d) => (
                  <DishCard key={d.id} dish={d} />
                ))}
              </View>
            )}
          </View>
        ) : (
          mockCategories.map((cat) => {
            const list = mockDishes.filter((d) => d.categoryId === cat.id)
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

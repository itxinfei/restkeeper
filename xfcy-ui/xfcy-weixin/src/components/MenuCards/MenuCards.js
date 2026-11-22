import Taro, { Component } from '@tarojs/taro'
import { connect } from '@tarojs/redux'
import * as actions from './../../actions/menu'
import { View, Text, ScrollView, Image } from '@tarojs/components'
import './MenuCards.scss'

@connect(state => state.menu, { ...actions })
class MenuCards extends Component {
  constructor () {
    super(...arguments)
    this.state = {
      actInd: 1,
    }
  }
  handleClick (value) {
    this.setState({
      actInd: value
    })
  }

  render () {
    const {
      actInd,
    } = this.state
    const {
      cardsList,
      onClick,
      onOpenDishDetail,
      cartList
    } = this.props
    
    const cards = cardsList.length > 0 ? cardsList.map((item, index) => {
      let menuNum = 0
      if (cartList) {
        let cart = cartList.dishList.find(dish => {
          return dish.id === item.dishId
        })
        if (cart) {
          menuNum = cart.number
        }
      }
      
      return (
        <View className='menuCaard' key={index}>
          <Image onClick={onOpenDishDetail.bind(this, item.dishId, item.type)} className='maunIco' src='./../../assets/img/banner.png' />
          <View className='info'>
            <View className='name' onClick={onOpenDishDetail.bind(this, item.dishId, item.type)}>{item.dishName}</View>
            <View className='des' onClick={onOpenDishDetail.bind(this, item.dishId, item.type)}>{item.desc}</View>
            <View className='pirBox'>
              <View className='pir'><Text>￥{(item.price / 100).toFixed(2)}</Text></View>
                <View className='number'>
                  {menuNum > 0 ? <Text className='red' onClick={onClick.bind(this, 'decrease', item, menuNum)}>-</Text> : null}
                  {menuNum > 0 ? <Text className='num'>{menuNum}</Text> : null}
                  <Text className='add' onClick={onClick.bind(this, 'add', item, menuNum)}>+</Text>
                </View>
            </View>
          </View>
        </View>
      )
    }) : <Text></Text>
    return cards
  }
}

export default MenuCards

MenuCards.defaultProps={
  cardsList: [],
  menuNum: 0,
  onClick: () => {}
}

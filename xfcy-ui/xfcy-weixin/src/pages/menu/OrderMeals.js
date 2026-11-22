import Taro, { Component } from '@tarojs/taro'
import { connect } from '@tarojs/redux'
import * as actions from './../../actions/menu'
import { View, Text, ScrollView, Image } from '@tarojs/components'
import classNames from 'classnames'
import './index.scss'
import MenuCards from '../../components/MenuCards/MenuCards'

@connect(state => state.menu, { ...actions })
class OrderMeal extends Component {
  constructor () {
    super(...arguments)
    this.state = {
      actInd: 0,
      typeName: ''
    }
    this.getMenu = this.getMenu.bind(this)
    this.changNum = this.changNum.bind(this)
  }
  componentDidMount(){
    this.props.dispatchMenuType({url: 'dish/category'}).then(res => {
      let category = res[0]
      this.getMenu(category.categoryId, category.type)
      this.setState({typeName: category.name})
    })
  }

  /**
   * 获取菜单列表
   * TODO: 滚动分页加载数据
   */
  getMenu (categoryId, type, page = 1, pageSize = 10) {
    this.props.dispatchMenu({url: `dish/dishPageList/${categoryId}/${type}/${page}/${pageSize}`})
  }

  handleClick (value) {
    this.setState({
    actInd: value,
  })
}

  onScroll () {
    console.log(88)
  }

  handleRoll (actInd, categoryId, typeId, typeName) {
    this.getMenu(categoryId, typeId)
    this.setState({actInd: actInd, typeName: typeName})
  }

  changNum(action, item, menuNum) {
    const { shopId, storeId, tableId } = Taro.$store.getState().app.tableInfo
    const dishId = item.dishId
    const type = item.type

    if (action === 'add') {
      if (menuNum > 0) {
        let cart = this.props.cartList.dishList.find(dish => {
          return dish.id === item.dishId
        })
        let dish = {
          tableId: tableId,
          storeId: storeId,
          shopId: shopId,
          dishId: dishId,
          dishName: item.dishName,
          type: type,
          price: item.price,
          dishNumber: 1,
          flavorRemark: item.type === 1 ? cart.flavorRemark : '[' + [].join(',') + ']'
        }
        this.props.dispatchCartAdd({url: 'order/addDish', method: 'POST', params: dish}).then(res => {
          if (!res) {
            Taro.showToast({
              title: '加菜失败',
              icon: 'none',
              duration: 2000
            })
          }
        })
      } else {
        this.props.dispatchDishFlavor({url: `dish/flavor/${dishId}`}).then(res => {
          // TODO: 需要和需求确认，套餐是否有口味
          if (res && res.length && type === 1) {
            this.props.onOpenDishFlavor(dishId)
          } else {
            let dish = {
              tableId: tableId,
              storeId: storeId,
              shopId: shopId,
              dishId: dishId,
              dishName: item.dishName,
              type: type,
              price: item.price,
              dishNumber: 1,
              flavorRemark: '[' + [].join(',') + ']'
            }
            this.props.dispatchCartAdd({url: 'order/addDish', method: 'POST', params: dish}).then(res => {
              if (!res) {
                Taro.showToast({
                  title: '加菜失败',
                  icon: 'none',
                  duration: 2000
                })
              }
            })
          }
        })
      }
    } else {
      this.props.dispatchCartAdd({url: `order/decreaseDish/${tableId}/${dishId}`}).then(res => {
        if (!res) {
          Taro.showToast({
            title: '减菜失败',
            icon: 'none',
            duration: 2000
          })
        }
      })
    }
  }

  handleOpenDishDetail(dishId, type) {
    this.props.onOpenDishDetail(dishId, type)
  }

  render () {
    const { actInd, rollState, typeName } = this.state
    const {
      rollStats,
      menuTypeItem,
      menuItem,
      onOpenDishDetail
    } = this.props

    return (
      <View className='orderMeal' >
        <View className='leftMeal'>
          {menuTypeItem && menuTypeItem.map((item, index) => {
            return <View className={classNames({act: actInd === index}, 'titItems')} key={index} onClick={() => this.handleRoll(index, item.categoryId, item.type, item.name)} > {item.name} </View>
          })}
        </View>
        <View className='ritCont'>
          <View className='topTitle'> {typeName} </View>
          <ScrollView scrollY className='scrollBox' onScroll={this.onScroll.bind()}>
            <MenuCards cardsList={menuItem} onClick={this.changNum} onOpenDishDetail={onOpenDishDetail} />
          </ScrollView>
        </View>
      </View>
    )
  }
}

export default OrderMeal
OrderMeal.defaultProps = {
  rollState: true,
  openMulti: () => {},
  menu:{
    menuTypeItem: [],
    dishType: false,
  }
}

import Taro, { Component } from '@tarojs/taro'
import { View, Text, ScrollView, Image } from '@tarojs/components'
import { AtButton } from 'taro-ui'
import { connect } from '@tarojs/redux'
import * as actions from './../../actions/menu'
import './index.scss'
import ButtonItem from './../../components/ButtonItem/ButtonItem'
import OrderMeal from './OrderMeals'
import './../../assets/img/meunBan.png'
import logoImg from './../../assets/img/logo.png'
import cartImg from './../../assets/img/cart.png'
import menuImg from './../../assets/img/menuDome.png'

@connect(state => state, { ...actions })
class Order extends Component {
  config = {
    navigationBarTitleText: '点餐'
  }

  constructor() {
    super(...arguments)
    this.state = {
      pos: true,
      current: 0,
      store: false,
      popup: false,
      opened: true,
      seatNumber: 1,
      speci: false,
      carts: false,
      menuDet: false,
      setMeal: false,
      popupInfo: false,
      menuDetails: [],
      mealDetails: [],
      flavorDetail: {}
    }
    // TODO: 这个地方看不懂
    this.topHidden = this.topHidden.bind(this)
    this.handleOpenDishFlavor = this.handleOpenDishFlavor.bind(this)
    this.handleOpenDishDetail = this.handleOpenDishDetail.bind(this)
  }

  componentDidMount() {
    const { shopId, storeId, tableId } = this.props.app.tableInfo
    // 获取当前桌台的状态
    this.props.dispatchTableStatus({ url: `table/tableStatus/${shopId}/${storeId}/${tableId}` }).then(res => {
      if (!res.opened) {
        this.setState({ popup: true, opened: false })
      } else {
        this.setState({ popup: false, opened: true })
        // 主动拉取购物车接口数据
        this.props.dispatchMenuCart({ url: `order/shopCart/${tableId}` })
      }
    })
  }

  /**
   * 选择就餐人数
   */
  changeSeatNumber(type) {
    let { seatNumber } = this.state
    if (type === 'add') {
      this.setState({ seatNumber: seatNumber + 1 })
    } else {
      if (seatNumber > 1) {
        this.setState({ seatNumber: seatNumber - 1 })
      }
    }
  }

  /**
   * 开桌
   */
  openCurrentTable() {
    const { tableId } = this.props.app.tableInfo
    let { seatNumber } = this.state

    this.props.dispatchOpenTable({ url: `table/open/${tableId}/${seatNumber}` }).then(res => {
      if (!res) {
        Taro.showToast({
          title: '开桌失败',
          icon: 'none',
          duration: 2000
        })
      }
      else {
        this.setState({ popup: false, opened: true })
      }
    })
  }

  topHidden(event) {
    if (event.detail.scrollTop > 148) {
      this.setState({ pos: true })
    } else {
      this.setState({ pos: false })
    }
  }

  scrollToLower(event) {
    const st = event.detail.scrollTop
    if (st >= 150 && this.state.pos === false) {
      this.setState({ pos: true })
    }
  }

  /**
   * 添加单品到购物车
   */
  addSingleDishCart(item) {
    let dishId = item.dishId

    let cart = Taro.$store.getState().menu.cartList.dishList.find(dish => {
      return dish.id === item.dishId
    })
    if (cart) {
      const { shopId, storeId, tableId } = this.props.app.tableInfo
      let dish = {
        tableId: tableId,
        storeId: storeId,
        shopId: shopId,
        dishId: dishId,
        dishName: item.name,
        type: item.type,
        price: item.price,
        dishNumber: 1,
        flavorRemark: cart.flavorRemark
      }
      this.props.dispatchCartAdd({ url: 'order/addDish', method: 'POST', params: dish }).then(res => {
        if (!res) {
          Taro.showToast({
            title: '添加购物车失败',
            icon: 'none',
            duration: 2000
          })
        } else {
          this.closePopu()
        }
      })
    } else {
      this.props.dispatchDishFlavor({ url: `dish/flavor/${dishId}` }).then(res => {
        if (res && res.length) {
          this.setState({ menuDet: false })
          this.handleOpenDishFlavor(dishId)
        } else {
          const { shopId, storeId, tableId } = this.props.app.tableInfo
          let dish = {
            tableId: tableId,
            storeId: storeId,
            shopId: shopId,
            dishId: dishId,
            dishName: item.name,
            type: item.type,
            price: item.price,
            dishNumber: 1,
            flavorRemark: '[' + [].join(',') + ']'
          }
          this.props.dispatchCartAdd({ url: 'order/addDish', method: 'POST', params: dish }).then(res => {
            if (!res) {
              Taro.showToast({
                title: '添加购物车失败',
                icon: 'none',
                duration: 2000
              })
            } else {
              this.closePopu()
            }
          })
        }
      })
    }
  }

  /**
   * 加菜、减菜
   */
  changNum(action, item) {
    const { shopId, storeId, tableId } = this.props.app.tableInfo
    if (action === 'add') {
      let dish = {
        tableId: tableId,
        storeId: storeId,
        shopId: shopId,
        dishId: item.id,
        dishName: item.name,
        type: item.type,
        price: item.price,
        dishNumber: 1,
        flavorRemark: item.flavorRemark
      }
      this.props.dispatchCartAdd({ url: 'order/addDish', method: 'POST', params: dish })
    } else {
      this.props.dispatchCartAdd({ url: `order/decreaseDish/${tableId}/${item.id}` })
    }
  }

  /**
   * 加菜
   */
  addDish(dish) {
    // const { dishId, type } = dish

    // // 判断当前菜品购物车是否已经存在
    // let cart = this.props.menu.cartList.dishList.find(item => {
    //   return item.id === dishId
    // })
    // let flavorRemark = '[]'

    // if (cart) {
    //   flavorRemark = cart.flavorRemark
    // } else {
    //   flavorRemark = type === 1 ? this.state.menuDetails.flavorRemark : '[]'
    // }
  }

  /**
   * 减菜
   */
  decreaseDish() { }

  /**
   * 添加套餐到购物车
   * TODO: 一期套餐不考虑口味
   */
  addMultiDishCart(item) {
    const { shopId, storeId, tableId } = this.props.app.tableInfo
    let dish = {
      tableId: tableId,
      storeId: storeId,
      shopId: shopId,
      dishId: item.dishId,
      dishName: item.dishName,
      type: item.type,
      price: item.price,
      dishNumber: 1,
      flavorRemark: '[' + [].join(',') + ']'
    }
    this.props.dispatchCartAdd({ url: 'order/addDish', method: 'POST', params: dish }).then(res => {
      if (!res) {
        Taro.showToast({
          title: '添加购物车失败',
          icon: 'none',
          duration: 2000
        })
      } else {
        this.closePopu()
      }
    })
  }

  /**
   * 清空购物车
   */
  cleanCart() {
    const { tableId } = this.props.app.tableInfo
    this.props.dispatchCartClean({ url: `order/cleanShopCart/${tableId}` }).then(res => {
      if (!res) {
        Taro.showToast({
          title: '清空购物车失败',
          icon: 'none',
          duration: 2000
        })
      } else {
        this.cartsHandle()
      }
    })
  }

  /**
   * 展开和收起购物车
   */
  cartsHandle() {
    if (this.state.popup) {
      this.setState({ popup: false, carts: false })
    } else {
      this.setState({ popup: true, carts: true })
    }
  }

  /**
   * 打开单品和套餐详情 
   */
  handleOpenDishDetail(dishId, type) {
    let menu = this.props.menu.menuItem.find(item => {
      return item.dishId === dishId
    })
    // 单品
    if (type === 1) {
      this.setState({ popup: true, popupInfo: true, menuDet: true, menuDetails: menu })
    }
    // 套餐
    else {
      this.props.dispatchMenuMealDet({ url: `dish/setmealDishList/${dishId}` }).then(res => {
        this.setState({
          popup: true, popupInfo: true, setMeal: true, mealDetails: {
            dishId: menu.dishId,
            dishName: menu.dishName,
            type: menu.type,
            price: menu.price,
            remainder: menu.remainder,
            imageUrl: menu.imageUrl,
            desc: menu.desc,
            setmealDishList: res
          }
        })
      })
    }
  }

  // 打开口味规格
  handleOpenDishFlavor(dishId) {
    if (this.state.speci) {
      this.addCartWithFlavorRemark(this.state.flavorDetail)
      return
    }
    // TODO: 此处重复请求口味列表
    let menu = this.props.menu.menuItem.find(item => {
      return item.dishId === dishId
    })
    let flavorDetail = {
      dishId: menu.dishId,
      dishName: menu.dishName,
      type: menu.type,
      price: menu.price,
      remainder: menu.remainder,
      imageUrl: menu.imageUrl,
      desc: menu.desc
    }
    let flavorList = []
    this.props.dispatchDishFlavor({ url: `dish/flavor/${dishId}` }).then(res => {
      res.map(group => {
        let temp = { flavor: group.flavor, flavorData: [] }
        group.flavorData.map(item => {
          temp.flavorData.push({
            flavor: item,
            selected: false
          })
        })
        flavorList.push(temp)
      })
      flavorDetail.flavorList = flavorList
      this.setState({ popup: true, popupInfo: true, speci: true, flavorDetail: flavorDetail })
    })
  }

  /**
   * 添加口味的菜品到购物车
   */
  addCartWithFlavorRemark(menu) {
    const { shopId, storeId, tableId } = this.props.app.tableInfo
    let flavorRemark = []
    menu.flavorList.map(group => {
      group.flavorData.map(item => {
        if (item.selected)
          flavorRemark.push(item.flavor)
      })
    })
    if (!flavorRemark.length) {
      Taro.showToast({
        title: '请选择口味',
        icon: 'none',
        duration: 2000
      })
      return
    }
    let dish = {
      tableId: tableId,
      storeId: storeId,
      shopId: shopId,
      dishId: menu.dishId,
      dishName: menu.dishName,
      type: menu.type,
      price: menu.price,
      dishNumber: 1,
      flavorRemark: '[' + flavorRemark.join(',') + ']'
    }
    this.props.dispatchCartAdd({ url: 'order/addDish', method: 'POST', params: dish }).then(res => {
      if (!res) {
        Taro.showToast({
          title: '添加购物车失败',
          icon: 'none',
          duration: 2000
        })
      } else {
        this.closePopu()
      }
    })
  }

  /**
   * 选择口味
   */
  handleSelectDishFlavor(groupIndex, itemIndex) {
    let flavorDetail = this.state.flavorDetail
    flavorDetail.flavorList[groupIndex].flavorData.map(val => {
      val.selected = false
    })
    flavorDetail.flavorList[groupIndex].flavorData[itemIndex].selected = !flavorDetail.flavorList[groupIndex].flavorData[itemIndex].selected
    this.setState({ flavorDetail: flavorDetail })
  }

  // 关闭弹窗
  closePopu() {
    this.setState({ popup: false, popupInfo: false, speci: false, setMeal: false, menuDet: false, })
  }

  get totalPrice() {
    let totalPrice = 0
    if (!this.props.menu.cartList) {
      return totalPrice
    }
    const dishList = this.props.menu.cartList.dishList
    dishList.map(dish => {
      totalPrice += (dish.price * dish.number)
    })
    return (totalPrice / 100).toFixed(2)
  }

  get totalNumber() {
    let totalNumber = 0
    if (!this.props.menu.cartList) {
      return totalNumber
    }
    const dishList = this.props.menu.cartList.dishList
    dishList.map(dish => {
      totalNumber += dish.number
    })
    return totalNumber
  }

  render() {
    const { current, pos, store, popup, opened, seatNumber, carts, speci, menuDet, setMeal, popupInfo, menuDetails, mealDetails, flavorDetail } = this.state
    const { menu } = this.props

    const cartBox = (<View>{menu.cartList && menu.cartList.dishList && menu.cartList.dishList.length > 0
      ? <View className='shopCart'>
        <View className='carts' onClick={this.cartsHandle.bind(this)}>
          <Image src={cartImg} className='cartImg' />
          <Text className='cartNum'>{this.totalNumber}</Text>
        </View>
        <View className='price' >￥ {this.totalPrice}</View>
        <View className='buts' onClick={() => Taro.navigateTo({ url: '/pages/order/index' })} >去结算</View>
      </View>
      : <View className='shopCart'>
        <View className='cart'>
          <Image src={cartImg} className='cartImg' />
        </View>
        <View className='price' >￥ 0</View>
        <View className='but' >请选购</View>
      </View>}
    </View>)
    // 请选择就餐人数
    const openTable = (<View className='seatNumberPopup seatNumberPopupOpen'>
      <View className='title'>
        <View className='title'> 请选择就餐人数 </View>
      </View>
      {/* TODO: 居中处理 */}
      <View className='content'>
        <View className='item'>
          <View className='number'>
            <Text className='red' onClick={this.changeSeatNumber.bind(this, 'decrease')}>-</Text>
            <Text className='num'>{seatNumber}</Text>
            <Text className='add' onClick={this.changeSeatNumber.bind(this, 'add')}>+</Text>
          </View>
        </View>
        <AtButton type='primary' onClick={this.openCurrentTable.bind(this)}>确定</AtButton>
      </View>
    </View>)
    // 购物车数据展示
    const cartsItem = (<View>
      <View className='title'>
        <View className='title'> 点餐购物车 </View><View className='title' onClick={this.cleanCart.bind(this)}> 清空购物车 </View>
      </View>
      <View className='content'>
        {menu.cartList && menu.cartList.dishList.length > 0 && menu.cartList.dishList.map((item, index) => (
          <View className='item' key={index}>
            <View className='info'>
              <View className='name'>{item.name}</View>
            </View>
            <View className='priceBox'>
              <Text>￥{(item.price / 100).toFixed(2)}</Text>
            </View>
            <View className='number'>
              <Text className='red' onClick={this.changNum.bind(this, 'decrease', item)}>-</Text>
              <Text className='num'>{item.number}</Text>
              <Text className='add' onClick={this.changNum.bind(this, 'add', item)}>+</Text>
            </View>
          </View>)
        )}
      </View>
    </View>)
    // 选规格数据展示
    const specification = (<View className='specification'>
      <Text className='closePopu' onClick={this.closePopu.bind(this)}>X</Text>
      <Image class='menuImg' src={menuImg} />
      <View className='title'> {flavorDetail.dishName} </View>
      <View className='contBox'>
        {flavorDetail.flavorList && flavorDetail.flavorList.map((group, groupIndex) =>
          <View key={groupIndex}>
            <View className='tit'> {group.flavor} </View>
            <View>
              {group.flavorData && group.flavorData.map((item, itemIndex) => <Text key={itemIndex} className={item.selected ? 'lab act' : 'lab'} onClick={this.handleSelectDishFlavor.bind(this, groupIndex, itemIndex)}>{item.flavor}</Text>)}
            </View>
          </View>
        )}
        <View className='bottom'><Text className='pir'>￥ {(flavorDetail.price / 100).toFixed(2)}</Text> <ButtonItem type='solid' value='加入购物车' onClick={this.addCartWithFlavorRemark.bind(this, flavorDetail)} /></View>
      </View>
    </View>)

    // 菜品详情
    const menuDetils = (<View className='menuDetils'>
      <Text className='closePopu' onClick={this.closePopu.bind(this)}>X</Text>
      <Image class='menuImg' src={menuImg} />
      <View className='contBox'>
        <View className='title'>{menuDetails.dishName}</View>
        <View className='detail'>{menuDetails.desc}</View>
        <View className='bottom'><Text className='pir'>￥ {(menuDetails.price / 100).toFixed(2)}</Text> <ButtonItem type='solid' value='加入购物车' onClick={this.addSingleDishCart.bind(this, menuDetails)} /></View>
      </View>
    </View>)
    // 套餐详情
    const setMeals = (<View className='setMeals'>
      <Text className='closePopu' onClick={this.closePopu.bind(this)}>X</Text>
      <View className='title'>{mealDetails.dishName}</View>
      <View className='contBox'>
        {mealDetails.setmealDishList && mealDetails.setmealDishList.map((item, index) => (<View className='items' key={index}>
          <Image class='menuImg' src={menuImg} />
          <View className='itemsInfo'>
            <View className=''>{item.dishName}</View>
            <View className='num'>{item.dishNumber}</View>
            <View className='pir'>￥ {(item.price / 100).toFixed(2)}</View>
          </View>
        </View>))}
        <View className='bottom'><Text className='pir'>￥ {(mealDetails.price / 100).toFixed(2)}</Text> <ButtonItem type='solid' value='加入购物车' onClick={this.addMultiDishCart.bind(this, mealDetails)} /></View>
      </View>
    </View>)

    return (
      <ScrollView onScroll={this.scrollToLower.bind(this)} className='meun'>
        <Image src='./../../assets/img/meunBan.png' className='meunBan' />
        <View className='meunCont'>
          <View className={store ? 'open meunTop' : 'meunTop'}>
            <View className='topInfo'>
              <Image className='logo' src={logoImg} />
              <View className='info'>
                <View>{menu.tableStatus.tableName}</View>
                <View className='tabPostion'>大厅</View>
              </View>
            </View>
          </View>
          <View>
            <OrderMeal data={menu} rollState={pos ? true : false} onOpenDishFlavor={this.handleOpenDishFlavor} onOpenDishDetail={this.handleOpenDishDetail} />
          </View>
        </View>
        {current === 0 ? cartBox : null}
        {popup ? <View className='popup'></View> : ''}
        <View className={carts ? 'cartsPopup cartsPopupOpen' : 'cartsPopup'}>
          {carts && menu.cartList ? cartsItem : null}
        </View>
        <View className={popupInfo ? 'popupBox popupInfo' : 'popupBox'}>
          {speci && flavorDetail ? specification : null}
          {menuDet ? menuDetils : null}
          {setMeal ? setMeals : null}
        </View>
        {!opened ? openTable : null}
      </ScrollView>
    )
  }
}

export default Order

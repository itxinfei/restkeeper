import {
  TABLE_STATUS, OPEN_TABLE, DISH_FLAVOR, MENU, MENU_TYPE, MENU_CART, MENU_MEAL_DET, MENU_CART_ADD, MENU_CART_CLEAN, CART_LIST
} from './../constants/menu'

const INITIAL_STATE = {
  tableStatus: {},
  openTable: false,
  menuItem: [],
  menuTypeItem: [],
  cartAdd: false,
  cartClean: false,
  cartList: {
    dishList: [],
    seatNumber: "",
    tableId: ""
  },
  setMealDet:[]
}

export default function home(state = INITIAL_STATE, action) {
  switch (action.type) {
    case CART_LIST:{
      return {
        ...state,
        cartList: action.cartList
      }
    }
    case TABLE_STATUS:{
      return {
        ...state,
        tableStatus: action.payload
      }
    }
    case OPEN_TABLE:{
      return {
        ...state,
        openTable: action.payload
      }
    }
    case DISH_FLAVOR:{
      return {
        ...state,
        dishFlavor: action.payload
      }
    }
    case MENU:{
      return {
        ...state,
        menuItem: action.payload.items
      }
    }
    case MENU_TYPE:{
      return {
        ...state,
        menuTypeItem: action.payload
      }
    }
    case MENU_CART:{
        return {
          ...state,
          cartList: action.payload
        }
      }
    case MENU_CART_ADD:{
      return {
        ...state,
        cartAdd: action.payload
      }
    }
    case MENU_CART_CLEAN:{
      return {
        ...state,
        cartClean: action.payload
      }
    }
    case MENU_MEAL_DET:{
      return {
        ...state,
        setMealDet: action.payload
      }
    }
    default:
      return {...state}
  }
}

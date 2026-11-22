import { TABLE_INFO } from './../constants/app'

const INITIAL_STATE = {
  tableInfo: {}
}

export default function home(state = INITIAL_STATE, action) {
  switch (action.type) {
    case TABLE_INFO:{
      return {
        ...state,
        tableInfo: action.tableInfo
      }
    }
    default:
      return {...state}
  }
}

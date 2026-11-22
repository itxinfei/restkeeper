<template>
  <div class="addDish">
      <div class="leftCont">
        <div class="tabBut" v-show="seachKey.trim() == ''">
          <span v-for="(item, index) in dishType" :key="index" :class="{act:index == keyInd}" @click="checkTypeHandle(index, item.id)">{{item.categoryName}}</span>
        </div>
        <div class="tabList">
          <div class="table">
            <el-checkbox-group v-model="checkedList" @change="checkedListHandle">
              <div class="items" v-for="(item, index) in dishList" :key="index">
                <el-checkbox :label="item" :key="index">
                  <div class="item">
                    <span style="flex: 3;text-align: left">{{item.name}}</span>
                    <span>{{item.status == 0 ? '停售' : '在售'}}</span>
                    <span>{{Number(item.price)/100}}</span>
                  </div>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </div>
      </div>
      <div class="ritCont">
        <div class="tit">已选菜品()</div>
        <div class="items">
          <div class="item" v-for="(item, ind) in checkedList" :key="ind">
            <span>{{item.dishName}}</span>
            <span class="price">￥ {{Number(item.price)/100}} </span>
            <span class="del" @click="delCheck(ind)">
              <img src="./../../../assets/icons/btn_clean@2x.png" alt="">
            </span>
          </div>
        </div>
      </div>
  </div>
</template>

<script lang="ts">
    import { Component, Prop, Vue, Watch } from 'vue-property-decorator'
    import {getDishTypeList, getDishListType} from '@/api/dish'

    @Component({
        name: 'selectInput'
    })

    export default class extends Vue {
        @Prop({ default: ''}) private value !: number
        @Prop({ default: []}) private checkList !:[]
        @Prop({ default: ''}) private seachKey !:string
        private dishType:[] = []
        private dishList:[] = []
        private dishListCache: [] = []
        private keyInd = 0
        private searchValue:string = ''
        private checkedList:[] = []

        created(){
            this.getDishType()
        }

        @Watch('checkList')
        private checkListChange(value:any){
            this.checkedList = value
        }

        @Watch('seachKey')
        private seachKeyChange(value:any){
            console.log(value.trim())
            if(value.trim()){
                this.getDishForName(this.seachKey)
            }
        }

        // 获取套餐分类
        private getDishType(){
            getDishTypeList({type:2}).then(res => {
                const { data } = res.data
                this.dishType = data
                this.getDishList(data[0].id)
            })
        }

        // 通过套餐ID获取菜品列表分类
        private getDishList(id:number){
            getDishListType({id}).then(res => {
                const { data } = res.data
                let newArr = data.items
                newArr.forEach((n:any) => {
                    n.dishId = n.id
                    n.dishName = n.name
                })
                this.dishList = newArr
            })
        }

        // 关键词收搜菜品列表分类
        private getDishForName(name:number){
            getDishListType({name}).then(res => {
                const { data } = res.data
                let newArr = data.items
                newArr.forEach((n:any) => {
                    n.dishId = n.id
                    n.dishName = n.name
                })
                this.dishList = newArr
            })
        }

        private checkTypeHandle(ind:number,id:any){
            this.keyInd = ind
            this.getDishList(id)
        }

        private checkedListHandle(value:any){
            this.$emit('checkList',this.checkedList)
        }

        open(done:any) {
            this.dishListCache = JSON.parse(JSON.stringify(this.checkList))
        }

        close(done:any) {
            this.checkList = this.dishListCache
        }

        // 删除
        private delCheck(ind:any){
            this.checkedList.splice(ind,1)
        }
    }
</script>
<style lang="scss">
  .addDish{
    .el-checkbox__label{
      width: 100%;
    }
  }
</style>
<style lang="scss" scoped>
  .addDish{
    padding:0 20px;
    display: flex;
    line-height: 40px;
    .leftCont{
      display: flex;
      border-right: solid 2px #E4E7ED;
      width: 60%;
      padding: 15px;
      .tabBut{
        width: 110px;
        span{
          display: block;
          text-align: center;
          border-right: solid 2px #f4f4f4;
          cursor: pointer;
        }
      }
      .act{
        border-color: $mine !important;
      }
      .tabList{
        flex: 1;
        padding: 15px;
        .table{
          border: solid 1px #f4f4f4;
          border-bottom: solid 1px #f4f4f4;
          .items{
            border-bottom: solid 1px #f4f4f4;
            padding: 0 10px;
            display: flex;
            .el-checkbox,.el-checkbox__label{
              width: 100%;
            }
            .item{
              display: flex;
              padding-right: 20px;
              span{
                display: inline-block;
                text-align: center;
                flex: 1;
              }
            }
          }
        }
      }
    }
    .ritCont{
      width: 40%;
      padding: 0 15px;
      .item {
        box-shadow:0px 1px 4px 3px rgba(0,0,0,0.03);
        display: flex;
        text-align: center;
        padding: 0 10px;
        margin-bottom: 20px;
        border-radius: 6px;
        color:#818693;
        span:first-child{
          text-align: left;
          color:#20232A;
        }
        .price{
          display: inline-block;
          flex: 1;
        }
        .del{
          cursor: pointer;
          img{
            position: relative;
            top:5px;
            width: 20px;
          }
        }
      }
    }
  }
</style>

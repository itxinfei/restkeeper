<template>
  <div class="selectInput">
    <el-input type="text" style="width: 100%" v-model="keyValue" @focus="selectFlavor(true)" @blur="outSelect(false)" @input="inputHandle" placeholder="请输入口味"/>
    <div v-if="mak" class="flavorSelect">
      <span v-for="(it, ind) in dishFlavorsData" class="items" :key="ind" @click="checkOption(it,ind)">{{it.flavor}}</span>
      <span class="none" v-if="dishFlavorsData == []">无数据</span>
    </div>
  </div>
</template>

<script lang="ts">
    import { Component, Prop, Vue } from 'vue-property-decorator'

    @Component({
        name: 'selectInput'
    })

    export default class extends Vue {
        @Prop({ default: []}) private dishFlavorsData !: []
        @Prop({ default: ''}) private value !: number
        @Prop({ default: 0}) private index !: number
        private keyValue = ''

        private mak: boolean = false
        // private value: string = ''

        created(){
            this.keyValue = this.value
            console.log(7878,this.dishFlavorsData, this.index)
        }

        private selectFlavor(st:boolean){
            this.mak = st
        }

        private outSelect(st:boolean){
            const _this = this
            setTimeout(function () {
                _this.mak = st
            }, 200)
        }

        private inputHandle(val:any){
            this.selectFlavor(false)
        }

        checkOption(val: any, ind: any){
            this.$emit('select', val.flavor, this.index, ind)
            this.keyValue = val.flavor
        }

    }
</script>

<style lang="scss" scoped>
  .selectInput{
    position: relative;
    width: 100%;
    min-width: 100px;
    .flavorSelect{
      position: absolute;
      width: 100%;
      padding: 0 10px;
      border-radius: 3px;
      border: solid 1px $mine;
      line-height: 30px;
      text-align: center;
      background: #fff;
      top:50px;
      z-index: 99;
      .items{
        cursor: pointer;
        display: inline-block;
        width: 100%;
        line-height: 35px;
        border-bottom:solid 1px #f4f4f4;
        color:#666;
      }
      .none{
         font-size: 14px;

      }
    }
  }
</style>

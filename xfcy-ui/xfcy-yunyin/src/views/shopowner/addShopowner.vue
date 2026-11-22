<template>
  <div class="addBrand-container">
    <HeadLable :title="'添加店长'" :goback="true" />
    <div class="container">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" :inline="true" label-width="180px" class="demo-ruleForm">
        <div>
          <el-form-item label="店长姓名:" prop="storeManagerName">
            <el-input v-model="ruleForm.storeManagerName" placeholder="请输入店长姓名"></el-input>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="店长联系方式:" prop="storeManagerPhone">
            <el-input v-model="ruleForm.storeManagerPhone" placeholder="请输入联系方式"></el-input>
          </el-form-item>
        </div>
        <div class="checkboxCont">
          <div class="title">负责门店:</div>
          <div class="labCont">
            <span :class="{act: active === '全国'}" @click="handleActive('全国')" >全国</span>
            <span v-for="(item,index) in regionData" :key="index" :class="{act: active === item}" @click="handleActive(item)" >{{item}}</span>
          </div>
          <div class="checkCont">
            <el-checkbox-group v-model="checkList">
              <el-checkbox v-for="(item,index) in stopList" :key="index"  :label="item.storeId">{{item.storeName}}</el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        <div class="subBox address">
          <el-form-item>
            <el-button @click="submitForm('ruleForm')">取消</el-button>
            <el-button type="primary" @click="submitForm('ruleForm')">保存</el-button>
            <el-button type="primary" class="continue" @click="submitForm('ruleForm', true)">保存并继续添加门店</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable'
import {addShopOwner,getShopOwnerDetails, editShopOwner, getProvince, getProvinceStopList} from '@/api/brand'

@Component({
  name: 'addShop',
  components: {
    HeadLable
  }
})
export default class extends Vue {
  private regionData = []
  private stopList = []
  private ruleForm =  {
      storeManagerName: '',
      storeManagerPhone: '',
      storeIds:[]
    }

  private checkList:[] = []

  private active:string = '全国'

  get rules() {
    return {
        storeManagerName: [{ required: true, message: '请输入店长姓名', trigger: 'blur' }],
        storeManagerPhone: [{ required: true, message: '请输入店长联系方式', trigger: 'blur' }],
    }
  }

  created(){
    this.init()
    if (this.$route.query.id) {
        this.getShopOwnerDet()
    }
  }

  private init(){
      getProvince().then(res => {
          const { data } = res.data
          this.regionData = data
      })
      this.getRegionStop('')
  }

  // 根据区域获取门店列表
  private getRegionStop (region:any) {
      getProvinceStopList({region}).then(res => {
        const { data } = res.data
        this.stopList = data
      })
  }

  private getShopOwnerDet(){
      getShopOwnerDetails({id: this.$route.query.id}).then(res => {
          const { data } = res.data
          const stores:[] = []
          data.forEach(n => {
              stores.push(n.storeId)
          })
          this.ruleForm = data
          this.ruleForm.stores = stores
      })
  }

  private submitForm(formName:any,st:any) {
     console.log(this.checkList)
     return
    this.$refs[formName].validate((valid:any) => {
      if (valid) {
          if (this.$route.query.id) {
              addShopOwner(this.ruleForm).then(res => {
                  this.$message.success('店铺添加成功！')
                  if (!st) {
                      this.$router.push({path: '/shopowner'})
                  }
              }).catch(err => {
                  this.$message.error('请求出错了：' + err)
              })
          } else {
              editShopOwner(this.ruleForm).then(res => {
                  this.$message.success('店铺添加成功！')
              }).catch(err => {
                  this.$message.error('请求出错了：' + err)
              })
          }
      } else {
        console.log('验证失败，请完善信息后重试!!')
        return false
      }
    })
  }

  private handleActive(state:any) {
    this.active = state
  }
}
</script>
<style lang="scss">
  .addBrand-container{
    .avatar-uploader .el-upload {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
    }
    .avatar-uploader .el-upload:hover {
      border-color: #409EFF;
    }
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 200px;
      height: 160px;
      line-height: 160px;
      text-align: center;
    }
    .avatar {
      width: 160px;
      height: 160px;
      display: block;
    }
    .el-form--inline .el-form-item__content{
      width: 293px;
    }
    .el-select > .el-input{
      width: 293px;
    }
    .address {
      .el-form-item__content{
        width: 777px !important;
      }
    }
  }
</style>
<style lang="scss" scoped>
  .addBrand {
    &-container {
      margin: 30px;
      .container {
        position: relative;
        z-index: 1;
        background: #fff;
        padding: 30px;
        border-radius: 4px;
        min-height: 500px;
        .checkboxCont{
          margin-bottom: 30px;
          .title{
            width: 180px;
            text-align: right;
            font-size: 14px;
            padding-right: 12px;
            color: $gray-2;
          }
          .labCont{
            color: $gray-2;
            margin: 20px 0;
            margin-left: 99px;
            span{
              font-size: 12px;
              cursor: pointer;
              display: inline-block;
              padding: 4px 10px;
              margin: 10px;
              border:solid 1px $gray-4;
              border-radius: 3px;
            }
            .act{
              color: $mine;
              border-color: $mine;
            }
          }
          .checkCont{
            margin-left: 108px;
          }
        }
        .subBox{
          padding-top: 30px;
          text-align: center;
          border-top: solid 1px $gray-5;
        }
      }
    }
  }
</style>

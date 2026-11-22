<template>
  <div class="addBrand-container">
    <HeadLable :title="'添加员工'" :goback="true" />
    <div class="container">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" :inline="false" label-width="180px" class="demo-ruleForm">
          <el-form-item label="企业名称:" prop="enterpriseName">
            <el-input v-model="ruleForm.enterpriseName" placeholder="请输入企业名称"></el-input>
          </el-form-item>
          <el-form-item label="申请人:" prop="applicant">
            <el-input v-model="ruleForm.applicant" placeholder="请输入申请人"></el-input>
          </el-form-item>
          <el-form-item label="手机号:" prop="phone">
            <el-input v-model="ruleForm.phone" placeholder="请输入手机号"></el-input>
          </el-form-item>
          <el-form-item label="地区:" prop="storeName">
            <area-cascader v-model="ruleForm.selected" type="text" :level=0 :data="pca"></area-cascader>
          </el-form-item>
          <el-form-item label="详细地址:" prop="address">
           <el-input type="textarea" style="width:294px;" v-model="ruleForm.address" placeholder="请输入详细地址"></el-input>
          </el-form-item>
          <el-form-item label="账号状态" prop="status">
            <el-select v-model="ruleForm.status" placeholder="请选择账号状态">
              <el-option label="试用" value="0"> </el-option>
              <el-option label="正常" value="1"> </el-option>
              <el-option v-if="ruleForm.status == -1" label="已禁用" value="-1"> </el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="ruleForm.status == 1" label="使用期限" prop="validityDay">
            <el-select v-model="ruleForm.validityDay" placeholder="请选择使用期限">
              <el-option label="7天" value="7"> </el-option>
              <el-option label="一个月" value="30"> </el-option>
              <el-option label="三个月" value="90"> </el-option>
              <el-option label="一年" value="365"> </el-option>
              <el-option label="两年" value="730"> </el-option>
              <el-option label="三年" value="1095"> </el-option>
              <el-option label="四年" value="1460"> </el-option>
              <el-option label="五年" value="1825"> </el-option>
              <el-option label="不限" value="9999999"> </el-option>
            </el-select>
          </el-form-item>
          <div class="subBox address">
            <el-form-item>
              <el-button @click="() => {$router.push('/member')}">取消</el-button>
              <el-button type="primary" @click="submitForm('ruleForm')">保存</el-button>
              <el-button type="primary" v-if="actionType == 'add'" @click="submitForm('ruleForm', 'go')" class="continue">保存并继续添加</el-button>
            </el-form-item>
          </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable'
import {getMemberDetile, addMember, editMember} from '@/api/member'
import {pca, pcaa} from 'area-data'

@Component({
  name: 'addShop',
  components: {
    HeadLable
  }
})
export default class extends Vue {
  private actionType = ''
  private pca = pca
  private ruleForm = {
      enterpriseName: '',
      applicant: '',
      selected: [],
      phone: '',
      address: '',
      status: '',
      validityDay: '',
    }

  get rules() {
    return {
        enterpriseName: [
          { required: true, message: '请输入企业名称', trigger: 'blur' }
        ],
        applicant: [
            { required: true, message: '请输入申请人', trigger: 'blur' }
        ],
        phone: [
            { required: true, message: '请输入手机号', trigger: 'blur' }
        ],
        province: [
            { required: true, message: '请输入员工姓名', trigger: 'blur' }
        ],
        address: [
            { required: true, message: '请输入详细地址', trigger: 'blur' }
        ],
        status: [
            { required: true, message: '请选择账号状态', trigger: 'change' }
        ],
        validityDay: [
            { required: true, message: '请选择使用期限', trigger: 'change' }
        ],
    }
  }
    created() {
        this.actionType = this.$route.query.id ? 'edit' : 'add'
        if (this.$route.query.id) {
            this.init()
        }
    }
  private async init(){
      getMemberDetile({id: this.$route.query.id}).then(res => {
          const { data } = res.data
          let selected = []
          selected.push(data.province)
          if (data.province == data.city){
              selected.push(data.area)
          } else {
              selected.push(data.city)
              selected.push(data.area)
          }
          this.ruleForm = data
          this.ruleForm.selected = selected
          this.ruleForm.status = String(this.ruleForm.status)
          this.ruleForm.validityDay = String(this.ruleForm.validityDay)
      })
  }
  private submitForm(formName:any, st:any) {
    this.$refs[formName].validate((valid:any) => {
      if (valid) {
          let params = {...this.ruleForm}
          params.province = this.ruleForm.selected[0]
          if (this.ruleForm.selected.length == 2) {
              params.city = this.ruleForm.selected[0]
              params.area = this.ruleForm.selected[1]
          } else {
              params.city = this.ruleForm.selected[1]
              params.area = this.ruleForm.selected[2]
          }
          if (this.actionType == 'add') {
              addMember(params).then(res => {
                  this.$message.success('员工添加成功！')
                  if (!st) {
                      this.$router.push({path: '/member'})
                  }
              }).catch(err => {
                  this.$message.error('请求出错了：' + err)
              })
          } else {
              editMember(params).then(res => {
                  this.$message.success('员工信息修改成功！')
                  this.$router.push({path: '/member'})
              }).catch(err => {
                  this.$message.error('请求出错了：' + err)
              })
          }
      } else {
        console.log('error submit!!')
        return false
      }
    })
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
    .area-select{
      height: 40px;
    }
    .large{
      width:294px !important;
    }
    .area-select .area-selected-trigger {
       padding-top: 0px;
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
    .el-input{
      width: 293px;
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
        .subBox{
          padding-top: 30px;
          text-align: center;
          border-top: solid 1px $gray-5;
        }
      }
    }
  }
</style>

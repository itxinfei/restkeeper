<template>
  <div class="addBrand-container">
    <HeadLable :title="'添加品牌'" :goback="true" />
    <div class="container">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" :inline="true" label-width="180px" class="demo-ruleForm">
        <div>
          <el-form-item label="品牌名称:" prop="brandName">
            <el-input v-model="ruleForm.brandName" placeholder="请输入品牌名称"></el-input>
          </el-form-item>
          <el-form-item label="负责人:" prop="contact">
            <el-input v-model="ruleForm.contact" placeholder="请输入负责人"></el-input>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="所属业态" prop="category">
            <el-select v-model="ruleForm.category" placeholder="请选择活动区域">
              <el-option label="甜点饮品" value="甜点饮品"></el-option>
              <el-option label="小吃/快餐" value="小吃/快餐"></el-option>
              <el-option label="中餐" value="中餐"></el-option>
              <el-option label="粥/粉/面" value="粥/粉/面"></el-option>
              <el-option label="火锅" value="火锅"></el-option>
              <el-option label="西餐" value="西餐"></el-option>
              <el-option label="烧烤/烤肉" value="烧烤/烤肉"></el-option>
              <el-option label="咖啡/酒吧" value="咖啡/酒吧"></el-option>
              <el-option label="美食广场（档口）" value="美食广场（档口）"></el-option>
              <el-option label="日韩料理" value="日韩料理"></el-option>
              <el-option label="便利店/零售店" value="便利店/零售店"></el-option>
              <el-option label="麻辣烫" value="麻辣烫"></el-option>
              <el-option label="香锅/烤鱼" value="香锅/烤鱼"></el-option>
              <el-option label="生鲜蔬菜" value="生鲜蔬菜"></el-option>
              <el-option label="海鲜" value="海鲜"></el-option>
              <el-option label="串串香" value="串串香"></el-option>
              <el-option label="饺子馆" value="饺子馆"></el-option>
              <el-option label="自助餐" value="自助餐"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="联系方式:" prop="contactPhone">
            <el-input v-model="ruleForm.contactPhone" placeholder="请输入联系方式"></el-input>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="品牌log:" prop="name">
            <el-upload
              class="avatar-uploader"
              action="http://39.98.133.55:8081/fileUpload"
              :data="imgData"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload">
              <img v-if="imageUrl" :src="imageUrl" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
          </el-form-item>
        </div>
        <div class="subBox">
          <el-form-item>
            <el-button @click="submitForm('ruleForm')">取消</el-button>
            <el-button type="primary" @click="submitForm('ruleForm')">保存</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable'
import { addBrand } from '@/api/brand'

@Component({
  name: 'brand',
  components: {
    HeadLable
  }
})
export default class extends Vue {
  private imgData = {fileName: 'img.png'}
  private ruleForm = {
    brandName: '',
    category: '',
    contact: '',
    contactPhone: '',
    logo: ''
  }

  get imageUrl() {
    return ''
  }

  get rules() {
    return {
      brandName: [
        { required: true, message: '请输入品牌名称', trigger: 'blur' }
      ],
      contact: [
        { required: true, message: '请输入负责人', trigger: 'blur' }
      ],
      category: [
        { required: true, message: '请选择所属业态', trigger: 'change' }
      ],
      contactPhone: [
        { required: true, message: '请输入负责人', trigger: 'blur' }
      ]
    }
  }

  private submitForm(formName:any) {
    this.$refs[formName].validate(async (valid:any) => {
      if (valid) {
        console.log(valid, this.ruleForm)
        const data = await addBrand({ ...this.ruleForm })
        console.log(data)
      } else {
        console.log('error submit!!')
        return false
      }
    })
  }

  private handleAvatarSuccess(file:any) {
    console.log(file)
  }

  private beforeAvatarUpload(file:any) {
    console.log(file)
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

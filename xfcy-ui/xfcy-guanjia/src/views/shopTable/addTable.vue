<template>
  <div class="addTable-container">
    <HeadLable
      :title="'新建桌台'"
      :goback="true"
    />
    <div class="container">
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :rules="rules"
        :inline="false"
        label-width="180px"
        class="demo-ruleForm"
      >
        <el-form-item
          label="所属区域"
          prop="region"
        >
          <el-select
            v-model="ruleForm.region"
            placeholder="请选择菜品分类"
          >
            <el-option
              label="区域一"
              value="shanghai"
            />
            <el-option
              label="区域二"
              value="beijing"
            />
          </el-select>
          <el-button
            type="primary"
            class="continue"
            style="margin-left: 10px;"
            @click="() => dialogVisible = true"
          >
            +添加区域
          </el-button>
        </el-form-item>
        <el-form-item
          label="桌台名称:"
          prop="name"
        >
          <el-input
            v-model="ruleForm.name"
            placeholder="请填写桌台名称"
          />
        </el-form-item>
        <el-form-item
          label="座位数:"
          prop="name"
        >
          <el-input
            v-model="ruleForm.name"
            type="number"
            placeholder="请输入1-99"
          />
        </el-form-item>
        <div class="subBox address">
          <el-form-item>
            <el-button @click="submitForm('ruleForm')">
              取消
            </el-button>
            <el-button
              type="primary"
              @click="submitForm('ruleForm')"
            >
              保存
            </el-button>
            <el-button
              type="primary"
              class="continue"
              @click="submitForm('ruleForm')"
            >
              保存并继续添加门店
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <el-dialog
      title="新建区域"
      :visible.sync="dialogVisible"
      width="50%"
      :before-close="handleClose"
    >
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :rules="rules"
        :inline="false"
        label-width="180px"
        class="demo-ruleForm"
      >
        <el-form-item
          label="区域名称:"
          prop="name"
        >
          <el-input
            v-model="ruleForm.name"
            placeholder="请填写桌台名称"
          />
        </el-form-item>
      </el-form>
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click="dialogVisible = false"
        >保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';

@Component({
    'name': 'addShop',
    'components': {
        HeadLable
    }
})

export default class extends Vue {
    private dialogVisible = false
    private textarea:string = ''
    private value:string = ''
    get ruleForm() {
        return {
            'name': '',
            'region': '',
            'date1': '',
            'date2': '',
            'delivery': false,
            'type': [],
            'resource': '',
            'desc': ''
        };
    }

    get imageUrl() {
        return '';
    }

    get rules() {
        return {
            'name': [
                { 'required': true, 'message': '请输入活动名称', 'trigger': 'blur' },
                { 'min': 3, 'max': 5, 'message': '长度在 3 到 5 个字符', 'trigger': 'blur' }
            ]
        };
    }

    private handleClose() {

    }

    private submitForm(formName:any) {
        this.$refs[formName].validate((valid:any) => {
            if (valid) {
                alert('submit!');
            } else {
                console.log('error submit!!');
                return false;
            }
        });
    }

    private handleAvatarSuccess(file:any) {

    }

    private beforeAvatarUpload(file:any) {

    }
}
</script>
<style lang="scss">
  .el-dialog__header{
    background: $gray-4;
    border-radius: 4px;
  }
  .addTable-container{
    .avatar-uploader .el-upload {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
    }
    .el-dialog__body{
      padding-bottom: 0;
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
    .address {
      .el-form-item__content{
        width: 777px !important;
      }
    }
  }
</style>
<style lang="scss" scoped>
  .addTable {
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
          position: absolute;
          bottom: 0;
          padding-top: 30px;
          text-align: center;
          border-top: solid 1px $gray-5;
        }
      }
    }
  }
</style>

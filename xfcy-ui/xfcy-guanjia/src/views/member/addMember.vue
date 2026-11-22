<template>
  <div class="addBrand-container">
    <HeadLable
      :title="'添加员工'"
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
        <!--          <el-form-item label="员工职级" prop="region">-->
        <!--            <el-select v-model="ruleForm.region" placeholder="请选择品牌名称">-->
        <!--              <el-option label="区域一" value="shanghai"></el-option>-->
        <!--              <el-option label="区域二" value="beijing"></el-option>-->
        <!--            </el-select>-->
        <!--            <el-button @click="submitForm('ruleForm')" type="primary" class="continue" style="margin-left: 10px;" >+新增职级</el-button>-->
        <!--          </el-form-item>-->
        <el-form-item
          label="员工姓名:"
          prop="staffName"
        >
          <el-input
            v-model="ruleForm.staffName"
            placeholder="请输入员工姓名"
          />
        </el-form-item>
        <el-form-item
          label="密码:"
          prop="password"
        >
          <el-input
            v-model="ruleForm.password"
            type="password"
            autocomplete="off"
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item
          label="确认密码:"
          prop="rePassword"
        >
          <el-input
            v-model="ruleForm.rePassword"
            type="password"
            autocomplete="off"
            placeholder="请输入确认密码"
          />
        </el-form-item>
        <el-form-item
          label="手机号:"
          prop="phone"
        >
          <el-input
            v-model="ruleForm.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item
          label="性别"
          prop="sex"
        >
          <el-radio-group v-model="ruleForm.sex">
            <el-radio label="男" />
            <el-radio label="女" />
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="身份证号"
          prop="idNumber"
        >
          <el-input
            v-model="ruleForm.idNumber"
            placeholder="请输入身份证号"
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
              v-if="actionType == 'add'"
              type="primary"
              class="continue"
              @click="submitForm('ruleForm', 'go')"
            >
              保存并继续添加
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import {getMemberDetile, addMember, editMember} from '@/api/member';

@Component({
    'name': 'addShop',
    'components': {
        HeadLable
    }
})
export default class extends Vue {
  private actionType = ''
  private ruleForm = {
      'staffName': '',
      'phone': '',
      'password': '',
      'rePassword': '',
      'sex': '男',
      'idNumber': ''
  }
  private validateRepassword(rule, value, callback) {
      if (value === '') {
          callback(new Error('请再次输入密码'));
      } else if (value !== this.ruleForm.password) {
          callback(new Error('两次输入密码不一致!'));
      } else {
          callback();
      }
  }

  get rules() {
      return {
          'staffName': [{ 'required': true, 'message': '请输入员工姓名', 'trigger': 'blur' }],
          'password': [
              { 'required': true, 'message': '请输入密码', 'trigger': 'blur' }
          ],
          'rePassword': [
              { 'required': true, 'message': '请确认密码', 'trigger': 'blur' },
              { 'validator': this.validateRepassword, 'trigger': 'blur' }
          ],
          'phone': [{ 'required': true, 'message': '请输入手机号', 'trigger': 'blur' }],
          'idNumber': [{ 'required': true, 'message': '请输输入身份证号', 'trigger': 'blur' }]
      };
  }
  created() {
      this.actionType = this.$route.query.id ? 'edit' : 'add';
      if (this.$route.query.id) {
          this.init();
      }
  }
  private async init(){
      getMemberDetile({'id': this.$route.query.id}).then(res => {
          const { data } = res.data;
          this.ruleForm = data;
          this.ruleForm.rePassword = data.password;
      });
  }
  private submitForm(formName:any, st:any) {
      this.$refs[formName].validate((valid:any) => {
          if (valid) {
              if (this.actionType == 'add') {
                  addMember(this.ruleForm).then(res => {
                      this.$message.success('员工添加成功！');
                      if (!st) {
                          this.$router.push({'path': '/shopAdmin/member'});
                      }
                  }).catch(err => {
                      this.$message.error('请求出错了：' + err);
                  });
              } else {
                  editMember(this.ruleForm).then(res => {
                      this.$message.success('员工信息修改成功！');
                      this.$router.push({'path': '/shopAdmin/member'});
                  }).catch(err => {
                      this.$message.error('请求出错了：' + err);
                  });
              }
          } else {
              console.log('error submit!!');
              return false;
          }
      });
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

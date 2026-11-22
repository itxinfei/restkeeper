<template>
  <div class="login-container">
    <div class="header">
      <div class="container">
        <img src="./../../assets/logo.png" width="153.6" alt=""> <span>|</span> 管家系统
      </div>
    </div>
    <div class="content">
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        autocomplete="on"
        label-position="left"
      >
        <div class="formBox">
          <div class="title-container"><img src="./../../assets/logo.png" width="153.6" alt=""></div>
          <el-form-item prop="loginName">
            <el-input ref="loginName" v-model="loginForm.loginName" name="username" type="text" autocomplete="on" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item prop="loginPass">
            <el-input :key="passwordType" ref="loginPass" v-model="loginForm.loginPass" :type="passwordType" placeholder="请输入密码" name="password" autocomplete="on" @keyup.enter.native="handleLogin" />
            <span class="show-pwd" @click="showPwd" >
          <svg-icon :name="passwordType === 'password' ? 'eye-off' : 'eye-on'" />
        </span>
          </el-form-item>

          <el-button
            :loading="loading"
            type="primary"
            style="width:100%; margin-bottom:30px;"
            @click.native.prevent="handleLogin"
          >
            登录
          </el-button>
        </div>
      </el-form>
    </div>
    <div class="footer">
      Copyright @ 2019 传智播客教育集团 京ICP备08015045 All Rights Reserved
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import { Route } from 'vue-router'
import { Form as ElForm, Input } from 'element-ui'
import { UserModule } from '@/store/modules/user'
import { isValidUsername } from '@/utils/validate'

@Component({
  name: 'Login'
})
export default class extends Vue {
  private validateUsername = (rule: any, value: string, callback: Function) => {
    if (!isValidUsername(value)) {
      callback(new Error('请输入用户名'))
    } else {
      callback()
    }
  }
  private validatePassword = (rule: any, value: string, callback: Function) => {
    if (value.length < 5) {
      callback(new Error('密码必须在6位以上'))
    } else {
      callback()
    }
  }
  private loginForm = {
      loginName: 'admin',
      loginPass: 'admin',
}
private loginRules = {
    loginName: [{ validator: this.validateUsername, trigger: 'blur' }],
    loginPass: [{ validator: this.validatePassword, trigger: 'blur' }]
}
private passwordType = 'password'
private loading = false
private showDialog = false
private redirect?: string

  @Watch('$route', { immediate: true })
  private onRouteChange(route: Route) {

  }

  mounted() {
    if (this.loginForm.loginName === '') {
      (this.$refs.loginName as Input).focus()
    } else if (this.loginForm.loginPass === '') {
      (this.$refs.loginPass as Input).focus()
    }
  }

  private showPwd() {
    if (this.passwordType === 'password') {
      this.passwordType = ''
    } else {
      this.passwordType = 'password'
    }
    this.$nextTick(() => {
      (this.$refs.loginPass as Input).focus()
    })
  }
  // 登录
  private handleLogin() {
    (this.$refs.loginForm as ElForm).validate(async(valid: boolean) => {
      if (valid) {
        this.loading = true
        await UserModule.Login(this.loginForm).then(res => {
            this.$router.push({ path: '/' })
            // this.$router.push({ path: this.redirect || '/' })
        }).catch(err => {
            this.$message.error('用户名或密码错误！')
            this.loading = false
        })

        // Just to simulate the time of the request
        setTimeout(() => {
          this.loading = false
        }, 0.5 * 1000)
      } else {
          console.log(999999)
        return false
      }
    })
  }
}
</script>

<style lang="scss">
// References: https://www.zhangxinxu.com/wordpress/2018/01/css-caret-color-first-line/
@supports (-webkit-mask: none) and (not (cater-color: $loginCursorColor)) {
  .login-container .el-input {
    input { color: $loginCursorColor; }
    input::first-line { color: $lightGray; }
  }
}

.login-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      height: 47px;
      background: #fff;
      border: 0px;
      border-radius: 3px;
      padding: 12px 5px 12px 15px;
      color: #999;
      caret-color: #999;
      -webkit-appearance: none;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px #fff inset !important;
        -webkit-text-fill-color: #999 !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
.login-container {
  height: 100%;
  width: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: $loginBg;
  .header{
    background: #fff;
    padding: 20px 0;
    .container{
      width: 1200px;
      margin: 0 auto;
      line-height: 52px;
      font-size: 28px;
      img{
        float: left;
      }
      span{
        display: inline-block;
        padding: 0 20px;
        color:#ccc;
      }
    }
  }
  .footer{
    background: #fff;
    color: #818693;
    text-align: center;
    font-size: 14px;
    line-height: 100px;
  }
  .content{
    flex:1;
    background: url("./../../assets/img_denglu_bj.png") top center no-repeat;
    background-size: cover;
  }
  .login-form {
    position: relative;
    max-width: 100%;
    top:50%;
    transform: translateY(-50%);
    overflow: hidden;
    .formBox{
      position: relative;
      background: #fff;
      border-radius: 16px;
      padding: 40px;
      left: 75%;
      transform: translateX(-50%);
      width: 450px;
    }
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .title-container {
    position: relative;
    margin: 20px;
    text-align: center;
    span{
      flex: 1;
      cursor:pointer;
      margin: 30px 0 40px 0;
      border-right: solid 1px #ccc;
    }
    span:last-child{
      border-right: none;
    }
    .act{
      color: $mine;
    }
  }
  .passwordChange{
   text-align: right;
   cursor: pointer;
  }
  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $darkGray;
    cursor: pointer;
    user-select: none;
  }
}
</style>

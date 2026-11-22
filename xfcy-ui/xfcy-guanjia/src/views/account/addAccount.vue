<template>
  <div class="addBrand-container">
    <HeadLable
      :title="'添加挂账'"
      :goback="true"
    />
    <div class="container">
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :rules="rules"
        :inline="true"
        label-width="180px"
        class="demo-ruleForm"
      >
        <div>
          <el-form-item
            label="挂账类型"
            prop="creditType"
          >
            <el-select
              v-model="ruleForm.creditType"
              placeholder="请选择挂账类型"
            >
              <el-option
                label="公司"
                value="1"
              />
              <el-option
                label="个人"
                value="2"
              />
            </el-select>
          </el-form-item>
        </div>
        <div v-if="ruleForm.creditType == '1'">
          <el-form-item
            label="挂账公司:"
            prop="companyName"
          >
            <el-input
              v-model="ruleForm.companyName"
              placeholder="请选择公司名称"
            />
          </el-form-item>
        </div>
        <div v-if="ruleForm.creditType == '2'">
          <el-form-item
            label="挂账人:"
            prop="companyName"
          >
            <el-input
              v-model="ruleForm.userName"
              placeholder="请填写挂载人"
            />
          </el-form-item>
        </div>
        <div v-if="ruleForm.creditType == '1'">
          <el-form-item
            label="挂账人:"
            prop="name"
          >
            <div class="accountBox">
              <div class="account">
                <div class="title">
                  <span class="name">姓名</span><span>联系电话</span>
                </div>
                <div class="cont">
                  <div
                    v-for="(item, index) in ruleForm.users"
                    :key="index"
                    class="items"
                  >
                    <div class="itTit">
                      <el-input
                        v-model="item.userName"
                        type="text"
                        style="width: 140px;"
                        placeholder="请输入姓名"
                      />
                    </div>
                    <div class="labItems">
                      <el-input
                        v-model="item.phone"
                        type="text"
                        style="width: 100%"
                        placeholder="请输入联系电话"
                      />
                    </div>
                    <span
                      class="delFlavor"
                      @click="delAccountMember(index)"
                    >删除</span>
                  </div>
                </div>
                <span
                  class="addBut"
                  @click="addAccountMember"
                > + 添加</span>
              </div>
            </div>
          </el-form-item>
        </div>
        <div v-if="ruleForm.creditType == '2'">
          <el-form-item
            label="联系电话:"
            prop="name"
          >
            <el-input
              v-model="ruleForm.phone"
              placeholder="请填写联系电话"
            />
          </el-form-item>
        </div>
        <div class="subBox address">
          <el-form-item>
            <el-button @click="() => $router.push('/shopAdmin/accounts')">
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
              @click="submitForm('ruleForm', 'goAnd')"
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
import { getAccountDet, addAccount, putAccount } from '@/api/account';

@Component({
    'name': 'addShop',
    'components': {
        HeadLable
    }
})
export default class extends Vue {
    private actionType = 'add'
    private value:string = ''
    private ruleForm = {
        'creditType': '2',
        'companyName': '',
        'userName':'',
        'creditAmount': '',
        'phone': '',
        'users': [
            {
                'userName':'',
                'phone': ''
            }
        ]
    }

    get rules() {
        return {
            'creditType': [
                { 'required': true, 'message': '请选择挂账类型', 'trigger': 'blur' }
            ]
        };
    }
    created() {
        this.actionType = this.$route.query.id ? 'edit' : 'add';
        if (this.actionType == 'edit') {
            this.init();
        }
    }

    private async init(){
        getAccountDet({'id': this.$route.query.id}).then(res => {
            const { data } = res.data;
            this.ruleForm = data;
            this.ruleForm.creditType = String(data.creditType);
            console.log(this.ruleForm);
        });
    }

    // 添加挂账人
    private addAccountMember(){
        this.ruleForm.users.push({'userName': '', 'phone': ''});
    }

    // 删除挂账人
    private delAccountMember(ind:number){
        this.ruleForm.users.splice(ind, 1);
    }

    private submitForm(formName:any,st:any) {
        this.$refs[formName].validate((valid:any) => {
            if (valid) {
                if (this.actionType == 'add') {
                    addAccount(this.ruleForm).then(res => {
                        this.$message.success('挂账人添加成功！');
                        if (!st) {
                            this.$router.push({'path': '/shopAdmin/accounts'});
                        }
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err);
                    });
                } else {
                    putAccount(this.ruleForm).then(res => {
                        this.$message.success('挂账人修改成功！');
                        if (!st) {
                            this.$router.push({'path': '/shopAdmin/accounts'});
                        }
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err);
                    });
                }
                console.log(this.ruleForm);
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
        .subBox{
          padding-top: 30px;
          text-align: center;
          border-top: solid 1px $gray-5;
        }
      }
      .accountBox{
        min-width: 500px;
        .addBut{
          background: $blue;
          display: inline-block;
          padding: 0px 15px;
          border-radius: 3px;
          line-height: 30px;
          color: #fff;
          cursor: pointer;
        }
        .account{
          border: solid 1px #dfe2e8;
          border-radius: 3px;
          padding: 15px;
          background: #fafafb;
          .title{
            color: #606168;
            .name{
              display: inline-block;
              padding-left: 5px;
              width: 170px;
            }
          }
          .cont{
            .items{
              display: flex;
              margin: 10px 0;
              .itTit{
                width: 150px;
                margin-right: 15px;
                input{
                  width: 100%;
                  line-height: 40px;
                  border:solid 1px #ccc;
                  border-radius: 3px;
                  padding: 0 10px;
                }
              }
              .labItems{
                flex: 1;
                border-radius: 3px;
                span{
                  display: inline-block;
                  color:#f19c59;
                  margin: 5px;
                  line-height: 30px;
                  height: 30px;
                  padding: 0 10px;
                  border-radius: 3px;
                  i{
                    cursor: pointer;
                    font-style: normal;
                  }
                }
              }
              .delFlavor{
                display: inline-block;
                padding: 0 10px;
                color:#f19c59;
                cursor: pointer;
              }
            }
          }
        }

      }
    }
  }
</style>

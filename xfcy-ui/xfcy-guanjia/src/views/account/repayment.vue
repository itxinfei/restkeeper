<template>
  <div class="addBrand-container">
    <HeadLable
      :title="'还款'"
      :goback="true"
    />
    <div class="container">
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :inline="true"
        label-width="180px"
        class="demo-ruleForm"
      >
        <div
          class="formDataTop"
          style="display: flex"
        >
          <div><span>挂账人类型：</span>{{ ruleForm.creditType == '1' ? '公司' : '个人' }}</div>
          <div v-if="ruleForm.creditType == '2'">
            <span>挂账人： </span>{{ ruleForm.userName }}
          </div>
          <div v-if="ruleForm.creditType == '1'">
            <span>挂账公司：</span>{{ ruleForm.userName }}
          </div>
          <div><span>欠款金额：</span>{{ ruleForm.creditAmount/100 }}</div>
        </div>
        <div style="margin-bottom: 20px;">
          <el-form-item
            label="还款方式"
            prop="region"
          >
            <el-select
              v-model="ruleForm.payType"
              placeholder="请选择还款方式"
            >
              <el-option
                label="现金"
                value="1"
              />
              <el-option
                label="微信"
                value="2"
              />
              <el-option
                label="支付宝"
                value="3"
              />
              <el-option
                label="银行卡"
                value="4"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label="还款金额:"
            prop="name"
          >
            <el-input
              v-model="ruleForm.repaymentAmount"
              placeholder="请填写还款金额"
            />
          </el-form-item>
        </div>
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
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import { getAccountDet, repayment } from '@/api/account';

    @Component({
        'name': 'addShop',
        'components': {
            HeadLable
        }
    })
export default class extends Vue {
        private textarea:string = ''
        private value:string = ''
        private ruleForm = {
            'creditType': '2',
            'userName': '',
            'creditAmount': '',
            'payType': '1',
            'repaymentAmount': 10000,
            'memberList': [
                {
                    'name':'',
                    'phone': ''
                }
            ]
        }

        created() {
            this.init();
        }

        private async init(){
            getAccountDet({'id': this.$route.query.id}).then(res => {
                const { data } = res.data;
                const ruleForm = {...data,'repaymentAmount':data.creditAmount/100, 'payType':'1'};
                this.ruleForm = ruleForm;
            });
        }

        private submitForm(formName:any) {
            this.$refs[formName].validate((valid:any) => {
                if (valid) {
                    const params = this.ruleForm;
                    params.repaymentAmount = this.ruleForm.repaymentAmount * 100;
                    console.log(params);
                    repayment(params).then(res => {
                        this.$message.success('还款成功！');
                        this.$router.push({'path': '/shopAdmin/accounts'});
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err);
                    });
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
      .formDataTop{
        display: flex;
        div{
          flex: 1;
          text-align: center;
          line-height: 50px;
          margin-bottom: 50px;
          font-size: 14px;
          color: $gray-2;
          span{
            color: #333;
          }
        }
      }
    }
  }
</style>

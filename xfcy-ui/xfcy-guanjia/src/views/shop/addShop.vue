<template>
  <div class="addBrand-container">
    <HeadLable
      :title="'添加门店'"
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
            label="门店名称:"
            prop="storeName"
          >
            <el-input
              v-model="ruleForm.storeName"
              placeholder="请输入门店名称"
            />
          </el-form-item>
          <el-form-item
            label="所属品牌:"
            prop="brandId"
          >
            <el-select
              v-model="ruleForm.brandId"
              placeholder="请选择品牌名称"
            >
              <el-option
                v-for="item in brandList"
                :label="item.brandName"
                :value="item.brandId"
              />
            </el-select>
          </el-form-item>
        </div>
        <div>
          <el-form-item
            label="地区:"
            prop="selected"
          >
            <area-cascader
              v-model="ruleForm.selected"
              type="text"
              :level="0"
              :data="pca"
            />
          </el-form-item>
        </div>
        <div class="address">
          <el-form-item
            label="详细地址:"
            prop="address"
          >
            <el-input
              v-model="ruleForm.address"
              type="textarea"
              :rows="3"
              placeholder="建议您如实填写详细地址，例如街道名称，门牌号，楼层和房间号码等"
            />
          </el-form-item>
        </div>
        <div>
          <el-form-item
            label="门店联系人:"
            prop="contact"
          >
            <el-input
              v-model="ruleForm.contact"
              placeholder="请输入门店联系人姓名"
            />
          </el-form-item>
        </div>
        <div>
          <el-form-item
            label="门店电话:"
            prop="contactPhone"
          >
            <el-input
              v-model="ruleForm.contactPhone"
              placeholder="请输入门店电话"
            />
          </el-form-item>
        </div>
        <div class="subBox address">
          <el-form-item>
            <el-button @click="() => $router.push('/shop')">
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
import {Component, Vue} from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import { getBrandList, getShopDetails, addShop, editShop } from '@/api/brand';
import {pca, pcaa} from 'area-data';

    @Component({
        'name': 'addShop',
        'components': {
            HeadLable
        }
    })
export default class extends Vue {
        private actionType = 'add'
        private brandList = {}
        private pca = pca
        private ruleForm = {
            'storeName': '',
            'selected': {},
            'address': '',
            'brandId': '',
            'contact': '',
            'contactPhone': ''
        }

        created() {
            this.getBrandList();
            this.actionType = this.$route.query.id ? 'edit' : 'add';
            if (this.$route.query.id) {
                this.init();
            }
        }

        get rules() {
            return {
                'storeName': [
                    {'required': true, 'message': '请输入门店名称', 'trigger': 'blur'}
                ],
                'brandId': [
                    {'required': true, 'message': '请选择品牌', 'trigger': 'blur'}
                ],
                'selected': [
                    {'required': true, 'message': '请输入地区', 'trigger': 'blur'}
                ],
                'address': [
                    {'required': true, 'message': '请输入详细地址', 'trigger': 'blur'}
                ],
                'contact': [
                    {'required': true, 'message': '请输入门店联系人', 'trigger': 'blur'}
                ],
                'contactPhone': [
                    {'required': true, 'message': '请输入门店电话', 'trigger': 'blur'}
                    // { type: 'number', message: '电话号必须为数字'}
                ]
            };
        }
        private async init(){
            getShopDetails({'id': this.$route.query.id}).then(res => {
                const { data } = res.data;
                data.selected = [];
                if (data.city == data.province){
                    data.selected[0] = data.province;
                    data.selected[1] = data.area;
                } else {
                    data.selected[0] = data.province;
                    data.selected[1] = data.city;
                    data.selected[2] = data.area;
                }
                data.brandId = data.brand.brandId;
                this.ruleForm = data;
            });
        }
        private async getBrandList() {
            const {data} = await getBrandList({'page': 1, 'pageSize': 1000});
            this.brandList = data.data.items;
        }

        private submitForm(formName: any, st:any) {
            this.$refs[formName].validate((valid: any) => {
                if (valid) {
                    const data = {...this.ruleForm};
                    data.area = data.selected[1];
                    data.city = data.selected[0];
                    data.province = data.selected[0];
                    if (this.actionType == 'add') {
                        addShop(data).then(res => {
                            this.$message.success('门店添加成功！');
                            if (!st) {
                                this.$router.push({'path': '/shop'});
                            }
                        }).catch(err => {
                            this.$message.error('请求出错了：' + err);
                        });
                    } else {
                        editShop(data).then(res => {
                            this.$message.success('门店修改成功！');
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
  .addBrand-container {
    .area-select.large {
      width: 292px;
      height: 40px;
    }

    .area-select .area-selected-trigger {
      padding-top: 0px;
    }

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

    .el-form--inline .el-form-item__content {
      width: 293px;
    }

    .el-select > .el-input {
      width: 293px;
    }

    .address {
      .el-form-item__content {
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

        .subBox {
          padding-top: 30px;
          text-align: center;
          border-top: solid 1px $gray-5;
        }
      }
    }
  }
</style>

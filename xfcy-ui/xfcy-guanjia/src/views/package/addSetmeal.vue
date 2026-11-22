<template>
  <div class="addBrand-container">
    <HeadLable
      :title="'套餐管理'"
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
            label="套餐名称:"
            prop="name"
          >
            <el-input
              v-model="ruleForm.name"
              placeholder="请填写套餐名称"
            />
          </el-form-item>
          <el-form-item
            label="套餐分类"
            prop="categoryId"
          >
            <el-select
              v-model="ruleForm.categoryId"
              placeholder="请选择套餐分类"
            >
              <el-option
                v-for="(item, index) in setMealList"
                :label="item.name"
                :value="item.category_id"
              />
            </el-select>
          </el-form-item>
        </div>
        <div>
          <el-form-item
            label="套餐价格:"
            prop="price"
          >
            <el-input
              v-model="ruleForm.price"
              placeholder="请设置套餐价格"
            />
          </el-form-item>
        </div>
        <div>
          <el-form-item label="套餐菜品:">
            <el-form-item>
              <div class="addDish">
                <span
                  v-if="dishTable.length == 0"
                  class="addBut"
                  @click="openAddDish"
                > + 添加菜品</span>
                <div
                  v-if="dishTable.length != 0"
                  class="content"
                >
                  <div
                    class="addBut"
                    style="margin-bottom: 20px;"
                    @click="openAddDish"
                  >
                    + 添加菜品
                  </div>
                  <div class="table">
                    <el-table
                      :data="dishTable"
                      style="width: 100%"
                    >
                      <el-table-column
                        prop="dishName"
                        label="名称"
                        width="180"
                        align="center"
                      />
                      <el-table-column
                        prop="price"
                        label="原价"
                        width="180"
                      >
                        <template slot-scope="scope">
                          {{ Number(scope.row.price) / 100 }}
                        </template>
                      </el-table-column>
                      <el-table-column
                        prop="address"
                        label="份数"
                        align="center"
                      >
                        <template slot-scope="scope">
                          <el-input-number
                            v-model="scope.row.copies"
                            size="small"
                            :min="1"
                            :max="99"
                            label="描述文字"
                            @change=""
                          />
                        </template>
                      </el-table-column>
                      <el-table-column
                        prop="address"
                        label="操作"
                        width="180px;"
                        align="center"
                      >
                        <template slot-scope="scope">
                          <el-button
                            type="text"
                            size="small"
                            @click="delDishHandle(scope.$index)"
                          >
                            删除
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="套餐图片">
            <el-upload
              class="avatar-uploader"
              action="http://39.98.133.55:8081/fileUpload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <img
                v-if="imageUrl"
                :src="imageUrl"
                class="avatar"
              >
              <i
                v-else
                class="el-icon-plus avatar-uploader-icon"
              />
            </el-upload>
          </el-form-item>
        </div>
        <el-form-item
          label="商品码:"
          prop="code"
        >
          <el-input
            v-model="ruleForm.code"
            placeholder="请输入商品码"
          />
        </el-form-item>
        <div class="address">
          <el-form-item label="菜品描述">
            <el-input
              v-model="ruleForm.description"
              type="textarea"
              :rows="3"
              placeholder="菜品描述，最长200字"
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
            <el-button
              v-if="actionType == 'add'"
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
      title="添加菜品"
      class="addDishList"
      :visible.sync="dialogVisible"
      width="60%"
      :before-close="handleClose"
    >
      <el-input
        v-model="value"
        class="seachDish"
        placeholder="请输入菜品名称进行搜索"
        style="width: 250px"
        size="small"
        clearable
      >
        <i
          slot="prefix"
          class="el-input__icon el-icon-search"
          style="cursor: pointer"
          @click="seachHandle"
        />
      </el-input>
      <AddDish
        ref="adddish"
        :check-list="checkList"
        :seach-key="seachKey"
        @checkList="getCheckList"
      />
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button @click="handleClose">取 消</el-button>
        <el-button
          type="primary"
          @click="addTableList"
        >确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import HeadLable from '@/components/HeadLable';
import AddDish from './components/AddDish';
import {getSetMealDel, addSetMeal, editSetMeal} from '@/api/setMeal';
import {getDishTypeList, getDishListType} from '@/api/dish';

@Component({
    'name': 'addShop',
    'components': {
        HeadLable,
        AddDish
    }
})
export default class extends Vue {
    private value:string = ''
    private setMealList:[] = []
    private seachKey: string= ''
    private dishList:[] = []
    private imageUrl:string = ''
    private actionType:string = ''
    private dishTable:[] = []
    private dialogVisible:boolean = false
    private checkList:[] = []
    private ruleForm = {
        'name': '',
        'categoryId':'',
        'price':'',
        'code': '',
        'image':'',
        'description': '',
        'dishList': [],
        'status': true
    }

    get rules() {
        return {
            'name': { 'required': true, 'message': '请输入套餐名称', 'trigger': 'blur' },
            'region':{ 'required': true, 'message': '请选择套餐分类', 'trigger': 'blur' },
            'price':{ 'required': true, 'message': '请输入套餐价格', 'trigger': 'blur' },
            'code':{ 'required': true, 'message': '请输入商品码', 'trigger': 'blur' }
        };
    }

    created() {
        this.getDishTypeList();
        this.actionType = this.$route.query.id ? 'edit' : 'add';
        if (this.actionType == 'edit') {
            this.init();
        }
    }

    private async init(){
        getSetMealDel({'id': this.$route.query.id}).then(res => {
            const { data } = res.data;
            this.ruleForm = data;
            this.ruleForm.status = data.status == '1';
            this.ruleForm.price = data.price/100;
            this.imageUrl = data.image;
            this.checkList = data.dishList;
            this.dishTable = data.dishList;
        });
    }
    private seachHandle(){
        this.seachKey = this.value;
    }
    // 获取套餐分类
    private getDishTypeList(){
        getDishTypeList({'type':2,'page':1,'pageSize':1000}).then(res => {
            const { data } = res.data;
            this.setMealList = data;
        });
    }

    // 通过套餐ID获取菜品列表分类
    private getDishList(id:number){
        getDishListType({id}).then(res => {
            const { data } = res.data;
            this.dishList = data;
        });
    }

    // 删除套餐菜品
    delDishHandle(index:any){
        this.dishTable.splice(index,1);
        this.checkList.splice(index,1);
    }

    // 获取添加菜品数据
    private getCheckList(value:any){
        this.checkList = [...value];
    }

    openAddDish(){
        this.seachKey = '';
        this.dialogVisible = true;
        //     this.$refs.adddish.open()
    }
    // 取消添加菜品
    handleClose(done:any) {
        // this.$refs.adddish.close()
        this.dialogVisible = false;
        // this.checkList = JSON.parse(JSON.stringify(this.dishTable))
        // this.dialogVisible = false
    }

    // 保存添加菜品列表
    private addTableList(){
        this.dishTable = JSON.parse(JSON.stringify(this.checkList));
        this.dishTable.forEach((n:any) => {
            n.copies = 1;
        });
        this.dialogVisible = false;
    }

    private submitForm(formName:any) {
        this.$refs[formName].validate((valid:any, st:any) => {
            if (valid) {
                let prams = {...this.ruleForm};
                prams.price *= 100;
                prams.dishList = this.dishTable;
                prams.status = this.ruleForm ? 1 : 0;
                if (this.actionType == 'add') {
                    addSetMeal(prams).then(res => {
                        this.$message.success('套餐添加成功！');
                        if (!st) {
                            this.$router.push({'path': '/setmeal'});
                        }
                    }).catch(err => {
                        this.$message.error('请求出错了：' + err);
                    });
                } else {
                    editSetMeal(prams).then(res => {
                        this.$message.success('套餐修改成功！');
                        if (!st) {
                            this.$router.push({'path': '/setmeal'});
                        }
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

    private handleAvatarSuccess(response:any, file:any, fileList:any) {
        this.imageUrl = response.data;
        this.ruleForm.image = response.data;
    }

    private beforeAvatarUpload(file:any) {
        console.log(file);
    }
}
</script>
<style>
  .avatar-uploader .el-icon-plus:after{
    position: absolute;
    display: inline-block;
    content: ' ' !important;
    left: calc(50% - 20px);
    top: calc(50% - 40px);
    width: 40px;
    height: 40px;
    background: url("./../../assets/icons/icon_upload@2x.png") center center no-repeat;
    background-size: 20px;
  }
</style>
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
    .addDish{
      .el-input{
        width: 130px;
      }
      .el-input-number__increase{
        border-left:solid 1px #FFE1CA;
        background:rgba(255,243,234,1);
      }
      .el-input-number__decrease{
        border-right:solid 1px #FFE1CA;
        background:rgba(255,243,234,1);
      }
      input{
        border:1px solid rgba(255,225,202,1);
      }
      .table{
        border: solid 1px #EBEEF5;
        border-radius: 3px;
        th{
           padding: 5px 0;
        }
        td{
          padding: 7px 0;
        }
      }
    }
    .addDishList{
      .seachDish{
          position: absolute;
          top:10px;
          right: 20px;
      }
      .el-dialog__body{
        padding: 0;
        border-bottom: solid 1px #ccc;
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
        .addDish {
          width: 777px;
          .addBut {
            background: $blue;
            display: inline-block;
            padding: 0px 15px;
            border-radius: 3px;
            line-height: 30px;
            color: #fff;
            cursor: pointer;
          }
          .content{
            background: #fafafb;
            padding: 20px;
            border: solid 1px #ccc;
            border-radius: 3px;
          }
        }
      }
    }
  }
</style>

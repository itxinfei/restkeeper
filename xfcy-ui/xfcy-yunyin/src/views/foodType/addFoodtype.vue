<template>
  <div class="addBrand-container">
    <HeadLable :title="'添加菜品'" :goback="true"/>
    <div class="container">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" :inline="true" label-width="180px" class="demo-ruleForm">
        <div>
          <el-form-item label="菜品名称:" prop="name">
            <el-input v-model="ruleForm.name" placeholder="请填写菜品名称"></el-input>
          </el-form-item>
          <el-form-item label="菜品分类" prop="region">
            <el-select v-model="ruleForm.categoryId" placeholder="请选择菜品分类">
              <el-option v-for="(item,index) in dishList" :label="item.categoryName" :value="item.categoryId"></el-option>
            </el-select>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="菜品价格:" prop="name">
            <el-input v-model="ruleForm.price" placeholder="请设置菜品价格"></el-input>
          </el-form-item>
          <el-form-item label="商品码:" prop="name">
            <el-input v-model="ruleForm.code" placeholder="请输入商品码"></el-input>
          </el-form-item>
        </div>
        <el-form-item label="口味做法配置:">
          <el-form-item>
            <div class="flavorBox">
              <span class="addBut" v-if="dishFlavors.length == 0" @click="addFlavore"> + 添加口味</span>
              <div class="flavor" v-if="dishFlavors.length != 0">
                <div class="title"><span>口味名（3个字内）</span><span>口味标签（输入标签回车添加）</span></div>
                <div class="cont">
                  <div class="items" v-for="(item, index) in dishFlavors" :key="index">
                    <div class="itTit">
                      <SelectInput v-on:select="selectHandle" :dishFlavorsData="dishFlavorsData" :index="index" :value="item.flavor"></SelectInput>
                    </div>
                    <div class="labItems" style="display: flex">
                      <span v-for="(it, ind) in item.flavorData" :key="ind">{{it}} <i @click="delFlavorLabel(index, ind)">X</i></span>
                      <div class="inputBox" :style="inputStyle" contenteditable="true" @focus="flavorPosition(index)" @keydown.enter="keyDownHandle"></div>
                    </div>
                    <span class="delFlavor" @click="delFlavor(index)">删除</span>
                  </div>
                </div>
                <div class="addBut" @click="addFlavore">添加口味</div>
              </div>
            </div>
          </el-form-item>
        </el-form-item>
        <div>
          <el-form-item label="店内售卖状态:" prop="name">
            <el-switch v-model="ruleForm.status" inactive-color="#ccc"></el-switch>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="菜品图片" prop="region">
            <el-upload
              class="avatar-uploader"
              action="http://39.98.133.55:8081/fileUpload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload">
              <img v-if="imageUrl" :src="imageUrl" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
          </el-form-item>
        </div>
        <div class="address">
          <el-form-item label="菜品描述" prop="region">
            <el-input type="textarea" :rows="3" placeholder="菜品描述，最长200字" v-model="ruleForm.description"></el-input>
          </el-form-item>
        </div>
        <div class="subBox address">
          <el-form-item>
            <el-button @click="submitForm('ruleForm')">取消</el-button>
            <el-button type="primary" @click="submitForm('ruleForm')">保存</el-button>
            <el-button v-if="actionType == 'add'" type="primary" @click="submitForm('ruleForm')" class="continue">保存并继续添加门店</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
    import {Component, Vue} from 'vue-property-decorator'
    import HeadLable from '@/components/HeadLable'
    import SelectInput from './components/SelectInput.vue'
    import {getDishDetails, addDish, editDish, getFlavorList, getDishTypeList} from '@/api/dish'

    @Component({
        name: 'addShop',
        components: {
            HeadLable,
            SelectInput
        }
    })
    export default class extends Vue {
        private textarea: string = ''
        private value: string = ''
        private imageUrl: string = ''
        private actionType: string = ''
        private dishList: [] = []
        private dishFlavorsData: [] = []
        private dishFlavors: [] = []
        private index = 0
        private inputStyle = {flex:1}
        private ruleForm = {
            name: '',
            categoryId: '',
            price: '',
            code: '',
            image: '',
            description: '',
            dishFlavors: [],
            status: true,
        }

        get rules() {
            return {
                name: [
                    {required: true, message: '请输入活动名称', trigger: 'blur'},
                    {min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur'}
                ]
            }
        }

        created() {
            this.getDishList()
            this.getFlavorListHand()
            this.actionType = this.$route.query.id ? 'edit' : 'add'
            if (this.$route.query.id) {
                this.init()
            }
        }

        private selectHandle(val:any, key:any, ind:any){
            const arrDate = [...this.dishFlavors]
            arrDate[key] = JSON.parse(JSON.stringify(this.dishFlavorsData[ind]))
            this.dishFlavors = arrDate
        }

        private async init() {
            getDishDetails({id: this.$route.query.id}).then(res => {
                const {data} = res.data
                this.ruleForm = data
                this.ruleForm.price = data.price/100
                this.ruleForm.status = data.status == '1' ? true : false
                this.dishFlavors = data.dishFlavors
                this.imageUrl = data.image
            })
        }

        // 按钮 - 添加口味
        private addFlavore() {
            this.dishFlavors.push({flavor: '', flavorData: []}) // JSON.parse(JSON.stringify(this.dishFlavorsData))
        }

        // 按钮 - 删除口味
        private delFlavor(ind: number) {
            this.dishFlavors.splice(ind, 1)
        }

        // 按钮 - 删除口味标签
        private delFlavorLabel(index: number, ind: number) {
            this.dishFlavors[index].flavorData.splice(ind, 1)
        }

        //口味位置记录
        private flavorPosition(index: number) {
            this.index = index
        }

        // 添加口味标签
        private keyDownHandle(val: any) {
            if (event) {
                event.cancelBubble=true;
                event.preventDefault();
                event.stopPropagation();
            }

            if (val.target.innerText.trim() != "") {
                this.dishFlavors[this.index].flavorData.push(val.target.innerText)
                val.target.innerText = ''
            }
        }

        // 获取菜品分类
        private getDishList() {
            getDishTypeList({type: 1}).then(res => {
                const {data} = res.data
                this.dishList = data
            })
        }

        // 获取口味列表
        private getFlavorListHand() {
            getFlavorList('').then(res => {
                const {data} = res.data
                this.dishFlavorsData = data
            })
        }

        private submitForm(formName: any, st: any) {
            this.$refs[formName].validate((valid: any) => {
                if (valid) {
                    let params:any = {...this.ruleForm}
                    params.dishFlavors = this.dishFlavors
                    params.price *= 100
                    if (this.actionType == 'add') {
                        addDish(params).then(res => {
                            this.$message.success('菜品添加成功！')
                            if (!st) {
                                this.$router.push({path: '/fond'})
                            }
                        }).catch(err => {
                            this.$message.error('请求出错了：' + err)
                        })
                    } else {
                        editDish(params).then(res => {
                            this.$message.success('菜品添加成功！')
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

        private handleAvatarSuccess(response: any, file: any, fileList: any) {
            this.imageUrl = response.data
            this.ruleForm.image = response.data
        }

        private beforeAvatarUpload(file: any) {
            console.log(file)
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
  .addBrand-container {
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

    .el-input {
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

  .flavorBox {
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

    .flavor {
      border: solid 1px #dfe2e8;
      border-radius: 3px;
      padding: 15px;
      background: #fafafb;

      .title {
        color: #606168;
      }

      .cont {
        .items {
          display: flex;
          margin: 10px 0;

          .itTit {
            width: 150px;
            margin-right: 15px;

            input {
              width: 100%;
              line-height: 40px;
              border-radius: 3px;
              padding: 0 10px;
            }
          }

          .labItems {
            flex: 1;
            display: flex;
            flex-wrap: wrap;
            border-radius: 3px;
            min-height: 39px;
            border: solid 1px #D8DDE3;
            background: #fff;
            padding: 0 5px;
            span {
              display: inline-block;
              color: #f19c59;
              margin: 5px;
              line-height: 26px;
              height: 26px;
              padding: 0 10px;
              background: #fdf4eb;
              border-radius: 3px;
              border: solid 1px #fae2cd;

              i {
                cursor: pointer;
                font-style: normal;
              }
            }
            .inputBox{
              display: inline-block;
              width: 100%;
              height: 36px;
              line-height:36px;
              overflow: hidden;
            }
          }

          .delFlavor {
            display: inline-block;
            padding: 0 10px;
            color: #f19c59;
            cursor: pointer;
          }
        }
      }
    }
  }
</style>

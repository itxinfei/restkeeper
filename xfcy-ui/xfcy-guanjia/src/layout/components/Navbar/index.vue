<template>
  <div class="navbar">
    <!--    <breadcrumb-->
    <!--      id="breadcrumb-container"-->
    <!--      class="breadcrumb-container"-->
    <!--    />-->
    <div class="right-menu">
      <div v-if="roles == 'shop'">
        当前店铺：
        <el-select
          v-model="storeId"
          placeholder="请选择"
          style="margin-right:10px;"
          size="mini"
          @change="changHandle"
        >
          <el-option
            v-for="item in userInfo.stores"
            :key="item.storeId"
            :label="item.storeName"
            :value="item.storeId"
          />
        </el-select>
        店长：
      </div>
      <div class="avatar-wrapper">
        {{ name }}
      </div>
      <img
        src="./../../../assets/icons/btn_close@2x.png"
        class="outLogin"
        alt="退出"
        @click="logout"
      >
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { AppModule } from '@/store/modules/app';
import { UserModule } from '@/store/modules/user';
import Breadcrumb from '@/components/Breadcrumb/index.vue';
import Hamburger from '@/components/Hamburger/index.vue';
import {changStore} from '@/api/shopSet';

@Component({
    'name': 'Navbar',
    'components': {
        Breadcrumb,
        Hamburger
    }
})

export default class extends Vue {

  private storeId = this.getStoreId

  get sidebar() {
      return AppModule.sidebar;
  }

  get device() {
      return AppModule.device.toString();
  }

  get userInfo() {
      return UserModule.userInfo;
  }

  get name() {
      return UserModule.name;
  }

  get roles() {
      return UserModule.roles;
  }

  get getStoreId() {
      const storeId = UserModule.storeId || UserModule.userInfo.stores[0].storeId;
      return storeId;
  }

  private toggleSideBar() {
      AppModule.ToggleSideBar(false);
  }

  private changHandle(){
      changStore({'storeId':this.storeId}).then(res => {
          UserModule.changeStore(res.data);
          if (this.$route.path == '/shopAdmin/about') {
              this.$router.go(0);
          } else {
              this.$router.push('/shopAdmin/about');
          }
      }).catch(err => {
          this.$message.error('请求出错了：' + err);
      });
  }

  private async logout() {
      await UserModule.LogOut();
      this.$router.push(`/login?redirect=${this.$route.fullPath}`);
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    padding: 0 15px;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .right-menu {
    float: right;
    display: flex;
    margin-right: 15px;
    height: 100%;
    line-height: 50px;
    color:$mine;
    img{
      margin-top: 14px;
      margin-left: 10px;
      width: 25px;
      height: 25px;
    }
    .outLogin{
      cursor: pointer;
    }
    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>

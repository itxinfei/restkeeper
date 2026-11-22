<template>
  <div class="dashboard-container">
    <div class="container">
      店铺概况
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { UserModule } from '@/store/modules/user'

@Component({
  name: 'shopAbout'
})
export default class extends Vue {
  get name() {
    return UserModule.name
  }

  get roles() {
    return UserModule.roles
  }
}
</script>

<style lang="scss" scoped>
  .dashboard {
    &-container {
      margin: 30px;
      .container{
        display: flex;
        flex-wrap: wrap;
      }
    }
  }
</style>

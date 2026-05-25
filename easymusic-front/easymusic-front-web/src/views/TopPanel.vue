<template>
  <div class="top-panel">
    <div class="iconfont icon-list" @click="showDrawerHandler"></div>
    <div class="title">EasyMusic</div>
  </div>

  <el-drawer v-model="showDrawer" direction="ltr" header-class="left-side-header" body-class="left-side-body"
    size="200px">
    <LeftSide></LeftSide>
  </el-drawer>
</template>

<script setup>
import LeftSide from './LeftSide.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const showDrawer = ref(false)
const showDrawerHandler = () => {
  showDrawer.value = true
}
</script>

<style lang="scss" scoped>
.top-panel {
  display: none;
}
@media (max-width: 500px) {
  .top-panel {
    display: flex;
    background: #262626;
    border-radius: 5px;
    position: fixed;
    top: 10px;
    left: 10px;
    right: 10px;
    display: flex;
    align-items: center;
    padding: 12px;
    z-index: 10;
    .icon-list {
      color: #fff;
      font-size: 25px;
    }
    .title {
      color: #fff;
      margin-left: 15px;
    }
  }
}
</style>

<template>
  <div class="part-title">
    <BackBtn></BackBtn>
    <div class="title">最新发布</div>
  </div>
  <div class="data-list">
    <LatestList></LatestList>
  </div>
</template>

<script setup>
import LatestList from './LatestList.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
</script>

<style lang="scss" scoped>
.part-title {
  font-size: 25px;
  color: #fff;
  display: flex;
  align-items: center;
  .title {
    margin-left: 5px;
  }
}
.data-list {
  margin-top: 15px;
  height: calc(100vh - 130px);
}

@media (max-width: 500px) {
  .data-list {
    padding: 0px 0px 0px 10px;
    height: calc(100vh - 200px);
  }
  .part-title {
    margin-top: 10px;
  }
}
</style>

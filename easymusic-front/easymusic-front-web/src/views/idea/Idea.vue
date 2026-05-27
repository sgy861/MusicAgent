<template>
  <div class="create-body">
    <div class="create-panel">
      <Create></Create>
    </div>
    <div class="music-panel">
      <MusicList></MusicList>
    </div>
  </div>
</template>

<script setup>
import MusicList from './MusicList.vue'
import Create from './Create.vue'
import Player from '@/component/common/Player.vue'
import { ref, reactive, getCurrentInstance, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
</script>

<style lang="scss" scoped>
.create-body {
  display: flex;
  .create-panel {
    width: 600px;
    background: #091e25;
    padding: 10px;
    border-right: 1px solid rgba(0, 245, 212, 0.15);
  }
  .music-panel {
    flex: 1;
    width: 0;
    height: calc(100vh - 70px);
  }
}
</style>

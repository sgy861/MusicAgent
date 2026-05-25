<template>
  <BackBtn></BackBtn>
  <div class="user-info-panel">
    <Avatar :avatar="userInfo.avatar" :width="100"></Avatar>
    <div class="user-info">
      <div class="nick-name">{{ userInfo.nickName }}</div>
      <div class="music-count">音乐：{{ userInfo.musicCount }}</div>
      <div class="good-count">获赞：{{ userInfo.goodCount }}</div>
    </div>
  </div>

  <div class="data-list">
    <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadMusicList">
      <template #default="{ data, index }">
        <MusicItem :data="data" @reload="reloadMusic" @playList="playList"></MusicItem>
      </template>
    </DataLoadMoreList>
  </div>
</template>

<script setup>
import MusicItem from '@/component/biz/MusicItem.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useMusicPlayStore } from '@/stores/musicPlay.js'
const musicPlayStore = useMusicPlayStore()

const userInfo = ref({})
const getUserInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getUserInfo,
    params: {
      userId: route.params.userId,
    },
  })
  if (!result) {
    return
  }
  userInfo.value = result.data
}

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])
const loadMusicList = async () => {
  if (
    Object.keys(dataSource.value).length > 0 &&
    dataSource.value.pageNo == dataSource.value.pageTotal
  ) {
    return
  }
  loading.value = true
  let pageNo = dataSource.value.pageNo || 0
  pageNo++
  let result = await proxy.Request({
    url: proxy.Api.loadUserMusic,
    showLoading: false,
    params: {
      pageNo,
      userId: route.params.userId,
    },
  })
  loading.value = false
  if (!result) {
    return
  }
  if (result.data.pageNo == 1) {
    dataList.value = result.data.list
  } else {
    dataList.value = dataList.value.concat(result.data.list)
  }
  result.data.list = dataList.value
  dataSource.value = result.data
}

const playList = () => {
  musicPlayStore.savePlayList(dataSource.value.list)
}

const reloadMusic = () => {
  dataSource.value.pageNo = 0
  loadMusicList()
}

onMounted(() => {
  getUserInfo()
})
</script>

<style lang="scss" scoped>
.user-info-panel {
  margin: 10px;
  display: flex;
  color: #fff;
  .user-info {
    margin-left: 10px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    .music-count,
    .good-count {
      margin-top: 5px;
      font-size: 14px;
      color: var(--text);
    }
  }
}
.data-list {
  height: calc(100vh - 230px);
}

@media (max-width: 500px) {
  .back-btn {
    margin-top: 10px;
  }
  .data-list {
    height: 200px;
  }
}
</style>

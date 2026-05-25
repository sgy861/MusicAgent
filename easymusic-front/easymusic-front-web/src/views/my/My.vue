<template>
  <div class="top-tab">
    <Switch :data="[
        { label: '我的作品', value: 0 },
        { label: '我喜欢的作品', value: 1 },
      ]" v-model="tabType" @change="changeTab"></Switch>
    <div class="iconfont icon-refresh" @click="loadMusicList(true)"></div>
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
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useMusicPlayStore } from '@/stores/musicPlay.js'
const musicPlayStore = useMusicPlayStore()

const tabType = ref(0)

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])
const loadMusicList = async (refresh) => {
  if (
    Object.keys(dataSource.value).length > 0 &&
    dataSource.value.pageNo == dataSource.value.pageTotal &&
    !refresh
  ) {
    return
  }
  loading.value = true
  let pageNo = dataSource.value.pageNo || 0
  pageNo++
  let result = await proxy.Request({
    url: proxy.Api.loadMyMusic,
    showLoading: false,
    params: {
      pageNo,
      queryLikeMusic: tabType.value == 1,
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

const changeTab = () => {
  dataSource.value.pageNo = 0
  loadMusicList()
}

const playList = () => {
  musicPlayStore.savePlayList(dataSource.value.list)
}

const reloadMusic = () => {
  dataSource.value.pageNo = 0
  loadMusicList()
}
</script>

<style lang="scss" scoped>
.top-tab {
  position: fixed;
  top: 0px;
  z-index: 2;
  display: flex;
  align-items: center;
  width: 100%;
  background: #0e0123;
  .icon-refresh {
    color: #fff;
    margin-left: 20px;
    cursor: pointer;
    &:hover {
      color: var(--purple);
    }
  }
}
.data-list {
  margin-top: 50px;
  height: calc(100vh - 125px);
}

@media (max-width: 500px) {
  .top-tab {
    top: 51px;
    left: 10px;
  }
  .data-list {
    height: calc(100vh - 175px);
  }
}
</style>

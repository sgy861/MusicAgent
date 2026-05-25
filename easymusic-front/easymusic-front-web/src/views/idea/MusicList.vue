<template>
  <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadMusicList">
    <template #default="{ data, index }">
      <MusicItem :data="data" @reload="reloadMusic" @playList="playList"></MusicItem>
    </template>
  </DataLoadMoreList>
</template>

<script setup>
import MusicItem from '@/component/biz/MusicItem.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { mitter } from '@/eventbus/eventBus.js'
import { useMusicPlayStore } from '@/stores/musicPlay.js'
const musicPlayStore = useMusicPlayStore()

import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()

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
    url: proxy.Api.loadMyMusic,
    showLoading: false,
    params: {
      pageNo,
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

  //第一页获取未完成的音乐
  if (pageNo == 1) {
    creatingMusicIds.value = dataSource.value.list
      .filter((item) => {
        return item.musicStatus == 0
      })
      .map((item) => {
        return item.musicId
      })
  }
}

const playList = () => {
  musicPlayStore.savePlayList(dataSource.value.list)
}

const creatingMusicIds = ref([])
const loadCreatingMusic = async () => {
  if (creatingMusicIds.value.length == 0) {
    return
  }
  let result = await proxy.Request({
    url: proxy.Api.loadCreatingMusic,
    showLoading: false,
    params: {
      musicIds: creatingMusicIds.value.join(','),
    },
  })
  if (!result) {
    return
  }
  if (result.data.length == 0) {
    return
  }
  result.data.forEach((newData) => {
    const musicIndex = dataSource.value.list.findIndex((item) => {
      return item.musicId == newData.musicId
    })
    if (musicIndex !== -1 && newData.musicStatus !== 0) {
      dataSource.value.list.splice(musicIndex, 1, newData)
    } else if (musicIndex == -1) {
      dataSource.value.list.unshift(newData)
    }
    //已经处理完成的音乐移除
    if (newData.musicStatus == 0) {
      return
    }
    const index = creatingMusicIds.value.findIndex((item) => {
      return item === newData.musicId
    })
    if (index !== -1) {
      creatingMusicIds.value.splice(index, 1)
    }
    //重新加载积分，生成失败会退还积分
    userInfoStore.updateLastReloadTime()
  })
}

const reloadMusic = () => {
  dataSource.value.pageNo = 0
  loadMusicList()
}

const timer = ref()
const initTimer = () => {
  timer.value = setInterval(() => {
    loadCreatingMusic()
  }, 5000)
}

onMounted(() => {
  initTimer()
  mitter.on('newMusic', (musicIdList) => {
    creatingMusicIds.value = [...musicIdList, ...creatingMusicIds.value]
    loadCreatingMusic()
  })
})

onUnmounted(() => {
  clearInterval(timer.value)
  mitter.off('newMusic')
})
</script>

<style lang="scss" scoped>
</style>

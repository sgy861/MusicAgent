<template>
  <div class="player-panel">
    <el-tooltip effect="dark" content="上一首" placement="top">
      <div class="iconfont icon-pre" @click="playNext(-1)"></div>
    </el-tooltip>
    <el-tooltip effect="dark" :content="isPlaying ? '暂停' : '播放'" placement="top">
      <div :class="[
          'iconfont',
          'play-btn',
          isPlaying ? 'icon-pause' : 'icon-play',
        ]" @click="togglePlay"></div>
    </el-tooltip>
    <el-tooltip effect="dark" content="下一首" placement="top">
      <div class="iconfont icon-next" @click="playNext(1)"></div>
    </el-tooltip>
    <div class="time">{{ proxy.Utils.seconds2Min(currentTime) || "00" }}</div>
    <div class="percent">
      <el-slider v-model="progressPercent" :show-tooltip="false" @change="seekAudio" @input="draggingHandler" />
    </div>
    <div class="total-time">
      {{ proxy.Utils.seconds2Min(duration) || "00" }}
    </div>
    <!--播放模式-->
    <div :class="[
        'play-mode iconfont',
        musicPlayStore.playModeRepeat ? 'icon-repeat' : 'icon-loop',
      ]" @click="changeMode"></div>
    <el-popover placement="top" :show-arrow="false" :width="34" popper-class="volume-popover">
      <template #reference>
        <div class="iconfont icon-audio"></div>
      </template>
      <el-slider v-model="volume" vertical height="100px" />
    </el-popover>
  </div>
  <audio v-if="audioPath" ref="audioPlayerRef" class="audio" :volume="volume / 100"
    :src="proxy.Api.getResource + audioPath" @loadedmetadata="onLoadedMetadataHandler" @timeupdate="onTimeUpdateHandler"
    @ended="playEndedHandler"></audio>
</template>

<script setup>
import { useMusicPlayStore } from '@/stores/musicPlay.js'
const musicPlayStore = useMusicPlayStore()

import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  watch,
  onUnmounted,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { mitter } from '@/eventbus/eventBus.js'

//音量 默认50；
const volume = ref(50)
//原生播放器
const audioPlayerRef = ref()
const isPlaying = ref(false)
//当前时间
const currentTime = ref(0)
//总时间
const duration = ref(0)
//播放进度
const progressPercent = ref(0)

const isDragging = ref(false)
const draggingHandler = () => {
  isDragging.value = true
}
// 设置进度
const seekAudio = (e) => {
  isDragging.value = false
  const seekTime = (e / 100) * duration.value
  audioPlayerRef.value.currentTime = seekTime
}

// 时间更新事件
const onTimeUpdateHandler = async () => {
  currentTime.value = audioPlayerRef.value.currentTime
  duration.value = audioPlayerRef.value.duration
  if (isDragging.value) {
    return
  }
  progressPercent.value = (currentTime.value / duration.value) * 100

  //更新store 播放时间
  musicPlayStore.updatePlayTime(currentTime.value)
}

//播放完成
const playEndedHandler = () => {
  isPlaying.value = false
  setPlayStatus()

  //播放完成根据模式循环，或者播放下一曲
  if (musicPlayStore.playModeRepeat) {
    audioPlayerRef.value.currentTime = 0
    togglePlay()
  } else {
    playNext(1)
  }
}
//媒体加载
const onLoadedMetadataHandler = async () => {
  duration.value = audioPlayerRef.value.duration
  try {
    await audioPlayerRef.value.play()
    isPlaying.value = true
    setPlayStatus()
  } catch (error) {
    if (error.name === 'NotAllowedError') {
      proxy.Alert({
        message: '由于您的浏览器设置，音乐无法自动播放，请手动点击播放。',
        btnText: '开始播放',
        okfun: () => {
          audioPlayerRef.value.play()
          isPlaying.value = true
          setPlayStatus()
        },
      })
    }
  }
}

//暂停播放
const togglePlay = () => {
  if (!audioPath.value) {
    return
  }
  if (isPlaying.value) {
    audioPlayerRef.value.pause()
  } else {
    audioPlayerRef.value.play()
  }
  isPlaying.value = !isPlaying.value

  setPlayStatus()
}

//更新store 播放状态
const setPlayStatus = () => {
  musicPlayStore.setPlayStatus(isPlaying.value)
}

//下一首
const playNext = (type) => {
  const currentIndex = musicPlayStore.playList.findIndex((item) => {
    return item.musicId == musicPlayStore.currentMusic?.musicId
  })
  if (currentIndex == -1) {
    return
  }
  const newIndex = currentIndex + type
  if (
    newIndex == -1 ||
    newIndex < 0 ||
    newIndex >= musicPlayStore.playList.length
  ) {
    return
  }
  musicPlayStore.play(musicPlayStore.playList[newIndex])
}

//修改模式
const changeMode = () => {
  musicPlayStore.playModeRepeat = !musicPlayStore.playModeRepeat
}

onMounted(() => {
  mitter.on('togglePlay', togglePlay)
})
onUnmounted(() => {
  mitter.off('togglePlay')
})

const updatePlayCount = async () => {
  let result = await proxy.Request({
    url: proxy.Api.updatePlayCount,
    showLoading: false,
    params: {
      musicId: musicPlayStore.currentMusic.musicId,
    },
  })
  if (!result) {
    return
  }
}

const audioPath = ref()
watch(
  () => musicPlayStore.updateTime,
  (newVal, oldVal) => {
    if (!newVal) {
      return
    }
    if (audioPath.value !== musicPlayStore.currentMusic.audioPath) {
      audioPath.value = musicPlayStore.currentMusic.audioPath
      updatePlayCount()
    } else {
      togglePlay()
    }
  },
  { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
.player-panel {
  width: 600px;
  min-width: 400px;
  display: flex;
  align-items: center;
  color: #fff;
  .iconfont {
    cursor: pointer;
  }
  .icon-pre {
    margin-right: 20px;
  }
  .play-btn {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: var(--btnBg);
    box-shadow: 0 5px 15px rgba(252, 0, 255, 0.4);
    margin-right: 20px;
  }
  .icon-next {
    margin-right: 20px;
  }
  .percent {
    flex: 1;
    margin: 0px 10px;
    height: 2px;
    background: #ddd;
    display: flex;
    align-items: center;
    :deep(.el-slider__runway) {
      height: 4px;
      border-radius: 2px;
    }
    :deep(.el-slider__bar) {
      height: 4px;
      background: linear-gradient(90deg, #00dbde, #fc00ff);
    }
    :deep(.el-slider__button-wrapper) {
      top: -16px;
    }
    :deep(.el-slider__button) {
      background: linear-gradient(135deg, #4fdeff 0%, #e675ff 100%);
      border: none;
      height: 15px;
      width: 15px;
    }
  }
  .time,
  .total-time {
    font-size: 12px;
  }
  .play-mode {
    margin: 0px 20px;
  }

  @media (max-width: 500px) {
    .time,
    .total-time,
    .play-mode,
    .icon-audio {
      display: none;
    }
    .percent {
      position: absolute;
      top: 2px;
      left: 0px;
      right: 0px;
    }
  }
}

.audio {
  display: none;
}
</style>

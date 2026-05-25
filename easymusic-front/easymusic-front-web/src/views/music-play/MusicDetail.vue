<template>
  <div class="music-detail-body">
    <BackBtn></BackBtn>
    <div class="music-panel">
      <div class="music-cover">
        <div
          :class="[
            'music-cover-bg',
            musicPlayStore.playing ? 'music-cover-bg-playing' : '',
          ]"
        ></div>
        <div class="cover">
          <Cover :width="150" :cover="musicInfo.cover" borderRadius="75px">
          </Cover>
        </div>
      </div>
      <div class="music-info">
        <div class="music-title">{{ musicInfo.musicTitle }}</div>
        <div class="user-info">{{ musicInfo.nickName || "--" }}</div>
        <div class="action-panel">
          <div
            :class="[
              'op-item play-btn iconfont',
              musicPlayStore.playing ? 'icon-pause' : 'icon-play',
            ]"
            @click="playMusic"
          ></div>
          <div class="op-item">
            <ActionGood :data="musicInfo"></ActionGood>
          </div>
          <div class="op-item">
            <ActionShare :data="musicInfo"></ActionShare>
          </div>
          <div class="op-item">
            <el-button type="primary" size="large" @click="createSame"
              >做同款</el-button
            >
          </div>
        </div>
        <div class="lyrics-panel" v-if="musicInfo.musicType === 0">
          <div class="lyrics-title">歌词：</div>
          <div
            :class="[
              'lyrics-item',
              musicPlayStore.currentPlayTime >= item.start &&
              musicPlayStore.currentPlayTime <= item.end
                ? 'active'
                : '',
            ]"
            v-for="item in musicInfo.lyrics"
          >
            {{ item.text }}
          </div>
        </div>
        <div v-else class="lyrics-panel">纯音乐，请欣赏。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import ActionShare from "@/component/biz/ActionShare.vue";
import ActionGood from "@/component/biz/ActionGood.vue";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();
import { mitter } from "@/eventbus/eventBus.js";

const currentMusicId = ref(route.params.musicId);
const musicInfo = ref({});
const getMusicInfo = async (autoPlay) => {
  let result = await proxy.Request({
    url: proxy.Api.musicDetail,
    params: {
      musicId: currentMusicId.value,
    },
  });
  if (!result) {
    return;
  }
  if (result.data.musicType === 0) {
    const lyrics = JSON.parse(result.data.lyrics);
    result.data.lyrics = lyrics;
  }
  musicInfo.value = result.data;
  if (!autoPlay) {
    return;
  }
  musicPlayStore.play({ ...result.data });
};

const playMusic = () => {
  if (musicPlayStore.currentMusic?.musicId == musicInfo.value.musicId) {
    mitter.emit("togglePlay");
    return;
  }
  musicPlayStore.play({ ...musicInfo.value });
};

const createSame = () => {
  router.push(`/idea/${musicInfo.value.creationId}`);
};

watch(
  () => route.params.musicId,
  async (newVal, oldVal) => {
    if (!newVal) {
      return;
    }
    currentMusicId.value = newVal;
    getMusicInfo(true);
  },
  { immediate: true, deep: true }
);
watch(
  () => musicPlayStore.currentMusic.musicId,
  async (newVal, oldVal) => {
    if (!newVal) {
      return;
    }
    router.push(`/play/${newVal}`);
  },
  { immediate: true, deep: true }
);
</script>

<style lang="scss" scoped>
.music-detail-body {
  padding: 20px 0px 0px 20px;
  .music-panel {
    display: flex;
    padding: 10px 10px 80px 10px;
    .music-cover {
      width: 250px;
      height: 250px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      .music-cover-bg {
        position: absolute;
        left: 0px;
        top: 0px;
        width: 250px;
        height: 250px;
        background: url("../../assets/img/play_cover_bg.png");
        background-repeat: no-repeat;
      }
      .music-cover-bg-playing {
        animation: rotateBackground 30s linear infinite;
      }
      .cover {
        position: absolute;
        z-index: 2;
      }
      .play-btn {
        position: absolute;
        z-index: 3;
        cursor: pointer;
      }
    }
    .music-info {
      flex: 1;
      color: #fff;
      margin-left: 30px;
      .music-title {
        font-size: 25px;
      }
      .user-info {
        margin-top: 10px;
      }
      .action-panel {
        margin-top: 10px;
        display: flex;
        align-items: center;
        .op-item {
          margin-right: 30px;
          cursor: pointer;
          width: 40px;
          height: 40px;
        }
        .iconfont {
          font-size: 25px;
        }
        .active {
          color: var(--purple);
        }
        .play-btn {
          font-size: 20px;
          background: #fff;
          border-radius: 50%;
          color: var(--purple);
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .lyrics-panel {
        margin-top: 20px;
        .lyrics-title {
          font-size: 20px;
        }
        .lyrics-item {
          padding: 5px 0px;
          font-size: 16px;
        }
        .active {
          color: #b939f6;
          font-size: 18px;
        }
      }
    }
  }

  @media (max-width: 500px) {
    .music-panel {
      flex-direction: column;
      text-align: center;
      .music-cover {
        margin: 0px auto;
      }
      .music-info {
        margin-left: 0px;
        margin-top: 5px;
        .action-panel {
          justify-content: space-around;
        }
      }
    }
  }
}

@keyframes rotateBackground {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>

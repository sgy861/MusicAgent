<template>
  <div class="player">
    <div class="music-info-panel">
      <div class="cover">
        <Cover :cover="musicPlayStore.currentMusic.cover" :lazy="false"></Cover>
        <PlayBtn :data="musicPlayStore.currentMusic" :showBorder="false"></PlayBtn>
      </div>
      <div class="music-info">
        <div class="music-title" @click="playMusic()">
          {{ musicPlayStore.currentMusic.musicTitle }}
        </div>
        <router-link class="music-author" :to="`/user/${musicPlayStore.currentMusic.userId}`">
          {{ musicPlayStore.currentMusic.nickName }}
        </router-link>
      </div>
    </div>
    <Player></Player>
    <div class="op-panel">
      <ActionGood :data="musicPlayStore.currentMusic"></ActionGood>
      <ActionShare :data="musicPlayStore.currentMusic"></ActionShare>
    </div>
  </div>
</template>

<script setup>
import ActionShare from '@/component/biz/ActionShare.vue'
import ActionGood from '@/component/biz/ActionGood.vue'
import Player from '@/component/common/Player.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useMusicPlayStore } from '@/stores/musicPlay.js'
const musicPlayStore = useMusicPlayStore()

const playMusic = () => {
  router.push(`/play/${musicPlayStore.currentMusic.musicId}`)
}
</script>

<style lang="scss" scoped>
.player {
  z-index: 500;
  position: fixed;
  bottom: 0px;
  background: #1a142d;
  height: 70px;
  width: calc(100% - 200px);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0px 20px;
  .music-info-panel {
    display: flex;
    .cover {
      width: 50px;
      height: 50px;
      position: relative;
    }
    .music-info {
      margin-left: 10px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      width: 200px;
      .music-title {
        cursor: pointer;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .music-author {
        margin-top: 5px;
        opacity: 0.7;
        font-size: 12px;
        text-decoration: none;
        color: var(--text);
      }
    }
  }
  .op-panel {
    display: flex;
    justify-content: space-between;
    .iconfont {
      cursor: pointer;
      font-size: 24px;
    }
  }
}

@media (max-width: 500px) {
  .player {
    width: 100vw;
    .op-panel {
      display: none;
    }
    .music-info-panel {
      .music-info {
        width: 170px;
      }
    }
  }
}
</style>

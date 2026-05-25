<template>
  <div class="music-item">
    <div class="cover">
      <Cover :cover="data.cover" borderRadius="50%"></Cover>
      <PlayBtn :data="data" @playList="playList"></PlayBtn>
    </div>
    <div class="music-info">
      <div class="music-title" @click="playMusic(true)">
        {{ data.musicTitle }}
      </div>
      <div class="music-prompt">{{ data.prompt }}</div>
      <div class="user-info">
        <div class="user-avatar">
          <router-link :to="`/user/${data.userId}`">
            <Avatar :avatar="data.avatar" :width="20"></Avatar>
          </router-link>
        </div>
        <router-link :to="`/user/${data.userId}`" class="user-name">{{
          data.nickName
        }}</router-link>
        <div class="iconfont icon-play">{{ data.playCount }}</div>
        <div class="iconfont icon-time">
          {{ proxy.Utils.seconds2Min(data.duration) }}
        </div>
      </div>
    </div>
    <div class="op-panel">
      <div class="opbtn opbtn-good">
        <ActionGood :data="data"></ActionGood>
      </div>
      <div class="opbtn opbtn-share">
        <ActionShare :data="data"></ActionShare>
      </div>
    </div>
  </div>
</template>

<script setup>
import ActionShare from "@/component/biz/ActionShare.vue";
import ActionGood from "@/component/biz/ActionGood.vue";
import { ref, reactive, getCurrentInstance, nextTick, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
});
const emits = defineEmits(["playList"]);
const playMusic = (jumpDetail) => {
  emits("playList");
  musicPlayStore.play({ ...props.data });
  if (!jumpDetail) {
    return;
  }
  router.push(`/play/${props.data.musicId}`);
};

const playList = () => {
  emits("playList");
};
</script>

<style lang="scss" scoped>
.music-item {
  display: flex;
  padding: 0px 20px 0px 0px;
  min-width: 320px;
  height: 100px;
  .cover {
    background: #302748;
    border-radius: 10px;
    width: 100px;
    height: 100px;
    border-radius: 10px;
    padding: 10px;
    cursor: pointer;
    position: relative;
    img {
      max-width: 100%;
      border-radius: 50%;
    }
  }
  .music-info {
    margin: 0px 15px;
    flex: 1;
    width: 0;
    color: #ffff;
    display: flex;
    flex-direction: column;
    justify-content: center;
    .music-title {
      display: inline-block;
      color: #fff;
      font-size: 16px;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      cursor: pointer;
      &:hover {
        color: #95aefc;
      }
    }
    .music-prompt {
      font-size: 12px;
      font-weight: 500;
      opacity: 0.5;
      margin-top: 5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .user-info {
      display: flex;
      align-items: center;
      margin-top: 10px;
      .user-avatar {
        margin-right: 5px;
        opacity: 1;
      }
      .user-name {
        font-size: 14px;
        margin-right: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        opacity: 0.5;
        text-decoration: none;
        color: #fff;
      }
      .iconfont {
        opacity: 0.5;
        &::before {
          margin-right: 4px;
        }
      }
      .icon-play {
        font-size: 12px;
        margin-right: 10px;
      }
      .icon-time {
        font-size: 14px;
      }
    }
  }
  .op-panel {
    display: flex;
    justify-content: space-between;
    color: #fff;
    align-items: center;
    width: 80px;
    .opbtn {
      cursor: pointer;
    }
  }
}
@media (max-width: 500px) {
  .music-item {
    padding: 3px;
    .user-name {
      display: none;
    }
  }
}
</style>

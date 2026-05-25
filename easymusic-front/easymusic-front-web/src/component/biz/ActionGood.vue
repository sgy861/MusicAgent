<template>
  <div
    :class="['iconfont', data.doGood ? 'icon-good-solid' : 'icon-good']"
    @click="goodMusic"
  ></div>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  computed,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useUserInfoStore } from "@/stores/userInfoStore";
const userInfoStore = useUserInfoStore();

import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
});

const goodMusic = async () => {
  if (!userInfoStore.checkLogin()) {
    return;
  }
  let result = await proxy.Request({
    url: proxy.Api.doGood,
    showLoading: false,
    params: {
      musicId: props.data.musicId,
    },
  });
  if (!result) {
    return;
  }
  props.data.doGood = !props.data.doGood;
  //判断当前正在播放的是不是该音乐，播放器和列表保持同步
  if (musicPlayStore.currentMusic?.musicId == props.data.musicId) {
    musicPlayStore.currentMusic.doGood = props.data.doGood;
  }
};

watch(
  () => musicPlayStore.currentMusic.doGood,
  (newVal, oldVal) => {
    if (
      newVal != null &&
      props.data.musicId === musicPlayStore.currentMusic.musicId
    ) {
      props.data.doGood = newVal;
    }
  },
  { immediate: true, deep: true }
);
</script>

<style lang="scss" scoped>
.iconfont {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  border-radius: 50%;
  &:hover {
    background: #3e3450;
  }
}
.icon-good-solid {
  color: var(--activeText);
}
</style>

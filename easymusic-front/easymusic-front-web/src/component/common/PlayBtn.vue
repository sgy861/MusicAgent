<template>
  <div
    :class="[
      'play-btn',
      musicPlayStore.currentMusic.musicId == data.musicId ? 'cur-play-btn' : '',
      showBorder ? '' : 'no-border',
    ]"
    @click="playMusic"
  >
    <template v-if="musicPlayStore.currentMusic.musicId == data.musicId">
      <template v-if="musicPlayStore.playing">
        <div class="btn-pause iconfont icon-pause"></div>
        <img :src="proxy.Utils.getLocalResource('img/loading.gif')" />
      </template>
      <div class="btn-play iconfont icon-play" v-else></div>
    </template>
    <div v-else class="hover-play iconfont icon-play"></div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();
import { mitter } from "@/eventbus/eventBus.js";

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
  showBorder: {
    type: Boolean,
    default: true,
  },
});

const playing = computed(() => {
  return (
    props.data.musicId == musicPlayStore.currentMusic.musicId &&
    musicPlayStore.playing
  );
});
const emits = defineEmits(["playList"]);
const playMusic = () => {
  emits("playList");
  if (musicPlayStore.currentMusic?.musicId == props.data.musicId) {
    mitter.emit("togglePlay");
    return;
  }
  props.data.playCount++;
  musicPlayStore.play({ ...props.data });
};
</script>

<style lang="scss" scoped>
.play-btn {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  border-radius: 10px;
  cursor: pointer;
  .iconfont {
    color: #fff;
    font-size: 25px;
    display: none;
  }
  &:hover {
    outline: 2px solid var(--purple);
    .hover-play {
      display: flex;
    }
  }
}
.cur-play-btn {
  outline: 2px solid var(--purple);
  img {
    width: 20px;
  }
  .btn-play {
    display: flex;
  }
  &:hover {
    img {
      display: none;
    }
    .btn-pause {
      display: flex;
    }
  }
}
.no-border {
  outline: none;
  &:hover {
    outline: none;
  }
}
</style>

<template>
  <div class="commend-list" ref="commendListRef">
    <div class="swiper-panel" ref="swipePanelRef">
      <div
        ref="swiperWraperRef"
        class="swiper-wraper"
        :style="{ transform: `translate3d(${xOffset}px, 0px, 0px)` }"
      >
        <MusicCommendItem
          v-for="item in commendList"
          :data="item"
          class="hot-item"
          @playList="playList"
        ></MusicCommendItem>
      </div>
    </div>
  </div>
</template>

<script setup>
import MusicCommendItem from "@/component/biz/MusicCommendItem.vue";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  computed,
  onMounted,
  onUnmounted,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();

const commendList = ref([]);
const loadCommend = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadCommendMusic,
  });
  if (!result) {
    return;
  }
  commendList.value = result.data;
};

const playList = () => {
  musicPlayStore.savePlayList(commendList.value);
};

const emit = defineEmits(["disableType"]);
const xOffset = ref(0);
const change = (type) => {
  const itemNodes = document.getElementsByClassName("hot-item");
  const itemWidth = itemNodes[0].clientWidth;
  const swiperWraperWidth = itemNodes.length * itemWidth;
  if (xOffset.value == 0 && type == 1) {
    return;
  }
  if (type == -1 && -xOffset.value + parentWidth.value >= swiperWraperWidth) {
    console.log(xOffset.value, parentWidth.value, swiperWraperWidth);
    return;
  }
  xOffset.value = xOffset.value + type * itemWidth;

  console.log(xOffset.value);

  //已经到最左边
  let disableType = null;
  if (xOffset.value >= 0) {
    disableType = 1;
  } else if (-xOffset.value + parentWidth.value >= swiperWraperWidth) {
    disableType = -1;
  }
  emit("disableType", disableType);
};

const commendListRef = ref();
//父元素宽度
const parentWidth = ref();

const swipePanelRef = ref();
//滚动元素宽度
const init = async () => {
  await nextTick();
  parentWidth.value = swipePanelRef.value.clientWidth;
};

onMounted(() => {
  loadCommend();
  init();
  window.addEventListener("resize", init);
});
onUnmounted(() => {
  window.removeEventListener("resize", init);
});
defineExpose({
  change,
});
</script>

<style lang="scss" scoped>
.commend-list {
  margin: 10px 0px;
  .swiper-panel {
    overflow: hidden;
    .swiper-wraper {
      display: flex;
      transition: transform 0.6s;
    }
  }
}
</style>

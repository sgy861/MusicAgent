<template>
  <div class="load-more-data-list" @scroll="handleScroll">
    <div
      :class="[layoutType == 'grid' ? 'data-list-grad' : '']"
      :style="{
        'grid-template-columns':
          layoutType == 'grid' ? `repeat(${gridCount}, 1fr)` : '',
      }"
    >
      <template v-for="(item, index) in dataSource.list">
        <slot :data="item" :index="index"></slot>
      </template>
    </div>
    <div class="bottom-loading" v-if="loading">
      <img
        :src="proxy.Utils.getLocalResource('img/loading.gif')"
      />数据加载中....
    </div>
    <div
      v-if="
        dataSource.pageNo >= dataSource.pageTotal &&
        !loading &&
        dataSource.list.length > 0 &&
        showLoadAll
      "
      class="reach-bottom"
    >
      没有更多数据了
    </div>
    <NoData
      v-if="dataSource.list && dataSource.list.length == 0 && !loading"
      msg="暂无数据"
    >
    </NoData>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
  watch,
} from "vue";
import { useRouter } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();

const props = defineProps({
  dataSource: {
    type: Object,
  },
  loading: {
    type: Boolean,
  },
  layoutType: {
    type: String,
    default: "line",
  },
  gridCount: {
    type: Number,
    default: 3,
  },
  showLoadAll: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["loadData"]);
const handleScroll = (e) => {
  if (props.loading) {
    return;
  }
  const { scrollHeight, scrollTop, clientHeight } = e.target;
  if (scrollHeight - (scrollTop + clientHeight) < 1) {
    emit("loadData");
  }
};

defineExpose({
  handleScroll,
});

onMounted(async () => {
  window.addEventListener("scroll", handleScroll);
  emit("loadData");
});
</script>

<style lang="scss" scoped>
.load-more-data-list {
  height: 100%;
  overflow: auto;
  padding: 10px;
  .reach-bottom {
    text-align: center;
    line-height: 40px;
    color: var(--text);
    font-size: 12px;
  }
  .bottom-loading {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text);
    font-size: 13px;
    img {
      width: 20px;
      margin-right: 10px;
    }
  }
  .data-list-grad {
    display: grid;
    grid-gap: 20px;
    padding-bottom: 10px;
  }
}
@media (max-width: 500px) {
  .load-more-data-list {
    padding: 0px;
    .data-list-grad {
      grid-template-columns: repeat(1, 1fr) !important;
    }
  }
}
</style>

<template>
  <DataLoadMoreList
    :dataSource="dataSource"
    :loading="loading"
    @loadData="loadLatestMusic"
    layoutType="grid"
    :gridCount="indexType === 1 ? 3 : 2"
    :showLoadAll="indexType != 1"
  >
    <template #default="{ data, index }">
      <MusicLatestItem :data="data" @playList="playList"></MusicLatestItem>
    </template>
  </DataLoadMoreList>
</template>

<script setup>
import MusicLatestItem from "@/component/biz/MusicLatestItem.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();

const props = defineProps({
  indexType: {
    type: Number,
  },
});

const dataSource = ref({});
const loading = ref(false);
const dataList = ref([]);
const loadLatestMusic = async () => {
  if (
    Object.keys(dataSource.value).length > 0 &&
    dataSource.value.pageNo == dataSource.value.pageTotal
  ) {
    return;
  }
  loading.value = true;
  let pageNo = dataSource.value.pageNo || 0;
  pageNo++;
  let result = await proxy.Request({
    url: proxy.Api.loadLatestMusic,
    showLoading: false,
    params: {
      pageNo,
      indexType: props.indexType,
    },
  });
  loading.value = false;
  if (!result) {
    return;
  }
  if (result.data.pageNo == 1) {
    dataList.value = result.data.list;
  } else {
    dataList.value = dataList.value.concat(result.data.list);
  }
  result.data.list = dataList.value;
  dataSource.value = result.data;
};

const playList = () => {
  musicPlayStore.savePlayList(dataSource.value.list);
};
</script>

<style lang="scss" scoped>
</style>

<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="800px"
    :showCancel="false"
    @close="dialogConfig.show = false"
    :padding="2"
  >
    <div class="data-list" v-if="dialogConfig.show">
      <DataLoadMoreList
        :dataSource="dataSource"
        :loading="loading"
        @loadData="loadRecord"
        layoutType="grid"
        :gridCount="2"
      >
        <template #default="{ data, index }">
          <div class="record-item">
            <div class="record-type">{{ data.recordTypeName }}</div>
            <div class="integral">积分：{{ data.changeIntegral }}</div>
            <div class="amount" v-if="data.recordType == 2">
              金额：{{ data.amount }}
            </div>
            <div>时间：{{ data.createTime }}</div>
            <div v-if="data.recordType == 2">订单号：{{ data.businessId }}</div>
          </div>
        </template>
      </DataLoadMoreList>
    </div>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useUserInfoStore } from "@/stores/userInfoStore";
const userInfoStore = useUserInfoStore();

const dialogConfig = ref({
  show: false,
  title: "积分记录",
});

const dataSource = ref({});
const loading = ref(false);
const dataList = ref([]);
const loadRecord = async () => {
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
    url: proxy.Api.integralRecords,
    showLoading: false,
    params: {
      pageNo,
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

const show = () => {
  if (!userInfoStore.checkLogin()) {
    return;
  }
  dialogConfig.value.show = true;
  dataSource.value.pageNo = 0;
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
.data-list {
  height: 500px;
  overflow: auto;
  .record-item {
    margin-bottom: 10px;
    padding: 5px;
    color: #fff;
    border: 1px solid #ddd;
    border-radius: 5px;
    .record-type {
      font-size: 16px;
    }
    .integral {
      margin-top: 10px;
    }
  }
}
</style>

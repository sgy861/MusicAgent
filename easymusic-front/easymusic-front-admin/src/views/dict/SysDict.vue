<template>
  <div class="dict-panel">
    <div class="parent">
      <SysDictData
        @loadDictValue="loadDictValueHandler"
        dictPcode="0"
      ></SysDictData>
    </div>
    <div class="dict-value">
      <SysDictData ref="sysDictDataRef" :dictPcode="dictPcode"></SysDictData>
    </div>
  </div>
</template>

<script setup>
import SysDictData from "./SysDictData.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const dictPcode = ref();
const sysDictDataRef = ref();
const loadDictValueHandler = async (data) => {
  dictPcode.value = data.dictCode;
  await nextTick();
  sysDictDataRef.value.loadDataList();
};
</script>

<style lang="scss" scoped>
.dict-panel {
  display: flex;
  justify-content: space-between;
  .parent {
    width: 35%;
  }
  .dict-value {
    width: 59%;
  }
}
</style>

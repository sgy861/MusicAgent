<template>
  <div>
    <el-button type="primary" @click="showEdit()" :disabled="dictPcode == null"
      >新增</el-button
    >
  </div>
  <Table
    ref="tableRef"
    :columns="resultColumns"
    :extHeight="extHeight"
    :fetch="loadDataList"
    :dataSource="dataSource"
    :options="tableOptions"
    @rowClick="rowClickHandler"
    :initFetch="dictPcode == '0'"
    rowKey="dictId"
  >
    <template #slotOp="{ row, index }">
      <span class="a-link" @click="showEdit(row)">修改</span>
      <el-divider direction="vertical" />
      <span class="a-link" @click="del(row)">删除</span>
    </template>
  </Table>

  <SysDictEdit ref="sysDictEditRef" @reload="loadDataList"></SysDictEdit>
</template>

<script setup>
import SysDictEdit from "./SysDictEdit.vue";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  computed,
  onMounted,
} from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

import Sortable from "sortablejs";

const props = defineProps({
  dictPcode: {
    type: String,
  },
});

const searchForm = ref({});
const dataSource = ref({});
const tableOptions = {};
const extHeight = ref(0);
const columns = [
  {
    label: "字典编号",
    prop: "dictCode",
    width: 200,
  },
  {
    label: "字典值",
    prop: "dictValue",
    width: 100,
  },
  {
    label: "字典描述",
    prop: "dictDesc",
  },
  {
    label: "操作",
    prop: "op",
    scopedSlots: "slotOp",
    width: 100,
  },
];

const resultColumns = computed(() => {
  if (props.dictPcode === "0") {
    return columns.filter((item) => {
      return item.prop != "dictValue";
    });
  } else {
    return columns;
  }
});

const tableRef = ref();
const loadDataList = async () => {
  let params = {
    pageNo: dataSource.value.pageNo,
    pageSize: dataSource.value.pageSize,
    dictPcode: props.dictPcode,
  };
  Object.assign(params, searchForm.value);
  let result = await proxy.Request({
    url: proxy.Api.loadSysDict,
    params: params,
  });
  if (!result) {
    return;
  }
  Object.assign(dataSource.value, result.data);
  if (props.dictPcode == "0" && result.data.list.length > 0) {
    tableRef.value.setCurrentRow("dictId", result.data.list[0].dictId);
    emit("loadDictValue", result.data.list[0]);
  }
};

const sysDictEditRef = ref();
const showEdit = (data = { dictPcode: props.dictPcode }) => {
  sysDictEditRef.value.show(data);
};

const emit = defineEmits(["loadDictValue"]);
const rowClickHandler = (row) => {
  if (props.dictPcode !== "0") {
    return;
  }
  emit("loadDictValue", row);
};

const del = (row) => {
  proxy.Confirm({
    message: `确定要删除吗?`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delDict,
        params: {
          dictId: row.dictId,
        },
      });
      if (!result) {
        return;
      }
      proxy.Message.success("删除成功");
      loadDataList();
    },
  });
};

defineExpose({
  loadDataList,
});

const iniSort = () => {
  const el = tableRef.value.$el.querySelector(".el-table__body-wrapper tbody");
  // 初始化 Sortable.js
  Sortable.create(el, {
    animation: 150, // 拖动时的动画效果（毫秒）
    onEnd: async (evt) => {
      const { oldIndex, newIndex } = evt;
      if (oldIndex === newIndex) {
        return;
      } // 位置未改变，无需处理
      const dataList = dataSource.value.list;

      const currRow = dataList.splice(oldIndex, 1)[0];
      dataList.splice(newIndex, 0, currRow);
      console.log(dataList);
      changeOrder();
    },
  });
};

const changeOrder = async () => {
  let result = await proxy.Request({
    url: proxy.Api.changeDictSort,
    params: {
      dictPcode: props.dictPcode,
      dictIds: dataSource.value.list
        .map((item) => {
          return item.dictId;
        })
        .join(","),
    },
  });
  if (!result) {
    return;
  }
};

onMounted(() => {
  iniSort();
});
</script>

<style lang="scss" scoped>
</style>

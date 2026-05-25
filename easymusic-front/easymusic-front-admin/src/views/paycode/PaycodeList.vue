<template>
  <div class="top-panel">
    <el-form :model="searchFormData" label-width="50px">
      <el-row>
        <el-col :span="4">
          <el-form-item label="日期">
            <el-date-picker
              v-model="searchFormData.createTimeRange"
              type="daterange"
              range-separator="~"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="loadDataList"
            ></el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="4" :style="{ 'padding-left': '10px' }">
          <el-button type="primary" @click="loadDataList">搜索</el-button>
          <el-button type="primary" @click="newPayCode">新建</el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
  <Table
    :columns="columns"
    :showPagination="true"
    :dataSource="tableData"
    :fetch="loadDataList"
    :options="tableOptions"
  >
    <template #slotNickName="{ index, row }">
      {{ row.nickName }}({{ row.useUserId }})
    </template>

    <template #amount="{ index, row }">
      {{ row.amount.toFixed(2) }}
    </template>
    <!--状态-->
    <template #status="{ index, row }">
      <span v-if="row.status == 1" :style="{ color: '#67c23a' }">已使用</span>
      <span v-if="row.status == 0" :style="{ color: '#f56c6c' }">待使用</span>
    </template>

    <template #op="{ index, row }">
      <span class="a-link" @click="delCode(row)">删除</span>
    </template>
  </Table>
  <PayCodeEdit ref="payCodeRef" @reload="loadDataList"></PayCodeEdit>
</template>

<script setup>
import PayCodeEdit from "./PayCodeEdit.vue";
import { getCurrentInstance, reactive, ref } from "vue";
const { proxy } = getCurrentInstance();

//列表
const columns = [
  {
    label: "支付码",
    prop: "payCode",
  },
  {
    label: "金额",
    prop: "amount",
    scopedSlots: "amount",
  },
  {
    label: "创建时间",
    prop: "createTime",
  },
  {
    label: "使用人",
    prop: "nickName",
    scopedSlots: "slotNickName",
  },
  {
    label: "使用时间",
    prop: "useTime",
  },
  {
    label: "状态",
    prop: "status",
    scopedSlots: "status",
    width: 80,
  },
  {
    label: "操作",
    prop: "op",
    scopedSlots: "op",
    width: 80,
  },
];
//搜索
const searchFormData = ref({});
//列表
const tableData = ref({});
const tableOptions = {
  extHeight: 50,
};

const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  };

  Object.assign(params, searchFormData.value);
  if (searchFormData.value.createTimeRange) {
    params.createTimeStart = searchFormData.value.createTimeRange[0];
    params.createTimeEnd = searchFormData.value.createTimeRange[1];
  }
  delete params.createTimeRange;
  let result = await proxy.Request({
    url: proxy.Api.loadPaycodeList,
    params,
  });
  if (!result) {
    return;
  }
  tableData.value = result.data;
};

const payCodeRef = ref();
const newPayCode = () => {
  payCodeRef.value.show();
};

//禁用用户
const delCode = (data) => {
  proxy.Confirm({
    message: `你确定要删除吗`,
    okfun: async () => {
      {
        let result = await proxy.Request({
          url: proxy.Api.delCode,
          params: {
            payCode: data.payCode,
          },
        });
        if (!result) {
          return;
        }
        loadDataList();
      }
    },
  });
};
</script>

<style lang="scss">
.op {
  cursor: pointer;
}
</style>
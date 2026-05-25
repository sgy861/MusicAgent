<template>
  <div class="top-panel">
    <el-form :model="searchForm" @submit.prevent>
      <el-row :gutter="10">
        <el-col :span="5">
          <el-form-item label="订单ID">
            <el-input clearable placeholder="输入完整的订单ID" v-model="searchForm.orderId"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="用户昵称">
            <el-input clearable placeholder="输入用户昵称" v-model="searchForm.nickNameFuzzy"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="状态" prop="">
            <el-select clearable placeholder="请选择状态" v-model="searchForm.status">
              <el-option :value="item.status" :label="item.desc" v-for="item in STATUS_LIST"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-button type="primary" @click="loadDataList">搜索</el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
  <Table ref="tableInfoRef" :columns="columns" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions"
    :extHeight="tableOptions.extHeight">
    <template #slotUser="{ index, row }">
      {{ row.nickName }}({{ row.userId }})
    </template>

    <template #slotOrder="{ index, row }">
      <div>支付订单号：{{ row.orderId }}</div>
      <div>通道订单号：{{ row.channelOrderId }}</div>
    </template>
    <template #slotCreateTime="{ index, row }">
      <div>创建时间：{{ row.createTime }}</div>
      <div>支付时间：{{ row.payTime }}</div>
    </template>

    <template #slotStatus="{ index, row }">
      {{statusName(row.status)}}
    </template>
  </Table>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()

const STATUS_LIST = [
  {
    status: 0,
    desc: '待支付',
  },
  {
    status: 1,
    desc: '已支付',
  },
  {
    status: -1,
    desc: '已超时',
  },
]

const statusName = (status) => {
  return STATUS_LIST.find((item) => {
    return item.status === status
  }).desc
}

const columns = [
  {
    label: '订单',
    prop: 'orderId',
    scopedSlots: 'slotOrder',
    width: 400,
  },
  {
    label: '订单时间',
    prop: 'createTime',
    scopedSlots: 'slotCreateTime',
    width: 250,
  },
  {
    label: '用户',
    prop: 'userId',
    scopedSlots: 'slotUser',
  },
  {
    label: '商品名称',
    prop: 'productName',
  },
  {
    label: '金额',
    prop: 'amount',
  },
  {
    label: '购买积分',
    prop: 'integral',
  },
  {
    label: '状态',
    prop: 'status',
    scopedSlots: 'slotStatus',
  },
]

const tableInfoRef = ref()
const tableOptions = ref({
  extHeight: 0,
})

const searchForm = ref({})
const tableData = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  }
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.Api.loadOrder,
    params: params,
  })
  if (!result) {
    return
  }
  Object.assign(tableData.value, result.data)
}
</script>

<style lang="scss" scoped>
</style>

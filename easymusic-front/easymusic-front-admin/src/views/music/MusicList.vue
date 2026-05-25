<template>
  <div class="top-panel">
    <el-form :model="searchForm" @submit.prevent>
      <el-row :gutter="10">
        <el-col :span="5">
          <el-form-item label="用户昵称">
            <el-input clearable placeholder="输入用户昵称" v-model="searchForm.nickNameFuzzy"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="状态" prop="">
            <el-select clearable placeholder="请选择状态" v-model="searchForm.status">
              <el-option :value="0" label="禁用"></el-option>
              <el-option :value="1" label="启用"></el-option>
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

    <template #slotCover="{ index, row }">
      <Cover :cover="row.cover"></Cover>
    </template>
    <template #slotCommendType="{ index, row }">
      {{ row.commendType == 1 ? "已推荐" : "未推荐" }}
    </template>

    <template #slotMusicStatus="{ index, row }">
      {{ row.musicStatus == 1 ? "已完成" : "创作中......" }}
    </template>

    <template #slotOperation="{ index, row }">
      <a href="javascript:void(0)" class="a-link" @click="changeCommendType(row)"
        v-if="row.musicStatus==1">{{ row.commendType == 0 ? "推荐" : "取消推荐" }}</a>
    </template>
  </Table>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()

const columns = [
  {
    label: '封面',
    prop: 'cover',
    scopedSlots: 'slotCover',
  },
  {
    label: '歌曲名称',
    prop: 'musicTitle',
  },
  {
    label: '作者',
    prop: 'nickName',
  },
  {
    label: '歌曲时长',
    prop: 'duration',
  },
  {
    label: '播放数',
    prop: 'playCount',
  },
  {
    label: '点赞数',
    prop: 'goodCount',
  },
  {
    label: '推荐',
    prop: 'commendType',
    scopedSlots: 'slotCommendType',
  },
  {
    label: '状态',
    prop: 'musicStatus',
    scopedSlots: 'slotMusicStatus',
  },
  {
    label: '操作',
    prop: 'operation',
    width: 80,
    scopedSlots: 'slotOperation',
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
    url: proxy.Api.loadMusic,
    params: params,
  })
  if (!result) {
    return
  }
  Object.assign(tableData.value, result.data)
}

const changeCommendType = (row) => {
  proxy.Confirm({
    message: `确定要${row.commendType == 0 ? '推荐' : '取消推荐'}吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.changeMusicCommendType,
        params: {
          musicId: row.musicId,
          commendType: row.commendType == 0 ? 1 : 0,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('操作成功')
      loadDataList()
    },
  })
}
</script>

<style lang="scss" scoped>
</style>

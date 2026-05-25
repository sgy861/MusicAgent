<template>
  <div class="product-item-panel">
    <div class="cover-panel">
      <Cover :cover="data.cover" borderRadius="0px" fit="cover"></Cover>
    </div>
    <div class="product-info">
      <div class="name">{{ data.productName }}</div>
      <div class="item">价格:{{ data.price }}</div>
      <div class="item">积分:{{ data.integral }}</div>
      <div class="op">
        <div class="item">
          上架状态:{{ data.onsaleType == 1 ? "已上架" : "未上架" }}
        </div>
        <el-dropdown>
          <div class="iconfont icon-more"></div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="showEdit()">编辑</el-dropdown-item>
              <el-dropdown-item @click="delProduct()">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
})

const emit = defineEmits(['showEdit', 'reload'])
const showEdit = () => {
  emit('showEdit', props.data)
}

const delProduct = () => {
  proxy.Confirm({
    message: `确定要删除【${props.data.productName}】`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delProduct,
        params: {
          productId: props.data.productId,
        },
      })
      if (!result) {
        return
      }
      emit('reload')
    },
  })
}
</script>

<style lang="scss" scoped>
.product-item-panel {
  display: flex;
  flex-direction: column;
  .cover-panel {
    display: flex;
    height: 200px;
    justify-content: top;
  }
  .product-info {
    padding: 5px;
    .name {
      text-align: center;
    }
    .item {
      font-size: 14px;
      color: var(--text);
      margin-top: 5px;
    }
    .op {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .icon-more {
      cursor: pointer;
      color: var(--text);
      margin-top: 5px;
      :focus {
        border: none;
      }
    }
  }
}
</style>

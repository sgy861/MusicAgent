<template>
  <VueDraggable v-model="productList" @Update="changeSort" handle=".move-handler" class="product-list"
    draggable=".product-item">
    <div class="product-item-add iconfont icon-add" @click="showEdit()"></div>
    <div class="product-item" v-for="item in productList">
      <ProductItem :data="item" @showEdit="showEdit" @reload="loadProduct"></ProductItem>
      <div class="move-handler">
        <div class="iconfont icon-move"></div>
      </div>
    </div>
  </VueDraggable>
  <div class="no-data-tips">
    <NoData v-if="productList.length == 0" msg="暂无商品"></NoData>
  </div>
  <ProductEdit @reload="loadProduct" ref="productEditRef"></ProductEdit>
</template>

<script setup>
import { VueDraggable } from 'vue-draggable-plus'
import ProductItem from './ProductItem.vue'
import ProductEdit from './ProductEdit.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const productList = ref([])
const loadProduct = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadProduct,
  })
  if (!result) {
    return
  }
  productList.value = result.data
}

const productEditRef = ref()
const showEdit = (data) => {
  productEditRef.value.show(data)
}

const changeSort = async () => {
  const productIds = productList.value.map((item) => {
    return item.productId
  })
  let result = await proxy.Request({
    url: proxy.Api.changeProductSort,
    params: {
      productIds: productIds.join(','),
    },
  })
  if (!result) {
    return
  }
  proxy.Message.success('排序成功')
}
onMounted(() => {
  loadProduct()
})
</script>

<style lang="scss" scoped>
.product-list {
  display: grid;
  grid-gap: 20px;
  grid-template-columns: repeat(5, 1fr);
  .icon-add {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 50px;
    color: var(--text2);
    cursor: pointer;
  }
  .product-item,
  .product-item-add {
    height: 320px;
    border: 1px solid #ddd;
    border-radius: 10px;
    position: relative;
    overflow: hidden;
    &:hover {
      .move-handler {
        display: flex;
      }
    }
    .move-handler {
      position: absolute;
      width: 100%;
      z-index: 1;
      left: 0px;
      top: 0px;
      height: 30px;
      cursor: move;
      border-bottom: 1px solid #ddd;
      display: none;
      align-items: center;
      justify-content: center;
      background: rgb(240, 240, 240, 0.5);
    }
  }
}
.no-data-tips {
  height: 50px;
}
</style>

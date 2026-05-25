<template>
  <div class="buy-title">充值</div>
  <div class="integral">
    积分：<span class="record-btn iconfont icon-narrow-right"
      @click="showIntegralRecord">{{ userInfoStore.userInfo.integral || 0 }}</span>
  </div>
  <div class="product-list">
    <ProductItem v-for="item in productList" :data="item" @pay="pay"></ProductItem>
  </div>
  <Pay ref="payRef" @showMyOrder="showIntegralRecord"></Pay>

  <IntegralRecord ref="integralRecordRef"></IntegralRecord>
</template>

<script setup>
import IntegralRecord from '@/views/my/IntegralRecord.vue'
import Pay from './Pay.vue'
import ProductItem from './ProductItem.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()

const productList = ref([])

const loadProduct = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadProduct,
    params: {},
  })
  if (!result) {
    return
  }
  productList.value = result.data
}

const payRef = ref()
const pay = (data) => {
  payRef.value.pay({ ...data })
}

const integralRecordRef = ref()
const showIntegralRecord = () => {
  integralRecordRef.value.show()
}
onMounted(() => {
  loadProduct()
})
</script>

<style lang="scss" scoped>
.buy-title {
  margin-top: 20px;
  text-align: center;
  color: #fff;
  font-size: 30px;
  font-weight: bold;
}
.integral {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: #fff;
  .record-btn {
    cursor: pointer;
    display: inline-block;
    text-decoration: underline;
    &::before {
      float: right;
      margin-top: 7px;
      font-size: 13px;
    }
  }
}
.product-list {
  margin-top: 10px;
  display: flex;
  padding: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 500px) {
  .product-list {
    flex-direction: column;
  }
}
</style>

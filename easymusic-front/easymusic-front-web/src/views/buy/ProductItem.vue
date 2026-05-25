<template>
  <div class="product-item">
    <div class="cover">
      <Cover :cover="data.cover" borderRadius="0px"></Cover>
    </div>
    <div class="product-info">
      <div class="product-name">{{ data.productName }}</div>
      <div class="price">¥ {{ proxy.Utils.convert2Amount(data.price) }}</div>
      <div class="integral">充值积分：{{ data.integral }}</div>
      <div class="product-description">{{ data.productDescription }}</div>
    </div>
    <div class="buy-btn" @click="buy">购买</div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
})

const emits = defineEmits(['pay'])
const buy = () => {
  if (!userInfoStore.checkLogin()) {
    return
  }
  emits('pay', props.data)
}
</script>

<style lang="scss" scoped>
.product-item {
  background: #241f47;
  width: 300px;
  height: 500px;
  margin-right: 20px;
  border-radius: 10px;
  overflow: hidden;
  color: #fff;
  position: relative;
  .cover {
    height: 200px;
    overflow: hidden;
  }
  .product-info {
    padding: 20px;
    .product-name {
      font-size: 22px;
      background: linear-gradient(104deg, #f6b1ff, #ecd3ff 53%, #ea8cff);
      background-clip: text;
      -webkit-text-fill-color: transparent;
      font-weight: bold;
      margin-bottom: 20px;
    }
    .price {
      font-size: 20px;
      font-weight: 500;
      color: #94adff;
    }
    .integral {
      line-height: 48px;
      font-weight: 700;
      background: linear-gradient(105deg, #4fdeff, #e675ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .product-description {
      font-size: 14px;
      color: var(--text);
    }
  }

  .buy-btn {
    position: absolute;
    bottom: 20px;
    right: 10px;
    left: 10px;
    text-align: center;
    padding: 10px;
    border-radius: 20px;
    background: var(--btnBg);
    margin-top: 10px;
    cursor: pointer;
  }
  &:hover {
    background: linear-gradient(163.52deg, #4940a9 4.84%, #161518 47.07%);
    outline: 2px solid var(--activeText);
  }
}

@media (max-width: 500px) {
  .product-item {
    width: 100%;
    margin-right: 0px;
    margin-bottom: 20px;
  }
}
</style>

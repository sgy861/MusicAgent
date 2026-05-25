<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="535px"
    :showCancel="false" @close="closePay">
    <div class="pay-panel">
      <div class="step-panel">
        <el-steps :space="200" :active="currentStep" finish-status="success" align-center>
          <el-step title="确认订单" />
          <el-step title="扫码支付" />
          <el-step title="购买成功" />
        </el-steps>
      </div>

      <template v-if="currentStep == 1">
        <div class="product-info-panel">
          <div class="title-info">订单详情信息</div>
          <div class="product-info">
            <div class="product-cover">
              <Cover :cover="productInfo.cover" :width="100"></Cover>
            </div>
            <div class="product-name-panel">
              <div class="product-name">{{ productInfo.productName }}</div>
              <div class="sku-name">充值积分:{{ productInfo.integral }}</div>
            </div>
            <div class="price">
              ￥<span>{{ proxy.Utils.convert2Amount(productInfo.price) }}</span>
            </div>
          </div>
        </div>
        <el-form class="pay-form" :rules="rules" :model="formData" ref="formDataRef" label-width="95px" @submit.prevent>
          <el-form-item label="支付方式：" prop="payType">
            <div class="pay-method">
              <el-radio-group v-model="formData.payType" @change="payTypeChange">
                <el-radio :value="1">微信支付(推荐)</el-radio>
                <el-radio :value="0">支付码支付</el-radio>
              </el-radio-group>
              <el-popover placement="right" :width="220" trigger="hover">
                <template #reference>
                  <div class="no-pay-tips">没有微信支付?</div>
                </template>
                <template #default>
                  <div class="show-qrcode">
                    <img :src="proxy.Utils.getLocalResource('img/qrcode.png')" :style="{ width: '200px' }" />
                    <div :style="{ 'text-align': 'left' }">
                      <div class="info">1、微信扫码联系管理员</div>
                      <div class="info">2、备注商品信息</div>
                      <div class="info">3、管理员会给你解决</div>
                    </div>
                  </div>
                </template>
              </el-popover>
            </div>
          </el-form-item>
          <template v-if="formData.payType == 0">
            <el-form-item label="支付码：" prop="payCode">
              <div class="form-item">
                <div class="input">
                  <el-input size="large" placeholder="输入支付码" v-model.trim="formData.payCode" :maxLength="8"></el-input>
                </div>
                <div class="input-tips">输入付款码</div>
              </div>
            </el-form-item>
            <el-form-item label="验证码：" prop="checkCode">
              <div class="form-item">
                <div class="input">
                  <el-input size="large" placeholder="输入图片验证码" v-model.trim="formData.checkCode"
                    @keyup.enter="payCodePay"></el-input>
                </div>
                <img :src="checkCodeUrl" @click="changeCheckCode" class="check-code" />
              </div>
            </el-form-item>
          </template>
        </el-form>
        <div class="pay-btn-panel">
          <div class="pay-btn" @click="getPayQrcode" v-if="formData.payType == 1">
            提交订单
          </div>
          <div class="pay-btn" @click="payCodePay" v-if="formData.payType == 0">
            立即购买
          </div>
        </div>
      </template>

      <!--获取支付信息-->
      <div class="step2" v-if="currentStep == 2">
        <div class="amount-panel">
          应付金额：￥<span class="amount">{{
            proxy.Utils.convert2Amount(productInfo.price)
          }}</span>
        </div>
        <div class="qrcode">
          <QrcodeVue :value="payInfo.payUrl" :size="180"></QrcodeVue>
          <div class="pay-remind">
            <div>
              支付完成后，页面在5秒钟后会跳转，如果未跳转，请点击下方
              “我已经支付”。
            </div>
            <div></div>
          </div>
          <div class="pay-info">
            <div class="iconfont icon-wxpay">
              <span class="text">微信扫码支付</span>
            </div>
            <div class="have-pay" @click="havePay">我已经支付？</div>
          </div>
        </div>
      </div>
      <div class="step3" v-if="currentStep == 3">
        <div class="iconfont icon-ok">恭喜你，支付成功</div>
        <div class="go-order-panel">
          <div class="go-btn" @click="showMyOrder">查看订单</div>
        </div>
      </div>
    </div>
  </Dialog>
</template>

<script setup>
import QrcodeVue from 'qrcode.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()

const dialogConfig = ref({
  show: false,
  title: '购买',
})
const currentStep = ref(1)

const productInfo = ref({})
const pay = async (data) => {
  dialogConfig.value.show = true
  currentStep.value = 1
  productInfo.value = data
  await nextTick()
  formDataRef.value.resetFields()
  formData.value = {
    payType: 1,
  }
}

const formData = ref({})
const formDataRef = ref()
const rules = {
  payType: [{ required: true, message: '请选择支付方式' }],
  payCode: [{ required: true, message: '请输入支付码' }],
  checkCode: [{ required: true, message: '请输入图片验证码' }],
}

const checkCodeUrl = ref(null)
const checkCodeKey = ref()
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode,
    showLoading: false,
  })
  if (!result) {
    return
  }
  checkCodeUrl.value = result.data.checkCode
  checkCodeKey.value = result.data.checkCodeKey
}

const payTypeChange = (e) => {
  if (e === 1) {
    return
  }
  changeCheckCode()
}

//获取支付二维码
const payInfo = ref({})
const getPayQrcode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getPayInfo,
    params: {
      productId: productInfo.value.productId,
      payType: formData.value.payType,
    },
  })
  if (!result) {
    return
  }
  currentStep.value = 2
  payInfo.value = result.data
  startTimer()
}

//校验支付结果
const checkPayInfo = async () => {
  const orderId = payInfo.value.orderId
  if (!orderId) {
    return
  }
  let result = await proxy.Request({
    showLoading: false,
    url: proxy.Api.checkPayOrder,
    params: {
      orderId: orderId,
    },
  })
  if (!result) {
    return
  }
  if (result.data != null && result.data) {
    cleanTimer()
    currentStep.value = 3
    //重新加载积分
    userInfoStore.updateLastReloadTime()
  }
}

//已经支付
const havePay = async () => {
  const orderId = payInfo.value.orderId
  if (!orderId) {
    proxy.Message.error('未获取到支付信息')
    return
  }
  let result = await proxy.Request({
    url: proxy.Api.havePay,
    params: {
      orderId: orderId,
    },
  })
  if (!result) {
    return
  }
  if (result.data != null && result.data) {
    cleanTimer()
    currentStep.value = 3
    //重新加载积分
    userInfoStore.updateLastReloadTime()
  } else {
    proxy.Message.error('未查询到已支付订单,请等30秒后再试')
  }
}

let timmer = ref(null)
const startTimer = () => {
  timmer.value = setInterval(() => {
    checkPayInfo()
  }, 5000)
}

const cleanTimer = () => {
  if (timmer.value !== null) {
    clearInterval(timmer.value)
    timmer.value = null
  }
}
const closePay = () => {
  dialogConfig.value.show = false
  cleanTimer()
}

//支付码支付
const payCodePay = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let result = await proxy.Request({
      url: proxy.Api.buyByPayCode,
      params: {
        checkCodeKey: checkCodeKey.value,
        checkCode: formData.value.checkCode,
        payCode: formData.value.payCode,
        productId: productInfo.value.productId,
      },
      errorCallback: () => {
        changeCheckCode()
      },
    })
    if (!result) {
      return
    }
    currentStep.value = 3
    //重新加载积分
    userInfoStore.updateLastReloadTime()
  })
}

const emits = defineEmits(['showMyOrder'])
const showMyOrder = () => {
  dialogConfig.value.show = false
  emits('showMyOrder')
}

defineExpose({
  pay,
})
</script>

<style lang="scss" scoped>
.pay-panel {
  color: #fff;
  .step-panel {
    margin-bottom: 5px;
    :deep(.el-step__title.is-process) {
      color: #fff;
    }
  }
  .product-info-panel {
    border: 1px solid var(--text);
    border-radius: 5px;
    overflow: hidden;
    .title-info {
      padding: 10px;
      background: var(--activeText);
    }
    .product-info {
      display: flex;
      align-items: center;
      padding: 10px;
      .product-cover {
        border-radius: 5px;
        overflow: hidden;
      }
      .product-name-panel {
        margin: 0px 10px;
        flex: 1;
        width: 0;
        .product-name {
          font-weight: bold;
        }
        .sku-name {
          margin-top: 5px;
          font-size: 13px;
        }
      }
      .price {
        color: #f36106;
        span {
          font-size: 20px;
        }
      }
    }
  }
  .pay-form {
    :deep(.el-form-item) {
      align-items: center;
      .el-form-item__label,
      .el-radio__label {
        color: #fff;
      }
      .el-input__wrapper {
        background-color: #fff0 !important;
        box-shadow: none !important;
        box-shadow: initial !important;
        border: 1px solid rgba(255, 255, 255, 0.3) !important;
        border-radius: 8px;
        .el-input__inner {
          color: #fff;
        }
      }
    }
    .form-item {
      display: flex;
      align-content: center;
      .input {
        flex: 1;
        margin-right: 10px;
      }
      .input-tips {
        color: var(--text);
      }
      .check-code {
        cursor: pointer;
        border-radius: 5px;
      }
    }
    .pay-method {
      width: 100%;
      display: flex;
      align-items: center;
      .no-pay-tips {
        cursor: pointer;
        font-size: 13px;
        margin-left: 15px;
        color: var(--activeText);
      }
    }
  }
  .pay-btn-panel {
    margin-top: 10px;
    display: flex;
    justify-content: center;
    .pay-btn {
      width: 150px;
      text-align: center;
      padding: 10px;
      background: var(--btnBg);
      border-radius: 20px;
      cursor: pointer;
      opacity: 0.8;
      &:hover {
        opacity: 1;
      }
    }
  }

  .step2 {
    text-align: center;
    .amount-panel {
      color: #ffd700;
      font-size: 18px;
      margin-top: 10px;
      .amount {
        font-size: 25px;
      }
    }
    .qrcode {
      margin: 20px auto;
      .pay-remind {
        margin-top: 5px;
        text-align: center;
        color: #aab4bf;
      }
      .pay-info {
        margin-top: 10px;
        display: flex;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .icon-wxpay {
        display: flex;
        align-items: center;
        justify-content: center;
        color: #22ac38;
        font-size: 20px;
        .text {
          color: #aab4bf;
          margin-left: 5px;
          font-size: 14px;
        }
      }
      .have-pay {
        margin-left: 10px;
        cursor: pointer;
        color: #fff;
      }
    }
  }

  .step3 {
    .icon-ok {
      text-align: center;
      color: #22ac38;
      font-size: 16px;
      margin: 40px 0px;
    }
    .icon-ok::before {
      margin-right: 10px;
    }
    .go-order-panel {
      margin: 30px 0px 20px 0px;
      text-align: center;
      .go-btn {
        background: #22ac38;
        color: #fff;
        line-height: 45px;
        text-align: center;
        margin: 0px auto;
        display: inline-block;
        padding: 0px 60px;
        cursor: pointer;
        border-radius: 20px;
        &:hover {
          opacity: 0.8;
        }
      }
    }
  }
}
</style>

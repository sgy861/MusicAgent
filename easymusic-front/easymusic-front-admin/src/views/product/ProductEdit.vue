<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="500px"
    :showCancel="true" @close="dialogConfig.show = false">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <!--input输入-->
      <el-form-item label="封面" prop="cover">
        <div class="cover">
          <!-- <Cover v-model="formData.cover" :size="100" fit="scale-down" :edit="true"></Cover> -->
          <ImageCoverUpload v-model="formData.cover" :width="300" :scale="0.6"></ImageCoverUpload>
        </div>
      </el-form-item>
      <el-form-item label="商品名称" prop="productName">
        <el-input clearable placeholder="请输入商品名称" v-model.trim="formData.productName"></el-input>
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input clearable placeholder="请输入价格" v-model.trim="formData.price">
          <template #suffix> 元 </template>
        </el-input>
      </el-form-item>
      <el-form-item label="积分" prop="integral">
        <el-input clearable placeholder="请输入积分" v-model.trim="formData.integral"></el-input>
      </el-form-item>
      <!-- 单选 -->
      <el-form-item label="上架" prop="onsaleType">
        <el-switch v-model="formData.onsaleType" :active-value="1" :inactive-value="0"></el-switch>
      </el-form-item>
      <el-form-item label="商品描述" prop="productDescription">
        <el-input type="textarea" clearable placeholder="请输入商品描述" v-model="formData.productDescription" :rows="5"
          resize="none"></el-input>
      </el-form-item>
    </el-form>
  </Dialog>
</template>

<script setup>
import ImageCoverUpload from '@/component/ImageCoverUpload.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const dialogConfig = ref({
  show: false,
  title: '编辑商品',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: (e) => {
        submitForm()
      },
    },
  ],
})

const formData = ref({})
const formDataRef = ref()
const rules = {
  cover: [{ required: true, message: '请选择封面' }],
  productName: [{ required: true, message: '请输入商品名称' }],
  price: [
    { required: true, message: '请输入价格' },
    { validator: proxy.Verify.floatNumber, message: '请输入正确的价格' },
  ],
  integral: [
    { required: true, message: '请输入积分' },
    { validator: proxy.Verify.number, message: '请输入正确的积分' },
  ],
  productDescription: [{ required: true, message: '请输入商品描述' }],
}

const show = async (data) => {
  dialogConfig.value.show = true
  await nextTick()
  formDataRef.value.resetFields()
  if (!data) {
    data = { onsaleType: 1, cover: '' }
  }
  formData.value = { ...data }
}
defineExpose({
  show,
})

const emit = defineEmits(['reload'])
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = { ...formData.value }
    Object.assign(params, formData.value)
    if (params.cover instanceof File) {
      params.coverFile = params.cover
      delete params.cover
    }
    let result = await proxy.Request({
      url: proxy.Api.saveProduct,
      params,
    })
    if (!result) {
      return
    }
    dialogConfig.value.show = false
    proxy.Message.success('保存成功')
    emit('reload')
  })
}
</script>

<style lang="scss" scoped>
</style>

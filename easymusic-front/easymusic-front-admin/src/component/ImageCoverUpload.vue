<template>
  <div class="cover" :style="{width:width+'px', height: width * props.scale + 'px' }">
    <el-image :src="coverFile" fit="scale-down" :width="width" v-if="coverFile">
      <template #error>
        <div class="iconfont icon-image-error"></div>
      </template>
    </el-image>
    <div class="mask" @click="selectImage">{{props.coverImage?"重新上传":"上传"}}</div>
  </div>
  <ImageCoverCut ref="imageCoverCutRef" :cutWidth="props.width" :scale="props.scale" @cutImage="cutImage">
  </ImageCoverCut>
</template>
<script setup>
import ImageCoverCut from './ImageCoverCut.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  inject,
  computed,
} from 'vue'
import { asyncComputed } from '@vueuse/core'

const { proxy } = getCurrentInstance()
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()

const props = defineProps({
  modelValue: {
    type: [String, File],
  },
  width: {
    type: Number,
    default: 200,
  },
  //高宽比例
  scale: {
    type: Number,
    default: 1,
  },
})

const coverFile = asyncComputed(async () => {
  if (!props.modelValue) {
    return null
  }
  if (typeof props.modelValue == 'string') {
    return proxy.Api.getResource + props.modelValue
  } else if (props.modelValue instanceof File) {
    const base64 = await convertFile2Base64(proxy.modelValue)
    return base64
  }
})

const convertFile2Base64 = (file) => {
  return new Promise((resolve, reject) => {
    let img = new FileReader()
    img.readAsDataURL(file)
    img.onload = ({ target }) => {
      resolve(target.result)
    }
  })
}
const imageCoverCutRef = ref()
const selectImage = async () => {
  imageCoverCutRef.value.show()
}

const emits = defineEmits(['update:modelValue'])
const cutImage = (file) => {
  emits('update:modelValue', file)
}
</script>

<style lang="scss" scoped>
.cover {
  background: #f0f0f0;
  position: relative;
  .mask {
    width: 100%;
    position: absolute;
    left: 0px;
    bottom: 0px;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1;
    color: #fff;
    text-align: center;
    cursor: pointer;
    padding: 5px 0px;
    line-height: normal;
  }
}
</style>

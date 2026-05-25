<template>
  <div>
    <div class="cover">
      <Cover :cover="modelValue" v-if="modelValue" :width="100" borderRadius="0"></Cover>
      <div class="mask" @click="selectImage">上传</div>
    </div>
    <ImageCoverCut ref="imageCoverCutRef" :cutWidth="100" :scale="1" @cutImage="cutImageHandler"></ImageCoverCut>
  </div>

</template>

<script setup>
import ImageCoverCut from './ImageCoverCut.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  modelValue: {
    type: [String, File],
  },
})

const imageCoverCutRef = ref()
const selectImage = () => {
  imageCoverCutRef.value.show()
}

const emits = defineEmits(['update:modelValue'])
const cutImageHandler = (file) => {
  emits('update:modelValue', file)
}
</script>

<style lang="scss" scoped>
.cover {
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
  }
}
</style>

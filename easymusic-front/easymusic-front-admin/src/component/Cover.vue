<template>
  <div
    class="image-panel"
    ref="coverRef"
    :style="{
      'border-radius': borderRadius,
      width: width ? width + 'px' : '100%',
      height: width ? width * scale + 'px' : '100%',
    }"
  >
    <el-image :lazy="lazy" :src="fileSource" :fit="fit">
      <template #error>
        <img
          :src="proxy.Utils.getLocalResource('404.png')"
          class="el-image__inner"
          :style="{ 'object-fit': fit }"
        />
      </template>
      <template #placeholder>
        <div class="loading">
          <img :src="proxy.Utils.getLocalResource('/img/loading.gif')" />
        </div>
      </template>
    </el-image>
  </div>
</template>

<script setup>
import { progressProps } from "element-plus";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  computed,
  onMounted,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const props = defineProps({
  cover: {
    type: String,
  },
  width: {
    type: Number,
  },
  scale: {
    type: Number,
    default: 1,
  },
  fit: {
    type: String,
    default: "cover",
  },
  borderRadius: {
    type: String,
    default: "5px",
  },
  lazy: {
    type: Boolean,
    default: true,
  },
  defaultImg: {
    type: String,
    default: "img/cover.png",
  },
});

const fileSource = computed(() => {
  if (!props.cover && props.defaultImg) {
    return proxy.Utils.getLocalResource(props.defaultImg);
  }
  if (props.cover?.startsWith("http")) {
    return props.cover;
  }
  return proxy.Api.getResource + props.cover;
});
</script>

<style lang="scss" scoped>
.image-panel {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
  :deep(.is-loading) {
    display: none;
  }
  :deep(.el-image__wrapper) {
    position: relative;
    vertical-align: top;
    width: 100%;
    height: 100%;
    display: flex;
  }
  .icon-image-error {
    margin: 0px auto;
    font-size: 20px;
    color: #838383;
    height: 100%;
  }
  .loading {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100px;
    img {
      width: 20px;
    }
  }
}
</style>

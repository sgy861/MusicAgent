<template>
  <div class="no-data">
    <div class="iconfont icon-nodata"></div>
    <div class="msg">{{ msg }}</div>
  </div>
</template>

<script setup>
const props = defineProps({
  msg: {
    type: String,
  },
});
</script>

<style lang="scss">
.no-data {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  .icon-nodata {
    color: var(--text2);
    font-size: 50px;
  }
  .msg {
    margin-top: 10px;
    color: var(--text2);
    font-size: 14px;
  }
}
</style>
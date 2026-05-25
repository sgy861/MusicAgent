<template>
  <div class="back-btn iconfont icon-narrow-left" @click="back"></div>
</template>

<script setup>
const back = () => {
  history.back(-1);
};
</script>

<style lang="scss" scoped>
.back-btn {
  color: #fff;
  width: 40px;
  height: 40px;
  background: #202128;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  &:hover {
    opacity: 0.9;
  }
}
</style>

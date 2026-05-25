<template>
  <el-config-provider :locale="zhCn" :message="{ max: 1 }">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
</script>

<style lang="scss" scoped>
</style>

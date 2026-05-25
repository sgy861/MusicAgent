<template>
  <Cover :source="avatar" :width="width" borderRadius="50%"></Cover>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
const props = defineProps({
  avatar: {
    type: String,
  },
  width: {
    type: Number,
  },
})
</script>

<style lang="scss" scoped>
</style>
<template>
  <div class="tab-select">
    <div
      v-for="item in data"
      :class="['tab', prpos.modelValue?.includes(item) ? 'selected' : '']"
      @click="selectTab(item)"
    >
      {{ item }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
const prpos = defineProps({
  data: {
    type: Array,
    default: [],
  },
  modelValue: {
    type: [Array, String],
    default: [],
  },
  multiple: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["update:modelValue"]);
const selectTab = (item) => {
  if (prpos.multiple) {
    const newValue = [...(proxy.modelValue || [])];
    const index = newValue.indexOf(item);
    // 切换选中状态
    if (index > -1) {
      newValue.splice(index, 1);
    } else {
      newValue.push(item);
    }
    emit("update:modelValue", newValue);
  } else {
    emit("update:modelValue", item);
  }
};
</script>

<style lang="scss" scoped>
.tab-select {
  display: flex;
  flex-wrap: wrap;
  .tab {
    color: var(--HiText);
    background: var(--text);
    border-radius: 12px;
    padding: 3px 10px;
    margin: 0px 8px 8px 0px;
    cursor: pointer;
    user-select: none;
    font-size: 14px;
  }
  .selected {
    background: var(--activeText);
  }
}
</style>

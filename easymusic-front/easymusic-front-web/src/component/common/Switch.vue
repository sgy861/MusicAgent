<template>
  <div class="switch-panel">
    <div
      :class="[
        'switch-item',
        modelValue == item.value ? 'switch-item-active' : '',
      ]"
      v-for="item in data"
      @click="changeModel(item)"
    >
      {{ item.label }}
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
    type: [Boolean, Number, String],
  },
});

const emit = defineEmits(["update:modelValue", "change"]);
const changeModel = (item) => {
  if (prpos.modelValue === item.value) {
    return;
  }
  emit("update:modelValue", item.value);
  emit("change", item);
};
</script>

<style lang="scss" scoped>
.switch-panel {
  display: flex;
  align-items: center;
  background: #271b39;
  border-radius: 25px;
  margin: 10px 0px;
  width: 250px;
  line-height: 35px;
  cursor: pointer;
  &:hover {
    background: #3e344f;
  }
  .switch-item {
    width: 50%;
    color: #fff;
    text-align: center;
    user-select: none;
  }
  .switch-item-active {
    background: #fff;
    color: #271b39;
    border-radius: 25px;
  }
}
</style>

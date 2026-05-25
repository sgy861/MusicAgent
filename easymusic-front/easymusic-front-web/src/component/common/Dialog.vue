<template>
  <el-dialog :show-close="false" :draggable="draggable" :model-value="show" :close-on-click-modal="false"
    class="cust-dialog" :top="top + 'px'" :width="width" @close="close" @open="open" append-to-body>
    <template #header="{ close, titleId, titleClass }">
      <div class="header">
        <div class="title">{{ title }}</div>
        <div class="iconfont icon-close" @click="close"></div>
      </div>
    </template>
    <div class="dialog-body" :style="{ 'max-height': maxHeight + 'px', padding: padding + 'px' }">
      <slot></slot>
    </div>
    <template v-if="(buttons && buttons.length > 0) || showCancel">
      <div class="dialog-footer">
        <el-button link @click="close" v-if="showCancel"> 取消 </el-button>
        <el-button v-for="btn in buttons" :type="btn.type || 'primary'" @click="btn.click">
          {{ btn.text }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  draggable: {
    type: Boolean,
    default: true,
  },
  title: {
    type: String,
  },
  show: {
    type: Boolean,
    default: false,
  },
  showClose: {
    type: Boolean,
    default: true,
  },
  showCancel: {
    type: Boolean,
    default: true,
  },
  top: {
    type: Number,
    default: 50,
  },
  width: {
    type: String,
    default: '30%',
  },
  buttons: {
    type: Array,
  },
  padding: {
    type: Number,
    default: 15,
  },
})

const maxHeight =
  window.innerHeight -
  props.top -
  (!props.buttons || props.buttons.length == 0 ? 50 : 90)

const emit = defineEmits(['close', 'open'])
const close = () => {
  emit('close')
}

const open = () => {
  emit('open')
}
</script>

<style lang="scss">
.cust-dialog {
  padding: 0px !important;
  margin-bottom: 5px !important;
  background: linear-gradient(130deg, #1a3c8d, #695bc5 47%, #3a1b6f);
  border-radius: 8px;
  .el-dialog__header {
    padding: 5px 0px 5px 10px;
  }
  .el-dialog__body {
    padding: 0px;
  }
  .header {
    padding: 10px 10px 0px 10px;
    display: flex;
    align-items: center;
    .title {
      flex: 1;
      width: 0;
      font-size: 20px;
      color: #fff;
    }
    .icon-close {
      color: #fff;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      &:hover {
        background: rgb(72, 72, 72);
      }
    }
  }

  .dialog-body {
    overflow: auto;
    overflow-x: hidden;
    padding-top: 0px !important;
  }
  .dialog-footer {
    text-align: right;
    padding: 0px 20px 10px;
  }

  @media (max-width: 500px) {
    max-width: 95%;
  }
}
</style>
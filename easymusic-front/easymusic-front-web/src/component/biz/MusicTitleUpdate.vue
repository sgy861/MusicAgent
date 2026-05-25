<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="400px"
    :showCancel="false"
    @close="dialogConfig.show = false"
  >
    <el-form
      class="form-panel"
      :model="formData"
      :rules="rules"
      ref="formDataRef"
      label-width="50px"
      @submit.prevent
    >
      <!--input输入-->
      <el-form-item label="标题" prop="">
        <el-input
          clearable
          placeholder="请输入标题"
          v-model.trim="formData.musicTitle"
          :maxlength="30"
        ></el-input>
      </el-form-item>
    </el-form>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const dialogConfig = ref({
  show: false,
  title: "修改标题",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: (e) => {
        submitForm();
      },
    },
  ],
});

const formData = ref({});
const formDataRef = ref();
const rules = {
  musicTitle: [{ required: true, message: "请输入标题" }],
};

const emits = defineEmits(["update"]);
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: proxy.Api.changeMusicTitle,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("修改成功");
    dialogConfig.value.show = false;
    emits("update", formData.value.musicTitle);
  });
};

const show = async (data) => {
  dialogConfig.value.show = true;
  await nextTick();
  formDataRef.value.resetFields();
  formData.value = { ...data };
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
.form-panel {
  :deep(.el-form-item) {
    align-items: center;
    .el-form-item__label,
    .el-radio__label {
      color: #fff;
    }
    .el-input__wrapper {
      background-color: #fff0 !important;
      box-shadow: none !important;
      box-shadow: initial !important;
      border: 1px solid rgba(255, 255, 255, 0.3) !important;
      border-radius: 8px;
      .el-input__inner {
        color: #fff;
      }
    }
  }
}
</style>

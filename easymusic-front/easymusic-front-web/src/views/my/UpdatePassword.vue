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
      label-width="0px"
      @submit.prevent
    >
      <el-form-item label="" prop="oldPassword">
        <el-input
          type="password"
          show-password
          clearable
          placeholder="请输入原始密码"
          v-model.trim="formData.oldPassword"
          size="large"
        ></el-input>
      </el-form-item>
      <el-form-item label="" prop="password">
        <el-input
          type="password"
          show-password
          clearable
          placeholder="请输入新密码"
          v-model.trim="formData.password"
          size="large"
        ></el-input>
      </el-form-item>
      <el-form-item label="" prop="rePassword">
        <el-input
          type="password"
          show-password
          clearable
          placeholder="请再次输入新密码"
          v-model.trim="formData.rePassword"
          size="large"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <div class="save-btn" @click="updatePassword">修改密码</div>
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
import { useUserInfoStore } from "@/stores/userInfoStore";
const userInfoStore = useUserInfoStore();

const dialogConfig = ref({
  show: false,
  title: "修改密码",
  buttons: [],
});

const checkRePassword = (rule, value, callback) => {
  if (value !== formData.value.password) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};
const formData = ref({});
const formDataRef = ref();
const rules = {
  oldPassword: [{ required: true, message: "请输入原始密码" }],
  password: [
    { required: true, message: "请输入新密码" },
    {
      validator: proxy.Verify.password,
      message: "密码只能是数字、字母、特殊字符8~18位",
    },
  ],
  rePassword: [
    { required: true, message: "请再次输入新密码" },
    {
      validator: checkRePassword,
      message: "两次输入的密码不一致",
    },
  ],
};

const selectFile = ({ file }) => {
  formData.value.avatar = file;
};

const show = async () => {
  dialogConfig.value.show = true;
  await nextTick();
  formDataRef.value.resetFields();
  formData.value = { ...userInfoStore.userInfo };
};

const updatePassword = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: proxy.Api.updatePassword,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("修改成功");
    dialogConfig.value.show = false;
  });
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
.form-panel {
  :deep(.el-form-item) {
    .el-input__wrapper {
      background-color: #fff0 !important;
      box-shadow: none !important;
      box-shadow: initial !important;
      border: 1px solid rgba(255, 255, 255, 0.3) !important;
      border-radius: 8px;
    }
    .el-form-item__label {
      color: var(--text);
    }
    .el-input__wrapper {
      background: none;
    }
    .el-input__inner {
      background: none;
      color: var(--text);
    }
    .el-radio__label {
      color: var(--text);
    }
  }
  .save-btn {
    width: 100%;
    cursor: pointer;
    padding: 10px;
    text-align: center;
    border-radius: 50px;
    background: var(--btnBg);
    color: #fff;
  }
}
</style>

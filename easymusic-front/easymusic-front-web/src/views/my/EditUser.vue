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
      <div class="avatar-panel">
        <ImageCoverUpload
          v-model="formData.avatar"
          :width="100"
          background="#3A2D87"
        ></ImageCoverUpload>
      </div>
      <!--input输入-->
      <div class="uid-info">用户ID：{{ formData.userId }}</div>
      <el-form-item prop="nickName">
        <el-input
          clearable
          placeholder="请输入昵称"
          v-model.trim="formData.nickName"
          :maxlength="20"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <div class="save-btn" @click="updateUserInfo">保存</div>
      </el-form-item>
    </el-form>
  </Dialog>
</template>

<script setup>
import ImageCoverUpload from "@/component/common/ImageCoverUpload.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useUserInfoStore } from "@/stores/userInfoStore";
const userInfoStore = useUserInfoStore();

const dialogConfig = ref({
  show: false,
  title: "修改个人信息",
  buttons: [],
});

const formData = ref({});
const formDataRef = ref();
const rules = {
  nickName: [{ required: true, message: "请输入昵称" }],
  sex: [{ required: true, message: "请选择性别" }],
};

const show = async () => {
  dialogConfig.value.show = true;
  await nextTick();
  formDataRef.value.resetFields();
  formData.value = { ...userInfoStore.userInfo };
};

const updateUserInfo = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: proxy.Api.updateUserInfo,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("修改成功");
    dialogConfig.value.show = false;
    userInfoStore.setLoginInfo(result.data);
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
  .avatar-panel {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }
  .uid-info {
    text-align: center;
    margin: 10px auto;
    color: var(--text);
  }
  .save-btn {
    width: 100%;
    cursor: pointer;
    padding: 8px;
    text-align: center;
    border-radius: 50px;
    background: var(--btnBg);
    color: #fff;
  }
}
</style>

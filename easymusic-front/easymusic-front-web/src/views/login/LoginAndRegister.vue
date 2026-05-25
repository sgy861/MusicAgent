<template>
  <Dialog
    :show="userInfoStore.showLogin"
    :title="isLogin ? '登录' : '注册'"
    :buttons="dialogConfig.buttons"
    :width="dialogWidth"
    :showCancel="false"
    @close="close"
    :padding="20"
  >
    <div class="login-form">
      <div class="error-msg">{{ errorMsg }}</div>
      <el-form
        :model="formData"
        ref="formDataRef"
        label-width="0px"
        @submit.prevent
      >
        <el-form-item prop="email">
          <el-input
            class="input"
            size="large"
            clearable
            placeholder="请输入邮箱"
            v-model.trim="formData.email"
            maxLength="30"
            @focus="clearVerify"
            :input-style="{ border: 'none' }"
          >
            <template #prefix>
              <span class="iconfont icon-email"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="nickName" v-if="!isLogin">
          <el-input
            size="large"
            clearable
            placeholder="请输入昵称"
            v-model.trim="formData.nickName"
            maxLength="15"
            @focus="clearVerify"
          >
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>
        <!--登录密码-->
        <el-form-item prop="password">
          <el-input
            type="password"
            size="large"
            placeholder="请输入密码"
            v-model.trim="formData.password"
            show-password
            @focus="clearVerify"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="rePassword" v-if="!isLogin">
          <el-input
            type="password"
            size="large"
            placeholder="请再次输入密码"
            v-model.trim="formData.rePassword"
            show-password
            @focus="clearVerify"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input
              size="large"
              placeholder="请输入验证码"
              v-model.trim="formData.checkCode"
              @focus="clearVerify"
              @keyup.enter="submit"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img
              :src="checkCodeUrl"
              class="check-code"
              @click="changeCheckCode"
            />
          </div>
        </el-form-item>
        <div class="login-btn" @click="submit">
          {{ isLogin ? "登录" : "注册" }}
        </div>
        <div class="bottom-link" @click="changeOpType">
          {{ isLogin ? "没有账号?" : "已有账号?" }}
        </div>
      </el-form>
    </div>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useUserInfoStore } from "@/stores/userInfoStore";
const userInfoStore = useUserInfoStore();
import md5 from "js-md5";

const dialogConfig = ref({
  show: true,
  buttons: [],
});

const dialogWidth = computed(() => {
  return window.innerWidth > 500 ? "500px" : "350px";
});

const checkCodeUrl = ref(null);
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode,
  });
  if (!result) {
    return;
  }
  checkCodeUrl.value = result.data.checkCode;
  localStorage.setItem("checkCodeKey", result.data.checkCodeKey);
};
changeCheckCode();

const isLogin = ref(true);
const formData = ref({});
const formDataRef = ref();

const changeOpType = async () => {
  isLogin.value = !isLogin.value;
  await nextTick();
  formDataRef.value.resetFields();
  formData.value = {};
  changeCheckCode();
  clearVerify();
};

const errorMsg = ref();
const checkValue = (type, value, msg) => {
  if (proxy.Utils.isEmpty(value)) {
    errorMsg.value = msg;
    return false;
  }
  if (type && !proxy.Verify[type](value)) {
    errorMsg.value = msg;
    return false;
  }
  return true;
};

const clearVerify = () => {
  errorMsg.value = "";
};

const showLoading = ref(false);
const submit = async () => {
  clearVerify();
  if (!checkValue("checkEmail", formData.value.email, "请输入正确的邮箱")) {
    return;
  }
  if (
    !isLogin.value &&
    !checkValue(null, formData.value.nickName, "请输入昵称")
  ) {
    return;
  }
  if (
    !checkValue(
      "checkPassword",
      formData.value.password,
      "密码只能是数字，字母，特殊字符8-18位，且必须包含数字或者字母"
    )
  ) {
    return;
  }
  if (!checkValue(null, formData.value.checkCode, "请输入验证码")) {
    return;
  }

  if (!isLogin.value && proxy.Utils.isEmpty(formData.value.rePassword)) {
    errorMsg.value = "请再次输入密码";
    return;
  }
  if (!isLogin.value && formData.value.password != formData.value.rePassword) {
    errorMsg.value = "两次输入的密码不一致";
    return;
  }

  if (isLogin.value) {
    showLoading.value = true;
  }
  showLoading.value = true;
  let result = await proxy.Request({
    url: isLogin.value ? proxy.Api.login : proxy.Api.register,
    showLoading: false,
    showError: false,
    params: {
      email: formData.value.email,
      password: isLogin.value
        ? md5(formData.value.password)
        : formData.value.password,
      checkCode: formData.value.checkCode,
      nickName: formData.value.nickName,
      checkCodeKey: localStorage.getItem("checkCodeKey"),
    },
    errorCallback: (response) => {
      showLoading.value = false;
      changeCheckCode();
      errorMsg.value = response.info;
    },
  });
  showLoading.value = false;
  if (!result) {
    return;
  }
  if (isLogin.value) {
    localStorage.setItem("token", result.data.token);
    userInfoStore.setLoginInfo(result.data);
    userInfoStore.showLogin = false;
    proxy.Message.success("登录成功");
  } else {
    proxy.Message.success("注册成功");
    changeOpType();
  }
};
const close = () => {
  userInfoStore.showLogin = false;
  isLogin.value = true;
};
</script>

<style lang="scss" scoped>
.login-form {
  padding: 0px 15px;
  :deep(.el-input__wrapper) {
    background-color: #fff0 !important;
    box-shadow: none !important;
    box-shadow: initial !important;
    border: 1px solid rgba(255, 255, 255, 0.3) !important;
    border-radius: 8px;
    .el-input__inner {
      color: #fff;
    }
  }
  .error-msg {
    line-height: 30px;
    height: 30px;
    color: rgb(242, 73, 73);
  }
  .check-code-panel {
    width: 100%;
    display: flex;
    .check-code {
      cursor: pointer;
      width: 120px;
      margin-left: 5px;
      border-radius: 5px;
    }
  }

  .login-btn {
    cursor: pointer;
    margin-top: 20px;
    width: 100%;
    border-radius: 30px;
    background: var(--btnBg);
    padding: 12px 0px;
    color: #fff;
    text-align: center;
    font-size: 16px;
  }
  .bottom-link {
    margin-top: 10px;
    text-align: right;
    font-size: 13px;
    color: #fff;
    cursor: pointer;
  }
}
</style>

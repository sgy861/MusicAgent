<template>
  <div>
    <Dialog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="400px"
      :showCancel="false"
      @close="dialogConfig.show = false"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formDataRef"
        label-width="80px"
        @submit.prevent
      >
        <!--input输入-->
        <el-form-item label="金额" prop="amount" v-if="!payCode">
          <el-input
            clearable
            placeholder="请输入付款码金额"
            v-model.trim="formData.amount"
          ></el-input>
        </el-form-item>
        <el-form-item label="付款码" v-if="payCode">
          {{ payCode }}
          <el-button
            type="primary"
            @click="copy"
            :style="{ 'margin-left': '10px' }"
            >复制</el-button
          >
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
const { proxy } = getCurrentInstance();

const dialogConfig = ref({
  show: false,
  title: "新建付款码",
  buttons: [
    {
      text: "确定",
      click: (e) => {
        submitForm();
      },
    },
  ],
});

const payCode = ref();

const show = () => {
  dialogConfig.value.show = true;
  dialogConfig.value.buttons = [
    {
      text: "确定",
      click: (e) => {
        submitForm();
      },
    },
  ];
  payCode.value = null;
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = {};
  });
};
defineExpose({
  show,
});

const formData = ref({});
const formDataRef = ref();
const rules = {
  amount: [
    { required: true, message: "请输入付款码金额" },
    {
      validate: proxy.Verify.money,
      message: "请输入正确的金额",
    },
  ],
};

const emit = defineEmits(["reload"]);
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: proxy.Api.createCode,
      params,
    });
    if (!result) {
      return;
    }
    payCode.value = result.data;
    dialogConfig.value.buttons = [
      {
        text: "关闭",
        click: (e) => {
          dialogConfig.value.show = false;
        },
      },
    ];
    emit("reload");
  });
};

const copy = async () => {
  await navigator.clipboard.writeText(payCode.value);
  proxy.Message.success("复制成功");
};
</script>

<style lang="scss" scoped>
</style>

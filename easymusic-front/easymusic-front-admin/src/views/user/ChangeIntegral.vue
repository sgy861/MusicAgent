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
      :model="formData"
      :rules="rules"
      ref="formDataRef"
      label-width="70px"
      @submit.prevent
    >
      <!--input输入-->
      <el-form-item label="积分数" prop="integral">
        <el-input
          clearable
          placeholder="输入负数就是扣减积分，正数就是增加积分"
          v-model.trim="formData.integral"
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
  title: "改变积分",
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
  integral: [{ required: true, message: "请输入积分" }],
};

const emits = defineEmits(["reload"]);
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: proxy.Api.changeIntegral,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("修改成功");
    dialogConfig.value.show = false;
    emits("reload");
  });
};

const show = async (userId) => {
  dialogConfig.value.show = true;
  await nextTick();
  formDataRef.value.resetFields();
  formData.value.userId = userId;
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
</style>

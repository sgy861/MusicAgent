<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="500px"
    showCancel
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
      <el-form-item
        label="字典编号"
        :prop="formData.dictPcode === '0' ? 'dictCode' : ''"
      >
        <el-input
          clearable
          placeholder="请输入编号"
          v-model.trim="formData.dictCode"
          :maxlength="30"
          show-word-limit
        ></el-input>
      </el-form-item>
      <el-form-item label="字典值" v-if="formData.dictPcode !== '0'">
        <el-input
          clearable
          placeholder="输入字典值"
          v-model.trim="formData.dictValue"
          :maxlength="30"
          show-word-limit
        ></el-input>
      </el-form-item>
      <el-form-item label="字典描述" prop="dictDesc">
        <el-input
          clearable
          placeholder="请输入描述"
          v-model.trim="formData.dictDesc"
          :maxlength="100"
          show-word-limit
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
  title: "编辑字典",
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
  dictCode: [{ required: true, message: "请输入编号" }],
  dictValue: [{ required: true, message: "请输入编号值" }],
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
      url: proxy.Api.saveSysDict,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("保存成功");
    dialogConfig.value.show = false;
    emit("reload");
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
</style>

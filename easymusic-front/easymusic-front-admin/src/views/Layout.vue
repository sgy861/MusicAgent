<template>
  <div class="layout">
    <div class="left">
      <div
        :class="[
          'menu-item',
          item.codes.includes(route.meta.code) ? 'active' : '',
        ]"
        v-for="item in menuList"
        @click="jump(item)"
      >
        <div :class="['iconfont', `icon-${item.icon}`]"></div>
        <div class="name">{{ item.name }}</div>
      </div>
      <div class="logout" @click="logout">退出</div>
    </div>
    <div class="right">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const menuList = ref([
  {
    icon: "dict",
    name: "字典",
    url: "/dict/sysDict",
    codes: ["dict"],
  },
  {
    icon: "product",
    name: "商品",
    url: "/product/productList",
    codes: ["product"],
  },
  {
    icon: "paycode",
    name: "付款码",
    url: "/paycode/paycodeList",
    codes: ["paycode"],
  },
  {
    icon: "order",
    name: "订单",
    url: "/order/orderList",
    codes: ["order"],
  },
  {
    icon: "music",
    name: "音乐",
    url: "/music/musicList",
    codes: ["music"],
  },
  {
    icon: "user",
    name: "用户",
    url: "/user/userList",
    codes: ["user"],
  },
]);

const jump = (item) => {
  router.push(item.url);
};

const logout = async () => {
  proxy.Confirm({
    message: "确定要退出吗?",
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.logout,
      });
      if (!result) {
        return;
      }
      localStorage.removeItem("token");
      router.push("/");
    },
  });
};
</script>

<style lang="scss" scoped>
.layout {
  display: flex;
  .left {
    width: 80px;
    padding: 5px;
    height: calc(100vh);
    display: flex;
    flex-direction: column;
    justify-content: center;
    background: #fafcff;
    .menu-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 10px;
      border-radius: 10px;
      cursor: pointer;
      margin-bottom: 5px;
      &:hover {
        background: #f2f3f7;
      }
      .iconfont {
        font-size: 30px;
        color: #6d7286;
      }
      .name {
        font-size: 12px;
        margin-top: 3px;
      }
    }
    .active {
      background: #f2f3f7;
      .iconfont {
        color: #1f2433;
        color: #1e5cff;
      }
      .name {
        font-size: #1f2433;
        color: #1e5cff;
      }
    }

    .logout {
      text-align: center;
      position: absolute;
      bottom: 20px;
      left: 20px;
      color: #1e5cff;
      cursor: pointer;
    }
  }
  .right {
    flex: 1;
    padding: 10px;
    background: #fff;
  }
}
</style>

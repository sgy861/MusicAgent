import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/account/Account.vue'),
    },
    {
      path: "/",
      name: "框架页",
      component: () => import('@/views/Layout.vue'),
      redirect: "/login",
      children: [{
        path: "/dict/sysdict",
        name: "字典",
        component: () => import('@/views/dict/SysDict.vue'),
        meta: {
          code: "dict",
        }
      }, {
        path: "/product/productList",
        name: "商品",
        component: () => import('@/views/product/ProductList.vue'),
        meta: {
          code: "product",
        }
      }, {
        path: "/paycode/paycodeList",
        name: "付款码",
        component: () => import('@/views/paycode/PaycodeList.vue'),
        meta: {
          code: "paycode",
        }
      }, {
        path: "/order/orderList",
        name: "订单",
        component: () => import('@/views/order/OrderList.vue'),
        meta: {
          code: "order",
        }
      }, {
        path: "/music/musicList",
        name: "音乐",
        component: () => import('@/views/music/MusicList.vue'),
        meta: {
          code: "music",
        }
      }, {
        path: "/user/userList",
        name: "用户",
        component: () => import('@/views/user/UserList.vue'),
        meta: {
          code: "user",
        }
      }]
    }
  ],
})

export default router

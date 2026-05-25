import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "框架页",
      component: () => import('@/views/Layout.vue'),
      redirect: "/",
      children: [{
        path: "/",
        name: "首页",
        component: () => import('@/views/index/Index.vue'),
        meta: {
          code: "index",
        }
      }, {
        path: "/latest",
        name: "最新发布",
        component: () => import('@/views/index/Latest.vue'),
        meta: {
          code: "index",
        }
      }, {
        path: "/idea",
        name: "音乐创作",
        component: () => import('@/views/idea/Idea.vue'),
        meta: {
          code: "idea",
        }
      }, {
        path: "/idea/:creationId",
        name: "同款创作",
        component: () => import('@/views/idea/Idea.vue'),
        meta: {
          code: "idea",
        }
      }, {
        path: "/play/:musicId",
        name: "详情",
        component: () => import('@/views/music-play/MusicDetail.vue'),
      }, {
        path: "/my",
        name: "我的",
        component: () => import('@/views/my/My.vue'),
        meta: {
          code: "my",
        }
      }, {
        path: "/buy",
        name: "充值",
        component: () => import('@/views/buy/Buy.vue'),
        meta: {
          code: "buy",
        }
      }, {
        path: "/user/:userId",
        name: "用户中心",
        component: () => import('@/views/user/UserHome.vue'),
        meta: {
          code: "user",
        }
      }]
    }
  ],
})

export default router

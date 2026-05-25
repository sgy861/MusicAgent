<template>
  <div class="left-side-panel">
    <div class="bg"></div>
    <div class="left-side">
      <div class="logo">EasyMusic</div>
      <div class="menu-list">
        <template v-for="item in menuList">
          <div :class="[
              'menu-item',
              item.codes.includes(route.meta.code) ? 'active' : '',
            ]" @click="jump(item)">
            <div :class="['iconfont', 'icon-' + item.icon]"></div>
            <div class="menu-name">{{ item.name }}</div>
          </div>
        </template>
      </div>
      <div class="integral-panel">
        <div class="integra">
          积分：{{ userInfoStore.userInfo.integral || 0 }}
        </div>
        <div class="record-btn" @click="showIntegralRecord">积分记录</div>
      </div>
      <div class="user-info-panel">
        <div class="login-btn" @click="login" v-if="Object.keys(userInfoStore.userInfo).length == 0">
          登录
        </div>
        <el-popover popper-class="user-info-popper" placement="top" trigger="click" :show-arrow="false" :offset="5"
          :width="150" ref="userInfoPopoverRef">
          <template #reference>
            <div class="user-info" v-if="Object.keys(userInfoStore.userInfo).length > 0">
              <Avatar :avatar="userInfoStore.userInfo.avatar" :width="30"></Avatar>
              <div class="user-name">{{ userInfoStore.userInfo.nickName }}</div>
            </div>
          </template>
          <div class="menu-item" @click="updatePassword">修改密码</div>
          <div class="menu-item" @click="editUserInfo">编辑个人资料</div>
          <div class="menu-item" @click="logout">退出登录</div>
        </el-popover>
      </div>
    </div>
  </div>
  <EditUser ref="editUserRef"></EditUser>

  <UpdatePassword ref="updatePasswordRef"></UpdatePassword>

  <IntegralRecord ref="integralRecordRef"></IntegralRecord>
</template>

<script setup>
import IntegralRecord from '@/views/my/IntegralRecord.vue'
import UpdatePassword from '@/views/my/UpdatePassword.vue'
import EditUser from '@/views/my/EditUser.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()

const menuList = ref([
  {
    name: '首页',
    icon: 'home',
    codes: ['index'],
    path: '/',
  },
  {
    name: '创作音乐',
    icon: 'music',
    path: '/idea',
    codes: ['idea', 'pure'],
  },
  {
    name: '我的',
    icon: 'user',
    path: '/my',
    codes: ['my'],
  },
  {
    name: '充值',
    icon: 'buy',
    path: '/buy',
    codes: ['buy'],
  },
])

const jump = (item) => {
  if (item.path == '/my' && !userInfoStore.checkLogin()) {
    return
  }
  router.push(item.path)
}

const login = () => {
  userInfoStore.showLogin = true
}

const userInfoPopoverRef = ref()
const logout = async () => {
  userInfoPopoverRef.value.hide()
  proxy.Confirm({
    message: '确定要退出吗?',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.logout,
      })
      if (!result) {
        return
      }
      userInfoStore.userInfo = {}
      userInfoStore.showLogin = false
      localStorage.removeItem('token')
    },
  })
}

const editUserRef = ref()
const editUserInfo = () => {
  userInfoPopoverRef.value.hide()
  editUserRef.value.show()
}

const updatePasswordRef = ref()
const updatePassword = () => {
  userInfoPopoverRef.value.hide()
  updatePasswordRef.value.show()
}

const integralRecordRef = ref()
const showIntegralRecord = () => {
  integralRecordRef.value.show()
}
</script>

<style lang="scss" scoped>
.left-side-panel {
  width: 200px;
  height: 100%;
  color: #fff;
  overflow: hidden;
  background: linear-gradient(180deg, #0000, #94adff1a);
  .bg {
    position: fixed;
    top: 0;
    left: 0;
    height: 405px;
    width: 200px;
    background: url('../assets/img/left-side-bg.png');
    background-size: 100% 100%;
    background-position: center center;
    z-index: 0;
  }
  .left-side {
    position: absolute;
    z-index: 1;
    width: 200px;
    height: calc(100vh);
    display: flex;
    flex-direction: column;
    .logo {
      font-size: 20px;
      font-weight: bold;
      padding: 20px;
    }
    .menu-list {
      flex: 1;
      .menu-item {
        padding: 10px 0px 10px 20px;
        color: var(--text);
        display: flex;
        align-items: center;
        cursor: pointer;
        &:hover {
          color: var(--hiText);
        }
        .iconfont {
          font-size: 20px;
        }
        .menu-name {
          margin-left: 10px;
        }
      }
      .active {
        color: var(--activeText);
        position: relative;
        &::before {
          content: '';
          left: 0px;
          top: 12px;
          bottom: 12px;
          width: 3px;
          background: var(--activeText);
          position: absolute;
          border-radius: 2px;
          font-weight: bold;
        }
        &:hover {
          color: var(--activeText);
        }
      }
    }
  }

  .integral-panel {
    color: #fff;
    display: flex;
    justify-content: space-between;
    justify-items: center;
    padding: 10px;
    .integral {
      flex: 1;
    }
    .record-btn {
      display: flex;
      align-items: center;
      font-size: 13px;
      cursor: pointer;
      color: var(--activeText);
    }
  }

  .user-info-panel {
    padding: 10px;
    .login-btn {
      cursor: pointer;
      padding: 10px;
      text-align: center;
      border-radius: 50px;
      background: var(--btnBg);
    }
    .user-info {
      display: flex;
      align-items: center;
      background: #fff3;
      border-radius: 20px;
      padding: 5px 10px;
      cursor: pointer;
      .user-name {
        flex: 1;
        width: 0;
        margin-left: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: var(--activeText);
      }
      .icon-logout {
        margin-left: 5px;
        color: #fff;
      }
    }
  }

  @media (max-width: 500px) {
    .bg,
    .logo {
      display: none;
    }

    .left-side {
      height: calc(100vh - 50px);
    }
  }
}
</style>

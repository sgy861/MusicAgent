<template>
  <div class="layout">
    <div class="left-body">
      <LeftSide></LeftSide>
    </div>
    <div class="right">
      <router-view></router-view>
      <GlobalPlayer></GlobalPlayer>
    </div>
  </div>
  <LoginAndRegister></LoginAndRegister>
  <!--移动端显示顶部-->
  <TopPanel></TopPanel>

  <!-- Global Chat Drawer for Private Messaging (CHAT) -->
  <div class="chat-widget-btn" @click="toggleChatDrawer" v-if="userInfoStore.userInfo.userId">
    <span class="chat-icon">💬</span>
    <span class="chat-btn-text">私信聊天</span>
    <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
  </div>

  <el-drawer
    v-model="drawerOpen"
    title="即时私信通信 (Netty + WebSocket)"
    direction="rtl"
    size="400px"
    class="chat-drawer"
  >
    <div class="chat-drawer-body">
      <div class="my-id-banner">
        <span>我的用户ID:</span>
        <code class="user-id-code" @click="copyMyId">{{ userInfoStore.userInfo.userId }}</code>
        <span class="copy-tip">(点击复制)</span>
      </div>

      <div class="recipient-input-panel">
        <span class="input-label">聊天对象ID:</span>
        <el-input v-model="chatUserId" placeholder="请输入对方 User ID..." size="small" clearable></el-input>
      </div>

      <div class="chat-messages-container">
        <div v-if="chatMessages.length === 0" class="empty-chat-prompt">
          <span class="chat-icon-large">📨</span>
          <p>暂无私信记录</p>
          <p class="sub-text">支持离线消息！对方离线时发送的消息，将在对方下次上线时由 Netty 自动推送送达。</p>
        </div>
        <div v-else class="chat-message-list">
          <div 
            v-for="(msg, idx) in filteredMessages" 
            :key="idx" 
            :class="['message-bubble-item', msg.isSelf ? 'self' : 'other']"
          >
            <div class="message-sender-id">{{ msg.isSelf ? '我' : '对方' }} ({{ msg.senderId.slice(0,6) }})</div>
            <div class="message-content-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input
          v-model="chatInput"
          placeholder="发送私信..."
          size="default"
          @keyup.enter="sendChatMessage"
          clearable
        >
          <template #append>
            <el-button @click="sendChatMessage">发送</el-button>
          </template>
        </el-input>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import TopPanel from './TopPanel.vue'
import LoginAndRegister from './login/LoginAndRegister.vue'
import GlobalPlayer from '@/views/player/GlobalPlayer.vue'
import LeftSide from './LeftSide.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
  watch,
  computed,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()
import { initWebSocket, sendWsMessage, closeWebSocket } from '@/utils/socket.js'
import { mitter } from '@/eventbus/eventBus.js'

const getLoginInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getLoginInfo,
    showLoading: false,
    params: {},
  })
  if (!result) {
    return
  }
  userInfoStore.setLoginInfo(result.data || {})
}

watch(
  () => userInfoStore.lastReloadTime,
  (newVal, oldVal) => {
    getLoginInfo()
  },
  { immediate: true, deep: true }
)

const drawerOpen = ref(false)
const chatUserId = ref('')
const chatInput = ref('')
const chatMessages = ref([])
const unreadCount = ref(0)

const copyMyId = async () => {
  await navigator.clipboard.writeText(userInfoStore.userInfo.userId)
  proxy.Message.success('用户ID已复制')
}

const toggleChatDrawer = () => {
  drawerOpen.value = !drawerOpen.value
  if (drawerOpen.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const sendChatMessage = () => {
  if (!chatUserId.value.trim()) {
    proxy.Message.warning('请输入对方用户ID')
    return
  }
  if (!chatInput.value.trim()) return
  const msg = {
    action: 'CHAT',
    receiverId: chatUserId.value.trim(),
    content: chatInput.value.trim()
  }
  sendWsMessage(msg)
  chatInput.value = ''
}

const filteredMessages = computed(() => {
  const targetId = chatUserId.value.trim()
  if (!targetId) return []
  return chatMessages.value.filter(msg => 
    (msg.senderId === targetId && msg.receiverId === userInfoStore.userInfo.userId) ||
    (msg.senderId === userInfoStore.userInfo.userId && msg.receiverId === targetId)
  )
})

const scrollToBottom = () => {
  const container = document.querySelector('.chat-messages-container')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

// Watch login state to connect/disconnect global socket
watch(
  () => userInfoStore.userInfo,
  (newVal) => {
    if (newVal && newVal.userId) {
      initWebSocket(localStorage.getItem('token'))
    } else {
      closeWebSocket()
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  mitter.on('wsMessage', (res) => {
    if (res.msgType === 'CHAT') {
      const isSelf = res.senderId === userInfoStore.userInfo.userId
      chatMessages.value.push({
        senderId: res.senderId,
        receiverId: res.receiverId,
        content: res.content,
        createTime: res.createTime || new Date(),
        isSelf
      })
      
      if (!isSelf && chatUserId.value !== res.senderId) {
        chatUserId.value = res.senderId
      }
      
      if (!drawerOpen.value) {
        unreadCount.value++
        proxy.Message.success(`收到新私信：${res.content}`)
      }
      
      nextTick(() => {
        scrollToBottom()
      })
    }
  })
})

onUnmounted(() => {
  mitter.off('wsMessage')
})
</script>

<style lang="scss" scoped>
.layout {
  height: calc(100vh);
  .left-body {
    position: fixed;
    height: calc(100vh);
  }
  .right {
    padding-left: 200px;
    overflow: auto;
    padding-bottom: 70px;
  }
}

.chat-widget-btn {
  position: fixed;
  right: 30px;
  bottom: 100px;
  z-index: 1000;
  background: var(--btnBg);
  box-shadow: var(--btnShadow);
  color: #fff;
  padding: 10px 18px;
  border-radius: 30px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  transition: all 0.3s ease;
  &:hover {
    transform: scale(1.05);
    opacity: 0.9;
  }
  .unread-badge {
    background: #ef4444;
    color: #fff;
    border-radius: 50%;
    padding: 2px 6px;
    font-size: 11px;
    font-weight: bold;
  }
}

:deep(.chat-drawer) {
  background: #091e25 !important;
  border-left: 1px solid rgba(0, 245, 212, 0.15);
  .el-drawer__header {
    margin-bottom: 0px;
    padding: 15px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    .el-drawer__title {
      color: #fff;
      font-weight: bold;
    }
    .el-drawer__close-btn {
      color: #fff;
    }
  }
  .el-drawer__body {
    padding: 0px;
  }
}

.chat-drawer-body {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 15px;
  background: #091e25;
  color: #fff;
  
  .my-id-banner {
    background: rgba(0, 245, 212, 0.08);
    border: 1px dashed rgba(0, 245, 212, 0.25);
    padding: 10px;
    border-radius: 8px;
    font-size: 12px;
    margin-bottom: 15px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 5px;
    
    .user-id-code {
      background: rgba(255, 255, 255, 0.1);
      padding: 2px 6px;
      border-radius: 4px;
      cursor: pointer;
      color: #00f5d4;
      font-family: monospace;
      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
    .copy-tip {
      font-size: 10px;
      opacity: 0.6;
    }
  }
  
  .recipient-input-panel {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 15px;
    .input-label {
      font-size: 13px;
      white-space: nowrap;
      color: #9ca3af;
    }
    :deep(.el-input__wrapper) {
      background-color: rgba(255, 255, 255, 0.05) !important;
      border: 1px solid rgba(255, 255, 255, 0.1) !important;
      box-shadow: none !important;
      .el-input__inner {
        color: #fff;
      }
    }
  }
  
  .chat-messages-container {
    flex: 1;
    overflow-y: auto;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 8px;
    padding: 10px;
    margin-bottom: 15px;
    border: 1px solid rgba(255, 255, 255, 0.05);
    
    .empty-chat-prompt {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #9ca3af;
      text-align: center;
      padding: 0 10px;
      
      .chat-icon-large {
        font-size: 32px;
        margin-bottom: 10px;
        opacity: 0.7;
      }
      p {
        font-size: 13px;
        margin-bottom: 5px;
      }
      .sub-text {
        font-size: 11px;
        opacity: 0.6;
        line-height: 1.5;
      }
    }
    
    .chat-message-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .message-bubble-item {
        display: flex;
        flex-direction: column;
        max-width: 80%;
        
        &.self {
          align-self: flex-end;
          align-items: flex-end;
          .message-content-bubble {
            background: var(--purple);
            color: #fff;
            border-radius: 12px 12px 0 12px;
          }
        }
        
        &.other {
          align-self: flex-start;
          align-items: flex-start;
          .message-content-bubble {
            background: rgba(255, 255, 255, 0.1);
            color: #fff;
            border-radius: 12px 12px 12px 0;
            border: 1px solid rgba(255, 255, 255, 0.05);
          }
        }
        
        .message-sender-id {
          font-size: 10px;
          color: #9ca3af;
          margin-bottom: 3px;
        }
        
        .message-content-bubble {
          padding: 8px 12px;
          font-size: 13px;
          word-break: break-all;
          line-height: 1.4;
        }
      }
    }
  }
  
  .chat-input-area {
    :deep(.el-input-group__append) {
      background: var(--purple) !important;
      border-color: var(--purple) !important;
      color: #fff !important;
      padding: 0 15px;
      cursor: pointer;
    }
    :deep(.el-input__wrapper) {
      background-color: rgba(255, 255, 255, 0.05) !important;
      box-shadow: none !important;
      border: 1px solid rgba(255, 255, 255, 0.15) !important;
      border-radius: 8px 0 0 8px;
      .el-input__inner {
        color: #fff;
      }
    }
  }
}

@media (max-width: 500px) {
  .layout {
    padding-top: 50px;
    .left-body {
      display: none;
    }
    .right {
      padding-left: 0px;
    }
  }
}
</style>

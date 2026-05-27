<template>
  <div class="music-detail-body">
    <BackBtn></BackBtn>
    <div class="music-panel">
      <div class="music-cover">
        <div
          :class="[
            'music-cover-bg',
            musicPlayStore.playing ? 'music-cover-bg-playing' : '',
          ]"
        ></div>
        <div class="cover">
          <Cover :width="150" :cover="musicInfo.cover" borderRadius="75px">
          </Cover>
        </div>
      </div>
      <div class="music-info">
        <div class="music-title">{{ musicInfo.musicTitle }}</div>
        <div class="user-info">{{ musicInfo.nickName || "--" }}</div>
        <div class="action-panel">
          <div
            :class="[
              'op-item play-btn iconfont',
              musicPlayStore.playing ? 'icon-pause' : 'icon-play',
            ]"
            @click="playMusic"
          ></div>
          <div class="op-item">
            <ActionGood :data="musicInfo"></ActionGood>
          </div>
          <div class="op-item">
            <ActionShare :data="musicInfo"></ActionShare>
          </div>
          <div class="op-item">
            <el-button type="primary" size="large" @click="createSame"
              >做同款</el-button
            >
          </div>
        </div>
        <div class="lyrics-and-chat-container">
          <div class="lyrics-panel-wrap">
            <div class="lyrics-panel" v-if="musicInfo.musicType === 0">
              <div class="lyrics-title">歌词：</div>
              <div
                :class="[
                  'lyrics-item',
                  musicPlayStore.currentPlayTime >= item.start &&
                  musicPlayStore.currentPlayTime <= item.end
                    ? 'active'
                    : '',
                ]"
                v-for="item in musicInfo.lyrics"
              >
                {{ item.text }}
              </div>
            </div>
            <div v-else class="lyrics-panel">纯音乐，请欣赏。</div>
          </div>

          <!-- Real-time Live Review Room -->
          <div class="chat-room-panel">
            <div class="chat-header">
              <span class="chat-title">
                <span class="chat-icon">💬</span> 实时在线乐评
              </span>
              <span class="status-indicator" :class="{ active: wsConnected }">
                <span class="status-dot"></span>
                {{ wsConnected ? '已加入乐评房' : '离线' }}
              </span>
            </div>
            
            <div class="chat-messages">
              <div v-if="reviews.length === 0" class="empty-chat">
                <span class="empty-icon">🎵</span>
                <p>暂无实时点评</p>
                <p class="sub-text">通过 Netty + WebSocket 与大家在线同步交流吧！</p>
              </div>
              <div v-else v-for="(msg, idx) in reviews" :key="idx" class="chat-message-item">
                <div class="msg-sender">用户: {{ msg.senderId.slice(0, 8) }}</div>
                <div class="msg-content">{{ msg.content }}</div>
              </div>
            </div>
            
            <div class="chat-input-area">
              <el-input 
                v-model="reviewInput" 
                placeholder="发送实时点评 (回车或发送)..." 
                size="default"
                @keyup.enter="sendReview"
                clearable
              >
                <template #append>
                  <el-button @click="sendReview" style="color: #fff; background: var(--purple); border: none;">发送</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import ActionShare from "@/component/biz/ActionShare.vue";
import ActionGood from "@/component/biz/ActionGood.vue";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { useMusicPlayStore } from "@/stores/musicPlay.js";
const musicPlayStore = useMusicPlayStore();
import { mitter } from "@/eventbus/eventBus.js";

const currentMusicId = ref(route.params.musicId);
const musicInfo = ref({});
const getMusicInfo = async (autoPlay) => {
  let result = await proxy.Request({
    url: proxy.Api.musicDetail,
    params: {
      musicId: currentMusicId.value,
    },
  });
  if (!result) {
    return;
  }
  if (result.data.musicType === 0) {
    const lyrics = JSON.parse(result.data.lyrics);
    result.data.lyrics = lyrics;
  }
  musicInfo.value = result.data;
  if (!autoPlay) {
    return;
  }
  musicPlayStore.play({ ...result.data });
};

const playMusic = () => {
  if (musicPlayStore.currentMusic?.musicId == musicInfo.value.musicId) {
    mitter.emit("togglePlay");
    return;
  }
  musicPlayStore.play({ ...musicInfo.value });
};

const createSame = () => {
  router.push(`/idea/${musicInfo.value.creationId}`);
};

// --- WebSocket Real-time Review Room Connection ---
import { getWsConnected, sendWsMessage } from "@/utils/socket.js";
const wsConnected = computed(() => getWsConnected().value);
const reviews = ref([]);
const reviewInput = ref("");

const handleWsMessage = (res) => {
  if (res.action === "ERROR") {
    proxy.Message.error(res.content || "连接错误");
    return;
  }
  if (res.msgType === "REVIEW" && res.receiverId === currentMusicId.value) {
    reviews.value.push({
      senderId: res.senderId,
      content: res.content,
      createTime: res.createTime || new Date(),
    });
    nextTick(() => {
      const chatBox = document.querySelector(".chat-messages");
      if (chatBox) {
        chatBox.scrollTop = chatBox.scrollHeight;
      }
    });
  }
};

const sendReview = () => {
  if (!wsConnected.value) {
    proxy.Message.warning("乐评室未连接，请先登录并刷新重试");
    return;
  }
  if (!reviewInput.value.trim()) {
    proxy.Message.warning("请输入点评内容");
    return;
  }
  const reviewMsg = {
    action: "REVIEW",
    receiverId: currentMusicId.value,
    content: reviewInput.value.trim(),
  };
  sendWsMessage(reviewMsg);
  reviewInput.value = "";
};

const leaveRoom = (roomId) => {
  const targetRoom = roomId || currentMusicId.value;
  if (wsConnected.value && targetRoom) {
    const leaveMsg = {
      action: "LEAVE_ROOM",
      receiverId: targetRoom,
    };
    sendWsMessage(leaveMsg);
  }
};

const joinRoom = () => {
  if (wsConnected.value && currentMusicId.value) {
    const joinMsg = {
      action: "JOIN_ROOM",
      receiverId: currentMusicId.value,
    };
    sendWsMessage(joinMsg);
  }
};

// Re-join if WebSocket status becomes connected
watch(
  () => wsConnected.value,
  (connected) => {
    if (connected) {
      joinRoom();
    }
  }
);

watch(
  () => route.params.musicId,
  async (newVal, oldVal) => {
    if (!newVal) {
      return;
    }
    // Leave previous room if switching
    if (oldVal) {
      leaveRoom(oldVal);
    }
    currentMusicId.value = newVal;
    getMusicInfo(true);
    reviews.value = []; // Clear current list
    joinRoom();
  },
  { immediate: true, deep: true }
);

watch(
  () => musicPlayStore.currentMusic.musicId,
  async (newVal, oldVal) => {
    if (!newVal) {
      return;
    }
    router.push(`/play/${newVal}`);
  },
  { immediate: true, deep: true }
);

onMounted(() => {
  mitter.on("wsMessage", handleWsMessage);
  joinRoom();
});

onUnmounted(() => {
  leaveRoom();
  mitter.off("wsMessage", handleWsMessage);
});
</script>

<style lang="scss" scoped>
.music-detail-body {
  padding: 20px 0px 0px 20px;
  .music-panel {
    display: flex;
    padding: 10px 10px 80px 10px;
    .music-cover {
      width: 250px;
      height: 250px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      .music-cover-bg {
        position: absolute;
        left: 0px;
        top: 0px;
        width: 250px;
        height: 250px;
        background: url("../../assets/img/play_cover_bg.png");
        background-repeat: no-repeat;
      }
      .music-cover-bg-playing {
        animation: rotateBackground 30s linear infinite;
      }
      .cover {
        position: absolute;
        z-index: 2;
      }
      .play-btn {
        position: absolute;
        z-index: 3;
        cursor: pointer;
      }
    }
    .music-info {
      flex: 1;
      color: #fff;
      margin-left: 30px;
      .music-title {
        font-size: 25px;
      }
      .user-info {
        margin-top: 10px;
      }
      .action-panel {
        margin-top: 10px;
        display: flex;
        align-items: center;
        .op-item {
          margin-right: 30px;
          cursor: pointer;
          width: 40px;
          height: 40px;
        }
        .iconfont {
          font-size: 25px;
        }
        .active {
          color: var(--purple);
        }
        .play-btn {
          font-size: 20px;
          background: #fff;
          border-radius: 50%;
          color: var(--purple);
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .lyrics-and-chat-container {
        display: flex;
        align-items: stretch;
        margin-top: 20px;
        
        .lyrics-panel-wrap {
          flex: 1;
          .lyrics-panel {
            .lyrics-title {
              font-size: 20px;
            }
            .lyrics-item {
              padding: 5px 0px;
              font-size: 16px;
            }
            .active {
              color: #00f5d4;
              font-size: 18px;
            }
          }
        }
        
        .chat-room-panel {
          width: 350px;
          margin-left: 40px;
          background: rgba(22, 48, 59, 0.2);
          border: 1px solid rgba(0, 245, 212, 0.15);
          border-radius: 12px;
          padding: 15px;
          backdrop-filter: blur(8px);
          display: flex;
          flex-direction: column;
          height: 400px;
          
          .chat-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid rgba(255, 255, 255, 0.08);
            padding-bottom: 8px;
            margin-bottom: 10px;
            
            .chat-title {
              font-size: 15px;
              font-weight: bold;
              color: #fff;
              display: flex;
              align-items: center;
              gap: 5px;
            }
            
            .status-indicator {
              font-size: 12px;
              color: #a78bfa;
              display: flex;
              align-items: center;
              gap: 4px;
              
              .status-dot {
                width: 6px;
                height: 6px;
                border-radius: 50%;
                background: #ef4444;
              }
              
              &.active {
                color: #00f5d4;
                .status-dot {
                  background: #10b981;
                  box-shadow: 0 0 6px #10b981;
                }
              }
            }
          }
          
          .chat-messages {
            flex: 1;
            overflow-y: auto;
            margin-bottom: 10px;
            padding-right: 5px;
            
            .chat-message-item {
              background: rgba(255, 255, 255, 0.05);
              border: 1px solid rgba(255, 255, 255, 0.03);
              border-radius: 8px;
              padding: 8px;
              margin-bottom: 8px;
              
              .msg-sender {
                font-size: 11px;
                color: #48cae4;
                margin-bottom: 3px;
                font-weight: 500;
              }
              
              .msg-content {
                font-size: 13px;
                color: #e5e7eb;
                word-break: break-all;
              }
            }
            
            .empty-chat {
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              height: 100%;
              color: #9ca3af;
              text-align: center;
              
              .empty-icon {
                font-size: 24px;
                margin-bottom: 8px;
                opacity: 0.7;
              }
              
              p {
                font-size: 13px;
                margin-bottom: 4px;
              }
              
              .sub-text {
                font-size: 11px;
                opacity: 0.6;
              }
            }
          }
          
          .chat-input-area {
            margin-top: auto;
            :deep(.el-input-group__append) {
              background: var(--purple) !important;
              border-color: var(--purple) !important;
              color: #fff !important;
              padding: 0 15px;
              cursor: pointer;
              button {
                background: none;
                border: none;
                padding: 0;
                margin: 0;
                height: 100%;
                width: 100%;
                color: inherit;
              }
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
      }
    }
  }

  @media (max-width: 800px) {
    .music-panel {
      .music-info {
        .lyrics-and-chat-container {
          flex-direction: column;
          .chat-room-panel {
            width: 100%;
            margin-left: 0px;
            margin-top: 30px;
          }
        }
      }
    }
  }

  @media (max-width: 500px) {
    .music-panel {
      flex-direction: column;
      text-align: center;
      .music-cover {
        margin: 0px auto;
      }
      .music-info {
        margin-left: 0px;
        margin-top: 5px;
        .action-panel {
          justify-content: space-around;
        }
      }
    }
  }
}

@keyframes rotateBackground {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>

<template>
  <div class="create-tab">
    <el-tabs v-model="formData.musicType">
      <el-tab-pane label="歌曲" :name="0"></el-tab-pane>
      <el-tab-pane label="纯音乐" :name="1"></el-tab-pane>
    </el-tabs>
  </div>
  <div class="create-form">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <Switch :data="[
          { label: '简单模式', value: 0 },
          { label: '高级模式', value: 1 },
        ]" v-model="formData.modeType"></Switch>
      <!--input输入-->
      <template v-if="formData.modeType == 0">
        <div class="input-panel">
          <el-input clearable placeholder="请输入你的想法" v-model="formData.prompt" type="textarea" :rows="8" resize="none"
            :maxlength="500" show-word-limit>
          </el-input>
          <div class="change-btn" @click="getPrompt">
            <div class="iconfont icon-magic">变变变</div>
          </div>
        </div>
      </template>
      <template v-else>
        <div :class="[
            'advanced-panel',
            formData.musicType === 1 ? 'advanced-panel-line' : '',
          ]">
          <div class="lyric-panel">
            <div class="input-panel">
              <el-input clearable placeholder="请输入提示，或者标题" v-model="formData.prompt" type="textarea" :rows="5"
                resize="none" :maxlength="500" show-word-limit>
              </el-input>
              <div class="change-btn" @click="getPrompt">
                <div class="iconfont icon-magic">变变变</div>
              </div>
            </div>
            <div class="input-panel lyric-input" v-if="formData.musicType === 0">
              <el-input clearable placeholder="请输入歌词" v-model="formData.lyrics" type="textarea" resize="none"
                :maxlength="1500" show-word-limit>
              </el-input>
            </div>
          </div>
          <div class="setting-panel">
            <div class="part-title">曲风</div>
            <TabSelect :data="sysSetting[SYS_SETTING_KEY.music_grenre.key]" v-model="formData.musicGener"></TabSelect>
            <div class="part-title">情绪</div>
            <TabSelect :data="sysSetting[SYS_SETTING_KEY.music_emotion.key]" v-model="formData.musicEmotion">
            </TabSelect>
            <template v-if="formData.musicType === 0">
              <div class="part-title">人声</div>
              <TabSelect :multiple="false" :data="sysSetting[SYS_SETTING_KEY.music_sex.key]"
                v-model="formData.musicSex"></TabSelect>
            </template>
          </div>
        </div>
      </template>

      <!-- AI 智能风格推荐 -->
      <div class="ai-recommendation-section">
        <div class="recommend-header">
          <div class="recommend-title">
            <span class="sparkle-icon">✨</span>
            <span>AI 智能灵感推荐</span>
            <span class="status-dot" :class="{ connected: wsConnected }"></span>
          </div>
          <div class="recommend-tips" v-if="wsConnected">基于您的点赞喜好实时计算</div>
          <div class="recommend-tips reconnecting" v-else-if="wsLoading">连接中...</div>
          <div class="recommend-tips disconnected" v-else @click="initWebSocket">连接已断开，点击重连</div>
        </div>

        <div class="recommend-cards" v-loading="wsLoading">
          <!-- AI CoT thinking process -->
          <div v-if="wsRecommending" class="recommend-thinking-panel">
            <div class="thinking-header">
              <span class="thinking-spinner">🔮</span>
              <span>AI 灵感推演中 (CoT)...</span>
            </div>
            <div class="thinking-content">{{ recommendThoughts }}</div>
          </div>

          <template v-if="!wsRecommending">
            <div 
              v-for="(rec, index) in recommendations" 
              :key="index" 
              class="recommend-card"
              @click="applyRecommendation(rec)"
            >
              <div class="card-glow"></div>
              <div class="card-content">
                <div class="card-header">
                  <span class="card-title">{{ rec.title }}</span>
                  <span class="card-arrow">→</span>
                </div>
                <div class="card-badges">
                  <span class="badge type">{{ rec.suggestedSettings.musicType === 1 ? '纯音乐' : '歌曲' }}</span>
                  <span class="badge genre">{{ rec.suggestedSettings.musicGener }}</span>
                  <span class="badge emotion">{{ rec.suggestedSettings.musicEmotion }}</span>
                  <span class="badge voice" v-if="rec.suggestedSettings.musicSex && rec.suggestedSettings.musicSex !== '无'">
                    {{ rec.suggestedSettings.musicSex }}
                  </span>
                </div>
                <div class="card-theme" v-if="rec.lyricTheme">
                  <span class="theme-label">灵感主题:</span>
                  <span class="theme-val">{{ rec.lyricTheme }}</span>
                </div>
                <div class="card-tags" v-if="rec.promptTags">
                  <span v-for="tag in rec.promptTags.split(',').slice(0, 3)" :key="tag" class="tag">#{{ tag.trim() }}</span>
                </div>
              </div>
            </div>
          </template>

          <!-- Fallback/Empty State if no recommendations yet -->
          <div v-if="recommendations.length === 0 && !wsLoading && !wsRecommending" class="empty-recommendations">
            <div class="empty-icon">🎵</div>
            <div class="empty-text">输入您的创作想法，AI将为您推荐个性化风格</div>
          </div>
        </div>
      </div>

      <div class="part-title">选择模型</div>
      <!-- 下拉框 -->
      <div class="model-select">
        <el-radio-group v-model="formData.model" class="model-select" size="large">
          <el-radio-button v-for="model in music_models" :value="model.dictCode" :label="model.dictCode">
          </el-radio-button>
        </el-radio-group>
        <div class="model-tips">{{ currentModel?.dictDesc }}</div>
      </div>
      <div class="submit-btn" @click="createMusic">
        <el-icon class="is-loading" v-if="creating">
          <Loading style="width: 1em; height: 1em" />
        </el-icon>
        <span v-else>创作音乐</span>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import Switch from '@/component/common/Switch.vue'
import TabSelect from '@/component/common/TabSelect.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  computed,
  onUnmounted,
  watch,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()

import { mitter } from '@/eventbus/eventBus.js'

const SYS_SETTING_KEY = {
  //曲风
  music_grenre: {
    key: 'music_grenre',
    valueKey: 'dictCode',
  },
  //情绪
  music_emotion: {
    key: 'music_emotion',
    valueKey: 'dictCode',
  },
  //人声
  music_sex: {
    key: 'music_sex',
    valueKey: 'dictCode',
  },
  //音乐提示词
  music_prompt: {
    key: 'music_prompt',
    valueKey: 'dictCode',
  },
  //纯音乐提示词
  music_prompt_pure: {
    key: 'music_prompt_pure',
    valueKey: 'dictCode',
  },
  //音乐模型
  music_model: {
    key: 'music_model',
  },
  //纯音乐模型
  music_model_pure: {
    key: 'music_model_pure',
  },
}

const getPrompt = () => {
  let prompts = []
  if (formData.value.musicType == 0) {
    prompts = sysSetting.value[SYS_SETTING_KEY.music_prompt.key]
  } else if (formData.value.musicType == 1) {
    prompts = sysSetting.value[SYS_SETTING_KEY.music_prompt_pure.key]
  }
  if (prompts == null) {
    return
  }
  formData.value.prompt = prompts[Math.floor(Math.random() * prompts.length)]
}

const music_models = computed(() => {
  const models =
    formData.value.musicType == 0
      ? sysSetting.value[SYS_SETTING_KEY.music_model.key]
      : sysSetting.value[SYS_SETTING_KEY.music_model_pure.key]
  if (models && models.length > 0 && !formData.value.model) {
    formData.value.model = models[0].dictCode
  }
  return models
})

const sysSetting = ref({})
const loadSysSetting = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadSysDict,
  })
  if (!result) {
    return
  }
  for (let key in result.data) {
    if (SYS_SETTING_KEY[key].valueKey) {
      result.data[key] = result.data[key].map((item) => {
        return item[SYS_SETTING_KEY[key].valueKey]
      })
    }
  }
  sysSetting.value = result.data
  if (route.params.creationId) {
    return
  }
  getPrompt()
}

const currentModel = computed(() => {
  if (music_models.value) {
    return music_models.value.find((item) => {
      return item.dictCode == formData.value.model
    })
  } else {
    return {}
  }
})

const formData = ref({
  modeType: 0,
  musicType: 0,
})
const formDataRef = ref()
const rules = {
  title: [{ required: true, message: '请输入内容' }],
}

const creating = ref(false)
const createMusic = async () => {
  if (!userInfoStore.checkLogin()) {
    return
  }

  if (creating.value) {
    return
  }
  if (!formData.value.prompt) {
    proxy.Message.warning('请输入提示词')
    return
  }
  creating.value = true
  let result = await proxy.Request({
    url: proxy.Api.createMusic,
    params: { ...formData.value },
    showLoading: false,
  })
  creating.value = false
  if (!result) {
    return
  }
  mitter.emit('newMusic', result.data)

  //重新加载积分
  userInfoStore.updateLastReloadTime()

  proxy.Alert({
    message: '创建成功音乐创作中....',
  })
}

const getCreation = async () => {
  const creationId = route.params.creationId
  if (!creationId) {
    return
  }

  let result = await proxy.Request({
    url: proxy.Api.getCreation,
    params: {
      creationId,
    },
  })
  if (!result) {
    return
  }
  //初始化设置
  if (result.data.modeType == 1) {
    result.data = { ...result.data, ...JSON.parse(result.data.settings) }
  }
  formData.value = result.data
}

// --- AI Recommendation WebSocket Integration ---
const socket = ref(null)
const wsConnected = ref(false)
const wsLoading = ref(false)
const recommendations = ref([])
const wsRecommending = ref(false)
const recommendThoughts = ref('')

const initWebSocket = () => {
  const token = localStorage.getItem('token') || 'test_token'
  if (!token) return

  if (socket.value) {
    socket.value.close()
  }

  wsLoading.value = true
  let wsUrl = `ws://localhost:8099/ws?token=${token}`
  if (process.env.NODE_ENV === 'production') {
    wsUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws?token=${token}`
  }
  
  try {
    socket.value = new WebSocket(wsUrl)
    
    socket.value.onopen = () => {
      wsConnected.value = true
      wsLoading.value = false
      console.log('WebSocket connected')
      // 握手成功后，立即主动发送首次推荐请求
      const msg = {
        action: 'TRIGGER_RECOMMEND',
        data: {
          currentInput: formData.value.prompt || ''
        }
      }
      socket.value.send(JSON.stringify(msg))
    }
    
    socket.value.onmessage = (event) => {
      wsLoading.value = false
      try {
        const res = JSON.parse(event.data)
        if (res.type === 'RECOMMEND_START') {
          wsRecommending.value = true
          recommendThoughts.value = ''
          recommendations.value = []
        } else if (res.type === 'RECOMMEND_THINK') {
          wsRecommending.value = true
          recommendThoughts.value += res.content || ''
        } else if (res.type === 'RECOMMEND_RESULT') {
          wsRecommending.value = false
          recommendations.value = res.recommendations || []
        } else if (res.type === 'RECOMMEND_ERROR') {
          wsRecommending.value = false
          proxy.Message.error(res.content || '智能推荐失败')
        }
      } catch (e) {
        console.error('Failed to parse websocket message', e)
      }
    }
    
    socket.value.onerror = (err) => {
      console.error('WebSocket error', err)
      wsLoading.value = false
    }
    
    socket.value.onclose = () => {
      wsConnected.value = false
      console.log('WebSocket connection closed')
      // Try to reconnect after 5 seconds if token exists
      setTimeout(() => {
        if (localStorage.getItem('token') || token === 'test_token') {
          initWebSocket()
        }
      }, 5000)
    }
  } catch (error) {
    console.error('WebSocket connection initialization failed', error)
    wsLoading.value = false
  }
}

const applyRecommendation = (rec) => {
  formData.value.modeType = 1 // 自动切换为高级模式以显示所有配置
  formData.value.musicType = rec.suggestedSettings.musicType
  formData.value.musicGener = rec.suggestedSettings.musicGener
  formData.value.musicEmotion = rec.suggestedSettings.musicEmotion
  if (rec.suggestedSettings.musicSex) {
    formData.value.musicSex = rec.suggestedSettings.musicSex
  }
  if (rec.suggestedSettings.model) {
    formData.value.model = rec.suggestedSettings.model
  }
  if (rec.promptTags) {
    formData.value.prompt = rec.promptTags
  }
  proxy.Message.success(`已应用风格：${rec.title}`)
}

let debounceTimer = null
const handlePromptInput = () => {
  if (!wsConnected.value || !socket.value) return
  
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  
  debounceTimer = setTimeout(() => {
    const msg = {
      action: 'TRIGGER_RECOMMEND',
      data: {
        currentInput: formData.value.prompt
      }
    }
    socket.value.send(JSON.stringify(msg))
  }, 1000)
}

watch(() => formData.value.prompt, (newVal) => {
  handlePromptInput()
})

onMounted(() => {
  loadSysSetting()
  getCreation()
  initWebSocket()
})

onUnmounted(() => {
  if (socket.value) {
    socket.value.close()
  }
})
</script>

<style lang="scss" scoped>
.create-tab {
  :deep(.el-tabs__header) {
    margin-bottom: 0px;
  }
  :deep(.el-tabs__item) {
    color: var(--text);
    font-size: 25px;
    padding-bottom: 10px;
  }
  :deep(.el-tabs__item.is-active) {
    color: var(--purple);
  }
  :deep(.el-tabs__active-bar) {
    background: var(--purple);
  }
  :deep(.el-tabs__nav-wrap) {
    &::after {
      background: #2c2c2c;
    }
  }
}
.create-form {
  color: #fff;
  .input-panel {
    background: #29244e;
    border-radius: 5px;
    overflow: hidden;
    :deep(.el-textarea__inner) {
      background: #29244e;
      box-shadow: none;
      height: 100%;
      border-radius: 0px;
      color: var(--hiText);
    }
    :deep(.el-input__count) {
      background: none;
    }
    ::-webkit-scrollbar {
      display: none;
    }
    .change-btn {
      text-align: right;
      padding: 10px;
      color: #fff;
      cursor: pointer;
      display: flex;
      justify-content: flex-end;
      .icon-magic {
        border-radius: 5px;
        padding: 5px 10px;
        background: #3f3a60;
        font-size: 13px;
        &::before {
          margin-right: 5px;
          font-size: 16px;
        }
      }
    }
  }

  .advanced-panel {
    display: flex;
    .lyric-panel {
      width: 300px;
      background: #29244d;
      border-radius: 5px;
      overflow: hidden;
      height: 100%;
      .lyric-input {
        border-radius: 0px;
        border-top: 1px solid var(--text);
        :deep(.el-textarea__inner) {
          height: calc(100vh - 515px);
          color: var(--hiText);
        }
      }
    }
    .setting-panel {
      padding: 0px 10px 0px 10px;
      flex: 1;
      width: 0;
    }
  }
  .advanced-panel-line {
    flex-direction: column;
    .lyric-panel {
      width: 100%;
    }
    .setting-panel {
      width: 100%;
    }
  }
  .part-title {
    line-height: 40px;
  }

  .model-select {
    width: 100%;
    :deep(.el-radio-button) {
      width: 50%;
      .el-radio-button__inner {
        width: 100%;
      }
    }
    .model-tips {
      margin-top: 5px;
      font-size: 13px;
      color: var(--text);
    }
  }

  .submit-btn {
    cursor: pointer;
    text-align: center;
    line-height: 45px;
    height: 45px;
    font-weight: bold;
    font-size: 20px;
    border-radius: 30px;
    margin-top: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--btnBg);
    box-shadow: var(-btnShadow);
    &:hover {
      opacity: 0.9;
    }
  }
}

@media (max-width: 500px) {
  .create-form {
    .advanced-panel {
      flex-direction: column;
      .lyric-panel {
        width: 100%;
      }
      .setting-panel {
        width: 100%;
      }
    }
  }
}

.ai-recommendation-section {
  margin: 20px 0;
  padding: 15px;
  background: rgba(41, 36, 78, 0.4);
  border: 1px solid rgba(138, 92, 246, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(8px);
  
  .recommend-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .recommend-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      text-shadow: 0 0 10px rgba(168, 85, 247, 0.5);
      
      .sparkle-icon {
        animation: pulse-glow 2s infinite ease-in-out;
      }
      
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #ef4444;
        box-shadow: 0 0 8px #ef4444;
        display: inline-block;
        
        &.connected {
          background: #10b981;
          box-shadow: 0 0 8px #10b981;
          animation: status-pulse 1.5s infinite alternate;
        }
      }
    }
    
    .recommend-tips {
      font-size: 12px;
      color: #a78bfa;
      cursor: pointer;
      
      &.reconnecting {
        color: #fbbf24;
      }
      &.disconnected {
        color: #f87171;
        text-decoration: underline;
      }
    }
  }

  .recommend-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    gap: 12px;
    min-height: 100px;

    .recommend-thinking-panel {
      grid-column: 1 / -1;
      background: rgba(30, 24, 60, 0.6);
      border: 1px dashed rgba(168, 85, 247, 0.4);
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 10px;
      animation: border-glow 2s infinite alternate;
      
      .thinking-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 600;
        color: #c084fc;
        margin-bottom: 8px;
        
        .thinking-spinner {
          display: inline-block;
          animation: spin 2s infinite linear;
        }
      }
      
      .thinking-content {
        font-size: 13px;
        color: #e2e8f0;
        line-height: 1.6;
        white-space: pre-wrap;
        text-align: left;
      }
    }
    
    .recommend-card {
      position: relative;
      background: rgba(30, 24, 60, 0.8);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 10px;
      padding: 12px;
      cursor: pointer;
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      
      .card-glow {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, rgba(168, 85, 247, 0.15) 0%, rgba(99, 102, 241, 0.15) 100%);
        opacity: 0;
        transition: opacity 0.3s ease;
        z-index: 1;
      }
      
      .card-content {
        position: relative;
        z-index: 2;
      }
      
      &:hover {
        transform: translateY(-4px);
        border-color: rgba(168, 85, 247, 0.4);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4), 0 0 15px rgba(168, 85, 247, 0.2);
        
        .card-glow {
          opacity: 1;
        }
        
        .card-arrow {
          transform: translateX(3px);
          color: #a855f7;
        }
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .card-title {
          font-size: 14px;
          font-weight: 600;
          color: #fff;
        }
        
        .card-arrow {
          font-size: 14px;
          color: #6b7280;
          transition: all 0.3s ease;
        }
      }
      
      .card-badges {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
        margin-bottom: 8px;
        
        .badge {
          font-size: 10px;
          padding: 2px 6px;
          border-radius: 4px;
          font-weight: 500;
          
          &.type {
            background: rgba(168, 85, 247, 0.2);
            color: #d8b4fe;
            border: 1px solid rgba(168, 85, 247, 0.3);
          }
          &.genre {
            background: rgba(99, 102, 241, 0.2);
            color: #c7d2fe;
            border: 1px solid rgba(99, 102, 241, 0.3);
          }
          &.emotion {
            background: rgba(236, 72, 153, 0.2);
            color: #fbcfe8;
            border: 1px solid rgba(236, 72, 153, 0.3);
          }
          &.voice {
            background: rgba(20, 184, 166, 0.2);
            color: #99f6e4;
            border: 1px solid rgba(20, 184, 166, 0.3);
          }
        }
      }
      
      .card-theme {
        font-size: 11px;
        margin-bottom: 8px;
        display: flex;
        gap: 4px;
        
        .theme-label {
          color: #9ca3af;
        }
        .theme-val {
          color: #e5e7eb;
          font-weight: 500;
        }
      }
      
      .card-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
        
        .tag {
          font-size: 10px;
          color: #a78bfa;
          background: rgba(0, 0, 0, 0.2);
          padding: 1px 4px;
          border-radius: 3px;
        }
      }
    }
    
    .empty-recommendations {
      grid-column: 1 / -1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px 0;
      color: #9ca3af;
      
      .empty-icon {
        font-size: 24px;
        margin-bottom: 8px;
        opacity: 0.7;
      }
      
      .empty-text {
        font-size: 13px;
        text-align: center;
      }
    }
  }
}

@keyframes pulse-glow {
  0%, 100% {
    transform: scale(1);
    opacity: 0.9;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
    filter: drop-shadow(0 0 5px rgba(168, 85, 247, 0.8));
  }
}

@keyframes status-pulse {
  from {
    opacity: 0.6;
    box-shadow: 0 0 4px #10b981;
  }
  to {
    opacity: 1;
    box-shadow: 0 0 12px #10b981;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes border-glow {
  from {
    border-color: rgba(168, 85, 247, 0.3);
    box-shadow: 0 0 5px rgba(168, 85, 247, 0.1);
  }
  to {
    border-color: rgba(168, 85, 247, 0.6);
    box-shadow: 0 0 15px rgba(168, 85, 247, 0.3);
  }
}
}
</style>

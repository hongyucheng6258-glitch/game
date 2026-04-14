<template>
  <div class="chat-page">
    <div class="chat-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <div class="chat-user-info">
        <UserAvatar :avatar="otherUserAvatar" :username="otherUserName" :size="36" />
        <span class="chat-username">{{ otherUserName || '用户' }}</span>
      </div>
    </div>
    <div class="chat-body">
      <ChatBox
        :messages="messages"
        :current-user-id="currentUserId"
        :current-user-avatar="currentUserAvatar"
        :current-user-name="currentUserName"
        :other-user-avatar="otherUserAvatar"
        :other-user-name="otherUserName"
        :sending="sending"
        :service-info="serviceInfo"
        @send="handleSend"
        @create-order="handleCreateOrder"
        @send-price-request="handleSendPriceRequest"
        @send-price-response="handleSendPriceResponse"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getChatHistory, sendMessage, sendPriceRequest, sendPriceResponse, markConversationAsRead } from '@/api/chat'
import { getUserById } from '@/api/user'
import type { Message } from '@/types/message'
import { useUserStore } from '@/stores/user'
import ChatBox from '@/components/business/ChatBox.vue'
import UserAvatar from '@/components/business/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id || 0)
const currentUserAvatar = computed(() => userStore.userInfo?.avatar || '')
const currentUserName = computed(() => userStore.userInfo?.username || '')

const otherUserId = computed(() => Number(route.params.userId))
const otherUserName = ref('')
const otherUserAvatar = ref('')
const messages = ref<Message[]>([])
const sending = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

// 服务信息（从路由查询参数获取）
const serviceInfo = computed(() => ({
  serviceId: route.query.serviceId ? Number(route.query.serviceId) : null,
  serviceTitle: route.query.serviceTitle as string || '',
  servicePrice: route.query.servicePrice ? Number(route.query.servicePrice) : null
}))

async function fetchMessages() {
  try {
    const res = await getChatHistory(otherUserId.value, { page: 1, size: 50 })
    messages.value = res.data.records
  } catch {
    // ignore
  }
}

async function fetchOtherUserInfo() {
  try {
    const res = await getUserById(otherUserId.value)
    otherUserName.value = res.data.username
    otherUserAvatar.value = res.data.avatar
  } catch {
    // ignore
  }
}

async function markAsRead() {
  try {
    await markConversationAsRead(otherUserId.value)
  } catch {
    // ignore
  }
}

async function handleSend(content: string) {
  sending.value = true
  try {
    await sendMessage({ receiverId: otherUserId.value, content })
    await fetchMessages()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

async function handleCreateOrder(serviceId: number, negotiatedPrice: number, messageId: number) {
  // 跳转到订单确认页面，带上价格协商参数
  router.push({
    path: `/order/confirm/${serviceId}`,
    query: {
      negotiatedPrice: negotiatedPrice.toString(),
      priceNegotiationMessageId: messageId.toString()
    }
  })
}

async function handleSendPriceRequest(requestedPrice: number, remark?: string) {
  if (!serviceInfo.value.serviceId) {
    ElMessage.error('无法发送价格请求，缺少服务信息')
    return
  }
  sending.value = true
  try {
    await sendPriceRequest({
      serviceId: serviceInfo.value.serviceId,
      requestedPrice,
      remark
    })
    ElMessage.success('价格请求已发送')
    await fetchMessages()
  } catch (err: any) {
    ElMessage.error(err.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function handleSendPriceResponse(serviceId: number, negotiatedPrice: number, requestMessageId: number, remark?: string) {
  sending.value = true
  try {
    await sendPriceResponse({
      serviceId,
      negotiatedPrice,
      requestMessageId,
      remark
    })
    ElMessage.success('价格协商已发送')
    await fetchMessages()
  } catch (err: any) {
    ElMessage.error(err.message || '发送失败')
  } finally {
    sending.value = false
  }
}

function startPolling() {
  pollTimer = setInterval(fetchMessages, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(async () => {
  await fetchOtherUserInfo()
  await fetchMessages()
  await markAsRead()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  max-height: 800px;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.chat-username {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.chat-body {
  flex: 1;
  min-height: 0;

  .chat-box {
    border-radius: 0 0 $border-radius-lg $border-radius-lg;
    border-top: none;
  }
}
</style>

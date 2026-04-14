<template>
  <div class="chat-box">
    <div ref="messageListRef" class="message-list">
      <template v-if="messages.length > 0">
        <ChatMessageItem
          v-for="msg in messages"
          :key="msg.id"
          :message="msg"
          :current-user-id="currentUserId"
          :current-user-avatar="currentUserAvatar"
          :current-user-name="currentUserName"
          :other-user-avatar="otherUserAvatar"
          :other-user-name="otherUserName"
          @create-order="handleCreateOrder"
          @respond-price="handleRespondPrice"
        />
      </template>
      <el-empty v-else description="暂无消息" :image-size="60" />
    </div>
    <div class="input-area">
      <div class="input-tools">
        <el-button
          v-if="serviceInfo?.serviceId"
          text
          size="small"
          @click="showPriceRequestDialog = true"
        >
          <el-icon><Discount /></el-icon>
          协商价格
        </el-button>
      </div>
      <div class="input-main">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="请输入消息..."
          resize="none"
          @keydown.enter.exact.prevent="handleSend"
        />
        <el-button type="primary" :loading="sending" @click="handleSend">
          发送
        </el-button>
      </div>
    </div>

    <!-- 发送价格请求对话框 -->
    <el-dialog v-model="showPriceRequestDialog" title="协商价格" width="400px">
      <el-form label-position="top">
        <el-form-item label="服务标题">
          <span>{{ serviceInfo?.serviceTitle }}</span>
        </el-form-item>
        <el-form-item label="原价">
          <span>¥{{ serviceInfo?.servicePrice?.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="请求价格">
          <el-input-number
            v-model="requestedPrice"
            :min="0"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注（可选）">
          <el-input v-model="priceRemark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPriceRequestDialog = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="handleSendPriceRequest">
          发送请求
        </el-button>
      </template>
    </el-dialog>

    <!-- 服务者响应价格请求对话框 -->
    <el-dialog v-model="showPriceResponseDialog" title="响应价格请求" width="400px">
      <el-form label-position="top">
        <el-form-item label="服务标题">
          <span>{{ priceResponseData?.serviceTitle }}</span>
        </el-form-item>
        <el-form-item label="用户请求价格">
          <span>¥{{ priceResponseData?.requestedPrice?.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="协商价格">
          <el-input-number
            v-model="negotiatedPrice"
            :min="0"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注（可选）">
          <el-input v-model="priceResponseRemark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPriceResponseDialog = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="handleSendPriceResponse">
          发送协商
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { Discount } from '@element-plus/icons-vue'
import ChatMessageItem from './ChatMessageItem.vue'

// 类型定义
interface Message {
  id: number
  senderId: number
  receiverId: number
  conversationId: number | null
  type: number
  content: string
  isRead: number
  createdAt: string
}

interface PriceRequestData {
  serviceId: number
  serviceTitle: string
  originalPrice: number
  requestedPrice: number
  remark?: string
}

const props = defineProps<{
  messages: Message[]
  currentUserId: number
  currentUserAvatar?: string
  currentUserName?: string
  otherUserAvatar?: string
  otherUserName?: string
  sending?: boolean
  serviceInfo?: {
    serviceId: number | null
    serviceTitle: string
    servicePrice: number | null
  }
}>()

const emit = defineEmits<{
  send: [content: string]
  createOrder: [serviceId: number, negotiatedPrice: number, messageId: number]
  sendPriceRequest: [requestedPrice: number, remark?: string]
  sendPriceResponse: [serviceId: number, negotiatedPrice: number, requestMessageId: number, remark?: string]
}>()

const inputText = ref('')
const messageListRef = ref<HTMLElement>()

// 价格请求对话框
const showPriceRequestDialog = ref(false)
const requestedPrice = ref(0)
const priceRemark = ref('')

// 价格响应对话框
const showPriceResponseDialog = ref(false)
const priceResponseData = ref<PriceRequestData | null>(null)
const priceResponseMessageId = ref<number>(0)
const negotiatedPrice = ref(0)
const priceResponseRemark = ref('')

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  emit('send', text)
  inputText.value = ''
}

function handleCreateOrder(serviceId: number, negotiatedPrice: number, messageId: number) {
  emit('createOrder', serviceId, negotiatedPrice, messageId)
}

function handleSendPriceRequest() {
  if (!requestedPrice.value) return
  emit('sendPriceRequest', requestedPrice.value, priceRemark.value || undefined)
  showPriceRequestDialog.value = false
  requestedPrice.value = 0
  priceRemark.value = ''
}

function handleRespondPrice(message: Message) {
  try {
    const data = JSON.parse(message.content) as PriceRequestData
    priceResponseData.value = data
    priceResponseMessageId.value = message.id
    negotiatedPrice.value = data.requestedPrice
    priceResponseRemark.value = ''
    showPriceResponseDialog.value = true
  } catch {
    // ignore
  }
}

function handleSendPriceResponse() {
  if (!priceResponseData.value || !negotiatedPrice.value || !priceResponseMessageId.value) return
  emit('sendPriceResponse', 
    priceResponseData.value.serviceId, 
    negotiatedPrice.value, 
    priceResponseMessageId.value,
    priceResponseRemark.value || undefined
  )
  showPriceResponseDialog.value = false
  priceResponseData.value = null
  priceResponseMessageId.value = 0
  negotiatedPrice.value = 0
  priceResponseRemark.value = ''
}

watch(() => props.messages.length, async () => {
  await nextTick()
  scrollToBottom()
})

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.chat-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  overflow: hidden;
  background: $bg-card;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  border-top: 1px solid $border-color;
}

.input-tools {
  display: flex;
  gap: $spacing-sm;
}

.input-main {
  display: flex;
  gap: $spacing-sm;
  align-items: flex-end;

  :deep(.el-textarea__inner) {
    background: $bg-input;
    border-color: $border-color;
    color: $text-primary;

    &:focus {
      border-color: $primary-color;
    }
  }
}
</style>

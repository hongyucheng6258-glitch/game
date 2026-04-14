<template>
  <div class="chat-message-item" :class="{ 'is-self': isSelf }">
    <UserAvatar
      v-if="!isSelf"
      :avatar="otherUserAvatar"
      :username="otherUserName"
      :size="36"
    />
    <div class="message-body">
      <div class="message-bubble" :class="{ 'price-message': isPriceMessage }">
        <template v-if="isPriceRequest">
          <div class="price-message-header">
            <el-icon><Discount /></el-icon>
            <span>价格请求</span>
          </div>
          <div class="price-message-content">
            <div class="service-title">{{ priceData.serviceTitle }}</div>
            <div class="price-row">
              <span class="label">原价：</span>
              <span class="original-price">¥{{ priceData.originalPrice.toFixed(2) }}</span>
            </div>
            <div class="price-row">
              <span class="label">请求价格：</span>
              <span class="requested-price">¥{{ priceData.requestedPrice.toFixed(2) }}</span>
            </div>
            <div v-if="priceData.remark" class="remark">{{ priceData.remark }}</div>
            <el-button
              v-if="!isSelf"
              type="primary"
              size="small"
              class="order-button"
              @click="handleRespondPrice"
            >
              响应协商
            </el-button>
          </div>
        </template>
        <template v-else-if="isPriceResponse">
          <div class="price-message-header">
            <el-icon><Checked /></el-icon>
            <span>价格协商</span>
          </div>
          <div class="price-message-content">
            <div class="service-title">{{ priceData.serviceTitle }}</div>
            <div class="price-row">
              <span class="label">协商价格：</span>
              <span class="negotiated-price">¥{{ priceData.negotiatedPrice.toFixed(2) }}</span>
            </div>
            <div v-if="priceData.remark" class="remark">{{ priceData.remark }}</div>
            <el-button 
              v-if="!isSelf && !localHasOrdered" 
              type="primary" 
              size="small" 
              class="order-button"
              @click="handleCreateOrder"
            >
              立即下单
            </el-button>
          </div>
        </template>
        <template v-else>
          {{ message.content }}
        </template>
      </div>
      <span class="message-time">{{ formatTime(message.createdAt) }}</span>
    </div>
    <UserAvatar
      v-if="isSelf"
      :avatar="currentUserAvatar"
      :username="currentUserName"
      :size="36"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Discount, Checked } from '@element-plus/icons-vue'
import UserAvatar from './UserAvatar.vue'
import { checkOrdered } from '@/api/order'

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

interface PriceResponseData {
  serviceId: number
  serviceTitle: string
  negotiatedPrice: number
  requestMessageId: number
  remark?: string
}

const MESSAGE_TYPE_PRICE_REQUEST = 2
const MESSAGE_TYPE_PRICE_RESPONSE = 3

const props = defineProps<{
  message: Message
  currentUserId: number
  currentUserAvatar?: string
  currentUserName?: string
  otherUserAvatar?: string
  otherUserName?: string
  hasOrdered?: boolean
}>()

const emit = defineEmits<{
  createOrder: [serviceId: number, negotiatedPrice: number, messageId: number]
  respondPrice: [message: Message]
}>()

const localHasOrdered = ref(props.hasOrdered || false)

const router = useRouter()

onMounted(async () => {
  if (props.message.type === MESSAGE_TYPE_PRICE_RESPONSE) {
    try {
      const res = await checkOrdered(props.message.id)
      localHasOrdered.value = res.data
    } catch {
      // ignore
    }
  }
})

const isSelf = computed(() => props.message.senderId === props.currentUserId)
const isPriceRequest = computed(() => props.message.type === MESSAGE_TYPE_PRICE_REQUEST)
const isPriceResponse = computed(() => props.message.type === MESSAGE_TYPE_PRICE_RESPONSE)
const isPriceMessage = computed(() => isPriceRequest.value || isPriceResponse.value)

const priceData = computed(() => {
  try {
    return JSON.parse(props.message.content) as PriceRequestData | PriceResponseData
  } catch {
    return null
  }
})

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function handleCreateOrder() {
  if (!priceData.value || !('negotiatedPrice' in priceData.value)) {
    ElMessage.error('无效的价格协商数据')
    return
  }
  localHasOrdered.value = true
  emit('createOrder', 
    priceData.value.serviceId, 
    priceData.value.negotiatedPrice,
    props.message.id
  )
}

function handleRespondPrice() {
  emit('respondPrice', props.message)
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.chat-message-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;

  &.is-self {
    flex-direction: row-reverse;
  }
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.is-self .message-body {
  align-items: flex-end;
}

.message-bubble {
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-lg;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  color: $text-primary;
  background: $bg-hover;

  &.price-message {
    min-width: 240px;
    padding: 0;
    overflow: hidden;
  }
}

.is-self .message-bubble {
  background: $primary-color;
  color: white;

  &.price-message {
    background: $bg-hover;
    color: $text-primary;
  }
}

.price-message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: $spacing-sm $spacing-md;
  background: linear-gradient(135deg, $primary-color, #409eff);
  color: white;
  font-weight: 600;
  font-size: 13px;
}

.price-message-content {
  padding: $spacing-md;

  .service-title {
    font-weight: 600;
    margin-bottom: $spacing-sm;
    color: $text-primary;
  }

  .price-row {
    display: flex;
    align-items: center;
    margin-bottom: 6px;
    font-size: 13px;

    .label {
      color: $text-muted;
      min-width: 80px;
    }

    .original-price {
      text-decoration: line-through;
      color: $text-muted;
    }

    .requested-price,
    .negotiated-price {
      font-size: 18px;
      font-weight: 600;
      color: $danger-color;
    }
  }

  .remark {
    margin-top: $spacing-sm;
    padding-top: $spacing-sm;
    border-top: 1px solid $border-color;
    font-size: 12px;
    color: $text-muted;
  }

  .order-button {
    width: 100%;
    margin-top: $spacing-md;
  }
}

.message-time {
  font-size: 11px;
  color: $text-muted;
}
</style>

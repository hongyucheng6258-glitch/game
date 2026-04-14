<template>
  <div class="message-center-page">
    <h2 class="page-title">消息中心</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane name="system">
        <template #label>
          <span>系统消息</span>
          <el-badge v-if="systemUnread > 0" :value="systemUnread" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="chat">
        <template #label>
          <span>聊天消息</span>
          <el-badge v-if="chatUnread > 0" :value="chatUnread" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 系统消息 -->
    <div v-if="activeTab === 'system'" class="message-list">
      <template v-if="systemMessages.length > 0">
        <div
          v-for="msg in systemMessages"
          :key="msg.id"
          class="message-item"
          :class="{ unread: msg.isRead === 0 }"
          @click="handleMessageClick(msg)"
        >
          <div class="message-icon">
            <el-icon :size="24" :color="msg.isRead === 0 ? '#6366f1' : '#64748b'">
              <Bell />
            </el-icon>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-title">{{ msg.content?.substring(0, 50) || '系统通知' }}</span>
              <span class="message-time">{{ formatDate(msg.createdAt) }}</span>
            </div>
            <div class="message-body">
              {{ msg.content }}
            </div>
          </div>
          <div v-if="msg.isRead === 0" class="unread-dot" />
          <div class="message-actions" @click.stop>
            <el-button
              type="danger"
              text
              size="small"
              @click="handleDeleteMessage(msg)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无系统消息" />
      <div v-if="systemTotal > 10" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="systemPage"
          :total="systemTotal"
          :page-size="10"
          layout="prev, pager, next"
          background
          @current-change="fetchSystemMessages"
        />
      </div>
    </div>

    <!-- 聊天消息 -->
    <div v-if="activeTab === 'chat'" v-loading="chatLoading" class="chat-list">
      <template v-if="conversations.length > 0">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          @click="$router.push(`/message/chat/${getOtherUserId(conv)}`)"
        >
          <UserAvatar :avatar="conv.otherUserAvatar" :username="conv.otherUserName" :size="48" />
          <div class="conversation-content">
            <div class="conversation-header">
              <span class="conversation-name">{{ conv.otherUserName || '用户' }}</span>
              <span class="conversation-time">{{ formatDate(conv.lastMessageTime) }}</span>
            </div>
            <div class="conversation-body">
              {{ conv.lastMessage || '暂无消息' }}
            </div>
          </div>
          <el-badge v-if="conv.unreadCount && conv.unreadCount > 0" :value="conv.unreadCount" :max="99" />
        </div>
      </template>
      <el-empty v-else description="暂无聊天消息" />
    </div>

    <div class="actions-bar">
      <el-button v-if="activeTab === 'system' && systemMessages.length > 0" text type="primary" @click="handleReadAll">
        全部标为已读
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConversations } from '@/api/chat'
import type { Conversation, Message } from '@/types/message'
import { formatDate } from '@/utils/format'
import UserAvatar from '@/components/business/UserAvatar.vue'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { useSystemStore } from '@/stores/system'

const router = useRouter()
const activeTab = ref('system')
const chatLoading = ref(false)
const systemPage = ref(1)
const conversations = ref<Conversation[]>([])
const chatUnread = ref(0)
const userStore = useUserStore()
const messageStore = useMessageStore()
const systemStore = useSystemStore()

const systemMessages = computed(() => messageStore.messages)
const systemTotal = computed(() => messageStore.total)
const systemUnread = computed(() => systemStore.unreadCount)

function getOtherUserId(conv: Conversation): number {
  const currentUserId = userStore.userInfo?.id
  if (currentUserId && conv.user1Id === currentUserId) {
    return conv.user2Id
  }
  return conv.user1Id
}

async function fetchSystemMessages() {
  await messageStore.fetchMessages({ page: systemPage.value, size: 10, type: 0 })
}

async function fetchConversations() {
  chatLoading.value = true
  try {
    const res = await getConversations()
    conversations.value = res.data
    chatUnread.value = conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
  } catch {
    // ignore
  } finally {
    chatLoading.value = false
  }
}

function handleMessageClick(msg: Message) {
  if (msg.isRead === 0) {
    handleReadSystem(msg)
  }
  if (msg.relatedType && msg.relatedId) {
    switch (msg.relatedType) {
      case 'order':
        router.push(`/order/${msg.relatedId}`)
        break
    }
  }
}

async function handleReadSystem(msg: Message) {
  if (msg.isRead === 0) {
    try {
      await messageStore.markAsRead(msg.id)
      msg.isRead = 1
      await messageStore.fetchUnreadCount()
    } catch {
      // ignore
    }
  }
}

async function handleReadAll() {
  try {
    await messageStore.markAllAsRead()
    messageStore.messages.forEach(m => { m.isRead = 1 })
    await messageStore.fetchUnreadCount()
    ElMessage.success('已全部标为已读')
  } catch {
    // ignore
  }
}

async function handleDeleteMessage(msg: Message) {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await messageStore.deleteMessage(msg.id)
    
    const newTotal = systemTotal.value - 1
    const totalPages = Math.ceil(newTotal / 10)
    if (systemPage.value > totalPages && totalPages > 0) {
      systemPage.value = totalPages
    } else if (totalPages === 0) {
      systemPage.value = 1
    }
    
    await fetchSystemMessages()
    await messageStore.fetchUnreadCount()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function loadData() {
  if (userStore.isLoggedIn) {
    fetchSystemMessages()
    fetchConversations()
    messageStore.fetchUnreadCount()
  }
}

onMounted(() => {
  loadData()
})

onActivated(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.message-center-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
  background: linear-gradient(135deg, $text-primary 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.tab-badge {
  margin-left: 4px;
}

:deep(.el-tabs) {
  .el-tabs__header {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid rgba(148, 163, 184, 0.06);
    border-radius: $border-radius-lg;
    padding: 4px $spacing-md;
  }

  .el-tabs__item {
    color: $text-secondary;
    transition: color $transition-fast;

    &.is-active {
      color: $primary-light;
      text-shadow: 0 0 12px rgba(99, 102, 241, 0.4);
    }

    &:hover {
      color: $primary-light;
    }
  }

  .el-tabs__active-bar {
    background: linear-gradient(90deg, $primary-color, $primary-light);
    border-radius: 2px;
    box-shadow: 0 0 8px rgba(99, 102, 241, 0.5);
  }

  .el-tabs__nav-wrap::after {
    display: none;
  }
}

// 系统消息
.message-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  min-height: 300px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius-lg;
  cursor: pointer;
  transition: transform $transition-normal, box-shadow $transition-normal, background $transition-fast;
  position: relative;

  &:hover {
    background: rgba(51, 65, 85, 0.6);
    transform: translateY(-2px);
    box-shadow: 0 0 20px rgba(99, 102, 241, 0.1), 0 8px 24px rgba(0, 0, 0, 0.2);
  }

  &.unread {
    border-left: 3px solid $primary-color;
    background: rgba(99, 102, 241, 0.05);

    .message-title {
      color: $text-primary;
    }
  }
}

.message-icon {
  flex-shrink: 0;
  margin-top: 2px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(99, 102, 241, 0.1);
  border-radius: $border-radius;
  transition: background $transition-fast;

  .message-item:hover & {
    background: rgba(99, 102, 241, 0.2);
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-xs;
}

.message-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color $transition-fast;
}

.message-time {
  font-size: 12px;
  color: $text-muted;
  flex-shrink: 0;
  margin-left: $spacing-md;
}

.message-body {
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $primary-color;
  flex-shrink: 0;
  margin-top: 6px;
  box-shadow: 0 0 8px rgba(99, 102, 241, 0.6);
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    box-shadow: 0 0 4px rgba(99, 102, 241, 0.4);
  }
  50% {
    box-shadow: 0 0 12px rgba(99, 102, 241, 0.8);
  }
}

.message-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  margin-top: 2px;
  opacity: 0;
  transition: opacity $transition-fast;

  .message-item:hover & {
    opacity: 1;
  }
}

// 聊天消息
.chat-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  min-height: 300px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius-lg;
  cursor: pointer;
  transition: transform $transition-normal, box-shadow $transition-normal, background $transition-fast;

  &:hover {
    background: rgba(51, 65, 85, 0.6);
    transform: translateY(-2px);
    box-shadow: 0 0 20px rgba(99, 102, 241, 0.1), 0 8px 24px rgba(0, 0, 0, 0.2);
  }
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-xs;
}

.conversation-name {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
  transition: color $transition-fast;

  .conversation-item:hover & {
    color: $primary-light;
  }
}

.conversation-time {
  font-size: 12px;
  color: $text-muted;
}

.conversation-body {
  font-size: 13px;
  color: $text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions-bar {
  display: flex;
  justify-content: flex-end;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $primary-light);
    border: none;

    &:hover {
      box-shadow: 0 0 16px rgba(99, 102, 241, 0.4);
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
}
</style>

<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <el-icon :size="28" color="#6366f1"><Trophy /></el-icon>
        <span class="logo-text">{{ systemStore.settings.siteName }}</span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/service">服务列表</router-link>
        <router-link to="/ranking">排行榜</router-link>
        <router-link to="/announcement">公告</router-link>
      </nav>

      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/message" class="message-btn">
            <el-badge :value="systemStore.unreadCount" :hidden="systemStore.unreadCount === 0" :max="99">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </router-link>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">
                {{ userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user')">个人中心</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isProvider" @click="$router.push('/provider')">服务者中心</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">管理后台</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/auth/login')">登录</el-button>
          <el-button @click="$router.push('/auth/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { Trophy, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import { useMessageStore } from '@/stores/message'
import { useRouter } from 'vue-router'
import { onMounted, onUnmounted } from 'vue'

const userStore = useUserStore()
const systemStore = useSystemStore()
const messageStore = useMessageStore()
const router = useRouter()
let pollTimer: ReturnType<typeof setInterval> | null = null

function handleLogout() {
  userStore.logout()
  router.push('/')
}

function startPolling() {
  if (userStore.isLoggedIn) {
    messageStore.fetchUnreadCount()
    pollTimer = setInterval(() => {
      messageStore.fetchUnreadCount()
    }, 30000)
  }
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
.app-header {
  background: #1e293b;
  border-bottom: 1px solid #334155;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.nav-links {
  display: flex;
  gap: 24px;
  a {
    color: #94a3b8;
    text-decoration: none;
    font-size: 14px;
    transition: color 0.2s;
    &:hover, &.router-link-active {
      color: #f1f5f9;
    }
  }
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.message-btn {
  color: #94a3b8;
  &:hover { color: #f1f5f9; }
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  color: #f1f5f9;
  font-size: 14px;
}
</style>

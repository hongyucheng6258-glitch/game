<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <div class="logo-icon">
          <el-icon :size="22" color="#a78bfa"><Trophy /></el-icon>
        </div>
        <span class="logo-text">{{ systemStore.settings.siteName }}</span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/" class="nav-item">
          <el-icon :size="16"><HomeFilled /></el-icon>
          <span>首页</span>
        </router-link>
        <router-link to="/service" class="nav-item">
          <el-icon :size="16"><Goods /></el-icon>
          <span>服务列表</span>
        </router-link>
        <router-link to="/ranking" class="nav-item">
          <el-icon :size="16"><Trophy /></el-icon>
          <span>排行榜</span>
        </router-link>
        <router-link to="/announcement" class="nav-item">
          <el-icon :size="16"><Bell /></el-icon>
          <span>公告</span>
        </router-link>
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
          <el-button plain @click="$router.push('/auth/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { Trophy, Bell, HomeFilled, Goods } from '@element-plus/icons-vue'
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
@use '@/assets/styles/variables' as *;

.app-header {
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all $transition-normal;
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
  gap: 10px;
  text-decoration: none;
  transition: all $transition-normal;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(167, 139, 250, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-normal;
}

.logo:hover .logo-icon {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(167, 139, 250, 0.3));
  transform: scale(1.05);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #818cf8, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: $text-secondary;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: $border-radius;
  transition: all $transition-normal;
  position: relative;

  &:hover {
    color: $text-primary;
    background: rgba($primary-color, 0.08);
  }

  &.router-link-active {
    color: $primary-light;
    background: rgba($primary-color, 0.12);

    &::after {
      content: '';
      position: absolute;
      bottom: -1px;
      left: 50%;
      transform: translateX(-50%);
      width: 20px;
      height: 2px;
      background: $primary-color;
      border-radius: 1px;
    }
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-btn {
  color: $text-secondary;
  padding: 8px;
  border-radius: $border-radius;
  transition: all $transition-normal;
  &:hover {
    color: $text-primary;
    background: rgba($primary-color, 0.08);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  transition: all $transition-normal;

  &:hover {
    background: rgba($primary-color, 0.08);
  }
}

.username {
  color: $text-primary;
  font-size: 14px;
  font-weight: 500;
}
</style>

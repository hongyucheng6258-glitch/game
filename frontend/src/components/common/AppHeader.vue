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
  background: rgba($bg-abyss, 0.88);
  backdrop-filter: blur(24px) saturate(1.4);
  -webkit-backdrop-filter: blur(24px) saturate(1.4);
  border-bottom: none;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all $transition-normal;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, $neon-cyan, $neon-purple, transparent);
    box-shadow: 0 0 12px rgba($neon-cyan, 0.4), 0 0 12px rgba($neon-purple, 0.3);
    animation: neonLinePulse 3s ease-in-out infinite;
  }
}

@keyframes neonLinePulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
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
  background: linear-gradient(135deg, rgba($neon-cyan, 0.15), rgba($neon-purple, 0.15));
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-normal;
  border: 1px solid rgba($neon-cyan, 0.2);
  box-shadow: 0 0 10px rgba($neon-cyan, 0.1);
}

.logo:hover .logo-icon {
  background: linear-gradient(135deg, rgba($neon-cyan, 0.25), rgba($neon-purple, 0.25));
  transform: scale(1.05);
  box-shadow: 0 0 18px rgba($neon-cyan, 0.25);
  border-color: rgba($neon-cyan, 0.4);
}

.logo-text {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, $neon-cyan, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 0 6px rgba($neon-cyan, 0.4));
  letter-spacing: 1px;
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

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 60%;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, $neon-purple);
    border-radius: 1px;
    transition: transform $transition-normal;
    box-shadow: 0 0 8px rgba($neon-cyan, 0.5);
  }

  &:hover {
    color: $text-primary;
    background: rgba($neon-cyan, 0.06);

    &::after {
      transform: translateX(-50%) scaleX(1);
    }
  }

  &.router-link-active {
    color: $neon-cyan;
    background: rgba($neon-cyan, 0.08);

    &::after {
      transform: translateX(-50%) scaleX(1);
      background: $neon-cyan;
      box-shadow: 0 0 10px rgba($neon-cyan, 0.6), 0 0 20px rgba($neon-cyan, 0.2);
    }
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, rgba($neon-cyan, 0.15), rgba($neon-purple, 0.15));
    border: 1px solid rgba($neon-cyan, 0.4);
    color: $neon-cyan;
    box-shadow: 0 0 12px rgba($neon-cyan, 0.15), inset 0 0 12px rgba($neon-cyan, 0.05);
    transition: all $transition-normal;

    &:hover {
      background: linear-gradient(135deg, rgba($neon-cyan, 0.25), rgba($neon-purple, 0.2));
      border-color: rgba($neon-cyan, 0.6);
      box-shadow: 0 0 20px rgba($neon-cyan, 0.3), inset 0 0 16px rgba($neon-cyan, 0.08);
      color: #fff;
    }
  }

  :deep(.el-button:not(.el-button--primary)) {
    border: 1px solid rgba($neon-purple, 0.35);
    color: $neon-purple;
    background: rgba($neon-purple, 0.06);
    box-shadow: 0 0 10px rgba($neon-purple, 0.1);
    transition: all $transition-normal;

    &:hover {
      background: rgba($neon-purple, 0.15);
      border-color: rgba($neon-purple, 0.6);
      box-shadow: 0 0 18px rgba($neon-purple, 0.25);
      color: #fff;
    }
  }
}

.message-btn {
  color: $text-secondary;
  padding: 8px;
  border-radius: $border-radius;
  transition: all $transition-normal;

  &:hover {
    color: $neon-cyan;
    background: rgba($neon-cyan, 0.08);
    box-shadow: 0 0 12px rgba($neon-cyan, 0.15);
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
  border: 1px solid transparent;

  &:hover {
    background: rgba($neon-cyan, 0.06);
    border-color: rgba($neon-cyan, 0.15);
  }

  :deep(.el-avatar) {
    border: 2px solid transparent;
    box-shadow: 0 0 8px rgba($neon-cyan, 0.3), 0 0 16px rgba($neon-purple, 0.2);
    transition: all $transition-normal;
    background: linear-gradient(135deg, rgba($neon-cyan, 0.2), rgba($neon-purple, 0.2));
  }

  &:hover :deep(.el-avatar) {
    border-color: rgba($neon-cyan, 0.5);
    box-shadow: 0 0 12px rgba($neon-cyan, 0.5), 0 0 24px rgba($neon-purple, 0.3);
  }
}

.username {
  color: $text-primary;
  font-size: 14px;
  font-weight: 500;
}
</style>

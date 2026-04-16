<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <div class="logo-icon effect-glow-border">
          <el-icon :size="24" color="#00ff9f"><Trophy /></el-icon>
        </div>
        <span class="logo-text">{{ systemStore.settings.siteName || '锐竞电竞' }}</span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/" class="nav-item">
          <el-icon :size="18"><HomeFilled /></el-icon>
          <span>首页</span>
        </router-link>
        <router-link to="/service" class="nav-item">
          <el-icon :size="18"><Goods /></el-icon>
          <span>服务列表</span>
        </router-link>
        <router-link to="/ranking" class="nav-item">
          <el-icon :size="18"><Trophy /></el-icon>
          <span>排行榜</span>
        </router-link>
        <router-link to="/announcement" class="nav-item">
          <el-icon :size="18"><Bell /></el-icon>
          <span>公告</span>
        </router-link>
      </nav>

      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/message" class="message-btn">
            <el-badge :value="systemStore.unreadCount" :hidden="systemStore.unreadCount === 0" :max="99">
              <el-icon :size="22"><Bell /></el-icon>
            </el-badge>
          </router-link>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar || undefined" class="user-avatar">
                {{ userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
              <el-badge v-if="userStore.userInfo?.currentLevel" :value="userStore.userInfo?.currentLevel" class="level-badge" />
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item @click="$router.push('/user')">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isProvider" @click="$router.push('/provider')">
                  <el-icon><Shop /></el-icon>
                  <span>服务者中心</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">
                  <el-icon><Setting /></el-icon>
                  <span>管理后台</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" class="btn-glow" @click="$router.push('/auth/login')">登录</el-button>
          <el-button plain class="btn-ghost" @click="$router.push('/auth/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { Trophy, Bell, HomeFilled, Goods, User, Shop, Setting, SwitchButton } from '@element-plus/icons-vue'
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
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border-bottom: 1px solid $glass-border;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all $transition-normal;
  box-shadow: $shadow-md;
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $primary-color, transparent);
    opacity: 0.6;
  }
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: relative;
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      linear-gradient(rgba($primary-color, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba($primary-color, 0.05) 1px, transparent 1px);
    background-size: 20px 20px;
    pointer-events: none;
    z-index: 0;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  transition: all $transition-normal;
  position: relative;
  z-index: 1;
  &:hover {
    transform: translateY(-2px);
  }
}

.logo-icon {
  width: 44px;
  height: 44px;
  border-radius: $border-radius-lg;
  background: linear-gradient(135deg, rgba($primary-color, 0.2), rgba($secondary-color, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-normal;
  box-shadow: $shadow-glow;
}

.logo:hover .logo-icon {
  background: linear-gradient(135deg, rgba($primary-color, 0.3), rgba($secondary-color, 0.3));
  transform: scale(1.1) rotate(5deg);
  box-shadow: $shadow-glow-lg;
}

.logo-text {
  font-size: 24px;
  font-weight: $font-weight-bold;
  font-family: $font-family-heading;
  background: linear-gradient(135deg, $primary-color, $secondary-color);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 15px rgba($primary-color, 0.4);
  letter-spacing: 1px;
}

.nav-links {
  display: flex;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: $text-secondary;
  text-decoration: none;
  font-size: 15px;
  font-weight: $font-weight-medium;
  font-family: $font-family-primary;
  padding: 10px 20px;
  border-radius: $border-radius;
  transition: all $transition-bounce;
  position: relative;
  overflow: hidden;
  &:hover {
    color: $text-primary;
    background: rgba($primary-color, 0.1);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary-color, 0.2);
  }
  &.router-link-active {
    color: $primary-color;
    background: rgba($primary-color, 0.15);
    box-shadow: 0 4px 12px rgba($primary-color, 0.2);
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background: linear-gradient(90deg, $primary-color, $secondary-color);
      border-radius: 2px;
    }
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba($primary-color, 0.1), transparent);
      transition: left $transition-normal;
    }
    &:hover::before {
      left: 100%;
    }
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.message-btn {
  color: $text-secondary;
  padding: 10px;
  border-radius: $border-radius;
  transition: all $transition-bounce;
  position: relative;
  &:hover {
    color: $primary-color;
    background: rgba($primary-color, 0.1);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary-color, 0.2);
  }
  .el-icon {
    transition: all $transition-normal;
  }
  &:hover .el-icon {
    transform: scale(1.1);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 16px 6px 8px;
  border-radius: $border-radius-full;
  transition: all $transition-bounce;
  background: rgba($bg-hover, 0.5);
  border: 1px solid $border-color;
  &:hover {
    background: rgba($primary-color, 0.1);
    border-color: rgba($primary-color, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary-color, 0.2);
  }
}

.user-avatar {
  border: 2px solid $primary-color;
  box-shadow: 0 0 10px rgba($primary-color, 0.5);
  transition: all $transition-normal;
  &:hover {
    box-shadow: 0 0 20px rgba($primary-color, 0.8);
    transform: scale(1.05);
  }
}

.username {
  color: $text-primary;
  font-size: 15px;
  font-weight: $font-weight-medium;
  font-family: $font-family-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.level-badge {
  background: linear-gradient(135deg, $primary-color, $secondary-color);
  color: $text-primary;
  font-size: 12px;
  font-weight: $font-weight-bold;
  box-shadow: 0 0 10px rgba($primary-color, 0.5);
}

.user-dropdown {
  background: $bg-card;
  border: 1px solid $glass-border;
  box-shadow: $shadow-lg, $shadow-glow;
  border-radius: $border-radius-lg;
  overflow: hidden;
  .el-dropdown-item {
    font-family: $font-family-primary;
    color: $text-primary;
    padding: 12px 20px;
    transition: all $transition-normal;
    display: flex;
    align-items: center;
    gap: 8px;
    &:hover {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
    }
    .el-icon {
      font-size: 16px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .header-inner {
    padding: 0 20px;
    height: 64px;
  }
  
  .logo-text {
    font-size: 20px;
  }
  
  .logo-icon {
    width: 36px;
    height: 36px;
  }
  
  .nav-item {
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .username {
    display: none;
  }
  
  .user-info {
    padding: 6px;
  }
}
</style>

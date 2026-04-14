<template>
  <div class="user-layout">
    <AppHeader />
    <div class="user-content">
      <AppSidebar :menus="menus" />
      <main class="user-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppSidebar from '@/components/common/AppSidebar.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const menus = computed(() => {
  const base = [
    { title: '个人中心', icon: 'User', path: '/user' },
    { title: '个人信息', icon: 'Edit', path: '/user/profile' },
    { title: '等级中心', icon: 'Medal', path: '/user/level' },
    { title: '游戏账号', icon: 'GamePad', path: '/user/game-accounts' },
    { title: '我的订单', icon: 'Document', path: '/order/list' },
    { title: '我的收藏', icon: 'Star', path: '/user/favorites' },
    { title: '我的评价', icon: 'ChatDotRound', path: '/user/reviews' },
    { title: '我的投诉', icon: 'Warning', path: '/user/complaints' },
    { title: '余额管理', icon: 'Wallet', path: '/user/balance' },
    { title: '支付记录', icon: 'Tickets', path: '/user/payment-records' },
    { title: '消息中心', icon: 'Bell', path: '/message' },
  ]
  if (userStore.isProvider) {
    base.push(
      { title: '服务者中心', icon: 'Trophy', path: '/provider' },
      { title: '我的服务', icon: 'Goods', path: '/provider/services' },
      { title: '接单管理', icon: 'List', path: '/provider/orders' },
    )
  }
  return base
})
</script>

<style scoped lang="scss">
.user-layout {
  min-height: 100vh;
  background: #0f172a;
}
.user-content {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  padding: 20px;
}
.user-main {
  flex: 1;
  min-width: 0;
}
</style>

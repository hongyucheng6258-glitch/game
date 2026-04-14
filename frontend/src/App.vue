<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useMessageStore } from '@/stores/message'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'

const messageStore = useMessageStore()
const userStore = useUserStore()
const systemStore = useSystemStore()

onMounted(() => {
  systemStore.fetchSettings()
  if (userStore.isLoggedIn) {
    messageStore.fetchUnreadCount()
  }
})

// 监听用户登录状态变化
userStore.$subscribe((mutation, state) => {
  if (state.isLoggedIn) {
    messageStore.fetchUnreadCount()
  }
})
</script>

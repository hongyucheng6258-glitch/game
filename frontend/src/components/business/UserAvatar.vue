<template>
  <el-avatar :size="size" :src="avatarUrl" class="user-avatar">
    {{ fallbackText }}
  </el-avatar>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getAvatarUrl } from '@/utils/avatar'

const props = withDefaults(defineProps<{
  avatar?: string | null
  username?: string
  size?: number
}>(), {
  size: 40,
})

const avatarUrl = computed(() => {
  return getAvatarUrl(props.avatar)
})

const fallbackText = computed(() => {
  return props.username?.charAt(0)?.toUpperCase() || 'U'
})
</script>

<style scoped lang="scss">
.user-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-weight: 600;
  flex-shrink: 0;
}
</style>

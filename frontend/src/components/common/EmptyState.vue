<template>
  <div class="empty-state">
    <div class="empty-icon-wrapper">
      <el-icon :size="48" color="#6366f1"><component :is="icon || 'Box'" /></el-icon>
    </div>
    <p class="empty-desc">{{ description || '暂无数据' }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  description?: string
}>()
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba($primary-color, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-desc {
  color: $text-muted;
  font-size: 14px;
  margin: 0;
}
</style>

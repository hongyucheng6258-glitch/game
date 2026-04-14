<template>
  <div class="empty-state">
    <el-icon :size="48" color="#64748b"><component :is="icon" /></el-icon>
    <p>{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  description?: string
}>()
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #64748b;
  p {
    margin-top: 12px;
    font-size: 14px;
  }
}
</style>

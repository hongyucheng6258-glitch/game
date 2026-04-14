<template>
  <div class="stat-card" :style="{ '--accent-color': color }">
    <div class="stat-icon">
      <el-icon :size="32"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-content">
      <span class="stat-title">{{ title }}</span>
      <span class="stat-value">{{ displayValue }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  title: string
  value: number | string
  icon?: string
  color?: string
  prefix?: string
  suffix?: string
}>(), {
  icon: 'DataLine',
  color: '#6366f1',
  prefix: '',
  suffix: '',
})

const displayValue = computed(() => {
  if (typeof props.value === 'number') {
    return `${props.prefix}${props.value.toLocaleString()}${props.suffix}`
  }
  return `${props.prefix}${props.value}${props.suffix}`
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $border-radius-lg;
  background: color-mix(in srgb, var(--accent-color) 15%, transparent);
  color: var(--accent-color);
  flex-shrink: 0;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  min-width: 0;
}

.stat-title {
  font-size: 14px;
  color: $text-secondary;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

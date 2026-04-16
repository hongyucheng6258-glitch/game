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
  transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;
  position: relative;
  overflow: hidden;

  // 霓虹边框效果
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    padding: 1px;
    background: linear-gradient(135deg, rgba($neon-cyan, 0.3), rgba($primary-color, 0.1), transparent);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0;
    transition: opacity $transition-normal;
    pointer-events: none;
  }

  // 顶部霓虹光线
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($neon-cyan, 0.4), transparent);
    opacity: 0;
    transition: opacity $transition-normal;
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-glow, 0 0 30px rgba($neon-cyan, 0.1);
    border-color: rgba($neon-cyan, 0.25);

    &::before {
      opacity: 1;
    }

    &::after {
      opacity: 1;
    }

    .stat-icon {
      box-shadow: 0 0 20px color-mix(in srgb, var(--accent-color) 30%, transparent);
    }

    .stat-value {
      text-shadow: 0 0 12px color-mix(in srgb, var(--accent-color) 40%, transparent);
    }
  }
}

.stat-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $border-radius-lg;
  background: linear-gradient(135deg, color-mix(in srgb, var(--accent-color) 20%, transparent), color-mix(in srgb, var(--accent-color) 5%, transparent));
  color: var(--accent-color);
  flex-shrink: 0;
  transition: box-shadow $transition-normal;
  position: relative;

  // 图标区域内部微光
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.08), transparent 70%);
    pointer-events: none;
  }
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
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Orbitron', 'Rajdhani', monospace;
  transition: text-shadow $transition-normal;
  text-shadow: 0 0 8px rgba($neon-cyan, 0.15);
}
</style>

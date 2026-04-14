<template>
  <div class="level-badge" :style="{ color: levelColor }">
    <span v-if="icon" class="level-icon">{{ icon }}</span>
    <span class="level-name">{{ name }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  level: number
  name?: string
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  name: '',
  icon: ''
})

const levelColor = computed(() => {
  const colors = [
    '#999999', // 1 新手
    '#cd7f32', // 2 青铜
    '#c0c0c0', // 3 白银
    '#ffd700', // 4 黄金
    '#e5e4e2', // 5 铂金
    '#b9f2ff', // 6 钻石
    '#ff6b6b', // 7 大师
    '#ff4757', // 8 王者
    '#ffd700', // 9 传奇
    '#ff0000'  // 10 至尊
  ]
  return colors[props.level - 1] || '#999999'
})
</script>

<style scoped lang="scss">
.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 12px;
  font-weight: 600;
  
  .level-icon {
    font-size: 14px;
  }
}
</style>

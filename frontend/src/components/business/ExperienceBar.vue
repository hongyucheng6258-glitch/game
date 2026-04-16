<template>
  <div class="exp-bar-container">
    <div class="exp-info">
      <span class="exp-text">{{ currentExp }} / {{ nextLevelExp }}</span>
      <span class="exp-percentage">{{ percentage }}%</span>
    </div>
    <div class="exp-bar">
      <div class="exp-progress" :style="{ width: percentage + '%' }"></div>
    </div>
    <div v-if="showToNext" class="exp-to-next">
      还需 {{ expToNext }} 经验升级
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  currentExp: number
  currentLevelExp: number
  nextLevelExp: number
  showToNext?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showToNext: true
})

const currentExp = computed(() => props.currentExp - props.currentLevelExp)
const nextLevelExp = computed(() => props.nextLevelExp - props.currentLevelExp)
const percentage = computed(() => {
  if (nextLevelExp.value <= 0) return 100
  return Math.min(100, Math.round((currentExp.value / nextLevelExp.value) * 100))
})
const expToNext = computed(() => Math.max(0, props.nextLevelExp - props.currentExp))
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.exp-bar-container {
  width: 100%;
  
  .exp-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
    font-size: 12px;
    
    .exp-text {
      color: $text-muted;
      font-family: 'Orbitron', monospace;
      font-size: 11px;
    }
    
    .exp-percentage {
      color: $neon-cyan;
      font-weight: 600;
      font-family: 'Orbitron', monospace;
      text-shadow: 0 0 8px rgba($neon-cyan, 0.4);
    }
  }
  
  .exp-bar {
    height: 10px;
    background: rgba($bg-hover, 0.8);
    border-radius: 5px;
    overflow: hidden;
    border: 1px solid rgba($neon-cyan, 0.1);
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.3);
    
    .exp-progress {
      height: 100%;
      // 渐变填充：$neon-cyan 到 $primary-color
      background: linear-gradient(90deg, $neon-cyan, $primary-color);
      border-radius: 5px;
      transition: width 0.5s ease;
      // 霓虹发光效果
      box-shadow:
        0 0 8px rgba($neon-cyan, 0.4),
        0 0 16px rgba($neon-cyan, 0.2);
      position: relative;

      // 进度条高光扫描效果
      &::after {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(
          90deg,
          transparent,
          rgba(255, 255, 255, 0.2),
          transparent
        );
        animation: shimmer 2.5s ease-in-out infinite;
      }
    }
  }
  
  .exp-to-next {
    margin-top: 6px;
    font-size: 12px;
    color: $text-muted;
    text-align: right;
  }
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 200%; }
}
</style>

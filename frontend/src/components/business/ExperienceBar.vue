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
.exp-bar-container {
  width: 100%;
  
  .exp-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
    font-size: 12px;
    color: #999;
    
    .exp-text {
      color: #666;
    }
    
    .exp-percentage {
      color: var(--el-color-primary);
      font-weight: 600;
    }
  }
  
  .exp-bar {
    height: 8px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
    
    .exp-progress {
      height: 100%;
      background: linear-gradient(90deg, #409eff, #66b1ff);
      border-radius: 4px;
      transition: width 0.5s ease;
    }
  }
  
  .exp-to-next {
    margin-top: 6px;
    font-size: 12px;
    color: #999;
    text-align: right;
  }
}
</style>

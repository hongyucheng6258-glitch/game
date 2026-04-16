<template>
  <div ref="chartRef" class="data-chart" :style="{ height: height }" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(defineProps<{
  option: echarts.EChartsOption
  height?: string
}>(), {
  height: '400px',
})

const chartRef = ref<HTMLElement>()
const chartInstance = shallowRef<echarts.ECharts>()

function initChart() {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value, 'dark')
  chartInstance.value.setOption(props.option)
}

function handleResize() {
  chartInstance.value?.resize()
}

watch(() => props.option, (newOption) => {
  chartInstance.value?.setOption(newOption, true)
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.data-chart {
  width: 100%;
  min-height: 300px;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  position: relative;
  overflow: hidden;
  transition: border-color $transition-normal, box-shadow $transition-normal;

  // 霓虹边框效果
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    padding: 1px;
    background: linear-gradient(135deg, rgba($neon-cyan, 0.2), rgba($primary-color, 0.1), transparent 60%);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    pointer-events: none;
    z-index: 1;
  }

  // 赛博网格纹理背景
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background-image:
      linear-gradient(rgba($neon-cyan, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba($neon-cyan, 0.03) 1px, transparent 1px);
    background-size: 30px 30px;
    pointer-events: none;
    z-index: 0;
  }

  &:hover {
    border-color: rgba($neon-cyan, 0.2);
    box-shadow: $shadow-glow, 0 0 20px rgba($neon-cyan, 0.08);
  }
}
</style>

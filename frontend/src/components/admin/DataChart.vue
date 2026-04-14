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
.data-chart {
  width: 100%;
  min-height: 300px;
}
</style>

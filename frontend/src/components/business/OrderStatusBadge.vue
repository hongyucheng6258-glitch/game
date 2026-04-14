<template>
  <el-tag :type="statusType" size="small" :effect="effect">
    {{ statusLabel }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ORDER_STATUS_LABELS, ORDER_STATUS_TYPES } from '@/utils/constants'

const props = withDefaults(defineProps<{
  status: number
  effect?: 'dark' | 'light' | 'plain'
}>(), {
  effect: 'plain',
})

const statusLabel = computed(() => ORDER_STATUS_LABELS[props.status] || '未知')

const statusType = computed(() => {
  const type = ORDER_STATUS_TYPES[props.status]
  return (type || 'info') as 'success' | 'warning' | 'danger' | 'info' | 'primary'
})
</script>

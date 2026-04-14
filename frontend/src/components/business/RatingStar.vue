<template>
  <el-rate
    :model-value="value"
    disabled
    :colors="['#f59e0b', '#f59e0b', '#f59e0b']"
    :size="size"
  />
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  value: number
  size?: 'large' | 'default' | 'small'
}>(), {
  size: 'default',
})
</script>

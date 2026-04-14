<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    :closable="closable"
    @close="$emit('close')"
  >
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  label: string
  type?: string
  size?: 'large' | 'default' | 'small'
  effect?: 'dark' | 'light' | 'plain'
  closable?: boolean
}>(), {
  type: '',
  size: 'default',
  effect: 'plain',
  closable: false,
})

defineEmits<{
  close: []
}>()

const tagType = computed(() => {
  if (props.type) return props.type as any
  const map: Record<string, string> = {
    '陪玩': 'primary',
    '代打': 'warning',
    'LOL': 'danger',
    'DOTA2': 'info',
    'CSGO': 'warning',
    '王者荣耀': 'success',
    '和平精英': 'primary',
    '原神': 'success',
    '永劫无间': 'danger',
    'APEX': 'warning',
  }
  return (map[props.label] || '') as any
})
</script>

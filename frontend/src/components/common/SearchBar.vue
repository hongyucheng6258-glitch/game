<template>
  <div class="search-bar">
    <el-input
      v-model="keyword"
      :placeholder="placeholder"
      clearable
      @keyup.enter="handleSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  placeholder?: string
  modelValue?: string
}>(), {
  placeholder: '搜索服务...',
  modelValue: '',
})

const emit = defineEmits<{
  (e: 'search', keyword: string): void
}>()

const keyword = ref(props.modelValue)

function handleSearch() {
  emit('search', keyword.value)
}
</script>

<style scoped lang="scss">
.search-bar {
  :deep(.el-input__wrapper) {
    border-radius: 24px;
    padding: 4px 16px;
    transition: all 0.3s ease;
  }
}
</style>

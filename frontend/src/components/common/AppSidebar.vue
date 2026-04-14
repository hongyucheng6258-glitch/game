<template>
  <aside class="app-sidebar">
    <el-menu :default-active="activeMenu" router>
      <el-menu-item v-for="menu in menus" :key="menu.path" :index="menu.path">
        <el-icon><component :is="menu.icon" /></el-icon>
        <span>{{ menu.title }}</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps<{
  menus: { title: string; icon: string; path: string }[]
}>()

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.app-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #1e293b;
  border-radius: 8px;
  overflow: hidden;
  .el-menu {
    background: transparent;
    border-right: none;
  }
  .el-menu-item {
    color: #94a3b8;
    &:hover, &.is-active {
      background: #334155;
      color: #f1f5f9;
    }
  }
}
</style>

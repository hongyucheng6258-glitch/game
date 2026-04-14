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
@use '@/assets/styles/variables' as *;

.app-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: $border-radius-xl;
  border: 1px solid rgba(148, 163, 184, 0.06);
  overflow: hidden;
  padding: 8px;

  .el-menu {
    background: transparent;
    border-right: none;
  }

  .el-menu-item {
    color: $text-secondary;
    border-radius: $border-radius;
    margin-bottom: 2px;
    transition: all $transition-normal;
    height: 44px;
    line-height: 44px;

    &:hover {
      background: rgba($primary-color, 0.08);
      color: $text-primary;
    }

    &.is-active {
      background: rgba($primary-color, 0.15);
      color: $primary-light;
      font-weight: 500;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        background: $primary-color;
        border-radius: 0 3px 3px 0;
      }
    }
  }
}
</style>

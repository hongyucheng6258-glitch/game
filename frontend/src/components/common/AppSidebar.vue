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
  background: linear-gradient(180deg, $bg-dark, $bg-abyss);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: $border-radius-xl;
  border: none;
  border-right: 1px solid $border-color;
  overflow: hidden;
  padding: 8px;

  .el-menu {
    background: transparent;
    border-right: none;
  }

  .el-menu-item {
    position: relative;
    color: $text-secondary;
    border-radius: $border-radius;
    margin-bottom: 2px;
    transition: all $transition-normal;
    height: 44px;
    line-height: 44px;

    &:hover {
      background: rgba($neon-cyan, 0.04);
      color: $text-primary;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 60%;
        background: $neon-cyan;
        border-radius: 0 3px 3px 0;
        box-shadow: 0 0 8px $neon-cyan, 0 0 20px rgba($neon-cyan, 0.4);
      }
    }

    &.is-active {
      background: rgba($neon-purple, 0.08);
      box-shadow: inset 0 0 20px rgba($neon-purple, 0.06);
      color: $neon-cyan;
      font-weight: 500;
      text-shadow: 0 0 10px rgba($neon-cyan, 0.4);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 60%;
        background: $neon-cyan;
        border-radius: 0 3px 3px 0;
        box-shadow: 0 0 8px $neon-cyan, 0 0 20px rgba($neon-cyan, 0.4);
      }
    }
  }
}
</style>
